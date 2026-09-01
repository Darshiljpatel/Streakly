package com.example.streakmatrix.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.streakmatrix.data.model.Habit
import com.example.streakmatrix.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // ───── Habit CRUD ─────
    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    // ───── Completions ─────
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getAllCompletionsForHabit(habitId: Long): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE date LIKE :yearMonth || '-%'")
    fun getCompletionsForMonth(yearMonth: String): Flow<List<HabitCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: String)

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND date < :today AND date >= :startDate")
    suspend fun countCompletionsInRange(habitId: Long, today: String, startDate: String): Int
}
