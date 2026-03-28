package com.mobile.travelhub.usecase

import com.mobile.travelhub.data.ItineraryBotRepo
import com.mobile.travelhub.models.StreamEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject
class ItineraryBotUseCase @Inject constructor(
    private val repo: ItineraryBotRepo
) {
    fun execute(): Flow<StreamEvent> {
        return repo.stream()
            .buffer()
            .conflate() // drop intermediate nếu UI chậm
    }
}
