package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MuscleStatus
import com.example.data.RecoveryState
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MuscleFatigued
import com.example.ui.theme.MuscleReady
import com.example.ui.theme.MuscleRecovering
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MuscleBodyRecoveryView(
    muscleStatuses: List<MuscleStatus>,
    modifier: Modifier = Modifier
) {
    var selectedViewIndex by remember { mutableIntStateOf(0) } // 0: Frente, 1: Espalda
    var selectedMuscleId by remember { mutableStateOf("PECHO") }

    val statusMap = remember(muscleStatuses) {
        muscleStatuses.associateBy { it.id }
    }

    val selectedStatus = statusMap[selectedMuscleId] ?: muscleStatuses.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "MAPA MUSCULAR DE RECUPERACIÓN",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Toca cualquier zona para ver su estado",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // View toggle (Frente / Espalda)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedViewIndex == 0) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { selectedViewIndex = 0 }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Frente",
                        fontSize = 12.sp,
                        fontWeight = if (selectedViewIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedViewIndex == 0) NeonCyan else TextMuted
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedViewIndex == 1) NeonCyan.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { selectedViewIndex = 1 }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Espalda",
                        fontSize = 12.sp,
                        fontWeight = if (selectedViewIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedViewIndex == 1) NeonCyan else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Body Canvas Illustration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF090C12))
                .border(1.dp, DarkSurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            BodySilhouetteCanvas(
                isBackView = selectedViewIndex == 1,
                statusMap = statusMap,
                selectedMuscleId = selectedMuscleId,
                onSelectMuscle = { selectedMuscleId = it }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Legend: Fatigado / Recuperando / Listo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecoveryLegendItem(color = MuscleFatigued, label = "Fatigado (<12h)")
            RecoveryLegendItem(color = MuscleRecovering, label = "Recuperando")
            RecoveryLegendItem(color = MuscleReady, label = "Listo (>36h)")
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Muscle Detail Box
        if (selectedStatus != null) {
            val statusColor = when (selectedStatus.state) {
                RecoveryState.FATIGUED -> MuscleFatigued
                RecoveryState.RECOVERING -> MuscleRecovering
                RecoveryState.READY -> MuscleReady
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.5.dp, statusColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
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
                                    .size(12.dp)
                                    .background(statusColor, CircleShape)
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedStatus.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        NeonBadge(
                            text = "${selectedStatus.recoveryPercent}% RECUPERADO",
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = selectedStatus.lastWorkoutDate,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = selectedStatus.recommendedAction,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun RecoveryLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BodySilhouetteCanvas(
    isBackView: Boolean,
    statusMap: Map<String, MuscleStatus>,
    selectedMuscleId: String,
    onSelectMuscle: (String) -> Unit
) {
    val chestColor = getMuscleColor(statusMap["PECHO"])
    val backColor = getMuscleColor(statusMap["ESPALDA"])
    val shoulderColor = getMuscleColor(statusMap["HOMBROS"])
    val armsColor = getMuscleColor(statusMap["BRAZOS"])
    val coreColor = getMuscleColor(statusMap["CORE"])
    val legsColor = getMuscleColor(statusMap["PIERNAS"])

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .pointerInput(isBackView) {
                detectTapGestures { offset ->
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val y = offset.y
                    val x = offset.x

                    // Hit test areas based on relative Y and X
                    if (y in (h * 0.18f)..(h * 0.32f)) {
                        if (x in (cx - 100f)..(cx + 100f)) {
                            onSelectMuscle(if (isBackView) "ESPALDA" else "PECHO")
                        } else {
                            onSelectMuscle("HOMBROS")
                        }
                    } else if (y in (h * 0.32f)..(h * 0.48f)) {
                        if (x in (cx - 50f)..(cx + 50f)) {
                            onSelectMuscle(if (isBackView) "ESPALDA" else "CORE")
                        } else {
                            onSelectMuscle("BRAZOS")
                        }
                    } else if (y in (h * 0.48f)..(h * 0.95f)) {
                        onSelectMuscle("PIERNAS")
                    } else if (y < (h * 0.18f)) {
                        onSelectMuscle("HOMBROS")
                    }
                }
            }
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val scale = size.height / 300f

        // Grid lines for high-tech neon fitness aesthetic
        drawNeonGrid(size)

        // Head
        drawCircle(
            color = Color(0xFF1E2638),
            radius = 16f * scale,
            center = Offset(cx, 32f * scale)
        )
        drawCircle(
            color = NeonCyan.copy(alpha = 0.4f),
            radius = 16f * scale,
            center = Offset(cx, 32f * scale),
            style = Stroke(width = 1.5f * scale)
        )

        // Traps / Neck
        drawRoundRect(
            color = Color(0xFF1B2333),
            topLeft = Offset(cx - 14f * scale, 44f * scale),
            size = Size(28f * scale, 16f * scale),
            cornerRadius = CornerRadius(4f * scale, 4f * scale)
        )

        if (!isBackView) {
            // FRONT VIEW

            // Shoulders (Left and Right Delts)
            val isShouldersSelected = selectedMuscleId == "HOMBROS"
            drawMuscleBlock(
                color = shoulderColor,
                rect = Offset(cx - 54f * scale, 58f * scale),
                size = Size(24f * scale, 22f * scale),
                isSelected = isShouldersSelected,
                label = "Deltoides"
            )
            drawMuscleBlock(
                color = shoulderColor,
                rect = Offset(cx + 30f * scale, 58f * scale),
                size = Size(24f * scale, 22f * scale),
                isSelected = isShouldersSelected,
                label = null
            )

            // Pectorals (Chest)
            val isChestSelected = selectedMuscleId == "PECHO"
            drawMuscleBlock(
                color = chestColor,
                rect = Offset(cx - 28f * scale, 60f * scale),
                size = Size(56f * scale, 34f * scale),
                isSelected = isChestSelected,
                label = "Pectorales"
            )

            // Arms (Biceps / Forearms)
            val isArmsSelected = selectedMuscleId == "BRAZOS"
            drawMuscleBlock(
                color = armsColor,
                rect = Offset(cx - 52f * scale, 84f * scale),
                size = Size(18f * scale, 50f * scale),
                isSelected = isArmsSelected,
                label = "Bíceps"
            )
            drawMuscleBlock(
                color = armsColor,
                rect = Offset(cx + 34f * scale, 84f * scale),
                size = Size(18f * scale, 50f * scale),
                isSelected = isArmsSelected,
                label = null
            )

            // Core / Abs
            val isCoreSelected = selectedMuscleId == "CORE"
            drawMuscleBlock(
                color = coreColor,
                rect = Offset(cx - 22f * scale, 98f * scale),
                size = Size(44f * scale, 44f * scale),
                isSelected = isCoreSelected,
                label = "Abdomen"
            )

            // Legs (Quadriceps / Calves)
            val isLegsSelected = selectedMuscleId == "PIERNAS"
            // Left thigh
            drawMuscleBlock(
                color = legsColor,
                rect = Offset(cx - 26f * scale, 148f * scale),
                size = Size(22f * scale, 64f * scale),
                isSelected = isLegsSelected,
                label = "Cuádriceps"
            )
            // Right thigh
            drawMuscleBlock(
                color = legsColor,
                rect = Offset(cx + 4f * scale, 148f * scale),
                size = Size(22f * scale, 64f * scale),
                isSelected = isLegsSelected,
                label = null
            )
            // Calves
            drawMuscleBlock(
                color = legsColor.copy(alpha = 0.85f),
                rect = Offset(cx - 24f * scale, 216f * scale),
                size = Size(18f * scale, 52f * scale),
                isSelected = isLegsSelected,
                label = null
            )
            drawMuscleBlock(
                color = legsColor.copy(alpha = 0.85f),
                rect = Offset(cx + 6f * scale, 216f * scale),
                size = Size(18f * scale, 52f * scale),
                isSelected = isLegsSelected,
                label = null
            )

        } else {
            // BACK VIEW

            // Back Shoulders (Rear Delts)
            val isShouldersSelected = selectedMuscleId == "HOMBROS"
            drawMuscleBlock(
                color = shoulderColor,
                rect = Offset(cx - 54f * scale, 58f * scale),
                size = Size(24f * scale, 22f * scale),
                isSelected = isShouldersSelected,
                label = null
            )
            drawMuscleBlock(
                color = shoulderColor,
                rect = Offset(cx + 30f * scale, 58f * scale),
                size = Size(24f * scale, 22f * scale),
                isSelected = isShouldersSelected,
                label = null
            )

            // Upper Back & Lats (Espalda / Dorsales)
            val isBackSelected = selectedMuscleId == "ESPALDA"
            drawMuscleBlock(
                color = backColor,
                rect = Offset(cx - 32f * scale, 60f * scale),
                size = Size(64f * scale, 74f * scale),
                isSelected = isBackSelected,
                label = "Dorsales"
            )

            // Arms (Tríceps)
            val isArmsSelected = selectedMuscleId == "BRAZOS"
            drawMuscleBlock(
                color = armsColor,
                rect = Offset(cx - 52f * scale, 84f * scale),
                size = Size(18f * scale, 50f * scale),
                isSelected = isArmsSelected,
                label = "Tríceps"
            )
            drawMuscleBlock(
                color = armsColor,
                rect = Offset(cx + 34f * scale, 84f * scale),
                size = Size(18f * scale, 50f * scale),
                isSelected = isArmsSelected,
                label = null
            )

            // Glutes & Hamstrings (Piernas posterior)
            val isLegsSelected = selectedMuscleId == "PIERNAS"
            drawMuscleBlock(
                color = legsColor,
                rect = Offset(cx - 26f * scale, 140f * scale),
                size = Size(22f * scale, 70f * scale),
                isSelected = isLegsSelected,
                label = "Isquios"
            )
            drawMuscleBlock(
                color = legsColor,
                rect = Offset(cx + 4f * scale, 140f * scale),
                size = Size(22f * scale, 70f * scale),
                isSelected = isLegsSelected,
                label = null
            )
            // Gemelos
            drawMuscleBlock(
                color = legsColor.copy(alpha = 0.85f),
                rect = Offset(cx - 24f * scale, 214f * scale),
                size = Size(18f * scale, 52f * scale),
                isSelected = isLegsSelected,
                label = null
            )
            drawMuscleBlock(
                color = legsColor.copy(alpha = 0.85f),
                rect = Offset(cx + 6f * scale, 214f * scale),
                size = Size(18f * scale, 52f * scale),
                isSelected = isLegsSelected,
                label = null
            )
        }
    }
}

private fun DrawScope.drawNeonGrid(size: Size) {
    val gridColor = Color(0xFF141C2E).copy(alpha = 0.5f)
    var x = 0f
    while (x <= size.width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
        x += 40f
    }
    var y = 0f
    while (y <= size.height) {
        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        y += 40f
    }
}

private fun DrawScope.drawMuscleBlock(
    color: Color,
    rect: Offset,
    size: Size,
    isSelected: Boolean,
    label: String?
) {
    val cornerRadius = CornerRadius(8f, 8f)

    // Semi-transparent muscle body fill
    drawRoundRect(
        color = color.copy(alpha = if (isSelected) 0.55f else 0.35f),
        topLeft = rect,
        size = size,
        cornerRadius = cornerRadius
    )

    // Glowing border
    drawRoundRect(
        color = if (isSelected) Color.White else color,
        topLeft = rect,
        size = size,
        cornerRadius = cornerRadius,
        style = Stroke(width = if (isSelected) 3f else 2f)
    )

    // Selection aura
    if (isSelected) {
        drawRoundRect(
            color = color.copy(alpha = 0.25f),
            topLeft = Offset(rect.x - 4f, rect.y - 4f),
            size = Size(size.width + 8f, size.height + 8f),
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(width = 2f)
        )
    }
}

private fun getMuscleColor(status: MuscleStatus?): Color {
    return when (status?.state) {
        RecoveryState.FATIGUED -> MuscleFatigued
        RecoveryState.RECOVERING -> MuscleRecovering
        RecoveryState.READY -> MuscleReady
        null -> MuscleReady
    }
}
