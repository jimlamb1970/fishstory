package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(), // GUID string primary key
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class NoteTrip(
    val tripId: String,
    val noteId: String
)

@Serializable
data class NoteEvent(
    val eventId: String,
    val noteId: String
)

@Serializable
data class NoteFish(
    val fishId: String,
    val noteId: String
)

interface BaseNote {
    val id: String
    val content: String
    val timestamp: Long
    val parentId: String
}

@Serializable
data class EventNote(
    override val id: String = UUID.randomUUID().toString(),
    override val content: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val parentId: String
) : BaseNote

@Serializable
data class FishNote(
    override val id: String = UUID.randomUUID().toString(),
    override val content: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val parentId: String
) : BaseNote

@Serializable
data class TripNote(
    override val id: String = UUID.randomUUID().toString(),
    override val content: String,
    override val timestamp: Long = System.currentTimeMillis(),
    override val parentId: String
) : BaseNote

