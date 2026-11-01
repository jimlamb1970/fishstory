package com.funjim.fishstory.database.mapper

import com.funjim.fishstory.database.entity.NoteEntity
import com.funjim.fishstory.database.entity.NoteEventEntity
import com.funjim.fishstory.database.entity.NoteFishEntity
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.NoteEvent
import com.funjim.fishstory.model.NoteFish

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        content = content,
        timestamp = timestamp
    )
}
fun List<NoteEntity>.toNoteDomainList(): List<Note> {
    return map { it.toDomain() }
}
fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        content = content,
        timestamp = timestamp
    )
}

fun NoteEventEntity.toDomain(): NoteEvent {
    return NoteEvent(
        eventId = eventId,
        noteId = noteId
    )
}
fun List<NoteEventEntity>.toEventNoteDomainList(): List<NoteEvent> {
    return map { it.toDomain() }
}
fun NoteEvent.toEntity(): NoteEventEntity {
    return NoteEventEntity(
        eventId = eventId,
        noteId = noteId
    )
}

fun NoteFishEntity.toDomain(): NoteFish {
    return NoteFish(
        fishId = fishId,
        noteId = noteId
    )
}
fun List<NoteFishEntity>.toFishNoteDomainList(): List<NoteFish> {
    return map { it.toDomain() }
}
fun NoteFish.toEntity(): NoteFishEntity {
    return NoteFishEntity(
        fishId = fishId,
        noteId = noteId
    )
}