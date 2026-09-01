package com.example.streakmatrix.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.streakmatrix.StreakMatrixApp
import com.example.streakmatrix.data.HabitRepository
import com.example.streakmatrix.data.model.Habit
import com.example.streakmatrix.data.model.HabitCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.example.streakmatrix.util.StreakCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

data class HabitWithCompletion(
    val habit: Habit,
    val isCompletedToday: Boolean
)

data class HomeUiState(
    val pendingHabits: List<HabitWithCompletion> = emptyList(),
    val completedHabits: List<HabitWithCompletion> = emptyList(),
    val totalToday: Int = 0,
    val completedToday: Int = 0
)

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val heatmap: Map<Int, Float> = emptyMap(),
    val selectedDateCompletedHabits: List<Habit> = emptyList()
)

class HabitsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository =
        (application as StreakMatrixApp).habitRepository

    val today: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    // Raw list of all habits
    val habits: StateFlow<List<Habit>> = repository.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Set of habitIds completed today
    private val _todayCompletionIds: StateFlow<Set<Long>> =
        repository.getCompletionsForDate(today)
            .combine(MutableStateFlow(Unit)) { completions, _ ->
                completions.map { it.habitId }.toSet()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val todayCompletionIds: StateFlow<Set<Long>> = _todayCompletionIds

    // Combined home UI state
    val homeUiState: StateFlow<HomeUiState> = combine(habits, _todayCompletionIds) { habitList, completedIds ->
        val withCompletion = habitList.map { habit ->
            HabitWithCompletion(habit, completedIds.contains(habit.id))
        }
        HomeUiState(
            pendingHabits = withCompletion.filter { !it.isCompletedToday },
            completedHabits = withCompletion.filter { it.isCompletedToday },
            totalToday = habitList.size,
            completedToday = completedIds.size.coerceAtMost(habitList.size)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    // ───── CRUD ─────

    fun addHabit(name: String, iconName: String, colorHex: String, frequency: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertHabit(
                Habit(
                    name = name.trim(),
                    iconName = iconName,
                    colorHex = colorHex,
                    frequency = frequency
                )
            )
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch { repository.updateHabit(habit) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repository.deleteHabit(habit) }
    }

    // ───── Completion Toggle ─────

    fun toggleCompletion(habitId: Long) {
        viewModelScope.launch {
            if (_todayCompletionIds.value.contains(habitId)) {
                repository.deleteCompletion(habitId, today)
            } else {
                repository.insertCompletion(HabitCompletion(habitId = habitId, date = today))
            }
            
            // Recalculate streaks
            val habit = repository.getHabitById(habitId) ?: return@launch
            val completions = repository.getAllCompletionsForHabit(habitId)
            val stats = StreakCalculator.calculate(
                completions = completions,
                frequency = habit.frequency,
                createdAtMillis = habit.createdAt
            )
            repository.updateHabit(
                habit.copy(
                    currentStreak = stats.currentStreak,
                    longestStreak = stats.longestStreak,
                    totalCompletedDays = stats.totalCompletedDays
                )
            )
        }
    }

    // For edit screen — loads habit into a MutableStateFlow
    private val _editingHabit = MutableStateFlow<Habit?>(null)
    val editingHabit: StateFlow<Habit?> = _editingHabit

    fun loadHabitForEdit(habitId: Long) {
        viewModelScope.launch {
            _editingHabit.value = repository.getHabitById(habitId)
        }
    }

    fun clearEditingHabit() {
        _editingHabit.value = null
    }

    // ───── Calendar State ─────
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _selectedDate = MutableStateFlow(LocalDate.now())

    fun setMonth(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val monthCompletions: StateFlow<List<HabitCompletion>> = _currentMonth
        .flatMapLatest { month ->
            repository.getCompletionsForMonth(month.toString())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val calendarUiState: StateFlow<CalendarUiState> = combine(
        habits,
        _currentMonth,
        _selectedDate,
        monthCompletions
    ) { habitList, month, selectedDate, completions ->
        val heatmap = mutableMapOf<Int, Float>()
        val daysInMonth = month.lengthOfMonth()

        for (day in 1..daysInMonth) {
            val date = month.atDay(day)
            
            // Only count habits that are required on this day
            val requiredCount = habitList.count { StreakCalculator.isDayRequired(date, it.frequency) }
            val completedCount = completions.count { it.date == date.format(DateTimeFormatter.ISO_LOCAL_DATE) }
            
            val ratio = if (requiredCount > 0) {
                (completedCount.toFloat() / requiredCount).coerceIn(0f, 1f)
            } else {
                0f
            }
            heatmap[day] = ratio
        }

        val selectedDateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val completedHabitIdsOnSelectedDate = completions
            .filter { it.date == selectedDateStr }
            .map { it.habitId }
            .toSet()
        
        val completedHabits = habitList.filter { it.id in completedHabitIdsOnSelectedDate }

        CalendarUiState(
            currentMonth = month,
            selectedDate = selectedDate,
            heatmap = heatmap,
            selectedDateCompletedHabits = completedHabits
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())
}
