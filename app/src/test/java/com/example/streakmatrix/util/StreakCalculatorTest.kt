package com.example.streakmatrix.util

import com.example.streakmatrix.data.model.HabitCompletion
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    @Test
    fun `calculate daily streak with perfect completions`() {
        // Today is 2026-09-01 (Tuesday)
        val today = LocalDate.of(2026, 9, 1)
        
        // Created 5 days ago (2026-08-28)
        val createdAt = today.minusDays(4)
        
        val completions = listOf(
            HabitCompletion(date = today.toString(), habitId = 1L),
            HabitCompletion(date = today.minusDays(1).toString(), habitId = 1L),
            HabitCompletion(date = today.minusDays(2).toString(), habitId = 1L),
            HabitCompletion(date = today.minusDays(3).toString(), habitId = 1L),
            HabitCompletion(date = today.minusDays(4).toString(), habitId = 1L)
        )

        val stats = StreakCalculator.calculate(
            completions = completions,
            frequency = "Daily",
            createdAtMillis = createdAt.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            today = today
        )

        assertEquals(5, stats.currentStreak)
        assertEquals(5, stats.longestStreak)
        assertEquals(5, stats.totalCompletedDays)
    }

    @Test
    fun `calculate daily streak missing today`() {
        val today = LocalDate.of(2026, 9, 1) // Tuesday
        val createdAt = today.minusDays(4)
        
        // Missed today, completed last 4 days
        val completions = listOf(
            HabitCompletion(date = today.minusDays(1).toString(), habitId = 1L),
            HabitCompletion(date = today.minusDays(2).toString(), habitId = 1L),
            HabitCompletion(date = today.minusDays(3).toString(), habitId = 1L),
            HabitCompletion(date = today.minusDays(4).toString(), habitId = 1L)
        )

        val stats = StreakCalculator.calculate(
            completions = completions,
            frequency = "Daily",
            createdAtMillis = createdAt.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            today = today
        )

        // Current streak should be 4 (because they still have time to complete today)
        assertEquals(4, stats.currentStreak)
        assertEquals(4, stats.longestStreak)
        assertEquals(4, stats.totalCompletedDays)
    }
    
    @Test
    fun `calculate daily streak broken yesterday`() {
        val today = LocalDate.of(2026, 9, 1) // Tuesday
        val createdAt = today.minusDays(4)
        
        // Missed yesterday
        val completions = listOf(
            HabitCompletion(date = today.toString(), habitId = 1L),
            HabitCompletion(date = today.minusDays(2).toString(), habitId = 1L),
            HabitCompletion(date = today.minusDays(3).toString(), habitId = 1L),
            HabitCompletion(date = today.minusDays(4).toString(), habitId = 1L)
        )

        val stats = StreakCalculator.calculate(
            completions = completions,
            frequency = "Daily",
            createdAtMillis = createdAt.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            today = today
        )

        // Current streak is 1 (today). Longest is 3.
        assertEquals(1, stats.currentStreak)
        assertEquals(3, stats.longestStreak)
        assertEquals(4, stats.totalCompletedDays)
    }

    @Test
    fun `calculate weekdays streak over a weekend`() {
        // Today is Monday (2026-08-31)
        val today = LocalDate.of(2026, 8, 31)
        val createdAt = today.minusDays(5) // Last Wednesday (2026-08-26)
        
        // Completed Wed, Thu, Fri, and Mon (Today). Missed Sat, Sun (which are ignored)
        val completions = listOf(
            HabitCompletion(date = today.toString(), habitId = 1L), // Mon
            HabitCompletion(date = today.minusDays(3).toString(), habitId = 1L), // Fri
            HabitCompletion(date = today.minusDays(4).toString(), habitId = 1L), // Thu
            HabitCompletion(date = today.minusDays(5).toString(), habitId = 1L)  // Wed
        )

        val stats = StreakCalculator.calculate(
            completions = completions,
            frequency = "Weekdays",
            createdAtMillis = createdAt.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            today = today
        )

        // Streak should be 4
        assertEquals(4, stats.currentStreak)
        assertEquals(4, stats.longestStreak)
    }
    
    @Test
    fun `calculate weekly streak`() {
        // Today is a Tuesday in Week 2
        val today = LocalDate.of(2026, 9, 8)
        val createdAt = today.minusWeeks(3) // 3 weeks ago
        
        val completions = listOf(
            HabitCompletion(date = today.toString(), habitId = 1L), // This week
            HabitCompletion(date = today.minusWeeks(1).plusDays(2).toString(), habitId = 1L), // Last week
            HabitCompletion(date = today.minusWeeks(2).minusDays(1).toString(), habitId = 1L), // 2 weeks ago
            HabitCompletion(date = today.minusWeeks(3).toString(), habitId = 1L) // 3 weeks ago
        )

        val stats = StreakCalculator.calculate(
            completions = completions,
            frequency = "Weekly",
            createdAtMillis = createdAt.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            today = today
        )

        // Streak should be 4 weeks
        assertEquals(4, stats.currentStreak)
        assertEquals(4, stats.longestStreak)
    }
}
