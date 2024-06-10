package com.example.facex.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.nio.ByteBuffer

@Entity(tableName = "persons")
data class PersonDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String,
    val embedding: ByteArray
)