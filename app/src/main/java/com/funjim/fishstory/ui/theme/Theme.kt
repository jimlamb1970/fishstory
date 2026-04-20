package com.funjim.fishstory.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = MichBlue,
    onPrimary = MichMaize,
    primaryContainer = MichBlueContainer,
    onPrimaryContainer = MichBlue,

    secondary = MichBlue, // Keeping it consistent
    onSecondary = Color.White,

    tertiary = MichMaize,
    onTertiary = MichBlue, // Blue text on Maize background looks sharp
    tertiaryContainer = MichMaizeContainer,
    onTertiaryContainer = Color(0xFF241A00),

    background = Color.White,
    surface = Color.White,
    onBackground = DarkGrey,
    onSurface = DarkGrey,
)

@Composable
fun FishstoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Setting to false to force my theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}