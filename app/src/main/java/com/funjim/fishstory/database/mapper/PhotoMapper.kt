package com.funjim.fishstory.database.mapper

import com.funjim.fishstory.database.entity.BaitPhotoEntity
import com.funjim.fishstory.database.entity.BodyOfWaterPhotoEntity
import com.funjim.fishstory.database.entity.EventPhotoEntity
import com.funjim.fishstory.database.entity.FishPhotoEntity
import com.funjim.fishstory.database.entity.FishermanPhotoEntity
import com.funjim.fishstory.database.entity.LurePhotoEntity
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
import com.funjim.fishstory.model.SkyConditionPhoto
import com.funjim.fishstory.model.SpeciesPhoto
import com.funjim.fishstory.model.TripPhoto
import com.funjim.fishstory.model.WaterClarityPhoto

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
