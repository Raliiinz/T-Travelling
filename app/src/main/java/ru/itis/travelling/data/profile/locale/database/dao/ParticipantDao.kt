package ru.itis.travelling.data.profile.locale.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.itis.travelling.data.profile.locale.database.entities.ParticipantEntity

@Dao
interface ParticipantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(participant: ParticipantEntity)

    @Query("SELECT * FROM participants LIMIT 1")
    suspend fun getParticipant(): ParticipantEntity?

    @Query("DELETE FROM participants")
    suspend fun clear()
}