package com.example.streakmatrix.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

val SELECTABLE_ICONS: List<Pair<String, ImageVector>> = listOf(
    "CheckCircle" to Icons.Default.CheckCircle,
    "WaterDrop" to Icons.Default.WaterDrop,
    "Book" to Icons.Default.Book,
    "FitnessCenter" to Icons.Default.FitnessCenter,
    "DirectionsRun" to Icons.Default.DirectionsRun,
    "SelfImprovement" to Icons.Default.SelfImprovement,
    "Restaurant" to Icons.Default.Restaurant,
    "NightsStay" to Icons.Default.NightsStay,
    "MusicNote" to Icons.Default.MusicNote,
    "LocalFireDepartment" to Icons.Default.LocalFireDepartment
)

val SELECTABLE_COLORS: List<Pair<String, Color>> = listOf(
    "#4FC3F7" to Color(0xFF4FC3F7),  // Sky Blue
    "#81C784" to Color(0xFF81C784),  // Green
    "#FFB74D" to Color(0xFFFFB74D),  // Orange
    "#F06292" to Color(0xFFF06292),  // Pink
    "#BA68C8" to Color(0xFFBA68C8),  // Purple
    "#FF7043" to Color(0xFFFF7043),  // Deep Orange
    "#4DB6AC" to Color(0xFF4DB6AC),  // Teal
    "#FFD54F" to Color(0xFFFFD54F)   // Yellow
)

fun habitIcon(name: String): ImageVector {
    return SELECTABLE_ICONS.firstOrNull { it.first == name }?.second
        ?: Icons.Default.CheckCircle
}

fun parseColor(hex: String): Color {
    return try {
        val cleaned = hex.trimStart('#')
        Color(android.graphics.Color.parseColor("#$cleaned"))
    } catch (e: Exception) {
        Color(0xFF4FC3F7)
    }
}
