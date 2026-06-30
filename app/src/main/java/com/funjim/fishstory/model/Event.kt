package com.funjim.fishstory.model

import androidx.room.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "event_table",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tripId"])
    ]
)
data class Event(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val tripId: String,
    val name: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val bodyOfWaterId: String? = null,
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false
)

data class EventWithPhotos(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "id",        // Event ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoEventCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>
)

data class EventWithFishermen(
    @Embedded val event: Event,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EventFishermanCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>
)


data class EventWithDetails(
    @Embedded val event: Event,

    @Relation(
        parentColumn = "tripId",   // The field in your Event entity
        entityColumn = "id"        // The primary key in your Trip entity
    )
    val trip: Trip,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EventFishermanCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "fishermanId"
        )
    )
    val fishermen: List<Fisherman>,

    @Relation(
        parentColumn = "id",        // Event ID
        entityColumn = "id",        // Photo ID
        associateBy = Junction(
            value = PhotoEventCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "photoId"
        )
    )
    val photos: List<Photo>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EventTargetSpecies::class,
            parentColumn = "eventId",
            entityColumn = "speciesId"
        )
    )
    val targetSpecies: List<Species>
)

data class EventWithInfo(
    @Embedded val event: Event,

    @Relation(
        entity = Species::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EventTargetSpecies::class,
            parentColumn = "eventId",
            entityColumn = "speciesId"
        )
    )
    val targetSpecies: List<Species>,

    @Relation(
        entity = BodyOfWater::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = EventBodyOfWater::class,
            parentColumn = "eventId",
            entityColumn = "bodyOfWaterId"
        )
    )
    val bodiesOfWater: List<BodyOfWater>
)

@DatabaseView(
    viewName = "v_event_detailed_summary",
    value = """
WITH 
-- Get the biggest fish per event for this specific trip
BigFishPerEvent AS (
    SELECT 
        f.eventId, 
        f.length, 
        f.speciesId, 
        f.fishermanId,
        ROW_NUMBER() OVER (PARTITION BY f.eventId ORDER BY f.length DESC, f.id ASC) as row_num
    FROM fish_table f
),
-- Get the biggest TARGET fish per event for this specific trip
BigTargetFishPerEvent AS (
    SELECT 
        f.eventId, 
        f.length, 
        f.speciesId, 
        f.fishermanId,
        ROW_NUMBER() OVER (PARTITION BY f.eventId ORDER BY f.length DESC, f.id ASC) as row_num
    FROM fish_table f
    INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
),
-- Get the fisherman with the most catches per event for this specific trip
MostCaughtPerEvent AS (
    SELECT 
        f.eventId, 
        f.fishermanId, 
        COUNT(f.id) as catchCount,
        ROW_NUMBER() OVER (PARTITION BY f.eventId ORDER BY COUNT(f.id) DESC, f.fishermanId ASC) as row_num
    FROM fish_table f
    GROUP BY f.eventId, f.fishermanId
),
-- Get the fisherman with the most TARGET catches per event for this specific trip
MostTargetCaughtPerEvent AS (
    SELECT 
        f.eventId, 
        f.fishermanId, 
        COUNT(f.id) as catchCount,
        ROW_NUMBER() OVER (PARTITION BY f.eventId ORDER BY COUNT(f.id) DESC, f.fishermanId ASC) as row_num
    FROM fish_table f
    INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
    GROUP BY f.eventId, f.fishermanId
)

SELECT 
    s.*,
    t.name as tripName,
    t.startDate as tripStartTime,
    t.endDate as tripEndTime,
        
    -- Basic Counts (Overall)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f WHERE f.eventId = s.id) as fishCaught,
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f WHERE f.eventId = s.id) as fishKept,
    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id) as fishermanCount,
    (SELECT COUNT(*) FROM event_fisherman_cross_ref xr WHERE xr.eventId = s.id AND xr.tackleBoxId IS NOT NULL) as tackleBoxCount,

    -- Basic Counts (Target Only)
    (SELECT COALESCE(SUM(f.caughtCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.eventId = s.id) as targetFishCaught,
     
    (SELECT COALESCE(SUM(f.keptCount), 0) FROM fish_table f 
     INNER JOIN event_target_species ts ON f.eventId = ts.eventId AND f.speciesId = ts.speciesId
     WHERE f.eventId = s.id) as targetFishKept,

    -- Big Fish Data (joined from CTE)
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
    
    -- Most Caught Data (joined from CTE)
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

FROM event_table s
INNER JOIN trip_table t ON s.tripId = t.id

-- Left join for overall metrics
LEFT JOIN BigFishPerEvent bf ON s.id = bf.eventId AND bf.row_num = 1
LEFT JOIN fisherman_table bfm ON bf.fishermanId = bfm.id
LEFT JOIN species_table bsp ON bf.speciesId = bsp.id

LEFT JOIN MostCaughtPerEvent mc ON s.id = mc.eventId AND mc.row_num = 1
LEFT JOIN fisherman_table mcm ON mc.fishermanId = mcm.id

-- Left joins for Target metrics
LEFT JOIN BigTargetFishPerEvent tbf ON s.id = tbf.eventId AND tbf.row_num = 1
LEFT JOIN fisherman_table tbfm ON tbf.fishermanId = tbfm.id
LEFT JOIN species_table tbsp ON tbf.speciesId = tbsp.id

LEFT JOIN MostTargetCaughtPerEvent tmc ON s.id = tmc.eventId AND tmc.row_num = 1
LEFT JOIN fisherman_table tmcm ON tmc.fishermanId = tmcm.id
        """
)
data class EventDetailedSummary(
    val id: String,
    val name: String,
    val tripId: String,
    val startTime: Long,
    val endTime: Long,

    val tripName: String,
    val tripStartTime: Long,
    val tripEndTime: Long,

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

data class EventSummary(
    @Embedded val event: Event,

    @Relation(
        parentColumn = "tripId",   // The field in your Event entity
        entityColumn = "id"        // The primary key in your Trip entity
    )
    val trip: Trip,

    val fishCaught: Int,
    val fishKept: Int,

    val targetFishCaught: Int,
    val targetFishKept: Int,

    val fishermanCount: Int,
    val tackleBoxCount: Int
)