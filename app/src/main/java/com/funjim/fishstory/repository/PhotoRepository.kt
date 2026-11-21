package com.funjim.fishstory.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import android.util.Size
import com.funjim.fishstory.database.dao.PhotoDao
import com.funjim.fishstory.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.room.withTransaction
import com.funjim.fishstory.database.FishstoryDatabase
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
import com.funjim.fishstory.database.mapper.toBaitPhotoDomainList
import com.funjim.fishstory.database.mapper.toBodyOfWaterPhotoDomainList
import com.funjim.fishstory.database.mapper.toEventPhotoDomainList
import com.funjim.fishstory.database.mapper.toFishPhotoDomainList
import com.funjim.fishstory.database.mapper.toFishermanPhotoDomainList
import com.funjim.fishstory.database.mapper.toLurePhotoDomainList
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
import com.funjim.fishstory.database.mapper.toSkyConditionPhotoDomainList
import com.funjim.fishstory.database.mapper.toSpeciesPhotoDomainList
import com.funjim.fishstory.database.mapper.toTripPhotoDomainList
import com.funjim.fishstory.database.mapper.toWaterClarityPhotoDomainList
import com.funjim.fishstory.model.BaitPhoto
import com.funjim.fishstory.model.BodyOfWaterPhoto
import com.funjim.fishstory.model.EventPhoto
import com.funjim.fishstory.model.FishPhoto
import com.funjim.fishstory.model.FishermanPhoto
import com.funjim.fishstory.model.LurePhoto
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
import kotlinx.coroutines.flow.map

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
    private val database: FishstoryDatabase,
    private val context: Context,
    private val photoDao: PhotoDao
) {
    suspend fun getAllBaitPhotosList(): List<BaitPhoto> {
        return photoDao.getAllBaitPhotosList().toBaitPhotoDomainList()
    }
    suspend fun getAllBodyOfWaterPhotosList(): List<BodyOfWaterPhoto> {
        return photoDao.getAllBodyOfWaterPhotosList().toBodyOfWaterPhotoDomainList()
    }
    suspend fun getAllEventPhotosList(): List<EventPhoto> {
        return photoDao.getAllEventPhotosList().toEventPhotoDomainList()
    }
    suspend fun getAllFishPhotosList(): List<FishPhoto> {
        return photoDao.getAllFishPhotosList().toFishPhotoDomainList()
    }
    suspend fun getAllFishermanPhotosList(): List<FishermanPhoto> {
        return photoDao.getAllFishermanPhotosList().toFishermanPhotoDomainList()
    }
    suspend fun getAllLurePhotosList(): List<LurePhoto> {
        return photoDao.getAllLurePhotosList().toLurePhotoDomainList()
    }
    suspend fun getAllSkyConditionPhotosList(): List<SkyConditionPhoto> {
        return photoDao.getAllSkyConditionPhotosList().toSkyConditionPhotoDomainList()
    }
    suspend fun getAllSpeciesPhotosList(): List<SpeciesPhoto> {
        return photoDao.getAllSpeciesPhotosList().toSpeciesPhotoDomainList()
    }
    suspend fun getAllTripPhotosList(): List<TripPhoto> {
        return photoDao.getAllTripPhotosList().toTripPhotoDomainList()
    }
    suspend fun getAllWaterClarityPhotosList(): List<WaterClarityPhoto> {
        return photoDao.getAllWaterClarityPhotosList().toWaterClarityPhotoDomainList()
    }

    private fun getRotationAngle(uri: Uri): Float {
        context.contentResolver.openInputStream(uri)?.use { input ->
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
//        val bitmap = context.contentResolver.loadThumbnail(uri, Size(512, 512), null)
        val bitmap = try {
            // Try local first
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.loadThumbnail(uri, Size(200, 200), null)
            } else {
                decodeScaledBitmap(uri)
            }
        } catch (e: Exception) {
            // Fall back to manual decode for cloud URIs
            decodeScaledBitmap(uri)
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
        val bytes = stream.toByteArray()

        bitmap.recycle()
        return bytes
    }
    private fun decodeScaledBitmap(uri: Uri): Bitmap {
        val rotation = getRotationAngle(uri)

        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Could not open stream for URI: $uri")

        return input.use {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(it, null, options)

            options.inSampleSize = calculateSampleSize(options, 200, 200)
            options.inJustDecodeBounds = false

            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream, null, options) ?: throw IOException(
                    "Could not decode bitmap for URI: $uri"
                )
                rotateBitmap(bitmap, rotation)
            } ?: throw IOException("Could not decode bitmap for URI: $uri")
        }
    }

    private fun calculateSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.run { outHeight to outWidth }
        var sampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / sampleSize >= reqHeight && halfWidth / sampleSize >= reqWidth) {
                sampleSize *= 2
            }
        }
        return sampleSize
    }
    private fun generateFileFingerprint(uri: Uri): String {
        val resolver = context.contentResolver

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

            val photoFile = File(context.filesDir, "$metaHash.jpg")

            context.contentResolver.openInputStream(uri)?.use { input ->
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
        } catch (_: IOException) {
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

    fun getPhotosForTrip(id: String): Flow<List<TripPhoto>> {
        return photoDao.getTripPhotos(id).map { list -> list.toTripPhotoDomainList() }
    }
    fun getPhotosForEvent(id: String): Flow<List<EventPhoto>> {
        return photoDao.getEventPhotos(id).map { list -> list.toEventPhotoDomainList() }
    }
    fun getPhotosForFisherman(id: String): Flow<List<FishermanPhoto>> {
        return photoDao.getFishermanPhotos(id).map { list -> list.toFishermanPhotoDomainList() }
    }
    fun getPhotosForFish(id: String): Flow<List<FishPhoto>> {
        return photoDao.getFishPhotos(id).map { list -> list.toFishPhotoDomainList() }
    }
    fun getPhotosForLure(id: String): Flow<List<LurePhoto>> {
        return photoDao.getLurePhotos(id).map { list -> list.toLurePhotoDomainList() }
    }

    suspend fun addBaitPhoto(
        baitId: String,
        uri: Uri,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)
            Log.d("PhotoRepository", "Photo metadata: ${metadata.hashcode}")

            val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                ?: throw IOException("Could not save scaled photo for URI: $uri")

            val photo = BaitPhotoEntity(
                uri = scaledPath,
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = baitId
            )
            photoDao.insertBaitPhoto(photo)

            onPhotoReady(photo.id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    suspend fun addBodyOfWaterPhoto(
        bodyOfWaterId: String,
        uri: Uri,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)
            Log.d("PhotoRepository", "Photo metadata: ${metadata.hashcode}")

            val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                ?: throw IOException("Could not save scaled photo for URI: $uri")

            val photo = BodyOfWaterPhotoEntity(
                uri = scaledPath,
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = bodyOfWaterId
            )
            photoDao.insertBodyOfWaterPhoto(photo)

            onPhotoReady(photo.id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    suspend fun addEventPhoto(
        eventId: String,
        uri: Uri,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)
            Log.d("PhotoRepository", "Photo metadata: ${metadata.hashcode}")

            val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                ?: throw IOException("Could not save scaled photo for URI: $uri")

            val photo = EventPhotoEntity(
                uri = scaledPath,
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = eventId
            )
            photoDao.insertEventPhoto(photo)

            onPhotoReady(photo.id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    suspend fun addFishPhoto(
        fishId: String,
        uri: Uri,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)
            Log.d("PhotoRepository", "Photo metadata: ${metadata.hashcode}")

            val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                ?: throw IOException("Could not save scaled photo for URI: $uri")

            val photo = FishPhotoEntity(
                uri = scaledPath,
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = fishId
            )
            photoDao.insertFishPhoto(photo)

            onPhotoReady(photo.id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    suspend fun addFishermanPhoto(
        fishermanId: String,
        uri: Uri,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)
            Log.d("PhotoRepository", "Photo metadata: ${metadata.hashcode}")

            val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                ?: throw IOException("Could not save scaled photo for URI: $uri")

            val photo = FishermanPhotoEntity(
                uri = scaledPath,
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = fishermanId
            )
            photoDao.insertFishermanPhoto(photo)

            onPhotoReady(photo.id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    suspend fun addLurePhoto(
        lureId: String,
        uri: Uri,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)
            Log.d("PhotoRepository", "Photo metadata: ${metadata.hashcode}")

            val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                ?: throw IOException("Could not save scaled photo for URI: $uri")

            val existingId = photoDao.getLurePhotoIdByHash(
                hashcode = metadata.hashcode,
                parentId = lureId
            )

            val photoId = if (existingId != null) {
                existingId
            } else {

                val photo = LurePhotoEntity(
                    uri = scaledPath,
                    hashcode = metadata.hashcode,
                    thumbnail = metadata.thumbnail,
                    parentId = lureId
                )
                photoDao.insertLurePhoto(photo)
                photo.id
            }

            onPhotoReady(photoId)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    suspend fun addSkyConditionPhoto(
        skyConditionId: String,
        uri: Uri,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)
            Log.d("PhotoRepository", "Photo metadata: ${metadata.hashcode}")

            val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                ?: throw IOException("Could not save scaled photo for URI: $uri")

            val photo = SkyConditionPhotoEntity(
                uri = scaledPath,
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = skyConditionId
            )
            photoDao.insertSkyConditionPhoto(photo)

            onPhotoReady(photo.id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    suspend fun addSpeciesPhoto(
        speciesId: String,
        uri: Uri,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)
            Log.d("PhotoRepository", "Photo metadata: ${metadata.hashcode}")

            val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                ?: throw IOException("Could not save scaled photo for URI: $uri")

            val photo = SpeciesPhotoEntity(
                uri = scaledPath,
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = speciesId
            )
            photoDao.insertSpeciesPhoto(photo)

            onPhotoReady(photo.id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    suspend fun addTripPhoto(
        tripId: String,
        uri: Uri,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)
            Log.d("PhotoRepository", "Photo metadata: ${metadata.hashcode}")

            val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                ?: throw IOException("Could not save scaled photo for URI: $uri")

            val photo = TripPhotoEntity(
                uri = scaledPath,
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = tripId
            )
            photoDao.insertTripPhoto(photo)

            onPhotoReady(photo.id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    suspend fun addWaterClarityPhoto(
        waterClarityId: String,
        uri: Uri,
        onPhotoReady: suspend (photoId: String) -> Unit
    ): Result<Unit> {
        return try {
            val metadata = getPhotoMetadata(uri)
            Log.d("PhotoRepository", "Photo metadata: ${metadata.hashcode}")

            val scaledPath = saveScaledPhoto(uri, metadata.hashcode)
                ?: throw IOException("Could not save scaled photo for URI: $uri")

            val photo = WaterClarityPhotoEntity(
                uri = scaledPath,
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = waterClarityId
            )
            photoDao.insertWaterClarityPhoto(photo)

            onPhotoReady(photo.id)
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    suspend fun addFishPhotos(fishId: String, photos: List<FishPhoto>) {
        photos.forEach { photo ->
            addFishPhoto(fishId, photo.uri.toUri()) {}
                .onSuccess { }
                .onFailure { }
        }
    }
    suspend fun addLurePhotos(lureId: String, photos: List<LurePhoto>) {
        photos.forEach { photo ->
            addLurePhoto(lureId, photo.uri.toUri()) {}
                .onSuccess { }
                .onFailure { }
        }
    }

    suspend fun setEventThumbnail(eventId: String, photoId: String) =
        photoDao.setPrimaryEventPhoto(eventId = eventId, photoId = photoId)
    suspend fun setFishThumbnail(fishId: String, photoId: String) =
        photoDao.setPrimaryFishPhoto(fishId = fishId, photoId = photoId)
    suspend fun setFishermanThumbnail(fishermanId: String, photoId: String) =
        photoDao.setPrimaryFishermanPhoto(fishermanId = fishermanId, photoId = photoId)
    suspend fun setLureThumbnail(lureId: String, photoId: String) =
        photoDao.setPrimaryLurePhoto(lureId = lureId, photoId = photoId)
    suspend fun setTripThumbnail(tripId: String, photoId: String) =
        photoDao.setPrimaryTripPhoto(tripId = tripId, photoId = photoId)

    suspend fun deleteEventPhoto(photoId: String) =
        photoDao.deleteEventPhotoById(photoId)
    suspend fun deleteFishPhoto(photoId: String) =
        photoDao.deleteFishPhotoById(photoId)
    suspend fun deleteFishPhotos(photos: List<FishPhoto>) {
        photos.forEach { photo ->
            photoDao.deleteFishPhotoById(photo.id)
        }
    }
    suspend fun deleteFishermanPhoto(photoId: String) =
        photoDao.deleteFishermanPhotoById(photoId)
    suspend fun deleteLurePhoto(photoId: String) {
        photoDao.deleteLurePhotoById(String())
    }
    suspend fun deleteLurePhotos(photos: List<LurePhoto>) {
        photos.forEach { photo ->
            photoDao.deleteLurePhotoById(photo.id)
        }
    }
    suspend fun deleteTripPhoto(photoId: String) =
        photoDao.deleteTripPhotoById(photoId)

    suspend fun deleteBaitThumbnail(id: String) {
        val existingPhoto = photoDao.getBaitPhoto(id)
        if (existingPhoto != null) {
            photoDao.deleteBaitPhoto(existingPhoto)
        }
    }
    suspend fun deleteBodyOfWaterThumbnail(id: String) {
        val existingPhoto = photoDao.getBodyOfWaterPhoto(id)
        if (existingPhoto != null) {
            photoDao.deleteBodyOfWaterPhoto(existingPhoto)
        }
    }
    suspend fun deleteSpeciesThumbnail(speciesId: String) {
        val existingPhoto = photoDao.getSpeciesPhoto(speciesId)
        if (existingPhoto != null) {
            photoDao.deleteSpeciesPhoto(existingPhoto)
        }
    }
    suspend fun deleteSkyConditionThumbnail(id: String) {
        val existingPhoto = photoDao.getSkyConditionPhoto(id)
        if (existingPhoto != null) {
            photoDao.deleteSkyConditionPhoto(existingPhoto)
        }
    }
    suspend fun deleteWaterClarityThumbnail(id: String) {
        val existingPhoto = photoDao.getWaterClarityPhoto(id)
        if (existingPhoto != null) {
            photoDao.deleteWaterClarityPhoto(existingPhoto)
        }
    }

    fun fetchBaitThumbnail(id: String): Flow<ByteArray?> {
        return photoDao.getBaitThumbnail(id)
    }
    fun fetchBodyOfWaterThumbnail(id: String): Flow<ByteArray?> {
        return photoDao.getBodyOfWaterThumbnail(id)
    }
    fun fetchEventThumbnail(id: String): Flow<ByteArray?> {
        return photoDao.getEventThumbnail(id)
    }
    fun fetchFishThumbnail(id: String): Flow<ByteArray?> {
        return photoDao.getFishThumbnail(id)
    }
    fun fetchFishermanThumbnail(id: String): Flow<ByteArray?> {
        return photoDao.getFishermanThumbnail(id)
    }
    fun fetchLureThumbnail(id: String): Flow<ByteArray?> {
        return photoDao.getLureThumbnail(id)
    }
    fun fetchSkyConditionThumbnail(id: String): Flow<ByteArray?> {
        return photoDao.getSkyConditionThumbnail(id)
    }
    fun fetchSpeciesThumbnail(id: String): Flow<ByteArray?> {
        return photoDao.getSpeciesThumbnail(id)
    }
    fun fetchTripThumbnail(id: String): Flow<ByteArray?> {
        return photoDao.getTripThumbnail(id)
    }
    fun fetchWaterClarityThumbnail(id: String): Flow<ByteArray?> {
        return photoDao.getWaterClarityThumbnail(id)
    }

    suspend fun updateBaitThumbnail(id: String, uri: Uri) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val existingPhoto = photoDao.getBaitPhoto(id)

            if (existingPhoto != null) {
                photoDao.deleteBaitPhoto(existingPhoto)
            }

            val metadata = getPhotoMetadata(uri)

            val photo = BaitPhotoEntity(
                uri = "bait_thumb_${id}_${System.currentTimeMillis()}",
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = id
            )
            photoDao.insertBaitPhoto(photo)
        }
    }

    suspend fun updateBodyOfWaterThumbnail(id: String, uri: Uri) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val existingPhoto = photoDao.getBodyOfWaterPhoto(id)

            if (existingPhoto != null) {
                photoDao.deleteBodyOfWaterPhoto(existingPhoto)
            }

            val metadata = getPhotoMetadata(uri)

            val photo = BodyOfWaterPhotoEntity(
                uri = "body_of_water_thumb_${id}_${System.currentTimeMillis()}",
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = id
            )
            photoDao.insertBodyOfWaterPhoto(photo)
        }
    }

    suspend fun updateSpeciesThumbnail(speciesId: String, uri: Uri) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val existingPhoto = photoDao.getSpeciesPhoto(speciesId)

            if (existingPhoto != null) {
                photoDao.deleteSpeciesPhoto(existingPhoto)
            }

            val metadata = getPhotoMetadata(uri)

            val photo = SpeciesPhotoEntity(
                uri = "species_thumb_${speciesId}_${System.currentTimeMillis()}",
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = speciesId
            )
            photoDao.insertSpeciesPhoto(photo)
        }
    }

    suspend fun updateSkyConditionThumbnail(id: String, uri: Uri) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val existingPhoto = photoDao.getSkyConditionPhoto(id)

            if (existingPhoto != null) {
                photoDao.deleteSkyConditionPhoto(existingPhoto)
            }

            val metadata = getPhotoMetadata(uri)

            // 3) Create a new entry for the photo table
            val photo = SkyConditionPhotoEntity(
                uri = "sky_condition_thumb_${id}_${System.currentTimeMillis()}",
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = id
            )
            photoDao.insertSkyConditionPhoto(photo)
        }
    }

    suspend fun updateWaterClarityThumbnail(id: String, uri: Uri) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val existingPhoto = photoDao.getWaterClarityPhoto(id)

            if (existingPhoto != null) {
                photoDao.deleteWaterClarityPhoto(existingPhoto)
            }

            val metadata = getPhotoMetadata(uri)

            val photo = WaterClarityPhotoEntity(
                uri = "water_clarity_thumb_${id}_${System.currentTimeMillis()}",
                hashcode = metadata.hashcode,
                thumbnail = metadata.thumbnail,
                parentId = id
            )
            photoDao.insertWaterClarityPhoto(photo)
        }
    }
}
