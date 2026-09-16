package com.example.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.GymApp
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    fun sendWorkoutReminder(context: Context, customMessage: String? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val messages = listOf(
            "🔥 ¡Es hora de entrenar! Mantén viva tu racha en NeonFit.",
            "⚡ Tus músculos están descansados y listos para levantar pesado hoy.",
            "💪 No rompas la disciplina: ¡Registra tu sesión de hoy y desbloquea logros!",
            "🚀 Récord personal a la vista: ¡Ve al GYM y supera tus límites!"
        )
        val text = customMessage ?: messages.random()

        val notification = NotificationCompat.Builder(context, GymApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_gym_launcher)
            .setContentTitle("⚡ Recordatorio NeonFit GYM")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notification)
    }
}
