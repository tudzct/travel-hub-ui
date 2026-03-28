package com.mobile.travelhub.data

import com.mobile.travelhub.models.StreamEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ItineraryBotRepo @Inject constructor() {

    fun stream(): Flow<StreamEvent> = callbackFlow {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url("http://localhost:8000/chat")
            .build()

        val eventSource = EventSources.createFactory(client)
            .newEventSource(request, object : EventSourceListener() {

                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    when (type) {
                        "thinking" -> trySend(StreamEvent.Thinking(parse(data)))
                        "message" -> trySend(StreamEvent.Message(parse(data)))
                        "done" -> {
                            trySend(StreamEvent.Done)
                            close()
                        }
                    }
                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: Response?
                ) {
                    close(t)
                }
            })

        awaitClose { eventSource.cancel() }
    }

    private fun parse(data: String): String {
        return JSONObject(data).optString("text")
            .ifEmpty { JSONObject(data).optString("reasoning") }
    }
}
