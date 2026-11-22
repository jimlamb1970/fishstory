package com.funjim.fishstory.database.mapper

import com.funjim.fishstory.database.entity.EventNoteEntity
import com.funjim.fishstory.database.entity.FishNoteEntity
import com.funjim.fishstory.database.entity.TripNoteEntity
import com.funjim.fishstory.model.EventNote
import com.funjim.fishstory.model.FishNote
import com.funjim.fishstory.model.TripNote

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
