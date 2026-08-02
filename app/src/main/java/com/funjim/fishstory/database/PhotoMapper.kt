package com.funjim.fishstory.database

import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.Photo

// Extension: Database Entity -> Domain Model
fun PhotoEntity.toDomain(): Photo {
    return Photo(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<PhotoEntity>.toPhotoDomainList(): List<Photo> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun Photo.toEntity(): PhotoEntity {
    return PhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption
    )
}

