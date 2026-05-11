package com.funjim.fishstory.repository

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Size
import com.funjim.fishstory.database.PhotoDao
import com.funjim.fishstory.model.Photo
import com.funjim.fishstory.model.PhotoEventCrossRef
import com.funjim.fishstory.model.PhotoFishCrossRef
import com.funjim.fishstory.model.PhotoFishermanCrossRef
import com.funjim.fishstory.model.PhotoLureCrossRef
import com.funjim.fishstory.model.PhotoTripCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import androidx.core.graphics.scale
import androidx.core.net.toUri

data class PhotoMetadata(
    val hashcode: String,
    val thumbnail: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PhotoMetadata

        if (hashcode != other.hashcode) return false
        // This is the magic line that compares the actual bytes:
        if (thumbnail != null) {
            if (other.thumbnail == null) return false
            if (!thumbnail.contentEquals(other.thumbnail)) return false
        } else if (other.thumbnail != null) return false

        return true
    }

    override fun hashCode(): Int {
        return (thumbnail?.contentHashCode() ?: 0)
    }
}

class PhotoRepository(
    private val application: Application,
    private val context: Context,
    private val photoDao: PhotoDao
) {
    private fun getRotationAngle(uri: Uri): Float {
        application.contentResolver.openInputStream(uri)?.use { input ->
            val exif = ExifInterface(input)
            return when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }
        return 0f
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        if (angle == 0f) return source
        val matrix = Matrix().apply { postRotate(angle) }
        val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        source.recycle() // Clean up the unrotated one
        return rotated
    }
    private fun generateThumbnail(uri: Uri): ByteArray? {
        val rotation = getRotationAngle(uri) // The helper we discussed
        val bitmap = application.contentResolver.loadThumbnail(uri, Size(512, 512), null)

        val correctedBitmap = if (rotation != 0f) {
            bitmap
        } else {
            bitmap
        }

        val stream = ByteArrayOutputStream()
        correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
        val bytes = stream.toByteArray()

        correctedBitmap.recycle()
        return bytes
    }
    private fun generateFileFingerprint(uri: Uri): String {
        val resolver = application.contentResolver

        val size = resolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) it.getLong(0) else 0L
        } ?: 0L

        // Read only the first 32KB for the hash
        val buffer = ByteArray(32768)
        val bytesRead = resolver.openInputStream(uri)?.use { it.read(buffer, 0, buffer.size) } ?: 0

        val digest = MessageDigest.getInstance("MD5")
        digest.update(buffer, 0, bytesRead)
        digest.update(size.toString().toByteArray()) // Still include size for safety

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    suspend fun getPhotoMetadata(uri: Uri): PhotoMetadata = withContext(Dispatchers.IO) {
        // Generate the MD5 fingerprint
        val metaHash = generateFileFingerprint(uri)

        // Fetch the thumbnail safely (SDK 29+)
        val thumbnail = try {
            generateThumbnail(uri)
        } catch (e: Exception) {
            null
        }

        return@withContext PhotoMetadata(metaHash, thumbnail)
    }

    suspend fun saveScaledPhoto(uri: Uri, metaHash: String): String? = withContext(Dispatchers.IO) {
        try {
            val rotation = getRotationAngle(uri)

            val photoFile = File(application.filesDir, "$metaHash.jpg")

            application.contentResolver.openInputStream(uri)?.use { input ->
                // 1. Decode the image from the stream
                val originalBitmap = BitmapFactory.decodeStream(input)

                // 2. Calculate scaled dimensions (e.g., max width/height of 1920px)
                val corrected = rotateBitmap(originalBitmap, rotation)
                val scaledBitmap = scaleBitmap(corrected, 1920)

                // 3. Save the scaled version to internal storage
                photoFile.outputStream().use { output ->
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
                }

                // Cleanup the bitmaps to free memory
                if (originalBitmap != scaledBitmap) originalBitmap.recycle()
                scaledBitmap.recycle()
            }

            return@withContext photoFile.absolutePath
        } catch (e: IOException) {
            null
        }
    }

    private fun scaleBitmap(source: Bitmap, maxDimension: Int): Bitmap {
        val width = source.width
        val height = source.height

        if (width <= maxDimension && height <= maxDimension) return source

        val aspectRatio = width.toFloat() / height.toFloat()
        val (targetWidth, targetHeight) = if (width > height) {
            maxDimension to (maxDimension / aspectRatio).toInt()
        } else {
            (maxDimension * aspectRatio).toInt() to maxDimension
        }

        return source.scale(targetWidth, targetHeight)
    }
    private fun md5Hash(uri: Uri): String {
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

    suspend fun addPhoto(
        uri: Uri,
        selected: Boolean = true,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)

            val existingId =
                if (selected) photoDao.getPhotoIdByHash(metadata.hashcode)
                else null

            val photoId = if (existingId != null) {
                existingId
            } else {
                val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                    ?: throw IOException("Could not save scaled photo for URI: $uri")

                val photo = Photo(
                    uri = scaledPath,
                    hashcode = metadata.hashcode,
                    thumbnail = metadata.thumbnail
                )
                photoDao.insertPhoto(photo)
                photo.id
            }

            onPhotoReady(photoId)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    suspend fun doesPhotoExist(uri: Uri): Boolean {
        return try {
            val metadata = getPhotoMetadata(uri)

            photoDao.getPhotoIdByHash(metadata.hashcode) != null
        } catch (e: IOException) {
            false
        }
    }
    suspend fun addTripPhoto(tripId: String, uri: Uri, selected: Boolean) =
        addPhoto(uri, selected) { photoId ->
            photoDao.addTripPhoto(PhotoTripCrossRef(photoId, tripId))
        }
    suspend fun deleteTripPhoto(tripId: String, photoId: String) =
        photoDao.deleteTripPhoto(PhotoTripCrossRef(photoId, tripId))
    suspend fun addEventPhoto(eventId: String, uri: Uri, selected: Boolean) =
        addPhoto(uri, selected) { photoId ->
            photoDao.addEventPhoto(PhotoEventCrossRef(photoId, eventId))
        }
    suspend fun deleteEventPhoto(eventId: String, photoId: String) =
        photoDao.deleteEventPhoto(PhotoEventCrossRef(photoId, eventId))

    suspend fun addFishermanPhoto(fishermanId: String, uri: Uri, selected: Boolean) =
        addPhoto(uri, selected) { photoId ->
            photoDao.addFishermanPhoto(PhotoFishermanCrossRef(photoId, fishermanId))
        }

    suspend fun deleteFishermanPhoto(fishermanId: String, photoId: String) =
        photoDao.deleteFishermanPhoto(PhotoFishermanCrossRef(photoId, fishermanId))

    suspend fun addLurePhotos(lureId: String, photos: List<Photo>) {
        photos.forEach { photo ->
            addPhoto(photo.uri.toUri(), !photo.hashcode.isEmpty()) { photoId ->
                photoDao.addLurePhoto(PhotoLureCrossRef(photoId, lureId)).toString()
            }
                .onSuccess { }
                .onFailure { }
        }
    }
    suspend fun deleteLurePhotos(lureId: String, photos: List<Photo>) {
        photos.forEach { photo ->
            photoDao.deleteLurePhoto(PhotoLureCrossRef(photo.id, lureId))
        }
    }
    suspend fun addFishPhotos(fishId: String, photos: List<Photo>) {
        photos.forEach { photo ->
            addPhoto(photo.uri.toUri(), !photo.hashcode.isEmpty()) { photoId ->
                photoDao.addFishPhoto(PhotoFishCrossRef(photoId, fishId)).toString()
            }
                .onSuccess { }
                .onFailure { }
        }
    }
    suspend fun deleteFishPhotos(fishId: String, photos: List<Photo>) {
        photos.forEach { photo ->
            photoDao.deleteFishPhoto(PhotoFishCrossRef(photo.id, fishId))
        }
    }
}
