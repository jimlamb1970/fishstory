package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.ui.theme.AppIcons

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
                imageVector = AppIcons.Default.Notes,
                contentDescription = "View Notes",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
