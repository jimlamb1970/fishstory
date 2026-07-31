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
        val Bait: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_bait)
        val BodyOfWater: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_body_of_water)
        val Boat: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_boat)
        val Fisherman: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_fisherman)
        val Fishermen: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_fishermen)
        val FishingBoat: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_fishing_boat)
        val LeapingFishWithFins: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_fish_with_fins)
        val LeapingFishWithFinsAdd: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_fish_with_fins_add)
        val LeapingFish: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_fish)
        val Lure: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_lure)
        val Settings: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_settings)
        val Species: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_species)
        val TackleBox: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_tacklebox)
        val TargetFish: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_target_fish)
        val Trip: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_trip)
        val WaterCup: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_water)
        val Water: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_water_unset)
        val WaterAdd: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_water_add)
        val WaterSet: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_water_set)
        val WaterSetAdd: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_water_set_add)
        val Weather: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_weather_unset)
        val WeatherAdd: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_weather_add)
        val WeatherSet: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_weather_set)
        val WeatherSetAdd: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_weather_set_add)
        val Worm: ImageVector
            @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_fishing_boat)

        // You can add more icons here
        // val StarIcon: ImageVector @Composable get() = ImageVector.vectorResource(id = R.drawable.ic_star)
    }
}