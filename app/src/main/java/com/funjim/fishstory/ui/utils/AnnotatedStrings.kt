package com.funjim.fishstory.ui.utils

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

@Composable
fun BoldingNumbersText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified
) {
    val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)

    val annotatedString = buildAnnotatedString {
        append(text)

        // Find all sequences of one or more digits (\d+)
        val regex = Regex("\\d+")

        regex.findAll(text).forEach { matchResult ->
            addStyle(
                style = boldStyle,
                start = matchResult.range.first,
                end = matchResult.range.last + 1
            )
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        style = style,
        color = color
    )
}