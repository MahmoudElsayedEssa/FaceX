package com.example.facex.domain.entities

data class PerformanceMetrics(
    val metrics: Map<String, String> = emptyMap()
)