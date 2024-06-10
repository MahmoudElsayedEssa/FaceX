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
    suspend fun insert(person: PersonDTO)

    @Query("SELECT * FROM persons")
    fun getAllPersons(): List<PersonDTO>

    @Delete
    suspend fun deletePerson(person: PersonDTO)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePerson(person: PersonDTO)

}
