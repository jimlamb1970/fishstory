package com.funjim.fishstory.database.mapper

import com.funjim.fishstory.database.entity.EventNoteEntity
import com.funjim.fishstory.database.entity.FishNoteEntity
import com.funjim.fishstory.database.entity.NoteEntity
import com.funjim.fishstory.database.entity.NoteEventEntity
import com.funjim.fishstory.database.entity.NoteFishEntity
import com.funjim.fishstory.database.entity.NoteTripEntity
import com.funjim.fishstory.database.entity.TripNoteEntity
import com.funjim.fishstory.model.EventNote
import com.funjim.fishstory.model.FishNote
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.NoteEvent
import com.funjim.fishstory.model.NoteFish
import com.funjim.fishstory.model.NoteTrip
import com.funjim.fishstory.model.TripNote

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

fun EventNote.toEntity(): EventNoteEntity {
    return EventNoteEntity(
        id = id,
        content = content,
        timestamp = timestamp,
        parentId = parentId
    )
}
fun FishNote.toEntity(): FishNoteEntity {
    return FishNoteEntity(
        id = id,
        content = content,
        timestamp = timestamp,
        parentId = parentId
    )
}
fun TripNote.toEntity(): TripNoteEntity {
    return TripNoteEntity(
        id = id,
        content = content,
        timestamp = timestamp,
        parentId = parentId
    )
}

fun EventNoteEntity.toDomain(): EventNote {
    return EventNote(
        id = id,
        content = content,
        timestamp = timestamp,
        parentId = parentId
    )
}
fun FishNoteEntity.toDomain(): FishNote {
    return FishNote(
        id = id,
        content = content,
        timestamp = timestamp,
        parentId = parentId
    )
}
fun TripNoteEntity.toDomain(): TripNote {
    return TripNote(
        id = id,
        content = content,
        timestamp = timestamp,
        parentId = parentId
    )
}

fun List<EventNoteEntity>.toEventNoteDomainList(): List<EventNote> {
    return map { it.toDomain() }
}
fun List<FishNoteEntity>.toFishNoteDomainList(): List<FishNote> {
    return map { it.toDomain() }
}
fun List<TripNoteEntity>.toTripNoteDomainList(): List<TripNote> {
    return map { it.toDomain() }
}
