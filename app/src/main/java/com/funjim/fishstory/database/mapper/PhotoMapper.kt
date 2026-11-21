package com.funjim.fishstory.database.mapper

import android.widget.GridLayout
import com.funjim.fishstory.database.entity.BaitPhotoEntity
import com.funjim.fishstory.database.entity.BodyOfWaterPhotoEntity
import com.funjim.fishstory.database.entity.EventPhotoEntity
import com.funjim.fishstory.database.entity.FishPhotoEntity
import com.funjim.fishstory.database.entity.FishermanPhotoEntity
import com.funjim.fishstory.database.entity.LurePhotoEntity
import com.funjim.fishstory.database.entity.PhotoBaitEntity
import com.funjim.fishstory.database.entity.PhotoBodyOfWaterEntity
import com.funjim.fishstory.database.entity.PhotoEntity
import com.funjim.fishstory.database.entity.PhotoEventEntity
import com.funjim.fishstory.database.entity.PhotoFishEntity
import com.funjim.fishstory.database.entity.PhotoFishermanEntity
import com.funjim.fishstory.database.entity.PhotoLureEntity
import com.funjim.fishstory.database.entity.PhotoSkyConditionEntity
import com.funjim.fishstory.database.entity.PhotoSpeciesEntity
import com.funjim.fishstory.database.entity.PhotoTripEntity
import com.funjim.fishstory.database.entity.PhotoWaterClarityEntity
import com.funjim.fishstory.database.entity.SkyConditionPhotoEntity
import com.funjim.fishstory.database.entity.SpeciesPhotoEntity
import com.funjim.fishstory.database.entity.TripPhotoEntity
import com.funjim.fishstory.database.entity.WaterClarityPhotoEntity
import com.funjim.fishstory.model.BaitPhoto
import com.funjim.fishstory.model.BodyOfWaterPhoto
import com.funjim.fishstory.model.EventPhoto
import com.funjim.fishstory.model.FishPhoto
import com.funjim.fishstory.model.FishermanPhoto
import com.funjim.fishstory.model.LurePhoto
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
import com.funjim.fishstory.model.SkyConditionPhoto
import com.funjim.fishstory.model.SpeciesPhoto
import com.funjim.fishstory.model.TripPhoto
import com.funjim.fishstory.model.WaterClarityPhoto

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

fun PhotoSkyConditionEntity.toDomain(): PhotoSkyCondition {
    return PhotoSkyCondition(
        photoId = photoId,
        skyConditionId = skyConditionId,
        isPrimary = isPrimary
    )
}
fun List<PhotoSkyConditionEntity>.toPhotoSkyConditionDomainList(): List<PhotoSkyCondition>
{
    return map { it.toDomain() }
}
fun PhotoSkyCondition.toEntity(): PhotoSkyConditionEntity
{
    return PhotoSkyConditionEntity(
        photoId = photoId,
        skyConditionId = skyConditionId,
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

fun PhotoWaterClarityEntity.toDomain(): PhotoWaterClarity {
    return PhotoWaterClarity(
        photoId = photoId,
        waterClarityId = waterClarityId,
        isPrimary = isPrimary
    )
}
fun List<PhotoWaterClarityEntity>.toPhotoWaterClarityDomainList(): List<PhotoWaterClarity>
{
    return map { it.toDomain() }
}
fun PhotoWaterClarity.toEntity(): PhotoWaterClarityEntity
{
    return PhotoWaterClarityEntity(
        photoId = photoId,
        waterClarityId = waterClarityId,
        isPrimary = isPrimary
    )
}

fun BaitPhotoEntity.toDomain(): BaitPhoto {
    return BaitPhoto(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
fun List<BaitPhotoEntity>.toBaitPhotoDomainList(): List<BaitPhoto> {
    return map { it.toDomain() }
}
fun BaitPhoto.toEntity(): BaitPhotoEntity {
    return BaitPhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}

fun BodyOfWaterPhotoEntity.toDomain(): BodyOfWaterPhoto {
    return BodyOfWaterPhoto(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
fun List<BodyOfWaterPhotoEntity>.toBodyOfWaterPhotoDomainList(): List<BodyOfWaterPhoto> {
    return map { it.toDomain() }
}
fun BodyOfWaterPhoto.toEntity(): BodyOfWaterPhotoEntity {
    return BodyOfWaterPhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}

fun EventPhotoEntity.toDomain(): EventPhoto {
    return EventPhoto(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
fun List<EventPhotoEntity>.toEventPhotoDomainList(): List<EventPhoto> {
    return map { it.toDomain() }
}
fun EventPhoto.toEntity(): EventPhotoEntity {
    return EventPhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}

fun FishPhotoEntity.toDomain(): FishPhoto {
    return FishPhoto(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
fun List<FishPhotoEntity>.toFishPhotoDomainList(): List<FishPhoto> {
    return map { it.toDomain() }
}
fun FishPhoto.toEntity(): FishPhotoEntity {
    return FishPhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}

fun FishermanPhotoEntity.toDomain(): FishermanPhoto {
    return FishermanPhoto(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
fun List<FishermanPhotoEntity>.toFishermanPhotoDomainList(): List<FishermanPhoto> {
    return map { it.toDomain() }
}
fun FishermanPhoto.toEntity(): FishermanPhotoEntity {
    return FishermanPhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}

fun LurePhotoEntity.toDomain(): LurePhoto {
    return LurePhoto(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
fun List<LurePhotoEntity>.toLurePhotoDomainList(): List<LurePhoto> {
    return map { it.toDomain() }
}
fun LurePhoto.toEntity(): LurePhotoEntity {
    return LurePhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}

fun SkyConditionPhotoEntity.toDomain(): SkyConditionPhoto {
    return SkyConditionPhoto(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
fun List<SkyConditionPhotoEntity>.toSkyConditionPhotoDomainList(): List<SkyConditionPhoto> {
    return map { it.toDomain() }
}
fun SkyConditionPhoto.toEntity(): SkyConditionPhotoEntity {
    return SkyConditionPhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}

fun SpeciesPhotoEntity.toDomain(): SpeciesPhoto {
    return SpeciesPhoto(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
fun List<SpeciesPhotoEntity>.toSpeciesPhotoDomainList(): List<SpeciesPhoto> {
    return map { it.toDomain() }
}
fun SpeciesPhoto.toEntity(): SpeciesPhotoEntity {
    return SpeciesPhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}

fun TripPhotoEntity.toDomain(): TripPhoto {
    return TripPhoto(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
fun List<TripPhotoEntity>.toTripPhotoDomainList(): List<TripPhoto> {
    return map { it.toDomain() }
}
fun TripPhoto.toEntity(): TripPhotoEntity {
    return TripPhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}

fun WaterClarityPhotoEntity.toDomain(): WaterClarityPhoto {
    return WaterClarityPhoto(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
fun List<WaterClarityPhotoEntity>.toWaterClarityPhotoDomainList(): List<WaterClarityPhoto> {
    return map { it.toDomain() }
}
fun WaterClarityPhoto.toEntity(): WaterClarityPhotoEntity {
    return WaterClarityPhotoEntity(
        id = id,
        uri = uri,
        hashcode = hashcode,
        thumbnail = thumbnail,
        timestamp = timestamp,
        caption = caption,
        isPrimary = isPrimary,
        parentId = parentId
    )
}
