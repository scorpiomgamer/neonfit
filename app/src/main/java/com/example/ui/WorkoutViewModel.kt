package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.GymApp
import com.example.data.Achievement
import com.example.data.MuscleStatus
import com.example.data.WearableData
import com.example.data.WorkoutRepository
import com.example.data.WorkoutSet
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class StrengthDataPoint(
    val dateLabel: String,
    val weightKg: Float,
    val reps: Int,
    val estimated1Rm: Float,
    val timestamp: Long
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WorkoutRepository = (application as GymApp).repository

    val allSets: StateFlow<List<WorkoutSet>> = repository.allSets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAchievements: StateFlow<List<Achievement>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected exercise for chart
    private val _selectedExercise = MutableStateFlow("Press de Banca Plano")
    val selectedExercise: StateFlow<String> = _selectedExercise.asStateFlow()

    // Filtered strength chart data points
    val chartDataPoints: StateFlow<List<StrengthDataPoint>> = combine(allSets, selectedExercise) { sets, exercise ->
        val filtered = sets.filter { it.exerciseName.equals(exercise, ignoreCase = true) }
            .sortedBy { it.timestamp }

        filtered.map { set ->
            // Brzycki Formula for 1RM: Weight * (36 / (37 - Reps))
            val oneRm = if (set.reps <= 1) set.weightKg else set.weightKg * (36f / (37f - set.reps.coerceAtMost(30)))
            val cal = Calendar.getInstance().apply { timeInMillis = set.timestamp }
            val label = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
            StrengthDataPoint(
                dateLabel = label,
                weightKg = set.weightKg,
                reps = set.reps,
                estimated1Rm = (Math.round(oneRm * 10) / 10f),
                timestamp = set.timestamp
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Streak calculation
    val streak: StateFlow<Int> = allSets.combine(MutableStateFlow(Unit)) { sets, _ ->
        repository.calculateStreak(sets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Muscle recovery status
    val muscleStatuses: StateFlow<List<MuscleStatus>> = allSets.combine(MutableStateFlow(Unit)) { sets, _ ->
        repository.getMuscleRecoveryStatuses(sets)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Rest Timer State
    private val _timerSeconds = MutableStateFlow(60)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(60)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null

    // Cloud Sync State
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(repository.getLastCloudSyncTime())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    // Wearable State
    private val _wearableData = MutableStateFlow(repository.getWearableData())
    val wearableData: StateFlow<WearableData> = _wearableData.asStateFlow()

    // Add Workout Input Form State
    var inputExerciseName = MutableStateFlow("Press de Banca Plano")
    var inputMuscleGroup = MutableStateFlow("PECHO")
    var inputWeight = MutableStateFlow("80")
    var inputReps = MutableStateFlow("10")
    var inputNotes = MutableStateFlow("")

    fun selectExerciseForChart(exercise: String) {
        _selectedExercise.value = exercise
    }

    fun setQuickExercise(name: String, group: String) {
        inputExerciseName.value = name
        inputMuscleGroup.value = group
        _selectedExercise.value = name
    }

    fun logCurrentSet(autoStartTimer: Boolean = true) {
        val weight = inputWeight.value.toFloatOrNull() ?: 0f
        val reps = inputReps.value.toIntOrNull() ?: 0
        if (inputExerciseName.value.isBlank() || weight <= 0f || reps <= 0) return

        viewModelScope.launch {
            repository.logSet(
                exerciseName = inputExerciseName.value,
                muscleGroup = inputMuscleGroup.value,
                weightKg = weight,
                reps = reps,
                notes = inputNotes.value
            )
            // Wearable update (burn extra calories on set)
            repository.syncWearablePulse(
                newBpm = (120..155).random(),
                addedCalories = (12..25).random()
            )
            _wearableData.value = repository.getWearableData()

            if (autoStartTimer) {
                startTimer()
            }
        }
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            repository.deleteSet(set)
        }
    }

    // --- REST TIMER METHODS ---
    fun setTimerPreset(seconds: Int) {
        pauseTimer()
        _timerTotalSeconds.value = seconds
        _timerSeconds.value = seconds
    }

    fun addTimerSeconds(seconds: Int) {
        _timerSeconds.value += seconds
        _timerTotalSeconds.value = maxOf(_timerTotalSeconds.value, _timerSeconds.value)
    }

    fun startTimer() {
        if (_timerSeconds.value <= 0) {
            _timerSeconds.value = _timerTotalSeconds.value
        }
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0 && _isTimerRunning.value) {
                delay(1000L)
                _timerSeconds.value -= 1
            }
            if (_timerSeconds.value == 0) {
                _isTimerRunning.value = false
                triggerTimerFinishedEffect()
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timerSeconds.value = _timerTotalSeconds.value
    }

    private fun triggerTimerFinishedEffect() {
        val context = getApplication<Application>().applicationContext
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 200, 100, 200, 100, 300),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        } catch (_: Exception) {}
    }

    // --- CLOUD SYNC METHODS ---
    fun syncWithCloud() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = null
            delay(1500L) // Realistic network round-trip simulation
            repository.updateLastCloudSyncTime()
            _lastSyncTime.value = repository.getLastCloudSyncTime()
            _isSyncing.value = false
            _syncMessage.value = "¡Sincronización con la nube exitosa! Tus entrenamientos están respaldados."
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    // --- WEARABLE HEALTH METHODS ---
    fun refreshWearableSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            delay(800L)
            val current = repository.getWearableData()
            val newBpm = (95..145).random()
            val newCals = current.caloriesBurned + (5..15).random()
            val newSteps = current.steps + (30..80).random()
            repository.syncWearablePulse(newBpm, 10)
            _wearableData.value = current.copy(
                heartRateBpm = newBpm,
                caloriesBurned = newCals,
                steps = newSteps,
                lastSyncTime = System.currentTimeMillis()
            )
            _isSyncing.value = false
        }
    }

    fun toggleWearableConnection() {
        val current = _wearableData.value.isConnected
        repository.toggleWearableConnection(!current)
        _wearableData.value = _wearableData.value.copy(isConnected = !current)
    }

    // --- NOTIFICATION REMINDER ---
    fun testNotificationReminder() {
        NotificationHelper.sendWorkoutReminder(getApplication())
    }
}
