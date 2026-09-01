package com.example.streakmatrix

import android.app.Application
import com.example.streakmatrix.data.DefaultHabitRepository
import com.example.streakmatrix.data.HabitRepository
import com.example.streakmatrix.data.StreakMatrixDatabase

class StreakMatrixApp : Application() {

    val database: StreakMatrixDatabase by lazy {
        StreakMatrixDatabase.getInstance(this)
    }

    val habitRepository: HabitRepository by lazy {
        DefaultHabitRepository(database.habitDao())
    }
}
