package com.funjim.fishstory

import android.app.Application
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.FishStoryRepository
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.LocationRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.LocationProviderImpl
import com.funjim.fishstory.viewmodels.AddEventViewModelFactory
import com.funjim.fishstory.viewmodels.AddTripViewModelFactory
import com.funjim.fishstory.viewmodels.EventViewModelFactory
import kotlin.getValue

class FishstoryApplication : Application() {
    val database: FishstoryDatabase by lazy { FishstoryDatabase.getDatabase(this) }

    val locationRepository by lazy {
        LocationRepository(context = applicationContext)
    }

    val locationProvider by lazy {
        LocationProviderImpl(locationRepository)
    }

    val fishermanRepository by lazy {
        FishermanRepository(
            fishermanDao = database.fishermanDao(),
            lureDao = database.lureDao(),
            photoDao = database.photoDao(),
            tackleBoxDao = database.tackleBoxDao(),
            tripDao = database.tripDao()
        )
    }

    val fishRepository by lazy {
        FishRepository(
            fishDao = database.fishDao(),
            fishermanDao = database.fishermanDao(),
            lureDao = database.lureDao(),
            photoDao = database.photoDao(),
            eventDao = database.eventDao(),
            tripDao = database.tripDao()
        )
    }

    val fishStoryRepository by lazy {
        FishStoryRepository(
            db = database,
            fishDao = database.fishDao(),
            fishermanDao = database.fishermanDao(),
            lureDao = database.lureDao(),
            eventDao = database.eventDao(),
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

    val photoRepository by lazy {
        PhotoRepository(
            database = database,
            context = applicationContext,
            photoDao = database.photoDao()
        )
    }

    val tripRepository by lazy {
        TripRepository(
            database = database,
            eventDao = database.eventDao(),
            tripDao = database.tripDao()
        )
    }

    fun getAddEventViewModelFactory() = AddEventViewModelFactory(
        locationProvider = locationProvider,
        fishermanRepository,
        fishRepository,
        photoRepository,
        tripRepository
    )

    fun getEventViewModelFactor() = EventViewModelFactory(
            locationProvider = locationProvider,
            fishermanRepository,
            fishRepository,
            photoRepository,
            tripRepository)

    fun getAddTripViewModelFactory() = AddTripViewModelFactory(
        locationProvider = locationProvider,
        fishermanRepository,
        fishRepository,
        photoRepository,
        tripRepository
    )
}
