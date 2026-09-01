package com.example.streakmatrix.util

import com.example.streakmatrix.data.model.HabitCompletion
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.time.temporal.WeekFields

data class StreakStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletedDays: Int,
    val completionPercentage: Float
)

object StreakCalculator {

    fun calculate(
        completions: List<HabitCompletion>,
        frequency: String,
        createdAtMillis: Long,
        today: LocalDate = LocalDate.now()
    ): StreakStats {
        val totalCompleted = completions.size
        if (totalCompleted == 0) {
            return StreakStats(0, 0, 0, 0f)
        }

        val completionDates = completions.map { 
            LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE) 
        }.toSet()

        val createdAtDate = Instant.ofEpochMilli(createdAtMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        if (frequency == "Weekly") {
            return calculateWeekly(completionDates, createdAtDate, today)
        }

        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        
        var dateIterator = today
        var isCurrentStreakBroken = false
        var totalRequiredDays = 0

        val isTodayRequired = isDayRequired(today, frequency)
        val isTodayCompleted = completionDates.contains(today)

        if (isTodayRequired && !isTodayCompleted) {
            dateIterator = today.minusDays(1)
            totalRequiredDays++ // we count today as required, but since it's not completed, it contributes to the denominator
        } else if (isTodayCompleted) {
            tempStreak++
            dateIterator = today.minusDays(1)
            if (isTodayRequired) totalRequiredDays++
        } else {
            dateIterator = today.minusDays(1)
        }

        while (!dateIterator.isBefore(createdAtDate)) {
            val isRequired = isDayRequired(dateIterator, frequency)
            val isCompleted = completionDates.contains(dateIterator)

            if (isRequired) {
                totalRequiredDays++
                if (isCompleted) {
                    tempStreak++
                } else {
                    if (!isCurrentStreakBroken) {
                        currentStreak = tempStreak
                        isCurrentStreakBroken = true
                    }
                    if (tempStreak > longestStreak) {
                        longestStreak = tempStreak
                    }
                    tempStreak = 0
                }
            } else {
                if (isCompleted) {
                    tempStreak++
                }
            }
            dateIterator = dateIterator.minusDays(1)
        }

        if (!isCurrentStreakBroken) {
            currentStreak = tempStreak
        }
        if (tempStreak > longestStreak) {
            longestStreak = tempStreak
        }

        val completionPercentage = if (totalRequiredDays > 0) {
            (totalCompleted.toFloat() / totalRequiredDays) * 100
        } else {
            if (totalCompleted > 0) 100f else 0f
        }

        return StreakStats(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCompletedDays = totalCompleted,
            completionPercentage = completionPercentage
        )
    }

    fun isDayRequired(date: LocalDate, frequency: String): Boolean {
        val dayOfWeek = date.dayOfWeek
        return when (frequency) {
            "Daily" -> true
            "Weekdays" -> dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
            "Weekends" -> dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
            else -> true
        }
    }

    private fun calculateWeekly(
        completionDates: Set<LocalDate>,
        createdAtDate: LocalDate,
        today: LocalDate
    ): StreakStats {
        val weekFields = WeekFields.ISO
        val completedWeeks = completionDates.map { date ->
            Pair(date.get(IsoFields.WEEK_BASED_YEAR), date.get(weekFields.weekOfWeekBasedYear()))
        }.toSet()

        val currentYear = today.get(IsoFields.WEEK_BASED_YEAR)
        val currentWeek = today.get(weekFields.weekOfWeekBasedYear())
        
        var currentStreak = 0
        var longestStreak = 0
        var tempStreak = 0
        var isCurrentStreakBroken = false

        var evalDate = today
        var totalRequiredWeeks = 0
        val createdWeekKey = Pair(
            createdAtDate.get(IsoFields.WEEK_BASED_YEAR),
            createdAtDate.get(weekFields.weekOfWeekBasedYear())
        )

        while (true) {
            val evalWeekKey = Pair(
                evalDate.get(IsoFields.WEEK_BASED_YEAR),
                evalDate.get(weekFields.weekOfWeekBasedYear())
            )
            val isCompleted = completedWeeks.contains(evalWeekKey)
            totalRequiredWeeks++

            if (evalWeekKey == Pair(currentYear, currentWeek)) {
                if (isCompleted) {
                    tempStreak++
                }
            } else {
                if (isCompleted) {
                    tempStreak++
                } else {
                    if (!isCurrentStreakBroken) {
                        currentStreak = tempStreak
                        isCurrentStreakBroken = true
                    }
                    if (tempStreak > longestStreak) {
                        longestStreak = tempStreak
                    }
                    tempStreak = 0
                }
            }

            if (evalWeekKey == createdWeekKey || evalDate.isBefore(createdAtDate)) break
            evalDate = evalDate.minusWeeks(1)
        }

        if (!isCurrentStreakBroken) {
            currentStreak = tempStreak
        }
        if (tempStreak > longestStreak) {
            longestStreak = tempStreak
        }

        val completionPercentage = if (totalRequiredWeeks > 0) {
            (completionDates.size.toFloat() / totalRequiredWeeks) * 100
        } else {
            if (completionDates.isNotEmpty()) 100f else 0f
        }

        return StreakStats(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCompletedDays = completionDates.size,
            completionPercentage = completionPercentage
        )
    }
}
