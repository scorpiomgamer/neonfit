package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_sets ORDER BY timestamp DESC")
    fun getAllSets(): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workout_sets WHERE exerciseName = :exerciseName ORDER BY timestamp ASC")
    fun getSetsByExercise(exerciseName: String): Flow<List<WorkoutSet>>

    @Query("SELECT * FROM workout_sets WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getSetsBetween(startTime: Long, endTime: Long): Flow<List<WorkoutSet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(workoutSet: WorkoutSet): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(workoutSets: List<WorkoutSet>)

    @Delete
    suspend fun deleteSet(workoutSet: WorkoutSet)

    @Query("DELETE FROM workout_sets")
    suspend fun clearAllSets()

    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements")
    suspend fun getAllAchievementsList(): List<Achievement>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)
}
