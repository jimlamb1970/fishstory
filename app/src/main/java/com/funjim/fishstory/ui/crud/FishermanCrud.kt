package com.funjim.fishstory.ui.crud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.model.Fisherman

@Composable
fun AddFishermanDialog(
    onDismiss: () -> Unit,
    onAdd: (firstName: String, lastName: String, nickname: String) -> Unit
) {
    EditFishermanDialog(
        fisherman = Fisherman(firstName = "", lastName = "", nickname = ""),
        title = "Add",
        onDismiss = onDismiss,
        onConfirm = { fisherman ->
            onAdd(fisherman.firstName, fisherman.lastName, fisherman.nickname)
        }
    )
}

@Composable
fun EditFishermanDialog(
    fisherman: Fisherman,
    title: String = "Rename",
    onDismiss: () -> Unit,
    onConfirm: (Fisherman) -> Unit
) {
    val origFirstName = remember(fisherman) { fisherman.firstName }
    val origLastName = remember(fisherman) { fisherman.lastName }
    val origNickName = remember(fisherman) { fisherman.nickname }

    var firstName by remember { mutableStateOf(origFirstName) }
    var lastName by remember { mutableStateOf(origLastName) }
    var nickname by remember { mutableStateOf(origNickName) }

    val isChanged = firstName != origFirstName ||
            lastName != origLastName ||
            nickname != origNickName

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$title Fisherman") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = firstName, onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = lastName, onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = nickname, onValueChange = { nickname = it },
                    label = { Text("Nickname (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(fisherman.copy(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    nickname = nickname.trim()))
                },
                enabled = (firstName.isNotBlank() || lastName.isNotBlank()) && isChanged
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}