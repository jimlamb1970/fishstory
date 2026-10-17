package com.funjim.fishstory.ui.crud

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

@Composable
fun AddBodyOfWaterDialog(
    onConfirm: (name: String) -> Unit,
    onDismiss: () -> Unit,
) {
    EditBodyOfWaterDialog(
        item = BodyOfWater(name = ""),
        title = "Add",
        onConfirm = { onConfirm(it.name) },
        onDismiss = onDismiss
    )
}

@Composable
fun EditBodyOfWaterDialog(
    item: BodyOfWater,
    title: String = "Rename",
    onConfirm: (BodyOfWater) -> Unit,
    onDismiss: () -> Unit
) {
    val origName = remember(BodyOfWater) { item.name }
    var name by remember { mutableStateOf(origName) }

    val isValid = name.isNotBlank() && (origName != name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title Body of Water") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                placeholder = { Text("Body of Water (e.g. Lake of the Woods)") }
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