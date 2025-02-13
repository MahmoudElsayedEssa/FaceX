package com.example.facex.data.repository

import com.example.facex.data.local.db.PersonDTO
import com.example.facex.data.local.db.PersonDao
import com.example.facex.data.repository.mappers.toEntity
import com.example.facex.data.toByteArray
import com.example.facex.di.IoDispatcher
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.Person
import com.example.facex.domain.repository.PersonRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(
    private val personDao: PersonDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : PersonRepository {
    override fun getAllPersons(): Flow<List<Person>> =
        personDao.getAllPersons().map { dtoList -> dtoList.map { it.toEntity() } }
            .flowOn(dispatcher)

    override suspend fun registerPerson(
        name: String, embedding: Embedding
    ): Result<Unit> = withContext(dispatcher) {
        runCatching {
            require(name.isNotBlank()) { "Person name cannot be blank" }
            val dto = PersonDTO(name = name, embedding = embedding.data.toByteArray())
            personDao.insert(dto)
        }
    }
}
