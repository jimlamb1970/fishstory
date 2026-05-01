package com.funjim.fishstory.ui.theme


import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.funjim.fishstory.R

// Define a top-level object
object AppIcons {
    // Mimic the Material library by creating a 'Default' inner object
    object Default {
        // Define your custom property. The getter converts the XML on demand.
        val LeapingFish: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_fish)
        val LeapingFish2: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_fish2)

        // You can add more icons here
        // val StarIcon: ImageVector @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_star)
    }
}