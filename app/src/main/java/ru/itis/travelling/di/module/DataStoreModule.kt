package ru.itis.travelling.di.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.itis.travelling.data.authregister.local.datasource.UserPreferencesDataSource
import ru.itis.travelling.data.profile.locale.database.AppDatabase
import ru.itis.travelling.data.profile.locale.database.dao.ParticipantDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    private val databaseName = "app_database"

    @Provides
    @Singleton
    fun provideUserPreferencesDataSource(
        @ApplicationContext context: Context
    ): UserPreferencesDataSource {
        return UserPreferencesDataSource(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            databaseName
        ).build()
    }

    @Provides
    fun provideParticipantDao(appDatabase: AppDatabase): ParticipantDao {
        return appDatabase.participantDao()
    }
}
