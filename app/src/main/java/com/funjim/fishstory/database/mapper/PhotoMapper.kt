package com.funjim.fishstory.database.mapper

import com.funjim.fishstory.database.entity.PhotoBaitEntity
import com.funjim.fishstory.database.entity.PhotoBodyOfWaterEntity
import com.funjim.fishstory.database.entity.PhotoEntity
import com.funjim.fishstory.database.entity.PhotoEventEntity
import com.funjim.fishstory.database.entity.PhotoFishEntity
import com.funjim.fishstory.database.entity.PhotoFishermanEntity
import com.funjim.fishstory.database.entity.PhotoLureEntity
import com.funjim.fishstory.database.entity.PhotoSpeciesEntity
import com.funjim.fishstory.database.entity.PhotoTripEntity
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.PhotoBait
import com.funjim.fishstory.model.PhotoBodyOfWater
import com.funjim.fishstory.model.PhotoEvent
import com.funjim.fishstory.model.PhotoFish
import com.funjim.fishstory.model.PhotoFisherman
import com.funjim.fishstory.model.PhotoLure
import com.funjim.fishstory.model.PhotoSpecies
import com.funjim.fishstory.model.PhotoTrip

// Extension: Database Entity -> Domain Model
fun PhotoEntity.toDomain(): Photo {
    return Photo(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption
    )
}

// Extension: List of Entities -> List of Domain Models
fun List<PhotoEntity>.toPhotoDomainList(): List<Photo> {
    return map { it.toDomain() }
}

// Extension: Domain Model -> Database Entity (for Inserts / Updates)
fun Photo.toEntity(): PhotoEntity {
    return PhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption
    )
}

fun PhotoBaitEntity.toDomain(): PhotoBait {
    return PhotoBait(
        photoId = photoId,
        baitId = baitId,
        isPrimary = isPrimary
    )
}
fun List<PhotoBaitEntity>.toPhotoBaitDomainList(): List<PhotoBait> {
    return map { it.toDomain() }
}
fun PhotoBait.toEntity(): PhotoBaitEntity {
    return PhotoBaitEntity(
        photoId = photoId,
        baitId = baitId,
        isPrimary = isPrimary
    )
}

fun PhotoBodyOfWaterEntity.toDomain(): PhotoBodyOfWater {
    return PhotoBodyOfWater(
        photoId = photoId,
        bodyOfWaterId = bodyOfWaterId,
        isPrimary = isPrimary
    )
}
fun List<PhotoBodyOfWaterEntity>.toPhotoBodyOfWaterDomainList(): List<PhotoBodyOfWater>
{
    return map { it.toDomain() }
}
fun PhotoBodyOfWater.toEntity(): PhotoBodyOfWaterEntity
{
    return PhotoBodyOfWaterEntity(
        photoId = photoId,
        bodyOfWaterId = bodyOfWaterId,
        isPrimary = isPrimary
    )
}

fun PhotoEventEntity.toDomain(): PhotoEvent {
    return PhotoEvent(
        photoId = photoId,
        eventId = eventId,
        isPrimary = isPrimary
    )
}
fun List<PhotoEventEntity>.toPhotoEventDomainList(): List<PhotoEvent>
{
    return map { it.toDomain() }
}
fun PhotoEvent.toEntity(): PhotoEventEntity
{
    return PhotoEventEntity(
        photoId = photoId,
        eventId = eventId,
        isPrimary = isPrimary
    )
}

fun PhotoFishEntity.toDomain(): PhotoFish {
    return PhotoFish(
        photoId = photoId,
        fishId = fishId,
        isPrimary = isPrimary
    )
}
fun List<PhotoFishEntity>.toPhotoFishDomainList(): List<PhotoFish>
{
    return map { it.toDomain() }
}
fun PhotoFish.toEntity(): PhotoFishEntity
{
    return PhotoFishEntity(
        photoId = photoId,
        fishId = fishId,
        isPrimary = isPrimary
    )
}

fun PhotoFishermanEntity.toDomain(): PhotoFisherman {
    return PhotoFisherman(
        photoId = photoId,
        fishermanId = fishermanId,
        isPrimary = isPrimary
    )
}
fun List<PhotoFishermanEntity>.toPhotoFishermanDomainList(): List<PhotoFisherman>
{
    return map { it.toDomain() }
}
fun PhotoFisherman.toEntity(): PhotoFishermanEntity
{
    return PhotoFishermanEntity(
        photoId = photoId,
        fishermanId = fishermanId,
        isPrimary = isPrimary
    )
}

fun PhotoLureEntity.toDomain(): PhotoLure {
    return PhotoLure(
        photoId = photoId,
        lureId = lureId,
        isPrimary = isPrimary
    )
}
fun List<PhotoLureEntity>.toPhotoLureDomainList(): List<PhotoLure>
{
    return map { it.toDomain() }
}
fun PhotoLure.toEntity(): PhotoLureEntity
{
    return PhotoLureEntity(
        photoId = photoId,
        lureId = lureId,
        isPrimary = isPrimary
    )
}

fun PhotoSpeciesEntity.toDomain(): PhotoSpecies {
    return PhotoSpecies(
        photoId = photoId,
        speciesId = speciesId,
        isPrimary = isPrimary
    )
}
fun List<PhotoSpeciesEntity>.toPhotoSpeciesDomainList(): List<PhotoSpecies>
{
    return map { it.toDomain() }
}
fun PhotoSpecies.toEntity(): PhotoSpeciesEntity
{
    return PhotoSpeciesEntity(
        photoId = photoId,
        speciesId = speciesId,
        isPrimary = isPrimary
    )
}

fun PhotoTripEntity.toDomain(): PhotoTrip {
    return PhotoTrip(
        photoId = photoId,
        tripId = tripId,
        isPrimary = isPrimary
    )
}
fun List<PhotoTripEntity>.toPhotoTripDomainList(): List<PhotoTrip>
{
    return map { it.toDomain() }
}
fun PhotoTrip.toEntity(): PhotoTripEntity
{
    return PhotoTripEntity(
        photoId = photoId,
        tripId = tripId,
        isPrimary = isPrimary
    )
}