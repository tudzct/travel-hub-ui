package com.mobile.travelhub.di

import com.mobile.travelhub.BuildConfig
import com.mobile.travelhub.data.api.ApiConfig
import com.mobile.travelhub.data.api.AiRecommendationApiService
import com.mobile.travelhub.data.api.AuthApiService
import com.mobile.travelhub.data.api.AuthHeaderInterceptor
import com.mobile.travelhub.data.api.DeviceApiService
import com.mobile.travelhub.data.api.ItineraryApiService
import com.mobile.travelhub.data.api.FileUploadApiService
import com.mobile.travelhub.data.api.ItineraryApiService
import com.mobile.travelhub.data.api.LocationApiService
import com.mobile.travelhub.data.api.PlaceApiService
import com.mobile.travelhub.data.api.PostApiService
import com.mobile.travelhub.data.api.RetrofitFactory
import com.mobile.travelhub.data.api.TripApiService
import com.mobile.travelhub.data.api.TokenAuthenticator
import com.mobile.travelhub.data.api.UploadApiService
import com.mobile.travelhub.data.api.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.logging.HttpLoggingInterceptor
import kotlin.time.Duration.Companion.seconds
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = ApiConfig.BASE_URL
    private const val AI_BASE_URL = ApiConfig.AI_BASE_URL
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authHeaderInterceptor: AuthHeaderInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {

        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(authHeaderInterceptor)
            .addInterceptor(logging)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    @Named("authenticated")
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return RetrofitFactory.create(
            baseUrl = BASE_URL,
            client = okHttpClient
        )
    }

    @Provides
    @Singleton
    @Named("public")
    fun providePublicRetrofit(): Retrofit {
        return RetrofitFactory.create(baseUrl = BASE_URL)
    }

    @Provides
    @Singleton
    @Named("ai-public")
    fun provideAiPublicRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return RetrofitFactory.create(baseUrl = AI_BASE_URL, client = okHttpClient)
    }

    @Provides
    @Singleton
    fun provideAuthApiService(@Named("public") publicRetrofit: Retrofit): AuthApiService {
        return publicRetrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLocationApiService(@Named("authenticated") retrofit: Retrofit): LocationApiService {
        return retrofit.create(LocationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(@Named("authenticated") retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDeviceApiService(@Named("authenticated") retrofit: Retrofit): DeviceApiService {
        return retrofit.create(DeviceApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePostApiService(@Named("authenticated") retrofit: Retrofit): PostApiService {
        return retrofit.create(PostApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUploadApiService(@Named("authenticated") retrofit: Retrofit): UploadApiService {
        return retrofit.create(UploadApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFileUploadApiService(@Named("public") publicRetrofit: Retrofit): FileUploadApiService {
        return publicRetrofit.create(FileUploadApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlaceApiService(@Named("authenticated") retrofit: Retrofit): PlaceApiService {
        return retrofit.create(PlaceApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideItineraryApiService(@Named("authenticated") retrofit: Retrofit): ItineraryApiService {
        return retrofit.create(ItineraryApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAiRecommendationApiService(@Named("ai-public") retrofit: Retrofit): AiRecommendationApiService {
        return retrofit.create(AiRecommendationApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTripApiService(@Named("authenticated") retrofit: Retrofit): TripApiService {
        return retrofit.create(TripApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideItineraryApiService(@Named("authenticated") retrofit: Retrofit): ItineraryApiService {
        return retrofit.create(ItineraryApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        val supabaseUrl = BuildConfig.SUPABASE_URL
        val publishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY

        require(supabaseUrl.isNotBlank()) { "SUPABASE_URL is not configured" }
        require(publishableKey.isNotBlank()) { "SUPABASE_PUBLISHABLE_KEY is not configured" }

        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = publishableKey
        ) {
            install(Storage) {
                transferTimeout = 90.seconds
            }
        }
    }

}
