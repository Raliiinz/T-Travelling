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
import ru.itis.travelling.data.authregister.remote.ImprovedTokenAuthenticator
import ru.itis.travelling.data.authregister.remote.api.AuthApi
import ru.itis.travelling.data.authregister.remote.api.RegisterApi
import ru.itis.travelling.data.authregister.remote.interceptor.BearerTokenInterceptor
import ru.itis.travelling.data.authregister.remote.interceptor.UnlockingInterceptor
import ru.itis.travelling.data.authregister.remote.interceptor.UuidInterceptor
import ru.itis.travelling.data.trips.remote.api.TripApi
import ru.itis.travelling.domain.authregister.repository.UserRepository
import ru.itis.travelling.domain.authregister.storage.TokenStorage
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Provides
    @Singleton
    fun providePublicOkHttpClient(
        uuidInterceptor: UuidInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(uuidInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @Named("authOkHttpClient")
    fun provideAuthOkHttpClient(
        tokenAuthenticator: ImprovedTokenAuthenticator,
        unlockingInterceptor: UnlockingInterceptor,
        uuidInterceptor: UuidInterceptor,
        bearerTokenInterceptor: BearerTokenInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(uuidInterceptor)
            .addInterceptor(bearerTokenInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(unlockingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun providePublicRetrofit(
        publicOkHttpClient: OkHttpClient,
        converterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_URL)
            .client(publicOkHttpClient)
            .addConverterFactory(converterFactory)
            .build()
    }

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

    @Provides
    @Singleton
    fun provideBearerTokenInterceptor(tokenStorage: TokenStorage): BearerTokenInterceptor {
        return BearerTokenInterceptor(tokenStorage)
    }

    @Provides
    @Singleton
    fun provideTripApi(@Named("authRetrofit") retrofit: Retrofit): TripApi {
        return retrofit.create(TripApi::class.java)
    }
}