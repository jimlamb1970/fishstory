package com.funjim.fishstory

import android.app.Application
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.LureRepository

class FishstoryApplication : Application() {
    val database: FishstoryDatabase by lazy { FishstoryDatabase.getDatabase(this) }

    val repositoryFisherman by lazy {
        FishermanRepository(
            fishermanDao = database.fishermanDao(),
            lureDao = database.lureDao(),
            photoDao = database.photoDao(),
            tackleBoxDao = database.tackleBoxDao()
        )
    }

    val repositoryFish by lazy {
        FishRepository(
            fishDao = database.fishDao(),
            fishermanDao = database.fishermanDao(),
            photoDao = database.photoDao(),
            segmentDao = database.segmentDao(),
            tripDao = database.tripDao()
        )
    }

    val repositoryLure by lazy {
        LureRepository(
            fishermanDao = database.fishermanDao(),
            lureDao = database.lureDao(),
            photoDao = database.photoDao(),
            tackleBoxDao = database.tackleBoxDao()
        )
    }
}
