package com.funjim.fishstory.ui.utils.crud

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
import com.funjim.fishstory.model.LureColor

@Composable
fun AddColorDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    EditColorDialog(
        item = LureColor(name = ""),
        title = "Add",
        onConfirm = { onConfirm(it.name) },
        onDismiss = onDismiss
    )
}

@Composable
fun EditColorDialog(
    item: LureColor,
    title: String = "Rename",
    onConfirm: (LureColor) -> Unit,
    onDismiss: () -> Unit
) {
    val origName = remember(item) { item.name }
    var name by remember { mutableStateOf(origName) }

    val isValid = name.isNotBlank() && (origName != name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title Color") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Color (e.g. Blue)") }
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
