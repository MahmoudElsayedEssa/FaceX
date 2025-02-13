package com.example.facex.data.repository.mappers

import android.graphics.Rect
import com.example.facex.data.local.db.PersonDTO
import com.example.facex.data.toFloatArray
import com.example.facex.domain.entities.Embedding
import com.example.facex.domain.entities.Person
import com.example.facex.domain.entities.Rectangle


fun PersonDTO.toEntity(): Person =
    Person(
        id = this.id,
        name = this.name.trim(),
        embedding = Embedding(embedding.toFloatArray())
    )


fun Rect.toEntity(): Rectangle =
    Rectangle(
        x = left,
        y = top,
        width = right - left,
        height = bottom - top,
    )
