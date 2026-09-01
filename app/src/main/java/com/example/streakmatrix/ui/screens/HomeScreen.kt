package com.example.streakmatrix.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.streakmatrix.ui.util.habitIcon
import com.example.streakmatrix.ui.util.parseColor
import com.example.streakmatrix.ui.viewmodel.HabitWithCompletion
import com.example.streakmatrix.ui.viewmodel.HabitsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HabitsViewModel,
    onNavigateToAddHabit: () -> Unit
) {
    val uiState by viewModel.homeUiState.collectAsState()
    val dateLabel = LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())
    )

    val progress = if (uiState.totalToday > 0)
        uiState.completedToday.toFloat() / uiState.totalToday
    else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "progress"
    )

    Scaffold(
        floatingActionButton = {
            if (uiState.totalToday == 0) {
                FloatingActionButton(
                    onClick = onNavigateToAddHabit,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Habit")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ──
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )
            }

            // ── Progress Card ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Daily Progress",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(Modifier.height(4.dp))
                            if (uiState.totalToday == 0) {
                                Text(
                                    text = "No habits yet — add one!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            } else {
                                Text(
                                    text = "${uiState.completedToday} of ${uiState.totalToday} completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.size(64.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                strokeWidth = 7.dp,
                                strokeCap = StrokeCap.Round
                            )
                            val pct = (animatedProgress * 100).toInt()
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // ── Pending Habits ──
            if (uiState.pendingHabits.isNotEmpty()) {
                item {
                    SectionHeader("Pending")
                }
                items(uiState.pendingHabits, key = { it.habit.id }) { item ->
                    HomeHabitCard(item = item, onToggle = { viewModel.toggleCompletion(item.habit.id) })
                }
            }

            // ── Completed Habits ──
            if (uiState.completedHabits.isNotEmpty()) {
                item {
                    SectionHeader("Completed ✓")
                }
                items(uiState.completedHabits, key = { it.habit.id }) { item ->
                    HomeHabitCard(item = item, onToggle = { viewModel.toggleCompletion(item.habit.id) })
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun HomeHabitCard(
    item: HabitWithCompletion,
    onToggle: () -> Unit
) {
    val habit = item.habit
    val habitColor = parseColor(habit.colorHex)

    val bgColor by animateColorAsState(
        targetValue = if (item.isCompletedToday)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(300),
        label = "cardBg"
    )
    val checkBg by animateColorAsState(
        targetValue = if (item.isCompletedToday) habitColor else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "checkBg"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color-coded icon blob
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(habitColor.copy(alpha = if (item.isCompletedToday) 0.3f else 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = habitIcon(habit.iconName),
                    contentDescription = null,
                    tint = habitColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (item.isCompletedToday)
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (habit.currentStreak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFFF7043)
                        )
                        val streakUnit = if (habit.frequency == "Weekly") "week" else "day"
                        Text(
                            text = " ${habit.currentStreak} $streakUnit streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF7043)
                        )
                    }
                }
            }

            // Check button
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(checkBg)
            ) {
                if (item.isCompletedToday) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
