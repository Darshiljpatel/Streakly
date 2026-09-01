package com.example.streakmatrix.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Habits : Screen("habits", "Habits", Icons.AutoMirrored.Filled.List)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)

    // AddEditHabit is NOT in the bottom nav — it's a full-screen destination
    // route: "add_edit_habit?habitId={habitId}"
    object AddEditHabit : Screen("add_edit_habit", "Add Habit", Icons.Default.Home) {
        const val ROUTE_WITH_ARG = "add_edit_habit?habitId={habitId}"
        fun routeForNew() = "add_edit_habit"
        fun routeForEdit(habitId: Long) = "add_edit_habit?habitId=$habitId"
    }
}

val BottomNavigationScreens = listOf(
    Screen.Home,
    Screen.Habits,
    Screen.Calendar
)
