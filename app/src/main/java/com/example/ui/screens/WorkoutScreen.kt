package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkoutSet
import com.example.ui.WorkoutViewModel
import com.example.ui.components.NeonBadge
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonCard
import com.example.ui.components.RestTimerCard
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val allSets by viewModel.allSets.collectAsState()
    val streak by viewModel.streak.collectAsState()

    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val timerTotalSeconds by viewModel.timerTotalSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    val inputExercise by viewModel.inputExerciseName.collectAsState()
    val inputMuscle by viewModel.inputMuscleGroup.collectAsState()
    val inputWeight by viewModel.inputWeight.collectAsState()
    val inputReps by viewModel.inputReps.collectAsState()
    val inputNotes by viewModel.inputNotes.collectAsState()

    var showTimerCard by remember { mutableStateOf(true) }

    // Today's sets
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todaySets = remember(allSets) {
        allSets.filter { it.timestamp >= todayStart }
    }
    val todayTotalVolume = remember(todaySets) {
        todaySets.sumOf { (it.weightKg * it.reps).toDouble() }.toFloat()
    }

    // Common exercises list
    val exercisePresets = remember {
        listOf(
            Triple("Press de Banca Plano", "PECHO", "Pecho"),
            Triple("Sentadilla con Barra", "PIERNAS", "Piernas"),
            Triple("Peso Muerto", "ESPALDA", "Espalda"),
            Triple("Press Militar", "HOMBROS", "Hombros"),
            Triple("Curl con Barra Z", "BRAZOS", "Brazos"),
            Triple("Dominadas", "ESPALDA", "Espalda"),
            Triple("Fondos en Paralelas", "PECHO", "Pecho"),
            Triple("Plancha Abdominal", "CORE", "Core")
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header with NeonFit branding & Streak
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .border(1.5.dp, NeonGreen, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NeonFit GYM",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Registro & Sesión Activa",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Streak pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, NeonOrange.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = NeonOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streak días de racha",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonOrange
                    )
                }
            }
        }

        // Rest Timer Section (Collapsible)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TEMPORIZADOR DE DESCANSO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (showTimerCard) "Ocultar" else "Mostrar",
                        fontSize = 12.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showTimerCard = !showTimerCard }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                AnimatedVisibility(visible = showTimerCard) {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        RestTimerCard(
                            remainingSeconds = timerSeconds,
                            totalSeconds = timerTotalSeconds,
                            isRunning = isTimerRunning,
                            onStart = { viewModel.startTimer() },
                            onPause = { viewModel.pauseTimer() },
                            onReset = { viewModel.resetTimer() },
                            onAddSeconds = { viewModel.addTimerSeconds(it) },
                            onSelectPreset = { viewModel.setTimerPreset(it) }
                        )
                    }
                }
            }
        }

        // Quick Preset Exercises Chips
        item {
            Column {
                Text(
                    text = "EJERCICIOS RÁPIDOS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(exercisePresets) { (name, group, label) ->
                        val isSelected = inputExercise.equals(name, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) NeonGreen.copy(alpha = 0.2f) else DarkSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonGreen else DarkSurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    viewModel.setQuickExercise(name, group)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) NeonGreen else TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                NeonBadge(
                                    text = label,
                                    color = if (isSelected) NeonGreen else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Log New Set Card
        item {
            NeonCard(
                borderColor = NeonGreen.copy(alpha = 0.5f),
                glowColor = NeonGreen.copy(alpha = 0.2f)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REGISTRAR SERIE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonGreen,
                            letterSpacing = 1.sp
                        )
                        NeonBadge(
                            text = inputMuscle,
                            color = NeonCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Exercise Name Field
                    OutlinedTextField(
                        value = inputExercise,
                        onValueChange = { viewModel.inputExerciseName.value = it },
                        label = { Text("Nombre del Ejercicio") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("exercise_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Weight and Reps Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Weight Field
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "PESO (KG)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val current = viewModel.inputWeight.value.toFloatOrNull() ?: 0f
                                        val next = (current - 2.5f).coerceAtLeast(0f)
                                        viewModel.inputWeight.value = if (next % 1f == 0f) next.toInt().toString() else next.toString()
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos peso", tint = TextPrimary)
                                }

                                OutlinedTextField(
                                    value = inputWeight,
                                    onValueChange = { viewModel.inputWeight.value = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp)
                                        .testTag("weight_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonGreen,
                                        unfocusedBorderColor = DarkSurfaceBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = DarkSurfaceElevated,
                                        unfocusedContainerColor = DarkSurfaceElevated
                                    ),
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = {
                                        val current = viewModel.inputWeight.value.toFloatOrNull() ?: 0f
                                        val next = current + 2.5f
                                        viewModel.inputWeight.value = if (next % 1f == 0f) next.toInt().toString() else next.toString()
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Más peso", tint = TextPrimary)
                                }
                            }
                        }

                        // Reps Field
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "REPETICIONES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val current = viewModel.inputReps.value.toIntOrNull() ?: 1
                                        viewModel.inputReps.value = (current - 1).coerceAtLeast(1).toString()
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Menos reps", tint = TextPrimary)
                                }

                                OutlinedTextField(
                                    value = inputReps,
                                    onValueChange = { viewModel.inputReps.value = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 6.dp)
                                        .testTag("reps_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonGreen,
                                        unfocusedBorderColor = DarkSurfaceBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = DarkSurfaceElevated,
                                        unfocusedContainerColor = DarkSurfaceElevated
                                    ),
                                    singleLine = true
                                )

                                IconButton(
                                    onClick = {
                                        val current = viewModel.inputReps.value.toIntOrNull() ?: 0
                                        viewModel.inputReps.value = (current + 1).toString()
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Más reps", tint = TextPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notes Field
                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { viewModel.inputNotes.value = it },
                        label = { Text("Notas de la serie (ej. RPE 8, buena pausa)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    NeonButton(
                        text = "GUARDAR SERIE (+ INICIAR DESCANSO)",
                        icon = Icons.Default.Check,
                        neonColor = NeonGreen,
                        onClick = { viewModel.logCurrentSet(autoStartTimer = true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_set_button")
                    )
                }
            }
        }

        // Today's Sets Summary Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SERIES REGISTRADAS HOY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${todaySets.size} series • Volumen: ${todayTotalVolume.toInt()} kg",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        if (todaySets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aún no has registrado series hoy",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Usa el formulario arriba para empezar tu sesión",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            items(todaySets, key = { it.id }) { set ->
                WorkoutSetItemCard(
                    set = set,
                    onDelete = { viewModel.deleteSet(set) }
                )
            }
        }
    }
}

@Composable
fun WorkoutSetItemCard(
    set: WorkoutSet,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = remember(set.timestamp) { timeFormat.format(Date(set.timestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Set badge number
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(DarkSurfaceElevated, CircleShape)
                        .border(1.5.dp, NeonGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S${set.setNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGreen
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = set.exerciseName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${set.weightKg} kg × ${set.reps} reps",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${((set.weightKg * set.reps)).toInt()} kg vol)",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                    if (set.notes.isNotBlank()) {
                        Text(
                            text = "📝 ${set.notes}",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    color = TextMuted
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar serie",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
