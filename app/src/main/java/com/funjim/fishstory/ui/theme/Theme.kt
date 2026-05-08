package com.funjim.fishstory.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val DarkMichiganColorScheme = darkColorScheme(
    primary = MichMaize,
    onPrimary = MichBlue,
    primaryContainer = MichMaize.copy(alpha = 0.15f),
    onPrimaryContainer = MichMaize,

    secondary = MichMaize,
    onSecondary = MichBlue,

    tertiary = Color.White,
    onTertiary = MichBlue,

    background = MichBlue,
    onBackground = Color.White,

    surface = MichBlue,
    onSurface = Color.White,

    surfaceContainer = MichBlue,
    surfaceContainerLow = MichBlue,
    surfaceContainerHigh = MichBlue,

    // This is for the "outline" of the menu or cards
    outlineVariant = MichMaize,
    surfaceVariant = MichBlue,
    onSurfaceVariant = MichMaize,
)
private val LightMichiganColorScheme = lightColorScheme(
    primary = MichBlue,
    onPrimary = MichMaize,
    primaryContainer = MichBlueContainer,
    onPrimaryContainer = MichBlue,

    secondary = MichBlue, // Keeping it consistent
    onSecondary = Color.White,

    tertiary = MichMaize,
    onTertiary = MichBlue, // Blue text on Maize background looks sharp

    background = Color.White,
    onBackground = MichBlue,

    surface = Color.White,
    onSurface = MichBlue,

    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color.White,

    // This is for the "outline" of the menu or cards
    outlineVariant = MichBlue,
    surfaceVariant = MichMaize,
    onSurfaceVariant = MichBlue,
)

@Composable
fun FishstoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true, // Setting to false to force my theme
    selectedTheme: String?,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        selectedTheme == "MichiganLight" -> LightMichiganColorScheme
        selectedTheme == "MichiganDark" -> DarkMichiganColorScheme
        selectedTheme == "Dark" -> DarkColorScheme
        selectedTheme == "Light" -> LightColorScheme

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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set the status bar background color to match your Top App Bar
            window.statusBarColor = colorScheme.primary.toArgb()

            val controller = WindowCompat.getInsetsController(window, view)
            // isAppearanceLightStatusBars = false means icons will be WHITE
            // isAppearanceLightStatusBars = true means icons will be BLACK
            controller.isAppearanceLightStatusBars = false
        }
    }
}