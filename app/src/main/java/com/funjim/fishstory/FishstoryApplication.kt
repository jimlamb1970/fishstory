package com.funjim.fishstory

import android.app.Application
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.FishStoryRepository
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.LocationRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import com.funjim.fishstory.ui.utils.LocationProviderImpl
import com.funjim.fishstory.viewmodels.AddEventViewModelFactory
import com.funjim.fishstory.viewmodels.AddFishViewModelFactory
import com.funjim.fishstory.viewmodels.AddTripViewModelFactory
import com.funjim.fishstory.viewmodels.DashboardViewModelFactory
import com.funjim.fishstory.viewmodels.EventViewModelFactory
import com.funjim.fishstory.viewmodels.FishViewModelFactory
import com.funjim.fishstory.viewmodels.ImportViewModelFactory
import com.funjim.fishstory.viewmodels.LureViewModelFactory
import com.funjim.fishstory.viewmodels.MainViewModelFactory
import com.funjim.fishstory.viewmodels.TripListViewModelFactory
import com.funjim.fishstory.viewmodels.TripViewModelFactory
import kotlin.getValue

class FishstoryApplication : Application() {
    val database: FishstoryDatabase by lazy { FishstoryDatabase.getDatabase(this) }

    val locationRepository by lazy {
        LocationRepository(context = applicationContext)
    }

    val locationProvider by lazy {
        LocationProviderImpl(locationRepository)
    }

    val environmentRepository by lazy {
        EnvironmentRepository(
            database = database,
            bodyOfWaterDao = database.bodyOfWaterDao(),
            eventDao = database.eventDao(),
        )
    }

     val fishermanRepository by lazy {
        FishermanRepository(
            fishermanDao = database.fishermanDao(),
            tackleBoxDao = database.tackleBoxDao()
        )
    }

    val fishRepository by lazy {
        FishRepository(
            eventDao = database.eventDao(),
            fishDao = database.fishDao(),
            fishermanDao = database.fishermanDao(),
            lureDao = database.lureDao(),
            photoDao = database.photoDao(),
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

    fun getAddFishViewModelFactory() = AddFishViewModelFactory(
        locationProvider = locationProvider,
        envRepo = environmentRepository,
        fishRepo = fishRepository,
        lureRepo = lureRepository,
        photoRepo = photoRepository,
        tripRepo = tripRepository
    )

    fun getAddTripViewModelFactory() = AddTripViewModelFactory(
        locationProvider = locationProvider,
        fishermanRepository,
        fishRepository,
        photoRepository,
        tripRepository
    )

    fun getDashboardViewModelFactory() = DashboardViewModelFactory(
        locationProvider = locationProvider,
        photoRepo = photoRepository,
        tripRepo = tripRepository
    )

    fun getEventViewModelFactory() = EventViewModelFactory(
            locationProvider = locationProvider,
            environmentRepository,
            fishermanRepository,
            fishRepository,
            photoRepository,
            tripRepository)

    fun getFishViewModelFactory() = FishViewModelFactory(
        locationProvider = locationProvider,
        fishRepo = fishRepository,
        lureRepo = lureRepository,
        photoRepo = photoRepository,
        tripRepo = tripRepository
    )

    fun getImportViewModelFactory() = ImportViewModelFactory(
        fishStoryRepository
    )

    fun getLureViewModelFactory() = LureViewModelFactory(
        lureRepository,
        photoRepository
    )

    fun getMainViewModelFactory() = MainViewModelFactory(
        locationProvider = locationProvider,
        bodyOfWaterDao = database.bodyOfWaterDao(),
        eventDao = database.eventDao(),
        fishDao = database.fishDao(),
        fishermanDao = database.fishermanDao(),
        lureDao = database.lureDao(),
        photoDao = database.photoDao(),
        tackleBoxDao = database.tackleBoxDao(),
        tripDao = database.tripDao()
    )

    fun getTripViewModelFactory() = TripViewModelFactory(
        locationProvider = locationProvider,
        environmentRepository,
        fishermanRepository,
        fishRepository,
        photoRepository,
        tripRepository
    )

    fun getTripListViewModelFactory() = TripListViewModelFactory(
        locationProvider = locationProvider,
        photoRepo = photoRepository,
        tripRepo = tripRepository
    )
}
