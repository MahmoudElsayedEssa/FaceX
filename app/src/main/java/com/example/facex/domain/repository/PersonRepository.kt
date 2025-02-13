package com.example.facex.domain.repository

import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.Person
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun getAllPersons(): Flow<List<Person>>

    suspend fun registerPerson(name: String, embedding: Embedding): Result<Unit>
}
