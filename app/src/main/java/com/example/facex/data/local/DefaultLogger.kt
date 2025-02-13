package com.example.facex.data.local

import android.util.Log
import com.example.facex.domain.logger.LogLevel
import com.example.facex.domain.logger.Logger

class DefaultLogger(
    override var tag: String = "DefaultTag",
    private val enabledLogLevels: Set<LogLevel> = LogLevel.entries.toSet(),
    private val messageFormatter: ((String, LogLevel) -> String)? = null,
) : Logger {
    @Synchronized
    override fun log(
        message: String,
        level: LogLevel,
    ) {
        if (level !in enabledLogLevels) return
        val formattedMessage = messageFormatter?.invoke(message, level) ?: message
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, formattedMessage)
            LogLevel.INFO -> Log.i(tag, formattedMessage)
            LogLevel.WARNING -> Log.w(tag, formattedMessage)
            LogLevel.ERROR -> Log.e(tag, formattedMessage)
        }
    }


    @Synchronized
    override fun logError(message: String, error: Throwable?) {
        if (LogLevel.ERROR !in enabledLogLevels) return
        val formattedMessage = messageFormatter?.invoke(message, LogLevel.ERROR) ?: message
        Log.e(tag, formattedMessage, error)
    }
}
