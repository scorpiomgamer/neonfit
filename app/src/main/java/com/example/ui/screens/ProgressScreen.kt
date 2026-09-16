package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WorkoutViewModel
import com.example.ui.components.InteractiveStrengthChart
import com.example.ui.components.MuscleBodyRecoveryView
import com.example.ui.components.NeonCard
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun ProgressScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val streak by viewModel.streak.collectAsState()
    val allSets by viewModel.allSets.collectAsState()
    val chartDataPoints by viewModel.chartDataPoints.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val muscleStatuses by viewModel.muscleStatuses.collectAsState()

    // Aggregate stats
    val totalVolumeKg = remember(allSets) {
        allSets.sumOf { (it.weightKg * it.reps).toDouble() }.toFloat()
    }
    val maxWeightPr = remember(allSets) {
        allSets.maxOfOrNull { it.weightKg } ?: 0f
    }
    val totalSetsCount = remember(allSets) { allSets.size }

    val distinctExercises = remember(allSets) {
        val list = allSets.map { it.exerciseName }.distinct()
        if (list.isEmpty()) listOf("Press de Banca Plano", "Sentadilla con Barra", "Peso Muerto") else list
    }

    // Weekly day streak indicators (L M X J V S D)
    val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")
    val todayDayOfWeek = remember {
        val cal = Calendar.getInstance()
        // Convert SUNDAY (1) ... SATURDAY (7) to Monday=0 ... Sunday=6
        val day = cal.get(Calendar.DAY_OF_WEEK)
        if (day == Calendar.SUNDAY) 6 else day - 2
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PROGRESO & ANÁLISIS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Fuerza, Racha y Recuperación Muscular",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.5.dp, NeonCyan, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Streak Card with Neon Fire and Weekly Tracker
        item {
            NeonCard(
                borderColor = NeonOrange.copy(alpha = 0.6f),
                glowColor = NeonOrange.copy(alpha = 0.25f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(NeonOrange.copy(alpha = 0.2f), CircleShape)
                                    .border(1.5.dp, NeonOrange, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = NeonOrange,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "$streak DÍAS EN RACHA",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonOrange,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = if (streak > 0) "¡Gran constancia! Mantén encendido el fuego." else "Entrena hoy para comenzar tu racha.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Weekly Tracker Row (L M X J V S D)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayLabels.forEachIndexed { index, label ->
                            val isPastOrToday = index <= todayDayOfWeek
                            val isActiveDay = isPastOrToday && streak > (todayDayOfWeek - index)
                            val isToday = index == todayDayOfWeek

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isToday) NeonOrange else TextMuted
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isActiveDay -> NeonOrange
                                                isToday -> NeonOrange.copy(alpha = 0.25f)
                                                else -> DarkSurfaceElevated
                                            }
                                        )
                                        .border(
                                            1.dp,
                                            if (isToday) NeonOrange else DarkSurfaceBorder,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isActiveDay) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = Color(0xFF140700),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    if (isToday) NeonOrange else TextMuted.copy(alpha = 0.4f),
                                                    CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Summary Metric Cards (Volumen total, PR Máximo, Series Totales)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Volumen Total",
                    value = "${totalVolumeKg.toInt()} kg",
                    subtitle = "Carga acumulada",
                    icon = Icons.Default.TrendingUp,
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "PR Máximo",
                    value = "${maxWeightPr.toInt()} kg",
                    subtitle = "Récord en serie",
                    icon = Icons.Default.MilitaryTech,
                    color = NeonYellow,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Series Hechas",
                    value = "$totalSetsCount",
                    subtitle = "Total historial",
                    icon = Icons.Default.FitnessCenter,
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Interactive Strength Chart
        item {
            InteractiveStrengthChart(
                dataPoints = chartDataPoints,
                selectedExercise = selectedExercise,
                availableExercises = distinctExercises,
                onSelectExercise = { viewModel.selectExerciseForChart(it) }
            )
        }

        // Muscle Recovery Body Illustration
        item {
            MuscleBodyRecoveryView(
                muscleStatuses = muscleStatuses
            )
        }
    }
}

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}
