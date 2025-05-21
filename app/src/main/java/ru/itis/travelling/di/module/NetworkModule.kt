package ru.itis.travelling.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.sync.Mutex
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.itis.travelling.BuildConfig.API_URL
import ru.itis.travelling.data.authregister.local.repository.UserRepositoryImpl
import ru.itis.travelling.data.authregister.local.storage.TokenStorage
import ru.itis.travelling.data.authregister.remote.ImprovedTokenAuthenticator
import ru.itis.travelling.data.authregister.remote.api.AuthApi
import ru.itis.travelling.data.authregister.remote.api.RegisterApi
import ru.itis.travelling.data.authregister.remote.interceptor.UnlockingInterceptor
import ru.itis.travelling.data.authregister.remote.interceptor.UuidInterceptor
import ru.itis.travelling.domain.authregister.repository.UserRepository
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

//    @Provides
//    fun provideOkHttpClient(): OkHttpClient {
//        return OkHttpClient.Builder()
////            .addInterceptor(AppIdInterceptor())
//            .build()
//    }
    // Базовый клиент без аутентификации (для регистрации и публичных запросов)
    @Provides
    @Singleton
    @Named("publicOkHttpClient")
    fun providePublicOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(UuidInterceptor()) // Добавляем UUID для всех запросов
            .build()
    }

    @Provides
    @Singleton
    @Named("authOkHttpClient")
    fun provideAuthOkHttpClient(
        uuidInterceptor: UuidInterceptor,
        tokenAuthenticator: ImprovedTokenAuthenticator,
        unlockingInterceptor: UnlockingInterceptor
    ): OkHttpClient {
        return providePublicOkHttpClient().newBuilder()
            .addInterceptor(uuidInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(unlockingInterceptor)
            .build()
    }

//    @Provides
//    @Singleton
//    fun provideRetrofit(
//        okHttpClient: OkHttpClient,
//        converterFactory: GsonConverterFactory
//    ): Retrofit {
//        return Retrofit.Builder()
//            .baseUrl(API_URL)
//            .client(okHttpClient)
//            .addConverterFactory(converterFactory)
//            .build()
//    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @Named("publicOkHttpClient") okHttpClient: OkHttpClient,
        converterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_URL)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
    }

    // Отдельный Retrofit для аутентифицированных запросов
    @Provides
    @Singleton
    @Named("authRetrofit")
    fun provideAuthRetrofit(
        @Named("authOkHttpClient") okHttpClient: OkHttpClient,
        converterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_URL)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
    }

    @Provides
    @Singleton
    fun provideRegisterApi(retrofit: Retrofit): RegisterApi {
        return retrofit.create(RegisterApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }


    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        mutex: Mutex,
        tokenStorage: TokenStorage,
        userRepository: UserRepository
    ): ImprovedTokenAuthenticator {
        return ImprovedTokenAuthenticator(
            mutex = mutex,
            tokenStorage = tokenStorage,
            userRepository = userRepository
        )
    }

    @Provides
    @Singleton
    fun provideMutex(): Mutex = Mutex()

    @Provides
    @Singleton
    fun provideUuidInterceptor(): UuidInterceptor = UuidInterceptor()

    @Provides
    @Singleton
    fun provideUnlockingInterceptor(mutex: Mutex): UnlockingInterceptor {
        return UnlockingInterceptor(mutex)
    }

//    @Provides
//    @Singleton
//    fun provideSecureApi(@Named("authRetrofit") retrofit: Retrofit): SecureApi {
//        return retrofit.create(SecureApi::class.java)
//    }


//    @Provides
//    @Singleton
//    fun provideMutex(): Mutex = Mutex()
//
//    @Provides
//    @Singleton
//    fun provideUuidInterceptor(): Interceptor = UuidInterceptor()
//
//    @Provides
//    @Singleton
//    fun provideUnlockingInterceptor(mutex: Mutex): Interceptor = UnlockingInterceptor(mutex)


//    @Provides
//    @Singleton
//    fun provideTokenAuthenticator(
//        mutex: Mutex,
//        tokenStorage: TokenStorage,
//        @ApplicationContext context: Context
//    ): Authenticator = TokenAuthenticator(
//        mutex = mutex,
//        isLogoutStarted = { false }, // Замените на реальную проверку
//        startLogout = { /* Реализуйте логаут */ },
//        tokenStorage = tokenStorage
//    )


//    @Provides
//    @Singleton
//    fun provideTokenAuthenticator(
//        mutex: Mutex,
//        @LogoutStatus isLogoutStarted: Boolean,
//        logoutHandler: LogoutHandler,
//        tokenStorage: TokenStorage,
//        authApiService: AuthApi
//    ): Authenticator {
//        return TokenAuthenticator(
//            mutex = mutex,
//            isLogoutStarted = isLogoutStarted,
//            logoutHandler = logoutHandler,
//            tokenStorage = tokenStorage,
//            authApiService = authApiService
//        )
//    }
//
//    @Provides
//    @Singleton
//    fun provideOkHttpClient(
//        uuidInterceptor: Interceptor,
//        unlockingInterceptor: Interceptor,
//        authenticator: Authenticator
//    ): OkHttpClient {
//        return OkHttpClient.Builder()
//            .addInterceptor(uuidInterceptor)
//            .addInterceptor(unlockingInterceptor)
//            .authenticator(authenticator)
//            .build()
//    }
//
//    @Provides
//    @Singleton
//    fun provideAuthApiService(client: OkHttpClient): AuthApi {
//        return Retrofit.Builder()
//            .baseUrl("http://141.105.71.181:8080/")
//            .client(client)
////            .addConverterFactory(MoshiConverterFactory.create())
//            .build()
//            .create(AuthApi::class.java)
//    }
}