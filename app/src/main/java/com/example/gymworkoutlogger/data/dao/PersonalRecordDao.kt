package com.example.gymworkoutlogger.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.gymworkoutlogger.data.entity.PersonalRecord
import com.example.gymworkoutlogger.data.relation.PersonalRecordWithExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(personalRecord: PersonalRecord): Long

    @Delete
    suspend fun delete(personalRecord: PersonalRecord)

    @Query("SELECT * FROM personal_records WHERE exercise_id = :exerciseId")
    suspend fun getPrForExercise(exerciseId: Long): PersonalRecord?

    @Transaction
    @Query("SELECT * FROM personal_records ORDER BY max_weight_kg DESC")
    fun getAllPersonalRecordsWithExercise(): Flow<List<PersonalRecordWithExercise>>
}
