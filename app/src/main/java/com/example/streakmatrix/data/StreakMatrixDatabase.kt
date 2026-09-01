package com.example.streakmatrix.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.streakmatrix.data.model.Habit
import com.example.streakmatrix.data.model.HabitCompletion

@Database(
    entities = [Habit::class, HabitCompletion::class],
    version = 2,
    exportSchema = false
)
abstract class StreakMatrixDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: StreakMatrixDatabase? = null

        fun getInstance(context: Context): StreakMatrixDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    StreakMatrixDatabase::class.java,
                    "streak_matrix_db"
                ).fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
