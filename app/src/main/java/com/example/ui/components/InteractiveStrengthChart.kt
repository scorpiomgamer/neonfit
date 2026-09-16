package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StrengthDataPoint
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun InteractiveStrengthChart(
    dataPoints: List<StrengthDataPoint>,
    selectedExercise: String,
    availableExercises: List<String>,
    onSelectExercise: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember(dataPoints) {
        mutableIntStateOf(if (dataPoints.isNotEmpty()) dataPoints.size - 1 else -1)
    }

    val currentPoint = if (selectedPointIndex in dataPoints.indices) dataPoints[selectedPointIndex] else null

    // Calculate progression metrics
    val firstWeight = dataPoints.firstOrNull()?.weightKg ?: 0f
    val latestWeight = dataPoints.lastOrNull()?.weightKg ?: 0f
    val percentGain = if (firstWeight > 0f) {
        (((latestWeight - firstWeight) / firstWeight) * 100f).roundToInt()
    } else 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        // Title & Strength Gain Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "INCREMENTO DE FUERZA (1RM)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Evolución de carga a lo largo del tiempo",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (dataPoints.size > 1) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (percentGain >= 0) NeonGreen.copy(alpha = 0.15f) else NeonPink.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (percentGain >= 0) NeonGreen else NeonPink,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (percentGain >= 0) "+$percentGain%" else "$percentGain%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (percentGain >= 0) NeonGreen else NeonPink
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Exercise Selector Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableExercises) { exercise ->
                val isSelected = exercise.equals(selectedExercise, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) NeonGreen.copy(alpha = 0.2f) else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) NeonGreen else DarkSurfaceBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectExercise(exercise) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = exercise,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NeonGreen else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (dataPoints.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sin registros para $selectedExercise",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Registra una serie para generar el gráfico",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            // Interactive Tooltip Info Box
            if (currentPoint != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Sesión: ${currentPoint.dateLabel}", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = "${currentPoint.weightKg} kg × ${currentPoint.reps} reps",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "1RM Estimado", fontSize = 11.sp, color = NeonCyan)
                        Text(
                            text = "${currentPoint.estimated1Rm} kg",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyan
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF090C12))
                    .border(1.dp, DarkSurfaceBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(dataPoints) {
                            detectTapGestures { tapOffset ->
                                val w = size.width
                                val step = if (dataPoints.size > 1) w / (dataPoints.size - 1) else w
                                val clickedIdx = if (dataPoints.size > 1) {
                                    ((tapOffset.x + step / 2f) / step).toInt().coerceIn(0, dataPoints.size - 1)
                                } else 0
                                selectedPointIndex = clickedIdx
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val maxVal = (dataPoints.maxOfOrNull { it.weightKg } ?: 100f).coerceAtLeast(10f) * 1.15f
                    val minVal = (dataPoints.minOfOrNull { it.weightKg } ?: 0f) * 0.85f
                    val valRange = (maxVal - minVal).coerceAtLeast(1f)

                    // Draw subtle horizontal grid lines
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val y = h - (h / gridLines) * i
                        drawLine(
                            color = Color(0xFF1B2335),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Calculate point offsets
                    val points = dataPoints.mapIndexed { idx, item ->
                        val x = if (dataPoints.size > 1) (w / (dataPoints.size - 1)) * idx else w / 2f
                        val normY = (item.weightKg - minVal) / valRange
                        val y = h - (normY * h).coerceIn(10f, h - 10f)
                        Offset(x, y)
                    }

                    // Draw area fill
                    if (points.size > 1) {
                        val areaPath = Path().apply {
                            moveTo(points.first().x, h)
                            points.forEach { lineTo(it.x, it.y) }
                            lineTo(points.last().x, h)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(NeonGreen.copy(alpha = 0.35f), Color.Transparent),
                                startY = 0f,
                                endY = h
                            )
                        )

                        // Draw glowing line
                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        drawPath(
                            path = strokePath,
                            brush = Brush.horizontalGradient(listOf(NeonGreen, NeonCyan)),
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }

                    // Draw point dots
                    points.forEachIndexed { index, pt ->
                        val isSelected = index == selectedPointIndex
                        val dotRadius = if (isSelected) 7.dp.toPx() else 4.dp.toPx()
                        val dotColor = if (isSelected) NeonYellow else NeonGreen

                        if (isSelected) {
                            // Pulsing halo
                            drawCircle(
                                color = dotColor.copy(alpha = 0.35f),
                                radius = dotRadius * 2f,
                                center = pt
                            )
                            // Vertical guide line
                            drawLine(
                                color = NeonYellow.copy(alpha = 0.5f),
                                start = Offset(pt.x, 0f),
                                end = Offset(pt.x, h),
                                strokeWidth = 1.5f
                            )
                        }

                        drawCircle(color = dotColor, radius = dotRadius, center = pt)
                        drawCircle(color = Color.White, radius = dotRadius * 0.45f, center = pt)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "💡 Toca cualquier punto del gráfico para inspeccionar la sesión.",
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
