package com.funjim.fishstory.ui.utils

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.funjim.fishstory.ui.theme.AppIcons

fun ByteArray.toImageBitmap(): ImageBitmap? {
    return try {
        val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
@Composable
fun ThumbnailBox(
    thumbnail: ByteArray?,
    modifier: Modifier = Modifier,
    imageVector: ImageVector = AppIcons.Default.LeapingFish
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = remember(thumbnail) {
            thumbnail?.toImageBitmap()
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Fish thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Optional: Show a "No Photo" icon if the blob is null
            Icon(
                imageVector = imageVector,
                contentDescription = "Fish",
//                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}