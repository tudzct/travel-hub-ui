package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.model.PreferenceUpdateRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val userApiService: UserApiService
) {
    suspend fun syncPreferencesToServer(
        tripType: String?,
        interests: List<String>,
        destination: String?
    ): Result<Unit> {
        val normalizedTripType = tripType?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedInterests = interests
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
        val normalizedDestination = destination?.trim()?.takeIf { it.isNotEmpty() }

        val session = authRepository.getSavedSession()
            ?: return Result.failure(IllegalStateException("Cannot sync preferences before login"))

        return runCatching {
            userApiService.updatePreferences(
                id = session.userId.toLong(),
                request = PreferenceUpdateRequest(
                    tripType = normalizedTripType,
                    interests = normalizedInterests,
                    destination = normalizedDestination,
                    isOnboarded = true
                )
            )
        }.onSuccess {
            authRepository.updateOnboardingStatus(isOnboarded = true)
        }.map { Unit }
    }
}

