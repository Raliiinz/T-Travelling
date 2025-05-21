package ru.itis.travelling.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.itis.travelling.data.authregister.handler.LogoutHandlerImpl
import ru.itis.travelling.domain.authregister.handler.LogoutHandler

//@Module
//@InstallIn(SingletonComponent::class)
//abstract class AuthHandlersModule {
//
//    @Binds
//    abstract fun bindLogoutHandler(
//        impl: LogoutHandlerImpl
//    ): LogoutHandler
//}
