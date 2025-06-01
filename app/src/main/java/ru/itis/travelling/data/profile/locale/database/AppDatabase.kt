package ru.itis.travelling.data.profile.locale.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.itis.travelling.data.profile.locale.database.dao.ParticipantDao
import ru.itis.travelling.data.profile.locale.database.entities.ParticipantEntity

@Database(
    entities = [ParticipantEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun participantDao(): ParticipantDao

    companion object {
        const val DB_LOG_KEY = "AppDatabase"
    }
}
