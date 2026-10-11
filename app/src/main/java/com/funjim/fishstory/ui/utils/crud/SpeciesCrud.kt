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
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.Species

@Composable
fun AddSpeciesDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    EditSpeciesDialog(
        item = Species(name = ""),
        title = "Add",
        onConfirm = { onConfirm(it.name) },
        onDismiss = onDismiss
    )
}

@Composable
fun EditSpeciesDialog(
    item: Species,
    title: String = "Rename",
    onDismiss: () -> Unit,
    onConfirm: (Species) -> Unit
) {
    val origName = remember(BodyOfWater) { item.name }
    var name by remember { mutableStateOf(origName) }

    val isValid = name.isNotBlank() && (origName != name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title Species") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Species Name (e.g. Walleye)") }
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