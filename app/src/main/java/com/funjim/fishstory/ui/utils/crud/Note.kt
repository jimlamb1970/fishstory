package com.funjim.fishstory.ui.utils.crud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AddNoteDialog(
    onConfirm: (content: String) -> Unit,
    onDismiss: () -> Unit,
) {
    EditNoteDialog(
        item = Note(content = ""),
        title = "Add",
        onConfirm = { onConfirm(it.content) },
        onDismiss = onDismiss
    )
}

@Composable
fun EditNoteDialog(
    item: Note,
    title: String = "Edit",
    onConfirm: (Note) -> Unit,
    onDismiss: () -> Unit
) {
    val origContent = remember(item) { item.content }
    var content by remember { mutableStateOf(origContent) }

    val isValid = content.isNotBlank() && (origContent != content)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title Note") },
        text = {
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Write your note here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 6
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(item.copy(content = content.trim())) },
                enabled = isValid
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun NotesDialog(
    notes: List<Note>,
    onDismiss: () -> Unit,
    onAddNote: (noteId: String?, text: String) -> Unit,
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

    val dateTimeFormatter = remember {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    }

    val dateTime = if (currentNote != null) {
        dateTimeFormatter.format(Date(currentNote.timestamp))
    } else null

    var textBuffer by remember(currentNote, isCreatingNew) {
        mutableStateOf(currentNote?.content ?: "")
    }

    val isValid = textBuffer.isNotBlank() && (currentNote?.content != textBuffer)

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
                        isCreatingNew -> "Add Note"
                        else -> "$dateTime"
                    }
                )

                // '+' Icon to add an extra note when viewing existing ones
                if (!isCreatingNew && notes.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            isCreatingNew = true
                            textBuffer = ""
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Note"
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
                enabled = isValid,
                onClick = {
                    onAddNote(currentNote?.id, textBuffer)
                    if (isCreatingNew) {
                        isCreatingNew = false
                        currentIndex = 0
                    }
                }
            ) {
                if (isCreatingNew)
                    Text("OK")
                else
                    Text("Update")
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
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}