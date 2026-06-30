package com.funjim.fishstory.model

import androidx.room.DatabaseView
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "trip_table")
data class Trip(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
)

data class TripWithPhotos(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",        // Trip ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoTripCrossRef::class,
            parentColumn = "tripId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>
)

data class TripWithFishermen(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripFishermanCrossRef::class,
            parentColumn = "tripId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>
)

data class TripWithFishermenAndSpecies(
    @Embedded val trip: Trip,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripFishermanCrossRef::class,
            parentColumn = "tripId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripTargetSpecies::class,
            parentColumn = "tripId",
            entityColumn = "speciesId"
        )
    )
    val targetSpecies: List<Species>
)

data class TripWithDetails(
    @Embedded val trip: Trip,

    @Relation(
        entity = Event::class,
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val events: List<EventWithInfo>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripFishermanCrossRef::class,
            parentColumn = "tripId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>,

    @Relation(
        parentColumn = "id",        // Trip ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoTripCrossRef::class,
            parentColumn = "tripId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripTargetSpecies::class,
            parentColumn = "tripId",
            entityColumn = "speciesId"
        )
    )
    val targetSpecies: List<Species>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TripBodyOfWater::class,
            parentColumn = "tripId",
            entityColumn = "bodyOfWaterId"
        )
    )
    val bodiesOfWater: List<BodyOfWater>
)

@DatabaseView(
    viewName = "v_trip_detailed_summary",
    value = """
WITH 
-- Get the single biggest fish for the trip
BigFishPerTrip AS (
    SELECT 
        f.tripId, 
        f.length, 
        f.speciesId, 
        f.fishermanId,
        ROW_NUMBER() OVER (PARTITION BY f.tripId ORDER BY f.length DESC, f.id ASC) as row_num
    FROM fish_table f
),
-- Get the single biggest TARGET fish for the trip
BigTargetFishPerTrip AS (
    SELECT 
        f.tripId, 
        f.length, 
        f.speciesId, 
        f.fishermanId,
        ROW_NUMBER() OVER (PARTITION BY f.tripId ORDER BY f.length DESC, f.id ASC) as row_num
    FROM fish_table f
    INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
),
-- Identify the fisherman with the most for the trip
MostCaughtPerTrip AS (
    SELECT 
        f.tripId, 
        f.fishermanId, 
        COUNT(f.id) as catchCount,
        ROW_NUMBER() OVER (PARTITION BY f.tripId ORDER BY COUNT(f.id) DESC, f.fishermanId ASC) as row_num
    FROM fish_table f
    GROUP BY f.tripId, f.fishermanId
),
-- Get the fisherman with the most TARGET catches for the trip
MostTargetCaughtPerTrip AS (
    SELECT 
        f.tripId, 
        f.fishermanId, 
        COUNT(f.id) as catchCount,
        ROW_NUMBER() OVER (PARTITION BY f.eventId ORDER BY COUNT(f.id) DESC, f.fishermanId ASC) as row_num
    FROM fish_table f
    INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
    GROUP BY f.tripId, f.fishermanId
)

SELECT 
    t.*,
    -- Counts
    (SELECT COUNT(*) FROM event_table et WHERE et.tripId = t.id) as eventCount,

    -- Basic Counts (Overall)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f WHERE f.tripId = t.id) as fishCaught,
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f WHERE f.tripId = t.id) as fishKept,
    (SELECT COUNT(*) FROM trip_fisherman_cross_ref xr WHERE xr.tripId = t.id) as fishermanCount,
    (SELECT COUNT(*) FROM trip_fisherman_cross_ref xr WHERE xr.tripId = t.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,

    -- Basic Counts (Target Only)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.tripId = t.id) as targetFishCaught,
     
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.tripId = t.id) as targetFishKept,

    -- Big Fish Data (joined via CTE)
    CASE 
        WHEN bfm.nickname IS NOT NULL AND bfm.nickname != '' 
        THEN bfm.firstName || ' "' || bfm.nickname || '" ' || bfm.lastName 
        ELSE bfm.firstName || ' ' || bfm.lastName 
    END as bigFishFisherman,
    bsp.name as bigFishSpecies,
    bf.length as bigFishLength,
    
    CASE 
        WHEN tbfm.nickname IS NOT NULL AND tbfm.nickname != '' 
        THEN tbfm.firstName || ' "' || tbfm.nickname || '" ' || tbfm.lastName 
        ELSE tbfm.firstName || ' ' || tbfm.lastName 
    END as targetBigFishFisherman,
    tbsp.name as targetBigFishSpecies,
    tbf.length as targetBigFishLength,
    
    -- Most Caught Data (joined via CTE)
    CASE 
        WHEN mcm.nickname IS NOT NULL AND mcm.nickname != '' 
        THEN mcm.firstName || ' "' || mcm.nickname || '" ' || mcm.lastName 
        ELSE mcm.firstName || ' ' || mcm.lastName 
    END as mostCaughtFisherman,
    mc.catchCount as mostCaught,

    CASE 
        WHEN tmcm.nickname IS NOT NULL AND tmcm.nickname != '' 
        THEN tmcm.firstName || ' "' || tmcm.nickname || '" ' || tmcm.lastName 
        ELSE tmcm.firstName || ' ' || tmcm.lastName 
    END as targetMostCaughtFisherman,
    tmc.catchCount as targetMostCaught

FROM trip_table t
-- Join Big Fish
LEFT JOIN BigFishPerTrip bf ON t.id = bf.tripId AND bf.row_num = 1
LEFT JOIN fisherman_table bfm ON bf.fishermanId = bfm.id
LEFT JOIN species_table bsp ON bf.speciesId = bsp.id

-- Join Most Caught
LEFT JOIN MostCaughtPerTrip mc ON t.id = mc.tripId AND mc.row_num = 1
LEFT JOIN fisherman_table mcm ON mc.fishermanId = mcm.id

-- Left joins for Target metrics
LEFT JOIN BigTargetFishPerTrip tbf ON t.id = tbf.tripId AND tbf.row_num = 1
LEFT JOIN fisherman_table tbfm ON tbf.fishermanId = tbfm.id
LEFT JOIN species_table tbsp ON tbf.speciesId = tbsp.id

LEFT JOIN MostTargetCaughtPerTrip tmc ON t.id = tmc.tripId AND tmc.row_num = 1
LEFT JOIN fisherman_table tmcm ON tmc.fishermanId = tmcm.id

"""
)
data class TripDetailedSummary(
    val id: String,
    val name: String,
    val startDate: Long,
    val endDate: Long,

    val eventCount: Int = 0,

    // Overall Fish Stats
    val fishCaught: Int,
    val fishKept: Int,
    val bigFishFisherman: String? = null,
    val bigFishSpecies: String? = null,
    val bigFishLength: Long? = null,
    val mostCaughtFisherman: String? = null,
    val mostCaught: Int? = null,

    // Target Fish Stats
    val targetFishCaught: Int,
    val targetFishKept: Int,
    val targetBigFishFisherman: String? = null,
    val targetBigFishSpecies: String? = null,
    val targetBigFishLength: Long? = null,
    val targetMostCaughtFisherman: String? = null,
    val targetMostCaught: Int? = null,

    // Crew Information
    val fishermanCount: Int,
    val tackleBoxCount: Int
)

data class TripSummary(
    @Embedded val trip: Trip,

    val eventCount: Int = 0,

    val fishCaught: Int,
    val fishKept: Int,

    val targetFishCaught: Int,
    val targetFishKept: Int,

    val fishermanCount: Int,
    val tackleBoxCount: Int
)