package ru.itis.travelling.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.itis.travelling.BuildConfig.API_URL
import ru.itis.travelling.data.authregister.remote.ImprovedTokenAuthenticator
import ru.itis.travelling.data.authregister.remote.api.AuthApi
import ru.itis.travelling.data.authregister.remote.api.RegisterApi
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
    @Named("publicOkHttpClient")
    fun providePublicOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(UuidInterceptor())
            .build()
    }

    @Provides
    @Singleton
    @Named("authOkHttpClient")
    fun provideAuthOkHttpClient(
        tokenAuthenticator: ImprovedTokenAuthenticator,
        unlockingInterceptor: UnlockingInterceptor,
        tokenStorage: TokenStorage
    ): OkHttpClient {
        return providePublicOkHttpClient().newBuilder()
            .authenticator(tokenAuthenticator)
            .addInterceptor(unlockingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()

                runBlocking {
                    val accessToken = tokenStorage.getAccessToken()
                    if (!accessToken.isNullOrBlank()) {
                        builder.header("Authorization", "Bearer $accessToken")
                    }
                }

                chain.proceed(builder.build())
            }
            .build()
    }

    @Provides
    @Singleton
    @Named("refreshOkHttpClient")
    fun provideRefreshOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(UuidInterceptor()) // можно добавить uuid, но никаких auth
            .build()
    }

    @Provides
    @Singleton
    @Named("refreshRetrofit")
    fun provideRefreshRetrofit(
        @Named("refreshOkHttpClient") okHttpClient: OkHttpClient,
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
    @Named("refreshAuthApi")
    fun provideRefreshAuthApi(@Named("refreshRetrofit") retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

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

//    @Provides
//    @Singleton
//    fun provideAuthApi(retrofit: Retrofit): AuthApi {
//        return retrofit.create(AuthApi::class.java)
//    }


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
    fun provideTripApi(@Named("authRetrofit") retrofit: Retrofit): TripApi {
        return retrofit.create(TripApi::class.java)
    }

}