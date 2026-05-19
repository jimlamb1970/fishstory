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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.funjim.fishstory.ui.theme.teams.*

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

var themeMap = mapOf(
    "Dark" to DarkColorScheme,
    "Light" to LightColorScheme,
    "Detroit Lions Dark" to DarkDetroitLionsColorScheme,
    "Detroit Lions Light" to LightDetroitLionsColorScheme,
    "Detroit Tigers Dark" to DarkDetroitTigersColorScheme,
    "Detroit Tigers Light" to LightDetroitTigersColorScheme,
    "Green Bay Packers" to LightPackersColorScheme,
    "Michigan State Spartans" to LightSpartansColorScheme,
    "Michigan Wolverines Dark" to DarkMichiganColorScheme,
    "Michigan Wolverines Light" to LightMichiganColorScheme,
    "Minnesota Golden Gophers" to LightGophersColorScheme,
    "Minnesota Vikings" to LightVikingsColorScheme,
    "Minnesota Wild" to LightWildColorScheme,
    "Wisconsin Badgers" to LightBadgersColorScheme,
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
        selectedTheme is String && themeMap.containsKey(selectedTheme) -> themeMap[selectedTheme]!!

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