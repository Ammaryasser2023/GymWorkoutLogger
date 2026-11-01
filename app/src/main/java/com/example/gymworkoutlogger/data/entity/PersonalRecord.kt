package com.example.gymworkoutlogger.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["exercise_id"], unique = true)
    ]
)
data class PersonalRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: Long,
    @ColumnInfo(name = "max_weight_kg")
    val maxWeightKg: Double,
    @ColumnInfo(name = "max_reps")
    val maxReps: Int,
    @ColumnInfo(name = "achieved_timestamp")
    val achievedTimestamp: Long
)
