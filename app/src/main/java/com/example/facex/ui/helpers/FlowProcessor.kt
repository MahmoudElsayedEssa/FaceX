package com.example.facex.ui.helpers

import com.example.facex.ui.screens.camera_face_recognition.FrameData
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class FlowProcessor {
    private val channel = Channel<Result<FrameData>>(Channel.CONFLATED)
    private var job: Job? = null
    private val isActive = AtomicBoolean(true)

    suspend fun process(onFrame: suspend (Result<FrameData>) -> Unit) {
        var shouldBreak = false
        job = coroutineScope {
            launch {
                while (isActive.get() && !shouldBreak) {
                    channel.receiveCatching().onSuccess { frame ->
                        onFrame(frame)
                    }.onFailure {
                        shouldBreak = true
                    }
                }
            }
        }
    }

    fun send(frame: Result<FrameData>) {
        if (isActive.get()) {
            channel.trySend(frame)
        }
    }

    fun stop() {
        isActive.set(false)
        job?.cancel()
        channel.close()
    }
}