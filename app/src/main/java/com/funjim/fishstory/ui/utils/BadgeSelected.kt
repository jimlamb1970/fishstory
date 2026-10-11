package com.funjim.fishstory.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeSelected(
    badgeColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    state: ToggleableState = ToggleableState.On,
    borderColor: Color = Color.Transparent,
) {
    Surface(
        shape = CircleShape,
        color = badgeColor,
        border = BorderStroke(
            width = 1.dp,
            color = borderColor),
        modifier = modifier.padding(start = 2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
/*
            TriStateCheckbox(
                state = state,
                onClick = null, // Handled by parent card/item click
                enabled = true,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.Transparent,
                    checkmarkColor = contentColor
                ),
                modifier = Modifier.size(12.dp)
            )
 */
            Icon(
                imageVector =
                    if (ToggleableState.On == state) Icons.Default.Check
                    else Icons.Default.Remove,
                contentDescription = "Selected",
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
