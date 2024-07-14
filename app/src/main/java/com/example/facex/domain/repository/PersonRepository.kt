package com.example.facex.domain.repository

import com.example.facex.domain.entities.Person
import kotlinx.coroutines.flow.Flow
import java.nio.ByteBuffer

interface PersonRepository {
    suspend fun getAllPersons(): Flow<List<Person>>
    suspend fun savePerson(name: String, embedding: ByteBuffer)
}
