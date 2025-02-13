package com.example.facex.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class PersonDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val embedding: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PersonDTO) return false
        return id == other.id &&
                name == other.name &&
                embedding.contentEquals(other.embedding)
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }

}