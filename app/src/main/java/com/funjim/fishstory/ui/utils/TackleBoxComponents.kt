package com.funjim.fishstory.ui.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.funjim.fishstory.model.TackleBox

@Composable
fun AddTackleBoxDialog(
    fishermanId: String,
    onConfirm: (TackleBox) -> Unit,
    onDismiss: () -> Unit,
) {
    EditTackleBoxDialog(
        item = TackleBox(name = "", fishermanId = fishermanId),
        title = "Add",
        onConfirm = { onConfirm(it) },
        onDismiss = onDismiss
    )
}

@Composable
fun EditTackleBoxDialog(
    item: TackleBox,
    title: String = "Rename",
    onConfirm: (TackleBox) -> Unit,
    onDismiss: () -> Unit
) {
    val origName = remember(TackleBox) { item.name }
    var name by remember { mutableStateOf(origName) }

    val isValid = name.isNotBlank() && (origName != name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title Tackle Box") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Tackle Box Name (e.g. Jim's Tackle Box)") }
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(item.copy(name = name.trim())) },
                enabled = isValid
            ) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
