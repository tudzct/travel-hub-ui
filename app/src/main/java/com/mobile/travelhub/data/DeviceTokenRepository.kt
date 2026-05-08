package com.mobile.travelhub.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.mobile.travelhub.data.api.DeviceApiService
import com.mobile.travelhub.data.model.DeviceTokenRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class DeviceTokenRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val deviceApiService: DeviceApiService
) {
    suspend fun registerCurrentDeviceToken(): Result<Unit> = withContext(Dispatchers.IO) {
        val token = runCatching {
            Tasks.await(FirebaseMessaging.getInstance().token)
                ?.trim()
                .orEmpty()
        }.getOrElse { throwable ->
            return@withContext Result.failure(throwable)
        }

        registerDeviceToken(token)
    }

    suspend fun registerDeviceToken(token: String): Result<Unit> {
        if (authRepository.getAccessToken().isNullOrBlank()) {
            return Result.success(Unit)
        }

        return runCatching {
            val normalizedToken = token.trim()
            if (normalizedToken.isEmpty()) {
                return@runCatching
            }

            val response = deviceApiService.registerDeviceToken(
                DeviceTokenRequest(token = normalizedToken)
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw IOException("Device token request failed (${response.code()}): $errorBody")
            }
        }
    }
}
