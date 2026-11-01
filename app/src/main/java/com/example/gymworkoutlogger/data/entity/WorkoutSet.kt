package com.example.gymworkoutlogger.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSession::class,
            parentColumns = ["id"],
            childColumns = ["workout_session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workout_session_id"]),
        Index(value = ["exercise_id"])
    ]
)
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "workout_session_id")
    val workoutSessionId: Long,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,
    @ColumnInfo(name = "set_number")
    val setNumber: Int,
    @ColumnInfo(name = "weight_kg")
    val weightKg: Double,
    val reps: Int,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
