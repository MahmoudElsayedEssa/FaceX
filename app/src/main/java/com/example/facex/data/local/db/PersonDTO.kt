package com.example.facex.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.facex.domain.entities.Embedding
import java.nio.ByteBuffer

@Entity(tableName = "persons")
data class PersonDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var name: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val embedding: ByteArray

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersonDTO

        if (id != other.id) return false
        if (name != other.name) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}