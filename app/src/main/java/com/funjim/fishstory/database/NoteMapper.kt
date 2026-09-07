package com.funjim.fishstory.database

import com.funjim.fishstory.model.Note

// Extension: Database Entity -> Domain Model
fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        content = content,
        timestamp = timestamp
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<NoteEntity>.toNoteDomainList(): List<Note> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        content = content,
        timestamp = timestamp
    )
}
