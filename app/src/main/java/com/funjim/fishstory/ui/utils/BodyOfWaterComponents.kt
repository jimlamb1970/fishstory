package com.funjim.fishstory.ui.utils

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.funjim.fishstory.model.BodyOfWater

@Composable
fun UpdateAllCatchesDialog(
    bodyOfWater: BodyOfWater,
    content: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Update Catches?") },
        text = { Text(text = content) },
        confirmButton = {
            TextButton(onClick = { onConfirm() }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}