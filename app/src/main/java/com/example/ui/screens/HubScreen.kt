package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.Achievement
import com.example.ui.WorkoutViewModel
import com.example.ui.components.NeonBadge
import com.example.ui.components.NeonButton
import com.example.ui.components.NeonCard
import com.example.ui.components.NeonOutlinedButton
import com.example.ui.components.PulsingDot
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
import java.util.Date
import java.util.Locale

@Composable
fun HubScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val achievements by viewModel.allAchievements.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val wearableData by viewModel.wearableData.collectAsState()

    var reminderEnabled by remember { mutableStateOf(true) }
    var selectedReminderTime by remember { mutableStateOf("19:00") }

    val unlockedCount = remember(achievements) { achievements.count { it.isUnlocked } }
    val totalAchievements = remember(achievements) { achievements.size.coerceAtLeast(1) }
    val achievementsProgress = remember(unlockedCount, totalAchievements) {
        unlockedCount.toFloat() / totalAchievements.toFloat()
    }

    val syncDateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val lastSyncStr = remember(lastSyncTime) { syncDateFormat.format(Date(lastSyncTime)) }

    // Permission launcher for Android 13+ push notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.testNotificationReminder()
        }
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
                        text = "HUB DE MOTIVACIÓN & CONECTIVIDAD",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPink,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Logros, Nube & Wearables",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonPink.copy(alpha = 0.15f))
                        .border(1.5.dp, NeonPink, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = NeonPink,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Section 1: Cloud Sync Card
        item {
            NeonCard(
                borderColor = NeonCyan.copy(alpha = 0.5f),
                glowColor = NeonCyan.copy(alpha = 0.2f)
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
                                    .size(40.dp)
                                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, NeonCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SINCRONIZACIÓN EN LA NUBE",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Tus datos nunca se perderán",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        NeonBadge(
                            text = "ACTIVA",
                            color = NeonGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Último respaldo seguro: $lastSyncStr",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    AnimatedVisibility(visible = syncMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = syncMessage ?: "",
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    NeonButton(
                        text = if (isSyncing) "SINCRONIZANDO..." else "SINCRONIZAR AHORA CON LA NUBE",
                        icon = if (isSyncing) null else Icons.Default.CloudDone,
                        neonColor = NeonCyan,
                        enabled = !isSyncing,
                        onClick = { viewModel.syncWithCloud() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 2: Health Wearables Card
        item {
            NeonCard(
                borderColor = NeonGreen.copy(alpha = 0.4f),
                glowColor = NeonGreen.copy(alpha = 0.15f)
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
                                    .size(40.dp)
                                    .background(NeonGreen.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, NeonGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Watch,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "WEARABLE DE SALUD",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = wearableData.deviceName,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.refreshWearableSync() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar métricas",
                                tint = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Wearable live metrics row: BPM, Calories, Steps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Heart rate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, NeonPink.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = NeonPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Pulso", fontSize = 11.sp, color = TextSecondary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${wearableData.heartRateBpm} BPM",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonPink
                                )
                                Text(text = "Zona quemagrasa", fontSize = 9.sp, color = TextMuted)
                            }
                        }

                        // Active Calories
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, NeonOrange.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = NeonOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Calorías", fontSize = 11.sp, color = TextSecondary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${wearableData.caloriesBurned} kcal",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonOrange
                                )
                                Text(text = "Activas en sesión", fontSize = 9.sp, color = TextMuted)
                            }
                        }

                        // Daily Steps
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceElevated)
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Pasos", fontSize = 11.sp, color = TextSecondary)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${wearableData.steps}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonCyan
                                )
                                Text(text = "Hoy", fontSize = 9.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Daily Workout Notifications & Push Reminders
        item {
            NeonCard(
                borderColor = NeonYellow.copy(alpha = 0.4f),
                glowColor = NeonYellow.copy(alpha = 0.15f)
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
                                    .size(40.dp)
                                    .background(NeonYellow.copy(alpha = 0.15f), CircleShape)
                                    .border(1.dp, NeonYellow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = NeonYellow,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "RECORDATORIOS DIARIOS",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonYellow,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Notificaciones push para motivarte",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0D111A),
                                checkedTrackColor = NeonYellow,
                                uncheckedTrackColor = DarkSurfaceElevated
                            )
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "HORA DEL ENTRENAMIENTO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("07:00 AM", "18:00 PM", "20:00 PM").forEach { time ->
                                val isSelected = selectedReminderTime == time
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonYellow.copy(alpha = 0.2f) else DarkSurfaceElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonYellow else DarkSurfaceBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedReminderTime = time }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = time,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NeonYellow else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        NeonOutlinedButton(
                            text = "PROBAR NOTIFICACIÓN AHORA",
                            icon = Icons.Default.Notifications,
                            neonColor = NeonYellow,
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val check = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                    if (check != PackageManager.PERMISSION_GRANTED) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.testNotificationReminder()
                                    }
                                } else {
                                    viewModel.testNotificationReminder()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Section 4: Unlockable Achievements
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SECCIÓN DE LOGROS DESBLOQUEABLES",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "$unlockedCount de $totalAchievements Desbloqueados",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    NeonBadge(
                        text = "${(achievementsProgress * 100).toInt()}% COMPLETADO",
                        color = NeonPink
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { achievementsProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonPink,
                    trackColor = DarkSurfaceElevated
                )
            }
        }

        // Achievements List
        items(achievements, key = { it.id }) { achievement ->
            AchievementCardItem(achievement = achievement)
        }
    }
}

@Composable
fun AchievementCardItem(achievement: Achievement) {
    val isUnlocked = achievement.isUnlocked
    val iconVector: ImageVector = when (achievement.iconName) {
        "bolt" -> Icons.Default.Bolt
        "local_fire_department" -> Icons.Default.LocalFireDepartment
        "military_tech" -> Icons.Default.MilitaryTech
        "trending_up" -> Icons.Default.TrendingUp
        else -> Icons.Default.FitnessCenter
    }

    val glowColor = if (isUnlocked) NeonPink else DarkSurfaceBorder

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(
                1.dp,
                if (isUnlocked) NeonPink.copy(alpha = 0.6f) else DarkSurfaceBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (isUnlocked) NeonPink.copy(alpha = 0.2f) else DarkSurfaceElevated,
                        CircleShape
                    )
                    .border(
                        1.5.dp,
                        if (isUnlocked) NeonPink else DarkSurfaceBorder,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (isUnlocked) NeonPink else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) TextPrimary else TextSecondary
                    )

                    if (isUnlocked) {
                        NeonBadge(
                            text = "DESBLOQUEADO",
                            color = NeonPink
                        )
                    } else {
                        Text(
                            text = "${achievement.currentValue.toInt()} / ${achievement.targetValue.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = achievement.description,
                    fontSize = 12.sp,
                    color = TextMuted
                )

                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = (achievement.currentValue / achievement.targetValue).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NeonPink.copy(alpha = 0.7f),
                        trackColor = DarkSurfaceElevated
                    )
                }
            }
        }
    }
}
