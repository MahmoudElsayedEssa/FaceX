package com.example.facex.domain.entities

data class Person(
    val id: Int,
    val name: String,
    val embedding: Embedding,
)
