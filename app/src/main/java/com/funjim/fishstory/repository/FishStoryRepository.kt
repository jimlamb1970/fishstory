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
import com.funjim.fishstory.database.dao.SpeciesDao
import com.funjim.fishstory.database.dao.TackleBoxDao
import com.funjim.fishstory.database.dao.TripDao
import com.funjim.fishstory.database.dao.WaterDao
import com.funjim.fishstory.database.dao.WeatherDao
import com.funjim.fishstory.database.entity.BaitPhotoEntity
import com.funjim.fishstory.database.entity.BodyOfWaterPhotoEntity
import com.funjim.fishstory.database.entity.EventNoteEntity
import com.funjim.fishstory.database.entity.EventPhotoEntity
import com.funjim.fishstory.database.entity.FishNoteEntity
import com.funjim.fishstory.database.entity.FishPhotoEntity
import com.funjim.fishstory.database.entity.FishermanPhotoEntity
import com.funjim.fishstory.database.entity.LurePhotoEntity
import com.funjim.fishstory.database.entity.SkyConditionPhotoEntity
import com.funjim.fishstory.database.entity.SpeciesPhotoEntity
import com.funjim.fishstory.database.entity.TripNoteEntity
import com.funjim.fishstory.database.entity.TripPhotoEntity
import com.funjim.fishstory.database.entity.WaterClarityPhotoEntity
import com.funjim.fishstory.database.mapper.toBaitDomainList
import com.funjim.fishstory.database.mapper.toBaitPhotoDomainList
import com.funjim.fishstory.database.mapper.toBodyOfWaterDomainList
import com.funjim.fishstory.database.mapper.toBodyOfWaterPhotoDomainList
import com.funjim.fishstory.database.mapper.toDomainList
import com.funjim.fishstory.database.mapper.toEntity
import com.funjim.fishstory.database.mapper.toEventBaitDomainList
import com.funjim.fishstory.database.mapper.toEventBodyOfWaterDomainList
import com.funjim.fishstory.database.mapper.toEventDomainList
import com.funjim.fishstory.database.mapper.toEventFishermanDomainList
import com.funjim.fishstory.database.mapper.toEventLimitDomainList
import com.funjim.fishstory.database.mapper.toEventNoteDomainList
import com.funjim.fishstory.database.mapper.toEventPhotoDomainList
import com.funjim.fishstory.database.mapper.toEventTargetSpeciesDomainList
import com.funjim.fishstory.database.mapper.toFishDomainList
import com.funjim.fishstory.database.mapper.toFishNoteDomainList
import com.funjim.fishstory.database.mapper.toFishPhotoDomainList
import com.funjim.fishstory.database.mapper.toFishermanDomainList
import com.funjim.fishstory.database.mapper.toFishermanPhotoDomainList
import com.funjim.fishstory.database.mapper.toLimitDomainList
import com.funjim.fishstory.database.mapper.toLimitSpeciesDomainList
import com.funjim.fishstory.database.mapper.toLureColorDomainList
import com.funjim.fishstory.database.mapper.toLureDomainList
import com.funjim.fishstory.database.mapper.toLureGlowColorDomainList
import com.funjim.fishstory.database.mapper.toLurePhotoDomainList
import com.funjim.fishstory.database.mapper.toLurePrimaryColorDomainList
import com.funjim.fishstory.database.mapper.toLureSecondaryColorDomainList
import com.funjim.fishstory.database.mapper.toSkyConditionPhotoDomainList
import com.funjim.fishstory.database.mapper.toSpeciesDomainList
import com.funjim.fishstory.database.mapper.toSpeciesPhotoDomainList
import com.funjim.fishstory.database.mapper.toTackleBoxDomainList
import com.funjim.fishstory.database.mapper.toTackleBoxLureDomainList
import com.funjim.fishstory.database.mapper.toTripBaitDomainList
import com.funjim.fishstory.database.mapper.toTripBodyOfWaterDomainList
import com.funjim.fishstory.database.mapper.toTripDomainList
import com.funjim.fishstory.database.mapper.toTripFishermanDomainList
import com.funjim.fishstory.database.mapper.toTripLimitDomainList
import com.funjim.fishstory.database.mapper.toTripNoteDomainList
import com.funjim.fishstory.database.mapper.toTripPhotoDomainList
import com.funjim.fishstory.database.mapper.toTripTargetSpeciesDomainList
import com.funjim.fishstory.database.mapper.toWaterClarityPhotoDomainList
import com.funjim.fishstory.database.mapper.toWaterDomainList
import com.funjim.fishstory.database.mapper.toWeatherDomainList
import com.funjim.fishstory.model.Photo
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
    private val speciesDao: SpeciesDao,
    private val tackleBoxDao: TackleBoxDao,
    private val tripDao: TripDao,
    private val waterDao: WaterDao,
    private val weatherDao: WeatherDao
) {
    suspend fun exportAllData(): FishStoryData = db.withTransaction {
        FishStoryData(
            // Main Databases (17)
            baits = baitDao.getAllBaitsList().toBaitDomainList(),
            bodiesOfWater = bodyOfWaterDao.getAllBodiesOfWaterList().toBodyOfWaterDomainList(),
            colors = lureDao.getAllLureColorsList().toLureColorDomainList(),
            skyConditions = weatherDao.getAllSkyConditionsList().toDomainList(),
            species = speciesDao.getAllSpeciesList().toSpeciesDomainList(),
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
            eventNotes = noteDao.getAllEventNotesList().toEventNoteDomainList(),
            fishNotes = noteDao.getAllFishNotesList().toFishNoteDomainList(),
            tripNotes = noteDao.getAllTripNotesList().toTripNoteDomainList(),
            // Cross Reference Tables (All of them)
            lurePrimaryColors = lureDao.getAllLurePrimaryColorsList().toLurePrimaryColorDomainList(),
            lureSecondaryColors = lureDao.getAllLureSecondaryColorsList().toLureSecondaryColorDomainList(),
            lureGlowColors = lureDao.getAllLureGlowColorsList().toLureGlowColorDomainList(),
            tackleBoxLures = tackleBoxDao.getAllTackleBoxLuresList().toTackleBoxLureDomainList(),
            limitSpecies = speciesDao.getAllLimitSpeciesList().toLimitSpeciesDomainList(),
            tripBaits = baitDao.getAllTripBaitsList().toTripBaitDomainList(),
            tripBodiesOfWater = bodyOfWaterDao.getAllTripBodiesOfWaterList().toTripBodyOfWaterDomainList(),
            tripFishermen = fishermanDao.getAllTripFishermenList().toTripFishermanDomainList(),
            tripLimits = limitDao.getAllTripLimitsList().toTripLimitDomainList(),
            tripTargetSpecies = speciesDao.getAllTripTargetSpeciesList().toTripTargetSpeciesDomainList(),
            eventBaits = baitDao.getAllEventBaitsList().toEventBaitDomainList(),
            eventBodiesOfWater = bodyOfWaterDao.getAllEventBodiesOfWaterList().toEventBodyOfWaterDomainList(),
            eventFishermen = fishermanDao.getAllEventFishermenList().toEventFishermanDomainList(),
            eventLimits = limitDao.getAllEventLimitsList().toEventLimitDomainList(),
            eventTargetSpecies = speciesDao.getAllEventTargetSpeciesList().toEventTargetSpeciesDomainList(),
            baitPhotos = photoDao.getAllBaitPhotosList().toBaitPhotoDomainList(),
            bodyOfWaterPhotos = photoDao.getAllBodyOfWaterPhotosList().toBodyOfWaterPhotoDomainList(),
            eventPhotos = photoDao.getAllEventPhotosList().toEventPhotoDomainList(),
            fishPhotos = photoDao.getAllFishPhotosList().toFishPhotoDomainList(),
            fishermanPhotos = photoDao.getAllFishermanPhotosList().toFishermanPhotoDomainList(),
            lurePhotos = photoDao.getAllLurePhotosList().toLurePhotoDomainList(),
            skyConditionPhotos = photoDao.getAllSkyConditionPhotosList().toSkyConditionPhotoDomainList(),
            speciesPhotos = photoDao.getAllSpeciesPhotosList().toSpeciesPhotoDomainList(),
            tripPhotos = photoDao.getAllTripPhotosList().toTripPhotoDomainList(),
            waterClarityPhotos = photoDao.getAllWaterClarityPhotosList().toWaterClarityPhotoDomainList()
        )
    }

    suspend fun importAllData(exportData: FishStoryData) {
        db.withTransaction {
            db.clearAllTables()

            // 1. Insert Independent & Dependent Lookups
            for (color in exportData.colors) lureDao.insertLureColor(color.toEntity())
            for (species in exportData.species) speciesDao.insertSpecies(species.toEntity())
            for (condition in exportData.skyConditions) weatherDao.insertSkyCondition(condition.toEntity())
            for (clarity in exportData.waterClarities) waterDao.insertWaterClarity(clarity.toEntity())
            for (bait in exportData.baits) baitDao.insertBait(bait.toEntity())

            // 2. Insert Environmental Conditions & Core Entities
            for (water in exportData.waterConditions) waterDao.insertWater(water.toEntity())
            for (weather in exportData.weatherConditions) weatherDao.insertWeather(weather.toEntity())
            for (bow in exportData.bodiesOfWater) bodyOfWaterDao.insertBodyOfWater(bow.toEntity())
            for (fisherman in exportData.fishermen) fishermanDao.insertFisherman(fisherman.toEntity())
            for (tackleBox in exportData.tackleboxes) tackleBoxDao.insertTackleBox(tackleBox.toEntity())

            // 3. Insert Lures & Limits
            for (lure in exportData.lures) lureDao.insertLure(lure.toEntity())
            for (limit in exportData.limits) limitDao.insertLimit(limit.toEntity())

            // 4. Insert Trips, Events, & Transactional Data (Fish, Notes, Photos)
            for (trip in exportData.trips) tripDao.insertTrip(trip.toEntity())
            for (event in exportData.events) eventDao.insertEvent(event.toEntity())
            for (fish in exportData.fish) fishDao.insertFish(fish.toEntity())

            for (note in exportData.eventNotes) noteDao.insertEventNote(note.toEntity())
            for (note in exportData.fishNotes) noteDao.insertFishNote(note.toEntity())
            for (note in exportData.tripNotes) noteDao.insertTripNote(note.toEntity())

            for (photo in exportData.baitPhotos) photoDao.insertBaitPhoto(photo.toEntity())
            for (photo in exportData.bodyOfWaterPhotos) photoDao.insertBodyOfWaterPhoto(photo.toEntity())
            for (photo in exportData.eventPhotos) photoDao.insertEventPhoto(photo.toEntity())
            for (photo in exportData.fishPhotos) photoDao.insertFishPhoto(photo.toEntity())
            for (photo in exportData.fishermanPhotos) photoDao.insertFishermanPhoto(photo.toEntity())
            for (photo in exportData.lurePhotos) photoDao.insertLurePhoto(photo.toEntity())
            for (photo in exportData.skyConditionPhotos) photoDao.insertSkyConditionPhoto(photo.toEntity())
            for (photo in exportData.speciesPhotos) photoDao.insertSpeciesPhoto(photo.toEntity())
            for (photo in exportData.tripPhotos) photoDao.insertTripPhoto(photo.toEntity())
            for (photo in exportData.waterClarityPhotos) photoDao.insertWaterClarityPhoto(photo.toEntity())

            // 5. Insert All Cross-Reference / Join Tables Last
            for (item in exportData.lurePrimaryColors) lureDao.upsertLurePrimaryColor(item.toEntity())
            for (item in exportData.lureSecondaryColors) lureDao.upsertLureSecondaryColor(item.toEntity())
            for (item in exportData.lureGlowColors) lureDao.upsertLureGlowColor(item.toEntity())
            for (item in exportData.tackleBoxLures) tackleBoxDao.upsertTackleBoxLure(item.toEntity())
            for (item in exportData.limitSpecies) speciesDao.insertLimitSpecies(item.toEntity())

            for (item in exportData.tripBaits) baitDao.upsertTripBait(item.toEntity())
            for (item in exportData.tripBodiesOfWater) bodyOfWaterDao.upsertTripBodyOfWater(item.toEntity())
            for (item in exportData.tripFishermen) fishermanDao.insertTripFisherman(item.toEntity())
            for (item in exportData.tripLimits) limitDao.insertTripLimit(item.toEntity())
            for (item in exportData.tripTargetSpecies) speciesDao.upsertTripTargetSpecies(item.toEntity())

            for (item in exportData.eventBaits) baitDao.upsertEventBait(item.toEntity())
            for (item in exportData.eventBodiesOfWater) bodyOfWaterDao.upsertEventBodyOfWater(item.toEntity())
            for (item in exportData.eventFishermen) fishermanDao.insertEventFisherman(item.toEntity())
            for (item in exportData.eventLimits) limitDao.insertEventLimit(item.toEntity())
            for (item in exportData.eventTargetSpecies) speciesDao.upsertEventTargetSpecies(item.toEntity())
        }
    }
}
