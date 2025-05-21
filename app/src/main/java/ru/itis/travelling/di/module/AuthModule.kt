//package ru.itis.travelling.di.module
//
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import ru.itis.travelling.data.authregister.handler.LogoutHandlerImpl
//import ru.itis.travelling.di.qualifies.LogoutStatus
//import ru.itis.travelling.domain.authregister.handler.LogoutHandler
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object AuthModule {
//
////    @Provides
////    @Singleton
////    fun provideLogoutStatus(): Boolean = false // Динамическое значение
//
//    @Provides
//    @LogoutStatus
//    fun provideLogoutStatus(): Boolean = false
//
//    @Provides
//    @Singleton
//    fun provideLogoutHandler(impl: LogoutHandlerImpl): LogoutHandler = impl
//}