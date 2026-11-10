package com.funjim.fishstory.repository

import androidx.room.withTransaction
import com.funjim.fishstory.database.dao.FishDao
import com.funjim.fishstory.database.dao.FishermanDao
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.database.dao.BaitDao
import com.funjim.fishstory.database.dao.BodyOfWaterDao
import com.funjim.fishstory.database.dao.LureDao
import com.funjim.fishstory.database.dao.EventDao
import com.funjim.fishstory.database.dao.LimitDao
import com.funjim.fishstory.database.dao.NoteDao
import com.funjim.fishstory.database.dao.PhotoDao
import com.funjim.fishstory.database.dao.TackleBoxDao
import com.funjim.fishstory.database.dao.TripDao
import com.funjim.fishstory.database.dao.WaterDao
import com.funjim.fishstory.database.dao.WeatherDao
import com.funjim.fishstory.database.mapper.toBaitDomainList
import com.funjim.fishstory.database.mapper.toBodyOfWaterDomainList
import com.funjim.fishstory.database.mapper.toDomainList
import com.funjim.fishstory.database.mapper.toEntity
import com.funjim.fishstory.database.mapper.toEventBaitDomainList
import com.funjim.fishstory.database.mapper.toEventBodyOfWaterDomainList
import com.funjim.fishstory.database.mapper.toEventDomainList
import com.funjim.fishstory.database.mapper.toEventFishermanDomainList
import com.funjim.fishstory.database.mapper.toEventLimitDomainList
import com.funjim.fishstory.database.mapper.toEventTargetSpeciesDomainList
import com.funjim.fishstory.database.mapper.toFishDomainList
import com.funjim.fishstory.database.mapper.toFishermanDomainList
import com.funjim.fishstory.database.mapper.toLimitDomainList
import com.funjim.fishstory.database.mapper.toLimitSpeciesDomainList
import com.funjim.fishstory.database.mapper.toLureColorDomainList
import com.funjim.fishstory.database.mapper.toLureDomainList
import com.funjim.fishstory.database.mapper.toLureGlowColorDomainList
import com.funjim.fishstory.database.mapper.toLurePrimaryColorDomainList
import com.funjim.fishstory.database.mapper.toLureSecondaryColorDomainList
import com.funjim.fishstory.database.mapper.toNoteDomainList
import com.funjim.fishstory.database.mapper.toNoteEventDomainList
import com.funjim.fishstory.database.mapper.toNoteFishDomainList
import com.funjim.fishstory.database.mapper.toNoteTripDomainList
import com.funjim.fishstory.database.mapper.toPhotoBaitDomainList
import com.funjim.fishstory.database.mapper.toPhotoBodyOfWaterDomainList
import com.funjim.fishstory.database.mapper.toPhotoDomainList
import com.funjim.fishstory.database.mapper.toPhotoEventDomainList
import com.funjim.fishstory.database.mapper.toPhotoFishDomainList
import com.funjim.fishstory.database.mapper.toPhotoFishermanDomainList
import com.funjim.fishstory.database.mapper.toPhotoLureDomainList
import com.funjim.fishstory.database.mapper.toPhotoSkyConditionDomainList
import com.funjim.fishstory.database.mapper.toPhotoSpeciesDomainList
import com.funjim.fishstory.database.mapper.toPhotoTripDomainList
import com.funjim.fishstory.database.mapper.toPhotoWaterClarityDomainList
import com.funjim.fishstory.database.mapper.toSpeciesDomainList
import com.funjim.fishstory.database.mapper.toTackleBoxDomainList
import com.funjim.fishstory.database.mapper.toTackleBoxLureDomainList
import com.funjim.fishstory.database.mapper.toTripBaitDomainList
import com.funjim.fishstory.database.mapper.toTripBodyOfWaterDomainList
import com.funjim.fishstory.database.mapper.toTripDomainList
import com.funjim.fishstory.database.mapper.toTripFishermanDomainList
import com.funjim.fishstory.database.mapper.toTripLimitDomainList
import com.funjim.fishstory.database.mapper.toTripTargetSpeciesDomainList
import com.funjim.fishstory.database.mapper.toWaterDomainList
import com.funjim.fishstory.database.mapper.toWeatherDomainList
import com.funjim.fishstory.viewmodels.FishStoryData

class FishStoryRepository(
    private val db: FishstoryDatabase,
    private val baitDao: BaitDao,
    private val bodyOfWaterDao: BodyOfWaterDao,
    private val eventDao: EventDao,
    private val fishDao: FishDao,
    private val fishermanDao: FishermanDao,
    private val limitDao: LimitDao,
    private val lureDao: LureDao,
    private val noteDao: NoteDao,
    private val photoDao: PhotoDao,
    private val tackleBoxDao: TackleBoxDao,
    private val tripDao: TripDao,
    private val waterDao: WaterDao,
    private val weatherDao: WeatherDao
) {

    // Inside your FishStoryRepository or Database helper
    suspend fun clearEntireDatabase() {
        db.clearAllTables()
    }
    suspend fun exportAllData(): FishStoryData = db.withTransaction {
        FishStoryData(
            // Main Databases (17)
            baits = baitDao.getAllBaitsList().toBaitDomainList(),
            bodiesOfWater = bodyOfWaterDao.getAllBodiesOfWaterList().toBodyOfWaterDomainList(),
            colors = lureDao.getAllLureColorsList().toLureColorDomainList(),
            skyConditions = weatherDao.getAllSkyConditionsList().toDomainList(),
            species = fishDao.getAllSpeciesList().toSpeciesDomainList(),
            waterClarities = waterDao.getAllWaterClarityList().toDomainList(),
            fishermen = fishermanDao.getAllFishermenList().toFishermanDomainList(),
            lures = lureDao.getAllLuresList().toLureDomainList(),
            tackleboxes = tackleBoxDao.getAllTackleBoxesList().toTackleBoxDomainList(),
            trips = tripDao.getAllTripsList().toTripDomainList(),
            events = eventDao.getAllEventsList().toEventDomainList(),
            waterConditions = waterDao.getAllWaterConditionsList().toWaterDomainList(),
            weatherConditions = weatherDao.getAllWeatherConditionsList().toWeatherDomainList(),
            limits = limitDao.getAllLimitsList().toLimitDomainList(),
            fish = fishDao.getAllFishList().toFishDomainList(),
            notes = noteDao.getAllNotesList().toNoteDomainList(),
            photos = photoDao.getAllPhotosList().toPhotoDomainList(),
            // Cross Reference Tables (All of them)
            lurePrimaryColors = lureDao.getAllLurePrimaryColorsList().toLurePrimaryColorDomainList(),
            lureSecondaryColors = lureDao.getAllLureSecondaryColorsList().toLureSecondaryColorDomainList(),
            lureGlowColors = lureDao.getAllLureGlowColorsList().toLureGlowColorDomainList(),
            tackleBoxLures = tackleBoxDao.getAllTackleBoxLuresList().toTackleBoxLureDomainList(),
            limitSpecies = limitDao.getAllLimitSpeciesList().toLimitSpeciesDomainList(),
            tripBaits = baitDao.getAllTripBaitsList().toTripBaitDomainList(),
            tripBodiesOfWater = bodyOfWaterDao.getAllTripBodiesOfWaterList().toTripBodyOfWaterDomainList(),
            tripFishermen = fishermanDao.getAllTripFishermenList().toTripFishermanDomainList(),
            tripLimits = limitDao.getAllTripLimitsList().toTripLimitDomainList(),
            tripTargetSpecies = fishDao.getAllTripTargetSpeciesList().toTripTargetSpeciesDomainList(),
            eventBaits = baitDao.getAllEventBaitsList().toEventBaitDomainList(),
            eventBodiesOfWater = bodyOfWaterDao.getAllEventBodiesOfWaterList().toEventBodyOfWaterDomainList(),
            eventFishermen = fishermanDao.getAllEventFishermenList().toEventFishermanDomainList(),
            eventLimits = limitDao.getAllEventLimitsList().toEventLimitDomainList(),
            eventTargetSpecies = fishDao.getAllEventTargetSpeciesList().toEventTargetSpeciesDomainList(),
            notesEvent = noteDao.getAllNotesEventList().toNoteEventDomainList(),
            notesFish = noteDao.getAllNotesFishList().toNoteFishDomainList(),
            notesTrip = noteDao.getAllNotesTripList().toNoteTripDomainList(),
            photosBait = photoDao.getAllPhotosForBaitList().toPhotoBaitDomainList(),
            photosBodyOfWater = photoDao.getAllPhotosForBodiesOfWaterList().toPhotoBodyOfWaterDomainList(),
            photosEvent = photoDao.getAllPhotosForEventsList().toPhotoEventDomainList(),
            photosFish = photoDao.getAllPhotosForFishList().toPhotoFishDomainList(),
            photosFisherman = photoDao.getAllPhotosForFishermenList().toPhotoFishermanDomainList(),
            photosLure = photoDao.getAllPhotosForLuresList().toPhotoLureDomainList(),
            photosSkyCondition = photoDao.getAllPhotosForSkyConditionsList().toPhotoSkyConditionDomainList(),
            photosSpecies = photoDao.getAllPhotosForSpeciesList().toPhotoSpeciesDomainList(),
            photosTrip = photoDao.getAllPhotosForTripsList().toPhotoTripDomainList(),
            photosWaterClarity = photoDao.getAllPhotosForWaterClarityList().toPhotoWaterClarityDomainList()
        )
    }

    suspend fun importAllData(exportData: FishStoryData) {
        db.withTransaction {
            db.clearAllTables()

            // 2. Insert Independent & Dependent Lookups
            for (color in exportData.colors) lureDao.insertLureColor(color.toEntity())
            for (species in exportData.species) fishDao.insertSpecies(species.toEntity())
            for (condition in exportData.skyConditions) weatherDao.insertSkyCondition(condition.toEntity())
            for (clarity in exportData.waterClarities) waterDao.insertWaterClarity(clarity.toEntity())
            for (bait in exportData.baits) baitDao.insertBait(bait.toEntity())

            // 3. Insert Environmental Conditions & Core Entities
            for (water in exportData.waterConditions) waterDao.insertWater(water.toEntity())
            for (weather in exportData.weatherConditions) weatherDao.insertWeather(weather.toEntity())
            for (bow in exportData.bodiesOfWater) bodyOfWaterDao.insertBodyOfWater(bow.toEntity())
            for (fisherman in exportData.fishermen) fishermanDao.insertFisherman(fisherman.toEntity())
            for (tackleBox in exportData.tackleboxes) tackleBoxDao.insertTackleBox(tackleBox.toEntity())

            // 4. Insert Lures & Limits
            for (lure in exportData.lures) lureDao.insertLure(lure.toEntity())
            for (limit in exportData.limits) limitDao.insertLimit(limit.toEntity())

            // 5. Insert Trips, Events, & Transactional Data (Fish, Notes, Photos)
            for (trip in exportData.trips) tripDao.insertTrip(trip.toEntity())
            for (event in exportData.events) eventDao.insertEvent(event.toEntity())
            for (fish in exportData.fish) fishDao.insertFish(fish.toEntity())
            for (note in exportData.notes) noteDao.insertNote(note.toEntity())
            for (photo in exportData.photos) photoDao.insertPhoto(photo.toEntity())

            // 6. Insert All Cross-Reference / Join Tables Last
            for (item in exportData.lurePrimaryColors) lureDao.upsertLurePrimaryColor(item.toEntity())
            for (item in exportData.lureSecondaryColors) lureDao.upsertLureSecondaryColor(item.toEntity())
            for (item in exportData.lureGlowColors) lureDao.upsertLureGlowColor(item.toEntity())
            for (item in exportData.tackleBoxLures) tackleBoxDao.upsertTackleBoxLure(item.toEntity())
            for (item in exportData.limitSpecies) limitDao.insertLimitSpecies(item.toEntity())

            for (item in exportData.tripBaits) baitDao.upsertTripBait(item.toEntity())
            for (item in exportData.tripBodiesOfWater) bodyOfWaterDao.upsertTripBodyOfWater(item.toEntity())
            for (item in exportData.tripFishermen) fishermanDao.insertTripFisherman(item.toEntity())
            for (item in exportData.tripLimits) limitDao.insertTripLimit(item.toEntity())
            for (item in exportData.tripTargetSpecies) tripDao.upsertTripTargetSpecies(item.toEntity())

            for (item in exportData.eventBaits) baitDao.upsertEventBait(item.toEntity())
            for (item in exportData.eventBodiesOfWater) bodyOfWaterDao.upsertEventBodyOfWater(item.toEntity())
            for (item in exportData.eventFishermen) fishermanDao.insertEventFisherman(item.toEntity())
            for (item in exportData.eventLimits) limitDao.insertEventLimit(item.toEntity())
            for (item in exportData.eventTargetSpecies) eventDao.upsertEventTargetSpecies(item.toEntity())

            for (item in exportData.notesEvent) noteDao.upsertEventNote(item.toEntity())
            for (item in exportData.notesFish) noteDao.upsertFishNote(item.toEntity())
            for (item in exportData.notesTrip) noteDao.upsertTripNote(item.toEntity())

            for (item in exportData.photosBait) photoDao.addBaitPhoto(item.toEntity())
            for (item in exportData.photosBodyOfWater) photoDao.addBodyOfWaterPhoto(item.toEntity())
            for (item in exportData.photosEvent) photoDao.addEventPhoto(item.toEntity())
            for (item in exportData.photosFish) photoDao.addFishPhoto(item.toEntity())
            for (item in exportData.photosFisherman) photoDao.addFishermanPhoto(item.toEntity())
            for (item in exportData.photosLure) photoDao.addLurePhoto(item.toEntity())
            for (item in exportData.photosSkyCondition) photoDao.addSkyConditionPhoto(item.toEntity())
            for (item in exportData.photosSpecies) photoDao.addSpeciesPhoto(item.toEntity())
            for (item in exportData.photosTrip) photoDao.addTripPhoto(item.toEntity())
            for (item in exportData.photosWaterClarity) photoDao.addWaterClarityPhoto(item.toEntity())
        }
    }
}
