package com.example.streakmatrix.data

import com.example.streakmatrix.data.model.Habit
import com.example.streakmatrix.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun getAllHabits(): Flow<List<Habit>>
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>>
    fun getCompletionsForMonth(yearMonth: String): Flow<List<HabitCompletion>>
    suspend fun getAllCompletionsForHabit(habitId: Long): List<HabitCompletion>
    suspend fun insertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun insertCompletion(completion: HabitCompletion)
    suspend fun deleteCompletion(habitId: Long, date: String)
    suspend fun getHabitById(id: Long): Habit?
}

class DefaultHabitRepository(private val dao: HabitDao) : HabitRepository {

    override fun getAllHabits(): Flow<List<Habit>> = dao.getAllHabits()

    override fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>> =
        dao.getCompletionsForDate(date)

    override fun getCompletionsForMonth(yearMonth: String): Flow<List<HabitCompletion>> =
        dao.getCompletionsForMonth(yearMonth)

    override suspend fun getAllCompletionsForHabit(habitId: Long): List<HabitCompletion> =
        dao.getAllCompletionsForHabit(habitId)

    override suspend fun insertHabit(habit: Habit): Long = dao.insertHabit(habit)

    override suspend fun updateHabit(habit: Habit) = dao.updateHabit(habit)

    override suspend fun deleteHabit(habit: Habit) = dao.deleteHabit(habit)

    override suspend fun insertCompletion(completion: HabitCompletion) =
        dao.insertCompletion(completion)

    override suspend fun deleteCompletion(habitId: Long, date: String) =
        dao.deleteCompletion(habitId, date)

    override suspend fun getHabitById(id: Long): Habit? = dao.getHabitById(id)
}
