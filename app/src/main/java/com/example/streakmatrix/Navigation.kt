package com.example.streakmatrix

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.streakmatrix.ui.navigation.BottomNavigationScreens
import com.example.streakmatrix.ui.navigation.Screen
import com.example.streakmatrix.ui.screens.AddEditHabitScreen
import com.example.streakmatrix.ui.screens.CalendarScreen
import com.example.streakmatrix.ui.screens.HabitsScreen
import com.example.streakmatrix.ui.screens.HomeScreen
import com.example.streakmatrix.ui.viewmodel.HabitsViewModel

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    // Shared ViewModel — survives navigation between tabs
    val habitsViewModel: HabitsViewModel = viewModel()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val showBottomBar = BottomNavigationScreens.any {
                it.route == currentDestination?.route
            }

            if (showBottomBar) {
                NavigationBar {
                    BottomNavigationScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = habitsViewModel,
                    onNavigateToAddHabit = { navController.navigate(Screen.AddEditHabit.routeForNew()) }
                )
            }
            composable(Screen.Habits.route) {
                HabitsScreen(
                    viewModel = habitsViewModel,
                    onNavigateToAddHabit = { navController.navigate(Screen.AddEditHabit.routeForNew()) },
                    onNavigateToEditHabit = { habitId ->
                        navController.navigate(Screen.AddEditHabit.routeForEdit(habitId))
                    }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(viewModel = habitsViewModel)
            }
            composable(
                route = Screen.AddEditHabit.ROUTE_WITH_ARG,
                arguments = listOf(
                    navArgument("habitId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getLong("habitId") ?: -1L
                AddEditHabitScreen(
                    viewModel = habitsViewModel,
                    habitId = if (habitId == -1L) null else habitId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
