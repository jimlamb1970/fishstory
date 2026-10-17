package com.funjim.fishstory.ui.utils

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.FishermanSummary
import com.funjim.fishstory.ui.theme.FishstoryTheme
import com.funjim.fishstory.ui.cards.FishermanItem
import kotlinx.coroutines.flow.flowOf

@Preview(showBackground = true)
@Composable
fun FishermanItemPreview() {
    FishstoryTheme(selectedTheme = null) {
        FishermanItem(
            fisherman = FishermanSummary(
                fisherman = Fisherman(firstName = "John", lastName = "Doe", nickname = "Big Fish"),
                fishCaught = 10,
                fishKept = 2,
                targetFishCaught = 8,
                targetFishKept = 1,
                totalTrips = 5,
                totalTackleBoxes = 3,
                largestFish = 0,
                smallestFish = 0
            ),
            verbose = true,
            thumbnailFlow = flowOf(null),
            photosFlow = flowOf(emptyList()),
            onClick = {},
            onFishClick = { _, _ -> },
            onPhotoAdded = {},
            onPhotoTaken = {},
            onSetThumbnail = {},
            onPhotoDeleted = {},
            onDelete = {},
        )
    }
}
