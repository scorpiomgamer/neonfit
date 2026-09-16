package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class RecoveryState {
    FATIGUED,    // < 12h: Red neon
    RECOVERING,  // 12h - 36h: Yellow/Amber neon
    READY        // > 36h: Green neon
}

data class MuscleStatus(
    val id: String,
    val displayName: String,
    val hoursElapsed: Float,
    val recoveryPercent: Int, // 0 to 100
    val state: RecoveryState,
    val lastWorkoutDate: String,
    val recommendedAction: String
)

data class WearableData(
    val isConnected: Boolean = true,
    val deviceName: String = "Galaxy Watch 6 Pro (BLE)",
    val heartRateBpm: Int = 128,
    val caloriesBurned: Int = 420,
    val steps: Int = 7450,
    val lastSyncTime: Long = System.currentTimeMillis()
)

class WorkoutRepository(
    private val dao: WorkoutDao,
    private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("neonfit_prefs", Context.MODE_PRIVATE)

    val allSets: Flow<List<WorkoutSet>> = dao.getAllSets()
    val allAchievements: Flow<List<Achievement>> = dao.getAllAchievements()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultsIfEmpty()
        }
    }

    private suspend fun seedDefaultsIfEmpty() {
        val existing = dao.getAllAchievementsList()
        if (existing.isEmpty()) {
            val initialAchievements = listOf(
                Achievement(
                    id = "first_lift",
                    title = "Primer Levantamiento",
                    description = "Registra tu primer ejercicio en el GYM",
                    targetValue = 1f,
                    currentValue = 0f,
                    isUnlocked = false,
                    iconName = "fitness_center"
                ),
                Achievement(
                    id = "club_100kg",
                    title = "Club de los 100 KG",
                    description = "Levanta 100 kg o más en una serie",
                    targetValue = 100f,
                    currentValue = 0f,
                    isUnlocked = false,
                    iconName = "bolt"
                ),
                Achievement(
                    id = "streak_3",
                    title = "Racha de Bronce",
                    description = "Alcanza 3 días consecutivos de entrenamiento",
                    targetValue = 3f,
                    currentValue = 0f,
                    isUnlocked = false,
                    iconName = "local_fire_department"
                ),
                Achievement(
                    id = "streak_7",
                    title = "Titán de la Semana",
                    description = "Mantén una racha de 7 días activa",
                    targetValue = 7f,
                    currentValue = 0f,
                    isUnlocked = false,
                    iconName = "military_tech"
                ),
                Achievement(
                    id = "volume_5k",
                    title = "Monstruo del Volumen",
                    description = "Alcanza 5,000 kg de volumen total levantado",
                    targetValue = 5000f,
                    currentValue = 0f,
                    isUnlocked = false,
                    iconName = "trending_up"
                ),
                Achievement(
                    id = "iron_master",
                    title = "Maestro del Hierro",
                    description = "Completa 25 series registradas",
                    targetValue = 25f,
                    currentValue = 0f,
                    isUnlocked = false,
                    iconName = "emoji_events"
                )
            )
            dao.insertAchievements(initialAchievements)
        }

        // Add demo workout records if database has no records
        val initialSets = dao.getAllSets().firstOrNull()
        if (initialSets.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val dayMs = 24L * 3600 * 1000
            val sampleSets = listOf(
                // 3 days ago - Legs
                WorkoutSet(
                    exerciseName = "Sentadilla con Barra",
                    muscleGroup = "PIERNAS",
                    weightKg = 90f,
                    reps = 8,
                    setNumber = 1,
                    notes = "Calentamiento fluido",
                    timestamp = now - (dayMs * 3) + 3600000
                ),
                WorkoutSet(
                    exerciseName = "Sentadilla con Barra",
                    muscleGroup = "PIERNAS",
                    weightKg = 105f,
                    reps = 6,
                    setNumber = 2,
                    notes = "Récord personal!",
                    timestamp = now - (dayMs * 3) + 7200000
                ),
                // 2 days ago - Back & Arms
                WorkoutSet(
                    exerciseName = "Peso Muerto",
                    muscleGroup = "ESPALDA",
                    weightKg = 110f,
                    reps = 5,
                    setNumber = 1,
                    notes = "Espalda recta, agarre mixto",
                    timestamp = now - (dayMs * 2) + 3600000
                ),
                WorkoutSet(
                    exerciseName = "Curl con Barra Z",
                    muscleGroup = "BRAZOS",
                    weightKg = 35f,
                    reps = 10,
                    setNumber = 1,
                    notes = "Pico de contracción 2s",
                    timestamp = now - (dayMs * 2) + 7200000
                ),
                // Yesterday - Chest & Shoulders
                WorkoutSet(
                    exerciseName = "Press de Banca Plano",
                    muscleGroup = "PECHO",
                    weightKg = 85f,
                    reps = 8,
                    setNumber = 1,
                    notes = "Control en la bajada",
                    timestamp = now - dayMs + 3600000
                ),
                WorkoutSet(
                    exerciseName = "Press Militar",
                    muscleGroup = "HOMBROS",
                    weightKg = 50f,
                    reps = 8,
                    setNumber = 1,
                    notes = "Core firme",
                    timestamp = now - dayMs + 7200000
                )
            )
            dao.insertSets(sampleSets)
            evaluateAchievements(sampleSets)
        }
    }

    suspend fun logSet(
        exerciseName: String,
        muscleGroup: String,
        weightKg: Float,
        reps: Int,
        notes: String = ""
    ): Long {
        // Count sets for this exercise today to set the setNumber
        val startOfToday = getStartOfDay(System.currentTimeMillis())
        val endOfToday = startOfToday + 24 * 3600 * 1000 - 1
        val todaySets = dao.getSetsBetween(startOfToday, endOfToday).firstOrNull() ?: emptyList()
        val currentSetNumber = todaySets.count { it.exerciseName.equals(exerciseName, ignoreCase = true) } + 1

        val newSet = WorkoutSet(
            exerciseName = exerciseName.trim(),
            muscleGroup = muscleGroup.uppercase().trim(),
            weightKg = weightKg,
            reps = reps,
            setNumber = currentSetNumber,
            notes = notes.trim(),
            timestamp = System.currentTimeMillis(),
            synced = false
        )
        val id = dao.insertSet(newSet)

        // Re-evaluate achievements
        val allCurrentSets = dao.getAllSets().firstOrNull() ?: emptyList()
        evaluateAchievements(allCurrentSets)

        return id
    }

    suspend fun deleteSet(set: WorkoutSet) {
        dao.deleteSet(set)
    }

    suspend fun clearAll() {
        dao.clearAllSets()
    }

    private suspend fun evaluateAchievements(sets: List<WorkoutSet>) {
        if (sets.isEmpty()) return

        val achievements = dao.getAllAchievementsList().associateBy { it.id }.toMutableMap()
        val totalSetsCount = sets.size.toFloat()
        val maxWeight = sets.maxOfOrNull { it.weightKg } ?: 0f
        val totalVolume = sets.sumOf { (it.weightKg * it.reps).toDouble() }.toFloat()
        val streak = calculateStreak(sets)

        // 1. First lift
        achievements["first_lift"]?.let { ach ->
            val updated = ach.copy(
                currentValue = totalSetsCount.coerceAtMost(1f),
                isUnlocked = totalSetsCount >= 1f,
                unlockedDate = if (!ach.isUnlocked && totalSetsCount >= 1f) System.currentTimeMillis() else ach.unlockedDate
            )
            dao.updateAchievement(updated)
        }

        // 2. Club 100kg
        achievements["club_100kg"]?.let { ach ->
            val updated = ach.copy(
                currentValue = maxWeight,
                isUnlocked = maxWeight >= 100f,
                unlockedDate = if (!ach.isUnlocked && maxWeight >= 100f) System.currentTimeMillis() else ach.unlockedDate
            )
            dao.updateAchievement(updated)
        }

        // 3. Streak 3
        achievements["streak_3"]?.let { ach ->
            val updated = ach.copy(
                currentValue = streak.toFloat(),
                isUnlocked = streak >= 3,
                unlockedDate = if (!ach.isUnlocked && streak >= 3) System.currentTimeMillis() else ach.unlockedDate
            )
            dao.updateAchievement(updated)
        }

        // 4. Streak 7
        achievements["streak_7"]?.let { ach ->
            val updated = ach.copy(
                currentValue = streak.toFloat(),
                isUnlocked = streak >= 7,
                unlockedDate = if (!ach.isUnlocked && streak >= 7) System.currentTimeMillis() else ach.unlockedDate
            )
            dao.updateAchievement(updated)
        }

        // 5. Volume 5k
        achievements["volume_5k"]?.let { ach ->
            val updated = ach.copy(
                currentValue = totalVolume,
                isUnlocked = totalVolume >= 5000f,
                unlockedDate = if (!ach.isUnlocked && totalVolume >= 5000f) System.currentTimeMillis() else ach.unlockedDate
            )
            dao.updateAchievement(updated)
        }

        // 6. 25 Sets
        achievements["iron_master"]?.let { ach ->
            val updated = ach.copy(
                currentValue = totalSetsCount,
                isUnlocked = totalSetsCount >= 25f,
                unlockedDate = if (!ach.isUnlocked && totalSetsCount >= 25f) System.currentTimeMillis() else ach.unlockedDate
            )
            dao.updateAchievement(updated)
        }
    }

    fun calculateStreak(sets: List<WorkoutSet>): Int {
        if (sets.isEmpty()) return 0
        val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val daysWithWorkouts = sets.map { dayFormat.format(Date(it.timestamp)) }.toSet()

        val calendar = Calendar.getInstance()
        var streak = 0

        // Check if user worked out today or yesterday to continue streak
        val todayStr = dayFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dayFormat.format(calendar.time)

        if (!daysWithWorkouts.contains(todayStr) && !daysWithWorkouts.contains(yesterdayStr)) {
            return 0
        }

        // Start checking backwards from today or yesterday
        val checkCalendar = Calendar.getInstance()
        if (!daysWithWorkouts.contains(todayStr)) {
            checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val key = dayFormat.format(checkCalendar.time)
            if (daysWithWorkouts.contains(key)) {
                streak++
                checkCalendar.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    fun getMuscleRecoveryStatuses(sets: List<WorkoutSet>): List<MuscleStatus> {
        val groups = listOf(
            Triple("PECHO", "Pectorales", "Press plano, declinado, aperturas"),
            Triple("ESPALDA", "Espalda / Dorsales", "Peso muerto, dominadas, remo"),
            Triple("PIERNAS", "Piernas / Cuádriceps", "Sentadillas, prensa, zancadas"),
            Triple("HOMBROS", "Hombros / Deltoides", "Press militar, elevaciones laterales"),
            Triple("BRAZOS", "Bíceps y Tríceps", "Curl con barra, fondos, polea"),
            Triple("CORE", "Core / Abdominales", "Planchas, elevación de piernas")
        )

        val now = System.currentTimeMillis()
        val fullRecoveryHours = 48f // standard muscle recovery window

        return groups.map { (id, name, exerciseTypes) ->
            val latestSet = sets.filter { it.muscleGroup.equals(id, ignoreCase = true) }
                .maxByOrNull { it.timestamp }

            if (latestSet == null) {
                MuscleStatus(
                    id = id,
                    displayName = name,
                    hoursElapsed = 999f,
                    recoveryPercent = 100,
                    state = RecoveryState.READY,
                    lastWorkoutDate = "Sin registros recientes",
                    recommendedAction = "100% Recuperado. Ideal para entrenar hoy ($exerciseTypes)."
                )
            } else {
                val elapsedMs = (now - latestSet.timestamp).coerceAtLeast(0)
                val hoursElapsed = elapsedMs / (1000f * 60 * 60)
                val percent = ((hoursElapsed / fullRecoveryHours) * 100f).coerceIn(0f, 100f).toInt()

                val state = when {
                    hoursElapsed < 14f -> RecoveryState.FATIGUED
                    hoursElapsed < 36f -> RecoveryState.RECOVERING
                    else -> RecoveryState.READY
                }

                val df = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                val lastDateStr = "Último: " + df.format(Date(latestSet.timestamp))

                val action = when (state) {
                    RecoveryState.FATIGUED -> "Fatiga alta (${hoursElapsed.toInt()}h). Descansa o entrena otro grupo muscular."
                    RecoveryState.RECOVERING -> "En recuperación (${percent}%). Realiza estiramientos ligeros o descansa."
                    RecoveryState.READY -> "100% Recuperado. ¡Excelente momento para romper récord en $name!"
                }

                MuscleStatus(
                    id = id,
                    displayName = name,
                    hoursElapsed = hoursElapsed,
                    recoveryPercent = percent,
                    state = state,
                    lastWorkoutDate = lastDateStr,
                    recommendedAction = action
                )
            }
        }
    }

    // Cloud Sync simulation / export
    fun getLastCloudSyncTime(): Long {
        return prefs.getLong("last_cloud_sync_time", System.currentTimeMillis() - 1000 * 60 * 35)
    }

    fun updateLastCloudSyncTime(time: Long = System.currentTimeMillis()) {
        prefs.edit().putLong("last_cloud_sync_time", time).apply()
    }

    suspend fun exportToJson(sets: List<WorkoutSet>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        val array = JSONArray()
        for (set in sets) {
            val obj = JSONObject().apply {
                put("id", set.id)
                put("exerciseName", set.exerciseName)
                put("muscleGroup", set.muscleGroup)
                put("weightKg", set.weightKg.toDouble())
                put("reps", set.reps)
                put("setNumber", set.setNumber)
                put("notes", set.notes)
                put("timestamp", set.timestamp)
            }
            array.put(obj)
        }
        root.put("sets", array)
        return root.toString(2)
    }

    suspend fun importFromJson(jsonString: String): Int {
        val root = JSONObject(jsonString)
        val array = root.getJSONArray("sets")
        val setsToInsert = mutableListOf<WorkoutSet>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            setsToInsert.add(
                WorkoutSet(
                    exerciseName = obj.getString("exerciseName"),
                    muscleGroup = obj.getString("muscleGroup"),
                    weightKg = obj.getDouble("weightKg").toFloat(),
                    reps = obj.getInt("reps"),
                    setNumber = obj.optInt("setNumber", 1),
                    notes = obj.optString("notes", ""),
                    timestamp = obj.getLong("timestamp"),
                    synced = true
                )
            )
        }
        if (setsToInsert.isNotEmpty()) {
            dao.insertSets(setsToInsert)
            val allSets = dao.getAllSets().firstOrNull() ?: emptyList()
            evaluateAchievements(allSets)
        }
        updateLastCloudSyncTime()
        return setsToInsert.size
    }

    // Wearable simulation data
    fun getWearableData(): WearableData {
        val isConnected = prefs.getBoolean("wearable_connected", true)
        val bpm = prefs.getInt("wearable_bpm", 134)
        val calories = prefs.getInt("wearable_calories", 485)
        val steps = prefs.getInt("wearable_steps", 8230)
        return WearableData(
            isConnected = isConnected,
            deviceName = "Galaxy Watch 6 Pro (BLE)",
            heartRateBpm = bpm,
            caloriesBurned = calories,
            steps = steps,
            lastSyncTime = System.currentTimeMillis()
        )
    }

    fun toggleWearableConnection(connected: Boolean) {
        prefs.edit().putBoolean("wearable_connected", connected).apply()
    }

    fun syncWearablePulse(newBpm: Int, addedCalories: Int) {
        prefs.edit()
            .putInt("wearable_bpm", newBpm)
            .putInt("wearable_calories", prefs.getInt("wearable_calories", 400) + addedCalories)
            .apply()
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
