package com.example.facex.data.repository

import com.example.facex.data.local.db.PersonDTO
import com.example.facex.data.local.db.PersonDao
import com.example.facex.data.repository.mappers.toEntity
import com.example.facex.data.toByteArray
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.Person
import com.example.facex.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(private val personDao: PersonDao) :
    PersonRepository {
    override suspend fun getAllPersons(): Flow<List<Person>> {
        return personDao.getAllPersons().toEntity()
    }


    override suspend fun savePerson(name: String, embedding: Embedding) {
        personDao.insert(PersonDTO(name = name, embedding = embedding.toByteArray()))
    }
}
