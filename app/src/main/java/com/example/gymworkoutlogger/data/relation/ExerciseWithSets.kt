package com.example.gymworkoutlogger.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymworkoutlogger.data.entity.Exercise
import com.example.gymworkoutlogger.data.entity.WorkoutSet

data class ExerciseWithSets(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "exercise_id"
    )
    val sets: List<WorkoutSet>
)
