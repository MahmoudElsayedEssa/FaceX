package com.example.facex.domain.repository

import com.example.facex.domain.entities.Person
import java.nio.ByteBuffer

interface  PersonRepository {
    suspend fun getAllPersons(): List<Person>
    suspend fun savePerson(name: String, embedding: ByteBuffer)
}
