package com.funjim.fishstory.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.funjim.fishstory.model.Bait
import com.funjim.fishstory.model.BodyOfWater
import com.funjim.fishstory.model.Event
import com.funjim.fishstory.model.EventBait
import com.funjim.fishstory.model.EventBodyOfWater
import com.funjim.fishstory.model.EventFisherman
import com.funjim.fishstory.model.EventLimit
import com.funjim.fishstory.model.EventTargetSpecies
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.Fisherman
import com.funjim.fishstory.model.Limit
import com.funjim.fishstory.model.LimitSpecies
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.LureGlowColor
import com.funjim.fishstory.model.LurePrimaryColor
import com.funjim.fishstory.model.LureSecondaryColor
import com.funjim.fishstory.model.Note
import com.funjim.fishstory.model.NoteEvent
import com.funjim.fishstory.model.NoteFish
import com.funjim.fishstory.model.NoteTrip
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.PhotoBait
import com.funjim.fishstory.model.PhotoBodyOfWater
import com.funjim.fishstory.model.PhotoEvent
import com.funjim.fishstory.model.PhotoFish
import com.funjim.fishstory.model.PhotoFisherman
import com.funjim.fishstory.model.PhotoLure
import com.funjim.fishstory.model.PhotoSkyCondition
import com.funjim.fishstory.model.PhotoSpecies
import com.funjim.fishstory.model.PhotoTrip
import com.funjim.fishstory.model.PhotoWaterClarity
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxLure
import com.funjim.fishstory.model.Trip
import com.funjim.fishstory.model.TripBait
import com.funjim.fishstory.model.TripBodyOfWater
import com.funjim.fishstory.model.TripFisherman
import com.funjim.fishstory.model.TripLimit
import com.funjim.fishstory.model.TripTargetSpecies
import com.funjim.fishstory.model.Water
import com.funjim.fishstory.model.WaterClarity
import com.funjim.fishstory.model.Weather
import com.funjim.fishstory.repository.ConfigurationRepository
import com.funjim.fishstory.repository.EnvironmentRepository
import com.funjim.fishstory.repository.FishRepository
import com.funjim.fishstory.repository.FishStoryRepository
import com.funjim.fishstory.repository.FishermanRepository
import com.funjim.fishstory.repository.LureRepository
import com.funjim.fishstory.repository.PhotoRepository
import com.funjim.fishstory.repository.TripRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

sealed interface ImportConflict {
    data class ExistingEvent(
        val eventName: String,
        val onResolve: (resolution: EventResolution) -> Unit
    ) : ImportConflict

    data class ExistingTrip(
        val tripName: String,
        val onResolve: (resolution: TripResolution) -> Unit
    ) : ImportConflict
}

enum class EventResolution { OVERWRITE, KEEP_BOTH, SKIP }
enum class TripResolution { CREATE_NEW, LINK_EXISTING, SKIP }

@Serializable
data class FishStoryEventData(
    val fileType: String = "FishStoryEventData",
    val schemaVersion: Int = 1,
    val trip: Trip,
    val event: Event,
    val baits: List<Bait> = emptyList(),
    val bodiesOfWater: List<BodyOfWater> = emptyList(),
    val fishermen: List<Fisherman> = emptyList(),
    val limits: List<Limit> = emptyList(),
    val lures: List<Lure> = emptyList(),
    val lureColors: List<LureColor> = emptyList(),
    val notes: List<Note> = emptyList(),
    val species: List<Species> = emptyList(),
    val skyConditions: List<SkyCondition> = emptyList(),
    val tackleBoxes: List<TackleBox> = emptyList(),
    val waterClarities: List<WaterClarity> = emptyList(),
    val waterConditions: List<Water> = emptyList(),
    val weatherConditions: List<Weather> = emptyList(),
    val fish: List<Fish> = emptyList(),

    val eventBaits: List<EventBait> = emptyList(),
    val eventBodiesOfWater: List<EventBodyOfWater> = emptyList(),
    val eventFishermen: List<EventFisherman> = emptyList(),
    val eventLimits: List<EventLimit> = emptyList(),
    val eventTargetSpecies: List<EventTargetSpecies> = emptyList(),
    val limitSpecies: List<LimitSpecies> = emptyList(),
    val lurePrimaryColors: List<LurePrimaryColor> = emptyList(),
    val lureSecondaryColors: List<LureSecondaryColor> = emptyList(),
    val lureGlowColors: List<LureGlowColor> = emptyList(),
    val notesEvent: List<NoteEvent> = emptyList(),
    val notesFish: List<NoteFish> = emptyList(),
    val tackleBoxLures: List<TackleBoxLure> = emptyList(),
)

@Serializable
data class FishStoryData(
    val fileType: String = "FishStoryData",
    val schemaVersion: Int = 1,
    // Main Databases (17)
    val baits: List<Bait> = emptyList(),
    val bodiesOfWater: List<BodyOfWater> = emptyList(),
    val colors: List<LureColor> = emptyList(),
    val skyConditions: List<SkyCondition> = emptyList(),
    val species: List<Species> = emptyList(),
    val waterClarities: List<WaterClarity> = emptyList(),
    val fishermen: List<Fisherman> = emptyList(),
    val lures: List<Lure> = emptyList(),
    val tackleboxes: List<TackleBox> = emptyList(),
    val trips: List<Trip> = emptyList(),
    val events: List<Event> = emptyList(),
    val waterConditions: List<Water> = emptyList(),
    val weatherConditions: List<Weather> = emptyList(),
    val limits: List<Limit> = emptyList(),
    val fish: List<Fish> = emptyList(),
    val notes: List<Note> = emptyList(),
    val photos: List<Photo> = emptyList(),
    // Lure and Tackle Box Cross References (4) (21)
    val lurePrimaryColors: List<LurePrimaryColor> = emptyList(),
    val lureSecondaryColors: List<LureSecondaryColor> = emptyList(),
    val lureGlowColors: List<LureGlowColor> = emptyList(),
    val tackleBoxLures: List<TackleBoxLure> = emptyList(),
    // LLimit Cross References (1) (22)
    val limitSpecies: List<LimitSpecies> = emptyList(),
    // Trip Cross References (5) (32)
    val tripBaits: List<TripBait> = emptyList(),
    val tripBodiesOfWater: List<TripBodyOfWater> = emptyList(),
    val tripFishermen: List<TripFisherman> = emptyList(),
    val tripLimits: List<TripLimit> = emptyList(),
    val tripTargetSpecies: List<TripTargetSpecies> = emptyList(),
    // Event Cross References (5) (27)
    val eventBaits: List<EventBait> = emptyList(),
    val eventBodiesOfWater: List<EventBodyOfWater> = emptyList(),
    val eventFishermen: List<EventFisherman> = emptyList(),
    val eventLimits: List<EventLimit> = emptyList(),
    val eventTargetSpecies: List<EventTargetSpecies> = emptyList(),
    // Note Cross References (3) (35)
    val notesEvent: List<NoteEvent> = emptyList(),
    val notesFish: List<NoteFish> = emptyList(),
    val notesTrip: List<NoteTrip> = emptyList(),
    // Photo Cross References (10) (45)
    val photosBait: List<PhotoBait> = emptyList(),
    val photosBodyOfWater: List<PhotoBodyOfWater> = emptyList(),
    val photosEvent: List<PhotoEvent> = emptyList(),
    val photosFish: List<PhotoFish> = emptyList(),
    val photosFisherman: List<PhotoFisherman> = emptyList(),
    val photosLure: List<PhotoLure> = emptyList(),
    val photosSkyCondition: List<PhotoSkyCondition> = emptyList(),
    val photosSpecies: List<PhotoSpecies> = emptyList(),
    val photosTrip: List<PhotoTrip> = emptyList(),
    val photosWaterClarity: List<PhotoWaterClarity> = emptyList(),
)

class ImportViewModel(
    private val repository: FishStoryRepository,
    private val envRepo: EnvironmentRepository,
    private val fishermanRepo: FishermanRepository,
    private val fishRepo: FishRepository,
    private val lureRepo: LureRepository,
    private val photoRepo: PhotoRepository,
    private val tripRepo: TripRepository,
) : ViewModel() {
    var isImporting by mutableStateOf(false)

    private val _toastMessage = MutableSharedFlow<String>(replay = 1)
    val toastMessage = _toastMessage.asSharedFlow()

    private val _snackBarMessage = MutableSharedFlow<String>(replay = 1)
    val snackBarMessage = _snackBarMessage.asSharedFlow()

    private val json = Json { prettyPrint = true }
    private val jsonImport = Json { ignoreUnknownKeys = true }

    fun cleanupDatabase() {
        viewModelScope.launch {
            try {
                // Call your repository or DAO method that wraps the purge logic
                val deletedCount = photoRepo.purgeOrphanedPhotos()

                if (deletedCount > 0) {
                    _snackBarMessage.emit("Successfully purged $deletedCount orphaned photo(s).")
                } else {
                    // Optional: You can choose to stay silent if nothing needed purging
                    _snackBarMessage.emit("No orphaned photos found.")
                }
            } catch (e: Exception) {
                Log.e("PhotoViewModel", "Error occurred while purging orphaned photos", e)
                _snackBarMessage.emit("An error occurred while cleaning up photos.")
            }
        }
    }

    private suspend fun processImportData(exportData: FishStoryEventData) {
        val baitMap = mutableMapOf<String, String>()
        val bodyOfWaterMap = mutableMapOf<String, String>()
        val fishermanMap = mutableMapOf<String, String>()
        val limitMap = mutableMapOf<String, String>()
        val lureColorMap = mutableMapOf<String, String>()
        val lureMap = mutableMapOf<String, String>()
        val noteMap = mutableMapOf<String, String>()
        val skyConditionMap = mutableMapOf<String, String>()
        val speciesMap = mutableMapOf<String, String>()
        val tackleBoxMap = mutableMapOf<String, String>()
        val waterClarityMap = mutableMapOf<String, String>()
        val waterMap = mutableMapOf<String, String>()
        val weatherMap = mutableMapOf<String, String>()

        // Trip is just a 'stub' but need to make sure that if a trip already exists,
        // that its dates are updated to cover the event.
        val tripResult = tripRepo.findOrCreateTrip(exportData.trip)
        val startTime = min(tripResult.item.startDate, exportData.event.startTime)
        val endTime = max(tripResult.item.endDate, exportData.event.endTime)
        tripRepo.upsertTrip(tripResult.item.copy(startDate = startTime, endDate = endTime))

        // Always update the event
        tripRepo.upsertEvent(exportData.event)

        // Import and keep track of new/existing id
        // When adding bait to event, use the mapped id
        for (bait in exportData.baits) {
            val baitResult = lureRepo.findOrCreateBait(bait)
            baitMap[bait.id] = baitResult.item.id
        }
        for (eventBait in exportData.eventBaits) {
            val bait = baitMap[eventBait.baitId]
            if (bait != null) {
                lureRepo.addBaitToEvent(bait, eventBait.eventId)
            }
        }

        // Import and keep track of new/existing id
        // When adding body of water to event (and trip), use the mapped id
        for (bodyOfWater in exportData.bodiesOfWater) {
            val bodyOfWaterResult = envRepo.findOrCreateBodyOfWater(bodyOfWater)
            bodyOfWaterMap[bodyOfWater.id] = bodyOfWaterResult.item.id
        }
        for (eventBodyOfWater in exportData.eventBodiesOfWater) {
            val bodyOfWater = bodyOfWaterMap[eventBodyOfWater.bodyOfWaterId]
            if (bodyOfWater != null) {
                val newBodyOfWater = eventBodyOfWater.copy(bodyOfWaterId = bodyOfWater)
                envRepo.insertBodyOfWaterForEvent(newBodyOfWater, true)
            }
        }

        // When adding fisherman/tacklebox to event (and trip), use the mapped ids
        for (fisherman in exportData.fishermen) {
            val fishermanResult = fishermanRepo.findOrCreateFisherman(fisherman)
            fishermanMap[fisherman.id] = fishermanResult.item.id
        }
        for (tackleBox in exportData.tackleBoxes) {
            val tackleBoxResult = fishermanRepo.findOrCreateTackleBox(tackleBox)
            tackleBoxMap[tackleBox.id] = tackleBoxResult.item.id
        }
        for (eventFisherman in exportData.eventFishermen) {
            val fishermanId = fishermanMap[eventFisherman.fishermanId]
            val tackleBoxId = tackleBoxMap[eventFisherman.tackleBoxId]
            if (fishermanId != null) {
                tripRepo.addFishermanToEventAndTrip(
                    tripId = exportData.trip.id,
                    eventId = eventFisherman.eventId,
                    fishermanId = fishermanId,
                    tackleBoxId = tackleBoxId
                )
            }
        }

        // When adding target species to event (and trip), use the mapped ids
        for (species in exportData.species) {
            val speciesResult = fishRepo.findOrCreateSpecies(species)
            speciesMap[species.id] = speciesResult.item.id
        }
        for (targetSpecies in exportData.eventTargetSpecies) {
            val speciesId = speciesMap[targetSpecies.speciesId]
            if (speciesId != null) {
                tripRepo.addTargetSpeciesToEvent(
                    EventTargetSpecies(
                        eventId = targetSpecies.eventId,
                        speciesId = speciesId,
                    ),
                    cascade = true
                )
            }
        }

        // When adding limit to event, use the mapped ids
        for (limit in exportData.limits) {
            val species: List<String> = exportData.limitSpecies
                .filter { it.limitId == limit.id }
                .mapNotNull { limitSpecies ->
                    speciesMap[limitSpecies.speciesId]
                }
            val limitResult = fishRepo.findOrCreateLimit(limit, species)
            limitMap[limit.id] = limitResult.item.id
        }
        for (eventLimit in exportData.eventLimits) {
            val limitId = limitMap[eventLimit.limitId]
            if (limitId != null) {
                fishRepo.addLimitToEvent(
                    eventId = eventLimit.eventId,
                    limitId = limitId
                )
            }
        }

        // When adding colors to lures, use the mapped ids
        for (color in exportData.lureColors) {
            val colorResult = lureRepo.findOrCreateLureColor(color)
            lureColorMap[color.id] = colorResult.item.id
        }
        for (lure in exportData.lures) {
            val primaryColors: List<String> = exportData.lurePrimaryColors
                .filter { it.lureId == lure.id }
                .mapNotNull { primaryColor ->
                    lureColorMap[primaryColor.colorId]
                }
            val secondaryColors: List<String> = exportData.lureSecondaryColors
                .filter { it.lureId == lure.id }
                .mapNotNull { secondaryColor ->
                    lureColorMap[secondaryColor.colorId]
                }
            val glowColors: List<String> = exportData.lureGlowColors
                .filter { it.lureId == lure.id }
                .mapNotNull { glowColor ->
                    lureColorMap[glowColor.colorId]
                }
            val lureResult = lureRepo.findOrCreateLure(lure, primaryColors, secondaryColors, glowColors)
            lureMap[lure.id] = lureResult.item.id
        }
        // When adding lures to tackle boxes, use the mapped ids
        for (tackleBoxLure in exportData.tackleBoxLures) {
            val lureId = lureMap[tackleBoxLure.lureId]
            val tackleBoxId = tackleBoxMap[tackleBoxLure.tackleBoxId]
            if (lureId != null && tackleBoxId != null) {
                lureRepo.addLureToTackleBox(
                    tackleBoxId = tackleBoxId,
                    lureId = lureId
                )
            }
        }

        // When adding sky condition to weather, use the mapped ids
        for (condition in exportData.skyConditions) {
            val conditionResult = envRepo.findOrCreateSkyCondition(condition)
            skyConditionMap[condition.id] = conditionResult.item.id
        }
        for (weather in exportData.weatherConditions) {
            val skyCondition = skyConditionMap[weather.skyConditionId]
            val newWeather = weather.copy(skyConditionId = skyCondition)
            val weatherResult = envRepo.findOrCreateWeatherCondition(newWeather)
            weatherMap[weather.id] = weatherResult.item.id
        }

        // When adding water clarity to water, use the mapped ids
        for (clarity in exportData.waterClarities) {
            val clarityResult = envRepo.findOrCreateWaterClarity(clarity)
            waterClarityMap[clarity.id] = clarityResult.item.id
        }
        for (water in exportData.waterConditions) {
            val clarity = waterClarityMap[water.clarityId]
            val newWater = water.copy(clarityId = clarity)
            val waterResult = envRepo.findOrCreateWaterCondition(newWater)
            waterMap[water.id] = waterResult.item.id
        }

        for (fish in exportData.fish) {
            val baitId = baitMap[fish.baitId]
            val bodyOfWaterId = bodyOfWaterMap[fish.bodyOfWaterId]
            val fishermanId = fishermanMap[fish.fishermanId]
            val lureId = lureMap[fish.lureId]
            val speciesId = speciesMap[fish.speciesId]
            val waterId = waterMap[fish.waterId]
            val weatherId = weatherMap[fish.weatherId]
            if ((fishermanId != null) && (speciesId != null)) {
                val newFish = fish.copy(
                    baitId = baitId,
                    bodyOfWaterId = bodyOfWaterId,
                    fishermanId = fishermanId,
                    lureId = lureId,
                    speciesId = speciesId,
                    waterId = waterId,
                    weatherId = weatherId
                )
                fishRepo.upsertFish(newFish)
            }
        }

        for (note in exportData.notes) {
            val noteResult = tripRepo.findOrCreateNote(note)
            noteMap[note.id] = noteResult.item.id
        }
        for (eventNote in exportData.notesEvent) {
            val noteId = noteMap[eventNote.noteId] ?: continue
            val newEventNote = eventNote.copy(noteId = noteId)
            tripRepo.insertEventNote(newEventNote)
        }
        for (fishNote in exportData.notesFish) {
            val noteId = noteMap[fishNote.noteId] ?: continue
            val newFishNote = fishNote.copy(noteId = noteId)
            tripRepo.insertFishNote(newFishNote)
        }
    }

    fun importEvent(
        context: Context,
        uri: Uri
    ) {
        viewModelScope.launch {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@launch

            val exportData = jsonImport.decodeFromString<FishStoryEventData>(jsonString)

            if (exportData.fileType == "FishStoryEventData") {
                processImportData(exportData)
            } else {
                // Handle error
            }
        }
    }

    suspend fun exportFullDatabase(context: Context, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val allData = repository.exportAllData()
/*
                val allData = FishStoryData(
                    // Main Databases (17)
                    baits = lureRepo.getAllBaitsList(),                             /* main DB */
                    bodiesOfWater = envRepo.getAllBodiesOfWaterList(),              /* main DB */
                    colors = lureRepo.getAllLureColorsList(),                       /* main DB */
                    skyConditions = envRepo.getAllSkyConditionsList(),              /* main DB */
                    species = fishRepo.getAllSpeciesList(),                         /* main DB */
                    waterClarities = envRepo.getAllWaterClarityList(),              /* main DB */
                    fishermen = fishermanRepo.getAllFishermenList(),                /* main DB */
                    lures = lureRepo.getAllLuresList(),                             /* main DB */
                    tackleboxes = fishermanRepo.getAllTackleBoxesList(),            /* main DB */
                    trips = tripRepo.getAllTripsList(),                             /* main DB */
                    events = tripRepo.getAllEventsList(),                           /* main DB */
                    waterConditions = envRepo.getAllWaterConditionsList(),          /* main DB */
                    weatherConditions = envRepo.getAllWeatherConditionsList(),      /* main DB */
                    limits = fishRepo.getAllLimitsList(),                           /* main DB */
                    fish = fishRepo.getAllFishList(),                               /* main DB */
                    notes = tripRepo.getAllNotesList(),                             /* main DB */
                    photos = photoRepo.getAllPhotosList(),                          /* main DB */
                    // Lure and Tackle Box Cross References (4) (21)
                    lurePrimaryColors = lureRepo.getAllLurePrimaryColorsList(),
                    lureSecondaryColors = lureRepo.getAllLureSecondaryColorsList(),
                    lureGlowColors = lureRepo.getAllLureGlowColorsList(),
                    tackleBoxLures = fishermanRepo.getAllTackleBoxLuresList(),
                    // Limit Cross References (1) (22)
                    limitSpecies = fishRepo.getAllLimitSpeciesList(),
                    // Trip Cross References (5) (27)
                    tripBaits = lureRepo.getAllTripBaitsList(),
                    tripBodiesOfWater = envRepo.getAllTripBodiesOfWaterList(),
                    tripFishermen = fishermanRepo.getAllTripFishermenList(),
                    tripLimits = fishRepo.getAllTripLimitsList(),
                    tripTargetSpecies = fishRepo.getAllTripTargetSpeciesList(),
                    // Event Cross References (5) (32)
                    eventBaits = lureRepo.getAllEventBaitsList(),
                    eventBodiesOfWater = envRepo.getAllEventBodiesOfWaterList(),
                    eventFishermen = fishermanRepo.getAllEventFishermenList(),
                    eventLimits = fishRepo.getAllEventLimitsList(),
                    eventTargetSpecies = fishRepo.getAllEventTargetSpeciesList(),
                    // Note Cross References (3) (35)
                    notesEvent = tripRepo.getAllNotesEventList(),
                    notesFish = tripRepo.getAllNotesFishList(),
                    notesTrip = tripRepo.getAllNotesTripList(),
                    // Photo Cross References (10) (45)
                    photosBait = photoRepo.getAllPhotosForBaitList(),
                    photosBodyOfWater = photoRepo.getAllPhotosForBodiesOfWaterList(),
                    photosEvent = photoRepo.getAllPhotosForEventsList(),
                    photosFish = photoRepo.getAllPhotosForFishList(),
                    photosFisherman = photoRepo.getAllPhotosForFishermenList(),
                    photosLure = photoRepo.getAllPhotosForLuresList(),
                    photosSkyCondition = photoRepo.getAllPhotosForSkyConditionsList(),
                    photosSpecies = photoRepo.getAllPhotosForSpeciesList(),
                    photosTrip = photoRepo.getAllPhotosForTripsList(),
                    photosWaterClarity = photoRepo.getAllPhotosForWaterClarityList()
                )
*/
                val jsonString = json.encodeToString(allData)
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(jsonString.toByteArray())
                }
                _snackBarMessage.emit("Database backed up successfully!")
                true
            } catch (e: Exception) {
                Log.e("ImportViewModel", "Failed to export database", e)
                false
            }
        }
    }
    fun importFullDatabase(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            isImporting = true
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                } ?: throw IllegalStateException("Could not read file from URI")

                val exportData = jsonImport.decodeFromString<FishStoryData>(jsonString)

                if (exportData.fileType == "FishStoryData") {
                    repository.importAllData(exportData)
                    _snackBarMessage.emit("Database restored successfully!")
                } else {
                    _snackBarMessage.emit("Invalid backup file type.")
                }
            } catch (e: Exception) {
                Log.e("ImportViewModel", "Failed to wipe and import database", e)
                _snackBarMessage.emit("An error occurred during import.")
            } finally {
                isImporting = false
            }
        }
    }
}


class ImportViewModelFactory(
    private val repository: FishStoryRepository,
    private val environmentRepository: EnvironmentRepository,
    private val fishermanRepository: FishermanRepository,
    private val fishRepository: FishRepository,
    private val lureRepository: LureRepository,
    private val photoRepository: PhotoRepository,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ImportViewModel(
                repository,
                environmentRepository,
                fishermanRepository,
                fishRepository,
                lureRepository,
                photoRepository,
                tripRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
