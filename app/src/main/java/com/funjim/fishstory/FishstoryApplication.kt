package com.funjim.fishstory

import android.app.Application
import com.funjim.fishstory.database.FishstoryDatabase

class FishstoryApplication : Application() {
    val database: FishstoryDatabase by lazy { FishstoryDatabase.getDatabase(this) }
}
