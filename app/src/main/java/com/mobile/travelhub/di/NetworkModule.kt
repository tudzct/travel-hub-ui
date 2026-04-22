package com.mobile.travelhub.di

import com.mobile.travelhub.data.api.ApiConfig
import com.mobile.travelhub.data.api.AuthHeaderInterceptor
import com.mobile.travelhub.data.api.TokenAuthenticator
import com.mobile.travelhub.data.api.TravelHubApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authHeaderInterceptor: AuthHeaderInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authHeaderInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTravelHubApiService(retrofit: Retrofit): TravelHubApiService {
        return retrofit.create(TravelHubApiService::class.java)
    }

    private const val BASE_URL = ApiConfig.BASE_URL
}