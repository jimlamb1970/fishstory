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
        val tripResult = tripRepo.findOrCreateTrip(exportData.trip)
        if (!tripResult.wasCreated) {
            _toastMessage.emit("Trip already exists: ${exportData.trip.name}")
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
