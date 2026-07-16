package com.funjim.fishstory.ui.utils

import android.content.Context
import android.content.Intent

fun shareContent(context: Context, textToShare: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, textToShare)
        type = "text/plain"
    }

    // Wrap the intent in a Chooser to ensure the system share sheet always displays properly
    val shareIntent = Intent.createChooser(sendIntent, "Share your catch via:")
    context.startActivity(shareIntent)
}
