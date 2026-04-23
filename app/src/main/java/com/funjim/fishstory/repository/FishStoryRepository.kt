package com.funjim.fishstory.repository

import androidx.room.withTransaction
import com.funjim.fishstory.database.FishDao
import com.funjim.fishstory.database.FishermanDao
import com.funjim.fishstory.database.FishstoryDatabase
import com.funjim.fishstory.database.LureDao
import com.funjim.fishstory.database.SegmentDao
import com.funjim.fishstory.database.TackleBoxDao
import com.funjim.fishstory.database.TripDao
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.Lure
import com.funjim.fishstory.model.LureColor
import com.funjim.fishstory.model.SegmentFishermanCrossRef
import com.funjim.fishstory.model.Species
import com.funjim.fishstory.model.TackleBoxLureCrossRef
import com.funjim.fishstory.model.TripFishermanCrossRef
import kotlinx.coroutines.flow.forEach
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.forEach

class FishStoryRepository(
    private val db: FishstoryDatabase,
    private val fishDao: FishDao,
    private val fishermanDao: FishermanDao,
    private val lureDao: LureDao,
    private val segmentDao: SegmentDao,
    private val tackleBoxDao: TackleBoxDao,
    private val tripDao: TripDao
) {
    fun parseDateTime(datePart: String, timePart: String): Long {
        val formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm a", Locale.ENGLISH)
        val localDateTime = LocalDateTime.parse("$datePart $timePart", formatter)
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    suspend fun importFromCsv(inputStream: InputStream) {
        val csvRows = mutableListOf<Array<String>>()

        // 1. Convert InputStream to a Reader
        val reader = inputStream.bufferedReader()

        reader.use { r ->
            // 2. Parse the lines (using a basic split or a library)
            r.lineSequence().forEachIndexed { index, line ->
                // Skip the header row (index 0)
                if (index > 0 && line.isNotBlank()) {
                    // Split by comma, handling potential spaces
                    val row = line.split(",").map { it.trim() }.toTypedArray()
                    csvRows.add(row)
                }
            }
        }

        // 3. Pass the finished list to your processing function
        if (csvRows.isNotEmpty()) {
            importFishingData(csvRows)
        }
    }

    suspend fun importFishingData(csvRows: List<Array<String>>) {
        db.withTransaction {
            val colorMap = mutableMapOf<String, String>()
            val speciesMap = mutableMapOf<String, String>()

            // 2. Manually populate it from the database
            val existingColors = lureDao.getAllLureColorsList() // Assuming this returns List<JigColor>
            existingColors.forEach { color ->
                colorMap[color.name] = color.id
            }

            val existingSpecies = fishDao.getAllSpeciesList() // Assuming this returns List<JigColor>
            existingSpecies.forEach { species ->
                speciesMap[species.name] = species.id
            }

            csvRows.forEach { row ->
                // Mapping based on your header:
                // 0:Year, 1:Day, 2:First, 3:Last, 4:Hole, 5:Date, 6:Time, 7:Lure...

                // 1. Handle the Trip (Year)
                val tripId = tripDao.getOrCreate(name = row[0])
                // 2. Handle the Segment (Day)
                val segmentId = segmentDao.getOrCreate(tripId, name = row[1])
                // 3. Handle the Fisherman
                val fishermanId = fishermanDao.getOrCreate(firstName = row[2], lastName = row[3])

                val tackleBoxName = "LOTW ${row[0]}"
                val tackleBoxId = tackleBoxDao.getOrCreate(fishermanId, tackleBoxName)

                tripDao.upsertTripFishermanCrossRef(TripFishermanCrossRef(tripId, fishermanId, tackleBoxId))
                segmentDao.upsertSegmentFishermanCrossRef(
                    SegmentFishermanCrossRef(
                        segmentId,
                        fishermanId,
                        tackleBoxId
                    )
                )

                val hole = row[4].toIntOrNull() ?: 0

                val timestamp = parseDateTime(row[5], row[6])

                val lureName = row[7].trim()

                val primaryName = row[8].trim()
                val primaryId = if (primaryName.isBlank()) {
                    null
                } else {
                    colorMap[primaryName] ?: run {
                        val newColor = LureColor(name = row[8])
                        lureDao.upsertLureColor(newColor)
                        colorMap[newColor.name] = newColor.id
                        newColor.id
                    }
                }

                val secondaryName = row[9].trim()
                val secondaryId = if (secondaryName.isBlank()) {
                    null
                } else {
                    colorMap[secondaryName] ?: run {
                        val newColor = LureColor(name = row[9])
                        lureDao.upsertLureColor(newColor)
                        colorMap[newColor.name] = newColor.id
                        newColor.id
                    }
                }

                val glows = row[10].contains("YES", ignoreCase = true)

                val glowName = row[11].trim()
                val glowId = if (glowName.isBlank()) {
                    null
                } else {
                    colorMap[glowName] ?: run {
                        val newColor = LureColor(name = row[11])
                        lureDao.upsertLureColor(newColor)
                        colorMap[newColor.name] = newColor.id
                        newColor.id
                    }
                }

                val speciesName = row[12].trim()
                val speciesId = if (speciesName.isBlank()) {
                    null
                } else {
                    speciesMap[speciesName] ?: run {
                        val newSpecies = Species(name = row[12])
                        fishDao.insertSpecies(newSpecies)
                        speciesMap[newSpecies.name] = newSpecies.id
                        newSpecies.id
                    }
                }

                val lure = Lure(
                    name = row[7],
                    primaryColorId = primaryId,
                    secondaryColorId = secondaryId,
                    hasSingleHook = true,
                    glows = glows,
                    glowColorId = glowId
                )
                val lureId = lureDao.getOrCreate(lure)

                tackleBoxDao.insertLureToTackleBox(TackleBoxLureCrossRef(tackleBoxId, lureId))

                val length = row[13].toDoubleOrNull() ?: 0.0
                val isKept = row[14].contains("Y", ignoreCase = true)

                val fish = Fish(
                    speciesId = speciesId,
                    fishermanId = fishermanId,
                    tripId = tripId,
                    segmentId = segmentId,
                    lureId = lureId,
                    length = length,
                    isReleased = !isKept,
                    timestamp = timestamp,
                    holeNumber = hole
                )
                fishDao.upsertFish(fish)
            }
        }
    }
}
