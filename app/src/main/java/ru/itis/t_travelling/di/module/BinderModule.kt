package ru.itis.t_travelling.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.t_travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.t_travelling.data.authregister.local.repository.UserPreferencesRepositoryImpl
import ru.itis.t_travelling.data.authregister.local.repository.UserRepositoryImpl
import ru.itis.t_travelling.data.trips.TripRepositoryImpl
import ru.itis.t_travelling.domain.authregister.repository.UserRepository
import ru.itis.t_travelling.domain.trips.repository.TripRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BinderModule {

    @Binds
    @Singleton
    fun bindUserPrefRepositoryToImpl(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    @Singleton
    fun bindUserRepositoryToImpl(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    fun bindTripRepositoryToImpl(impl: TripRepositoryImpl): TripRepository
}