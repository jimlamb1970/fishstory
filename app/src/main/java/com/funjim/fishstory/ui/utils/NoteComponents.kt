package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Note

@Composable
fun NotesIconButton(
    noteCount: Int,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        BadgedBox(
            badge = {
                if (noteCount > 0) {
                    Badge {
                        Text(text = noteCount.toString())
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                contentDescription = "View Notes"
            )
        }
    }
}

@Composable
fun NotesDialog(
    notes: List<Note>,
    onDismiss: () -> Unit,
    onSaveNote: (noteId: String?, text: String) -> Unit,
    onDeleteNote: (noteId: String) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var isCreatingNew by remember { mutableStateOf(notes.isEmpty()) }

    LaunchedEffect(notes) {
        if (notes.isEmpty()) {
            isCreatingNew = true
            currentIndex = 0
        } else if (currentIndex >= notes.size) {
            // Keep index within valid bounds if an intermediate note is deleted
            currentIndex = (notes.size - 1).coerceAtLeast(0)
        }
    }

    // Resolve active note being viewed/edited
    val currentNote = if (!isCreatingNew && notes.isNotEmpty()) {
        notes.getOrNull(currentIndex.coerceIn(0, notes.lastIndex))
    } else null

    var textBuffer by remember(currentNote, isCreatingNew) {
        mutableStateOf(currentNote?.content ?: "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        isCreatingNew -> "New Note"
                        else -> "Note ${currentIndex + 1} of ${notes.size}"
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                // '+' Icon to add an extra note when viewing existing ones
                if (!isCreatingNew && notes.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            isCreatingNew = true
                            textBuffer = ""
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add New Note"
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = textBuffer,
                    onValueChange = { textBuffer = it },
                    placeholder = { Text("Write your note here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    maxLines = 6
                )

                // Pagination controls when viewing multiple existing notes
                if (!isCreatingNew && notes.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            enabled = currentIndex > 0,
                            onClick = { currentIndex-- }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous Note"
                            )
                        }

                        Text(
                            text = "${currentIndex + 1} / ${notes.size}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        IconButton(
                            enabled = currentIndex < notes.lastIndex,
                            onClick = { currentIndex++ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Note"
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = textBuffer.isNotBlank(),
                onClick = {
                    onSaveNote(currentNote?.id, textBuffer)
                    if (isCreatingNew) {
                        isCreatingNew = false
                        currentIndex = notes.size // Jump focus to newly added note
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                if (currentNote != null) {
                    TextButton(
                        onClick = {
                            onDeleteNote(currentNote.id)
                            if (currentIndex > 0) currentIndex--
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}
