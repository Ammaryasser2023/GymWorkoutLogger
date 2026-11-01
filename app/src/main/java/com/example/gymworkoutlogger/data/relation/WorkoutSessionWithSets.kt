package com.example.gymworkoutlogger.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymworkoutlogger.data.entity.WorkoutSession
import com.example.gymworkoutlogger.data.entity.WorkoutSet

data class WorkoutSessionWithSets(
    @Embedded val session: WorkoutSession,
    @Relation(
        parentColumn = "id",
        entityColumn = "workout_session_id"
    )
    val sets: List<WorkoutSet>
)
