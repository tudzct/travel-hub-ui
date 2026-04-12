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
import java.io.EOFException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ItineraryBotRepo @Inject constructor() {

    fun stream(): Flow<StreamEvent> = callbackFlow {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url("http://192.168.1.105:8888/chat")
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
                        "thinking" -> parsePayload(data)?.let { payload ->
                            parseText(
                                payload,
                                "reasoning"
                            )?.let { trySend(StreamEvent.Thinking(it)) }
                        }

                        "message" -> parsePayload(data)?.let { payload ->
                            parseText(payload, "text")?.let { trySend(StreamEvent.Message(it)) }
                        }

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
                    val isNormalEnd = t is EOFException
                    if (isNormalEnd) {
                        trySend(StreamEvent.Done)
                        close()
                        return
                    }

                    val message = t?.message ?: "Unknown stream error"
                    trySend(StreamEvent.Error(message))
                    trySend(StreamEvent.Done)
                    close()
                }
            })

        awaitClose { eventSource.cancel() }
    }

    private fun parsePayload(data: String): JSONObject? {
        return runCatching { JSONObject(data) }.getOrNull()
    }

    private fun parseText(payload: JSONObject, key: String): String? {
        return payload.optString(key).takeIf { it.isNotEmpty() }
    }
}
