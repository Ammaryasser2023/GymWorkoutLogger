package com.example.gymworkoutlogger.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gymworkoutlogger.data.entity.Exercise
import com.example.gymworkoutlogger.data.entity.PersonalRecord

data class PersonalRecordWithExercise(
    @Embedded val personalRecord: PersonalRecord,
    @Relation(
        parentColumn = "exercise_id",
        entityColumn = "id"
    )
    val exercise: Exercise
)
