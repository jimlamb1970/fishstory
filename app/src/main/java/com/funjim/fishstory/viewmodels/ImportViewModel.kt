package com.funjim.fishstory.viewmodels

import android.content.Context
import android.net.Uri
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
import com.funjim.fishstory.model.SkyCondition
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.TackleBox
import com.funjim.fishstory.model.TackleBoxLure
import com.funjim.fishstory.model.Trip
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStream

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
    val trip: Trip,                                         // Imported
    val event: Event,                                       // Imported
    val baits: List<Bait> = emptyList(),                    // Imported
    val bodiesOfWater: List<BodyOfWater> = emptyList(),     // Imported
    val fishermen: List<Fisherman> = emptyList(),           // Imported
    val limits: List<Limit> = emptyList(),                  // Imported
    val lures: List<Lure> = emptyList(),                    // Imported
    val lureColors: List<LureColor> = emptyList(),          // Imported
    val notes: List<Note> = emptyList(),                    // Imported
    val species: List<Species> = emptyList(),               // Imported
    val skyConditions: List<SkyCondition> = emptyList(),    // Imported
    val tackleBoxes: List<TackleBox> = emptyList(),
    val waterClarities: List<WaterClarity> = emptyList(),   // Imported
    val waterConditions: List<Water> = emptyList(),         // Imported
    val weatherConditions: List<Weather> = emptyList(),     // Imported
    val fish: List<Fish> = emptyList(),

    val eventBaits: List<EventBait> = emptyList(),                  // Imported
    val eventBodiesOfWater: List<EventBodyOfWater> = emptyList(),   // Imported
    val eventFishermen: List<EventFisherman> = emptyList(),
    val eventLimits: List<EventLimit> = emptyList(),
    val eventTargetSpecies: List<EventTargetSpecies> = emptyList(),
    val limitSpecies: List<LimitSpecies> = emptyList(),             // Imported
    val lurePrimaryColors: List<LurePrimaryColor> = emptyList(),    // Imported
    val lureSecondaryColors: List<LureSecondaryColor> = emptyList(),// Imported
    val lureGlowColors: List<LureGlowColor> = emptyList(),          // Imported
    val notesEvent: List<NoteEvent> = emptyList(),
    val notesFish: List<NoteFish> = emptyList(),
    val tackleBoxLures: List<TackleBoxLure> = emptyList(),
)

class ImportViewModel(
    private val repository: FishStoryRepository,
    private val configRepo: ConfigurationRepository,
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
    private val jsonImport = Json { ignoreUnknownKeys = true }

    fun startImport(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            isImporting = true
            try {
                repository.importFromCsv(inputStream)
            } catch (e: Exception) {
            } finally {
                isImporting = false
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
        val skyConditionMap = mutableMapOf<String, String>()
        val speciesMap = mutableMapOf<String, String>()
        val waterClarityMap = mutableMapOf<String, String>()
        val waterMap = mutableMapOf<String, String>()
        val weatherMap = mutableMapOf<String, String>()

        // Map not needed for Trip
        val tripResult = tripRepo.findOrCreateTrip(exportData.trip)
        // Map not needed for Event
        val eventResult = tripRepo.findOrCreateEvent(exportData.event)
        // Map needed for Bait
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

        // Map needed for Body of Water
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

        // Map needed for Fisherman
        for (fisherman in exportData.fishermen) {
            val fishermanResult = fishermanRepo.findOrCreateFisherman(fisherman)
            fishermanMap[fisherman.id] = fishermanResult.item.id
        }
        // Map not needed for Note
        for (note in exportData.notes) {
            val noteResult = tripRepo.findOrCreateNote(note)
        }
        // Map needed for Species
        for (species in exportData.species) {
            val speciesResult = fishRepo.findOrCreateSpecies(species)
            speciesMap[species.id] = speciesResult.item.id
        }
        // Map needed for Limits
        for (limit in exportData.limits) {
            val species: List<String> = exportData.limitSpecies
                .filter { it.limitId == limit.id }
                .mapNotNull { limitSpecies ->
                    speciesMap[limitSpecies.speciesId]
                }
            val limitResult = fishRepo.findOrCreateLimit(limit, species)
            limitMap[limit.id] = limitResult.item.id
        }
        // Map needed for LureColors
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
        // Map needed for SkyCondition
        for (condition in exportData.skyConditions) {
            val conditionResult = envRepo.findOrCreateSkyCondition(condition)
            skyConditionMap[condition.id] = conditionResult.item.id
        }
        // Map needed for WaterClarity
        for (clarity in exportData.waterClarities) {
            val clarityResult = envRepo.findOrCreateWaterClarity(clarity)
            waterClarityMap[clarity.id] = clarityResult.item.id
        }
        for (water in exportData.waterConditions) {
            val clarity = waterClarityMap[water.clarityId]
            val newWater = water.copy(clarityId = clarity)
            val waterResult = envRepo.findOrCreateWaterCondition(water)
            waterMap[water.id] = waterResult.item.id
        }
        for (weather in exportData.weatherConditions) {
            val skyCondition = skyConditionMap[weather.skyConditionId]
            val newWeather = weather.copy(skyConditionId = skyCondition)
            val weatherResult = envRepo.findOrCreateWeatherCondition(newWeather)
            weatherMap[weather.id] = weatherResult.item.id
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
}

class ImportViewModelFactory(
    private val repository: FishStoryRepository,
    private val configurationRepository: ConfigurationRepository,
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
                configurationRepository,
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
