package com.example.facex.data.repository.mappers

import com.example.facex.data.local.db.PersonDTO
import com.example.facex.domain.entities.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun Flow<List<PersonDTO>>.toEntity(): Flow<List<Person>> {
    return this.map { dtoList -> dtoList.map { it.toEntity() } }
}

fun PersonDTO.toEntity(): Person {
    return Person(
        id = this.id,
        name = this.name,
        embedding = this.embedding
    )
}


