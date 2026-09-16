package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sets")
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseName: String,
    val muscleGroup: String, // "PECHO", "ESPALDA", "PIERNAS", "HOMBROS", "BRAZOS", "CORE"
    val weightKg: Float,
    val reps: Int,
    val setNumber: Int = 1,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
