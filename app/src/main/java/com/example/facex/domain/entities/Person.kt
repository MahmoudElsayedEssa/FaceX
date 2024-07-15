package com.example.facex.domain.entities

import java.nio.ByteBuffer

data class Person(
    val id: Long,
    val name: String,
    val embedding: Embedding
)