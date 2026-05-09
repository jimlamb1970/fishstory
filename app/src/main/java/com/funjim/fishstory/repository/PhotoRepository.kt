package com.funjim.fishstory.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.PhotoEventCrossRef
import com.funjim.fishstory.model.PhotoFishermanCrossRef
import com.funjim.fishstory.model.PhotoLureCrossRef
import com.funjim.fishstory.model.PhotoTripCrossRef
import kotlinx.coroutines.flow.Flow
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.MessageDigest

class PhotoRepository(
    private val context: Context,
    private val photoDao: PhotoDao
) {
    private fun generateThumbnail(uri: Uri): ByteArray {
        val bitmap = context.contentResolver.loadThumbnail(uri, Size(200, 200), null)

        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            bitmap.recycle()
            out.toByteArray()
        }
    }

    fun md5Hash(uri: Uri): String {
        val digest = MessageDigest.getInstance("MD5")
        context.contentResolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(8192)
            var bytes = input.read(buffer)
            while (bytes != -1) {
                digest.update(buffer, 0, bytes)
                bytes = input.read(buffer)
            }
        } ?: throw IOException("Could not open input stream for URI: $uri")

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun getPhotosForTrip(id: String): Flow<List<Photo>> = photoDao.getPhotosForTrip(id)
    fun getPhotosForEvent(id: String): Flow<List<Photo>> = photoDao.getPhotosForEvent(id)
    fun getPhotosForFisherman(id: String): Flow<List<Photo>> = photoDao.getPhotosForFisherman(id)

    suspend fun addTripPhoto(tripId: String, uri: Uri): Result<Unit> {
        return try {
            val hash = md5Hash(uri)
            val existing = photoDao.getPhotoIdByHash(hash)
            if (existing != null) {
                photoDao.addTripPhoto(PhotoTripCrossRef(existing, tripId))
                Result.success(Unit)
            } else {
                val photo =
                    Photo(uri = uri.toString(), hashcode = hash, thumbnail = generateThumbnail(uri))
                photoDao.insertPhoto(photo)
                photoDao.addTripPhoto(PhotoTripCrossRef(photo.id, tripId))

                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    suspend fun deleteTripPhoto(tripId: String, photoId: String) =
        photoDao.deleteTripPhoto(PhotoTripCrossRef(photoId, tripId))

    suspend fun addEventPhoto(eventId: String, uri: Uri): Result<Unit> {
        return try {
            val hash = md5Hash(uri)
            val existing = photoDao.getPhotoIdByHash(hash)
            if (existing != null) {
                photoDao.addEventPhoto(PhotoEventCrossRef(existing, eventId))
                Result.success(Unit)
            } else {
                val photo =
                    Photo(uri = uri.toString(), hashcode = hash, thumbnail = generateThumbnail(uri))
                photoDao.insertPhoto(photo)
                photoDao.addEventPhoto(PhotoEventCrossRef(photo.id, eventId))

                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    suspend fun deleteEventPhoto(eventId: String, photoId: String) =
        photoDao.deleteEventPhoto(PhotoEventCrossRef(photoId, eventId))

    suspend fun addFishermanPhoto(fishermanId: String, uri: Uri): Result<Unit> {
        return try {
            val hash = md5Hash(uri)
            val existing = photoDao.getPhotoIdByHash(hash)
            if (existing != null) {
                photoDao.addFishermanPhoto(PhotoFishermanCrossRef(existing, fishermanId))
                Result.success(Unit)
            } else {
                val photo =
                    Photo(uri = uri.toString(), hashcode = hash, thumbnail = generateThumbnail(uri))
                photoDao.insertPhoto(photo)
                photoDao.addFishermanPhoto(PhotoFishermanCrossRef(photo.id, fishermanId))

                Result.success(Unit)
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    suspend fun deleteFishermanPhoto(fishermanId: String, photoId: String) =
        photoDao.deleteFishermanPhoto(PhotoFishermanCrossRef(photoId, fishermanId))

    suspend fun addLurePhotos(lureId: String, photos: List<Photo>) {
        photos.forEach { photo ->
            val hash = photo.hashcode
            val existing = photoDao.getPhotoIdByHash(hash)
            if (existing != null) {
                photoDao.addLurePhoto(PhotoLureCrossRef(existing, lureId))
            } else {
                photoDao.insertPhoto(photo)
                photoDao.addLurePhoto(PhotoLureCrossRef(photo.id, lureId))
            }
        }
    }

    suspend fun deleteLurePhotos(lureId: String, photos: List<Photo>) {
        photos.forEach { photo ->
            photoDao.deleteLurePhoto(PhotoLureCrossRef(photo.id, lureId))
        }
    }
}
