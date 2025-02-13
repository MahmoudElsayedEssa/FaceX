package com.example.facex.domain.logger

interface Logger {
    var tag: String

    fun log(message: String, level: LogLevel)

    fun logError(message: String, error: Throwable?)
}

enum class LogLevel {
    DEBUG, INFO, WARNING, ERROR,
}

fun <T> T.logInfo(
    logger: Logger,
    message: String,
): T = logWithLevel(logger, LogLevel.INFO, message)


fun <T> T.logInfo(
    logger: Logger,
    message: (T) -> String,
): T = logWithLevel(logger, LogLevel.INFO, message(this))

fun Logger.withTag(tag:String){
    this.tag = tag
}

fun String.logInfo(logger: Logger): String = logWithLevel(logger, LogLevel.INFO, this)

fun <T> T.logDebug(
    logger: Logger,
    message: String,
): T = logWithLevel(logger, LogLevel.DEBUG, message)

fun <T> T.logDebug(
    logger: Logger,
    message: (T) -> String,
): T = logWithLevel(logger, LogLevel.DEBUG, message(this))

fun String.logDebug(logger: Logger): String = logWithLevel(logger, LogLevel.DEBUG, this)

fun <T> T.logWarning(
    logger: Logger,
    message: String,
): T = logWithLevel(logger, LogLevel.WARNING, message)

fun <T> T.logWarning(
    logger: Logger,
    message: (T) -> String,
): T = logWithLevel(logger, LogLevel.WARNING, message(this))

fun String.logWarning(logger: Logger): String = logWithLevel(logger, LogLevel.WARNING, this)

private fun <T> T.logWithLevel(
    logger: Logger,
    level: LogLevel,
    message: String,
): T = apply { logger.log(message, level) }