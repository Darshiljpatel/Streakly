package com.example.streakmatrix.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val habitId: Long,
    val date: String  // ISO format: "2026-09-01"
)
