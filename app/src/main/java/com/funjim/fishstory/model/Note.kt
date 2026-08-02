package com.funjim.fishstory.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(), // GUID string primary key
    val text: String,
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
