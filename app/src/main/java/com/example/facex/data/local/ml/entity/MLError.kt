package com.example.facex.data.local.ml.entity

sealed class MLError : Exception() {
    data class ModelLoadError(override val cause: Throwable?) : MLError()
    data class InferenceError(override val cause: Throwable?) : MLError()
    data class DelegateChangeError(override val cause: Throwable?) : MLError()
    data class EmbeddingGenerationError(override val cause: Throwable?) : MLError()
}