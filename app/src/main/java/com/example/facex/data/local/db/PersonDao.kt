package com.example.facex.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(personDTO: PersonDTO)

    @Query("SELECT * FROM persons")
    fun getAllPersons(): Flow<List<PersonDTO>>


    @Query("SELECT * FROM persons WHERE name = :name")
    fun getPersonByName(name: String): PersonDTO

    @Delete
    suspend fun deletePerson(personDTO: PersonDTO)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePerson(personDTO: PersonDTO)
}
