package ru.itis.travelling.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.travelling.domain.authregister.repository.UserPreferencesRepository
import ru.itis.travelling.data.authregister.local.repository.UserPreferencesRepositoryImpl
import ru.itis.travelling.data.authregister.local.repository.UserRepositoryImpl
import ru.itis.travelling.data.authregister.local.storage.TokenStorageImpl
import ru.itis.travelling.data.base.repository.LocaleRepositoryImpl
import ru.itis.travelling.data.contacts.ContactsRepositoryImpl
import ru.itis.travelling.data.profile.repository.ProfileRepositoryImpl
import ru.itis.travelling.data.transactions.repository.TransactionRepositoryImpl
import ru.itis.travelling.data.trips.repository.TripRepositoryImpl
import ru.itis.travelling.domain.authregister.repository.UserRepository
import ru.itis.travelling.domain.authregister.storage.TokenStorage
import ru.itis.travelling.domain.base.repository.LocaleRepository
import ru.itis.travelling.domain.contacts.repository.ContactsRepository
import ru.itis.travelling.domain.profile.repository.ProfileRepository
import ru.itis.travelling.domain.transactions.repository.TransactionRepository
import ru.itis.travelling.domain.trips.repository.TripRepository
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

    @Binds
    @Singleton
    fun bindContactsRepositoryToImpl(impl: ContactsRepositoryImpl): ContactsRepository

    @Binds
    @Singleton
    fun bindTokenStorageToImpl(impl: TokenStorageImpl): TokenStorage

    @Binds
    @Singleton
    fun bindLocaleRepositoryToImpl(impl: LocaleRepositoryImpl): LocaleRepository

    @Binds
    @Singleton
    fun bindProfileRepositoryToImpl(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    fun bindTransactionsRepositoryToImpl(impl: TransactionRepositoryImpl): TransactionRepository

}