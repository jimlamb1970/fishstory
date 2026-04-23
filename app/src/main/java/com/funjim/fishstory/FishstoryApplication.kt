package com.funjim.fishstory

import android.app.Application
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.FishStoryRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.TripRepository

class FishstoryApplication : Application() {
    val database: FishstoryDatabase by lazy { FishstoryDatabase.getDatabase(this) }

    val fishermanRepository by lazy {
        FishermanRepository(
            fishermanDao = database.fishermanDao(),
            lureDao = database.lureDao(),
            photoDao = database.photoDao(),
            tackleBoxDao = database.tackleBoxDao()
        )
    }

    val fishRepository by lazy {
        FishRepository(
            fishDao = database.fishDao(),
            fishermanDao = database.fishermanDao(),
            photoDao = database.photoDao(),
            segmentDao = database.segmentDao(),
            tripDao = database.tripDao()
        )
    }

    val fishStoryRepository by lazy {
        FishStoryRepository(
            db = database,
            fishDao = database.fishDao(),
            fishermanDao = database.fishermanDao(),
            lureDao = database.lureDao(),
            segmentDao = database.segmentDao(),
            tackleBoxDao = database.tackleBoxDao(),
            tripDao = database.tripDao()
        )
    }

    val lureRepository by lazy {
        LureRepository(
            fishermanDao = database.fishermanDao(),
            lureDao = database.lureDao(),
            photoDao = database.photoDao(),
            tackleBoxDao = database.tackleBoxDao()
        )
    }

    val tripRepository by lazy {
        TripRepository(
            fishermanDao = database.fishermanDao(),
            photoDao = database.photoDao(),
            segmentDao = database.segmentDao(),
            tripDao = database.tripDao()
        )
    }
}
