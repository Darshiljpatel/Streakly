package com.example.streakmatrix.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.streakmatrix.ui.util.habitIcon
import com.example.streakmatrix.ui.util.parseColor
import com.example.streakmatrix.ui.viewmodel.HabitsViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: HabitsViewModel) {
    val uiState by viewModel.calendarUiState.collectAsState()

    val today = LocalDate.now()
    val yearMonth = uiState.currentMonth
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7 // 0=Sun

    val monthLabel = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consistency Matrix", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.setMonth(yearMonth.minusMonths(1)) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                    }
                    Text(
                        monthLabel,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    IconButton(onClick = { viewModel.setMonth(yearMonth.plusMonths(1)) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }
            }

            // ── Calendar Grid Card ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Day of week header
                        val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            dayLabels.forEach { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        // Calendar cells: leading empty cells + day cells
                        val totalCells = firstDayOfWeek + daysInMonth
                        val rows = (totalCells + 6) / 7

                        repeat(rows) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                repeat(7) { col ->
                                    val cellIndex = row * 7 + col
                                    val dayNumber = cellIndex - firstDayOfWeek + 1

                                    if (dayNumber < 1 || dayNumber > daysInMonth) {
                                        Box(modifier = Modifier.size(36.dp))
                                    } else {
                                        val date = yearMonth.atDay(dayNumber)
                                        val isToday = date == today
                                        val isSelected = date == uiState.selectedDate
                                        val isFuture = date.isAfter(today)
                                        val completionRatio = uiState.heatmap[dayNumber] ?: 0f

                                        val cellColor = when {
                                            isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            completionRatio == 0f -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            completionRatio < 0.5f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            completionRatio < 1f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                            else -> MaterialTheme.colorScheme.primary
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(cellColor)
                                                .border(
                                                    width = if (isSelected) 2.dp else 0.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { viewModel.setSelectedDate(date) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isToday) {
                                                Text(
                                                    text = "$dayNumber",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = if (completionRatio > 0 && !isFuture) Color.White else MaterialTheme.colorScheme.primary
                                                )
                                            } else if (isSelected) {
                                                Text(
                                                    text = "$dayNumber",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (row < rows - 1) Spacer(Modifier.height(6.dp))
                        }

                        Spacer(Modifier.height(16.dp))

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Less",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(6.dp))
                            listOf(0.15f, 0.3f, 0.6f, 1f).forEach { alpha ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 2.dp)
                                        .size(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "More",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Selected Date Habits ──
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            if (uiState.selectedDateCompletedHabits.isEmpty()) {
                item {
                    Text(
                        "No habits completed on this day.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(uiState.selectedDateCompletedHabits, key = { it.id }) { habit ->
                    val habitColor = parseColor(habit.colorHex)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(habitColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = habitIcon(habit.iconName),
                                    contentDescription = null,
                                    tint = habitColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = habitColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
