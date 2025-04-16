package ru.itis.t_travelling.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.t_travelling.domain.repository.UserPreferencesRepository
import ru.itis.t_travelling.data.repository.UserPreferencesRepositoryImpl
import ru.itis.t_travelling.data.repository.UserRepositoryImpl
import ru.itis.t_travelling.domain.repository.UserRepository
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
}