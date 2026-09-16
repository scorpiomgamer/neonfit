package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val targetValue: Float,
    val currentValue: Float = 0f,
    val isUnlocked: Boolean = false,
    val iconName: String = "trophy",
    val unlockedDate: Long? = null
)
