package com.example.streakmatrix.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val iconName: String = "CheckCircle",
    val colorHex: String = "#4FC3F7",
    val frequency: String = "Daily",
    val createdAt: Long = System.currentTimeMillis(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompletedDays: Int = 0
)
