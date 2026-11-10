package com.funjim.fishstory.database.mapper

import com.funjim.fishstory.database.entity.NoteEntity
import com.funjim.fishstory.database.entity.NoteEventEntity
import com.funjim.fishstory.database.entity.NoteFishEntity
import com.funjim.fishstory.database.entity.NoteTripEntity
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.NoteEvent
import com.funjim.fishstory.model.NoteFish
import com.funjim.fishstory.model.NoteTrip

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
fun List<NoteEventEntity>.toNoteEventDomainList(): List<NoteEvent> {
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
fun List<NoteFishEntity>.toNoteFishDomainList(): List<NoteFish> {
    return map { it.toDomain() }
}
fun NoteFish.toEntity(): NoteFishEntity {
    return NoteFishEntity(
        fishId = fishId,
        noteId = noteId
    )
}

fun NoteTripEntity.toDomain(): NoteTrip {
    return NoteTrip(
        tripId = tripId,
        noteId = noteId
    )
}
fun List<NoteTripEntity>.toNoteTripDomainList(): List<NoteTrip> {
    return map { it.toDomain() }
}
fun NoteTrip.toEntity(): NoteTripEntity {
    return NoteTripEntity(
        tripId = tripId,
        noteId = noteId
    )
}
