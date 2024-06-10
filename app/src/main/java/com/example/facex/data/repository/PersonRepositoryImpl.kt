package com.example.facex.data.repository

import com.example.facex.data.local.db.PersonDTO
import com.example.facex.data.local.db.PersonDao
import com.example.facex.data.toByteArray
import com.example.facex.data.toByteBuffer
import com.example.facex.domain.entities.Person
import com.example.facex.domain.repository.PersonRepository
import java.nio.ByteBuffer
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(private val personDao: PersonDao) :
    PersonRepository {
    override suspend fun getAllPersons(): List<Person> {
        return personDao.getAllPersons().map { entity ->
            Person(
                id = entity.id,
                name = entity.name,
                embedding = entity.embedding.toByteBuffer()
            )
        }
    }

    override suspend fun savePerson(name: String, embedding: ByteBuffer) {
        val personEntity = PersonDTO(
            name = name,
            embedding = embedding.toByteArray()
        )
        personDao.insert(personEntity)
    }
}
