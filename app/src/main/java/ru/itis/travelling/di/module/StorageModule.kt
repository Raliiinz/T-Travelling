//package ru.itis.travelling.di.module
//
//import dagger.Binds
//import dagger.Module
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import ru.itis.travelling.data.authregister.local.storage.SecureTokenStorage
//import ru.itis.travelling.data.authregister.local.storage.TokenStorage
//
//@Module
//@InstallIn(SingletonComponent::class)
//abstract class StorageModule {
//
//    @Binds
//    abstract fun bindTokenStorage(
//        secureTokenStorage: SecureTokenStorage
//    ): TokenStorage
//}