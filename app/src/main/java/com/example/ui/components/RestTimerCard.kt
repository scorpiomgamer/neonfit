package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RestTimerCard(
    remainingSeconds: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onAddSeconds: (Int) -> Unit,
    onSelectPreset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "timerProgress"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val timerColor = when {
        remainingSeconds == 0 -> NeonGreen
        remainingSeconds <= 15 -> NeonOrange
        else -> NeonCyan
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurface)
            .border(1.dp, timerColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = timerColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TEMPORIZADOR DE DESCANSO",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = timerColor,
                    letterSpacing = 1.sp
                )
            }

            if (isRunning) {
                PulsingDot(color = timerColor, size = 10.dp)
            } else {
                Text(
                    text = if (remainingSeconds == 0) "¡A ENTRENAR!" else "PAUSADO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (remainingSeconds == 0) NeonGreen else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Circular Timer Display
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(170.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 10.dp.toPx()
                // Track background
                drawCircle(
                    color = DarkSurfaceElevated,
                    radius = (size.minDimension - strokeWidth) / 2f,
                    style = Stroke(width = strokeWidth)
                )

                // Active glowing sweep
                if (animatedProgress > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(timerColor.copy(alpha = 0.6f), timerColor, timerColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (remainingSeconds == 0) "¡Tiempo cumplido!" else "Descanso entre series",
                    fontSize = 11.sp,
                    color = if (remainingSeconds == 0) NeonGreen else TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset chips: 30s, 60s, 90s, 120s, 180s
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(30, 60, 90, 120, 180).forEach { sec ->
                val isSelected = totalSeconds == sec
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) timerColor.copy(alpha = 0.2f) else DarkSurfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) timerColor else DarkSurfaceBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectPreset(sec) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${sec}s",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) timerColor else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timer action buttons: Start/Pause, Reset, +15s
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset button
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(46.dp)
                    .background(DarkSurfaceElevated, CircleShape)
                    .border(1.dp, DarkSurfaceBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reiniciar",
                    tint = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Play / Pause main button
            IconButton(
                onClick = if (isRunning) onPause else onStart,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(12.dp, CircleShape, ambientColor = timerColor, spotColor = timerColor)
                    .background(timerColor, CircleShape)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pausar" else "Iniciar",
                    tint = Color(0xFF071018),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // +15s button
            IconButton(
                onClick = { onAddSeconds(15) },
                modifier = Modifier
                    .size(46.dp)
                    .background(DarkSurfaceElevated, CircleShape)
                    .border(1.dp, DarkSurfaceBorder, CircleShape)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Sumar 15s",
                        tint = timerColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "15",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = timerColor
                    )
                }
            }
        }
    }
}
