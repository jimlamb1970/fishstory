package com.funjim.fishstory

import android.app.Application
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.repository.FishStoryRepository

class FishstoryApplication : Application() {
    val database: FishstoryDatabase by lazy { FishstoryDatabase.getDatabase(this) }

    val repository by lazy {
        FishStoryRepository(
            fishDao = database.fishDao(),
            fishermanDao = database.fishermanDao(),
            lureDao = database.lureDao(),
            photoDao = database.photoDao(),
            tackleBoxDao = database.tackleBoxDao(),
            segmentDao = database.segmentDao(),
            tripDao = database.tripDao()
        )
    }
}
