package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.EventLimitEntity
import com.funjim.fishstory.database.entity.LimitEntity
import com.funjim.fishstory.database.entity.LimitSpeciesEntity
import com.funjim.fishstory.database.entity.LimitWithSpeciesEntity
import com.funjim.fishstory.database.entity.TripLimitEntity
import com.funjim.fishstory.model.LimitType
import kotlinx.coroutines.flow.Flow

@Dao
interface LimitDao {
    // Lure queries
    @Query("SELECT * FROM limit_table")
    fun getAllLimits(): Flow<List<LimitEntity>>
    @Query("DELETE FROM limit_table")
    suspend fun deleteAllLimits()

    @Delete
    suspend fun deleteLimit(entity: LimitEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(entity: LimitEntity)
    @Upsert
    suspend fun upsertLimit(entity: LimitEntity)

    @Query("SELECT * FROM limit_table WHERE id = :id")
    fun getLimit(id: String): Flow<LimitEntity?>
    @Query("SELECT * FROM limit_table WHERE id = :id")
    suspend fun geLimitById(id: String): LimitEntity?

    @Query("""
        SELECT * FROM limit_table AS l
        WHERE l.name = :name
          AND l.type = :type
          AND l.count = :count
          AND l.lowerSize IS :lowerSize
          AND l.lowerInclusive = :lowerInclusive
          AND l.upperSize IS :upperSize
          AND l.upperInclusive = :upperInclusive
          AND (
            SELECT COALESCE(GROUP_CONCAT(speciesId, ','), '') 
            FROM (SELECT speciesId FROM limit_species_table WHERE limitId = l.id ORDER BY speciesId)
          ) = :sortedSpeciesIds
        LIMIT 1
    """)
    suspend fun getLimitByData(
        name: String,
        type: LimitType,
        count: Int = 0,
        lowerSize: Long? = null,
        lowerInclusive: Boolean = true,
        upperSize: Long? = null,
        upperInclusive: Boolean = true,
        sortedSpeciesIds: String
    ): LimitEntity?

    @Query("""
        SELECT limit_table.* FROM limit_table
        INNER JOIN event_limit_table ON limit_table.id = event_limit_table.limitId
        WHERE event_limit_table.eventId = :eventId
    """)
    suspend fun getAllLimitsForEvent(eventId: String): List<LimitEntity>
    @Query("""
        SELECT event_limit_table.* FROM event_limit_table
        WHERE event_limit_table.eventId = :eventId
    """)
    suspend fun getEventLimitsForEvent(eventId: String): List<EventLimitEntity>

    @Query("DELETE FROM limit_species_table WHERE limitId = :limitId")
    suspend fun deleteLimitSpeciesXRefsForLimit(limitId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimitSpeciesXRefs(xRefs: List<LimitSpeciesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventLimit(entity: EventLimitEntity)
    @Delete
    suspend fun deleteEventLimit(entity: EventLimitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripLimit(entity: TripLimitEntity)
    @Delete
    suspend fun deleteTripLimit(entity: TripLimitEntity)

    @Transaction
    suspend fun insertLimit(
        limit: LimitEntity,
        species: List<String>
    ) {
        upsertLimit(limit)
        deleteLimitSpeciesXRefsForLimit(limit.id)
        if (species.isNotEmpty()) {
            val speciesXRefs = species.map { species ->
                LimitSpeciesEntity(
                    limitId = limit.id,
                    speciesId = species
                )
            }
            insertLimitSpeciesXRefs(speciesXRefs)
        }
    }

    @Transaction
    suspend fun insertLimitForEvent(
        limit: LimitEntity,
        species: List<String>,
        eventId: String
    ) {
        upsertLimit(limit)
        deleteLimitSpeciesXRefsForLimit(limit.id)
        if (species.isNotEmpty()) {
            val speciesXRefs = species.map { species ->
                LimitSpeciesEntity(
                    limitId = limit.id,
                    speciesId = species
                )
            }
            insertLimitSpeciesXRefs(speciesXRefs)
        }

        val eventLimit = EventLimitEntity(
            eventId = eventId,
            limitId = limit.id
        )
        insertEventLimit(eventLimit)
    }

    @Transaction
    suspend fun insertLimitForTrip(
        limit: LimitEntity,
        species: List<String>,
        tripId: String
    ) {
        upsertLimit(limit)
        deleteLimitSpeciesXRefsForLimit(limit.id)
        if (species.isNotEmpty()) {
            val speciesXRefs = species.map { species ->
                LimitSpeciesEntity(
                    limitId = limit.id,
                    speciesId = species
                )
            }
            insertLimitSpeciesXRefs(speciesXRefs)
        }

        val tripLimit = TripLimitEntity(
            tripId = tripId,
            limitId = limit.id
        )
        insertTripLimit(tripLimit)
    }

    @Transaction
    suspend fun removeLimitFromTrip(limit: String, tripId: String) {
        val tripLimit = TripLimitEntity(
            tripId = tripId,
            limitId = limit
        )
        deleteTripLimit(tripLimit)
    }
    @Transaction
    suspend fun removeLimitFromEvent(limit: String, eventId: String) {
        val eventLimit = EventLimitEntity(
            eventId = eventId,
            limitId = limit
        )
        deleteEventLimit(eventLimit)
    }
    @Transaction
    suspend fun removeLimitFromEvents(limit: String, eventIds: List<String>) {
        eventIds.forEach { eventId ->
            val eventLimit = EventLimitEntity(
                eventId = eventId,
                limitId = limit
            )
            deleteEventLimit(eventLimit)
        }
    }

    @Transaction
    @Query("""
        SELECT limit_table.* FROM limit_table
        INNER JOIN event_limit_table ON limit_table.id = event_limit_table.limitId
        WHERE event_limit_table.eventId = :eventId
    """)
    fun getLimitsForEvent(eventId: String): Flow<List<LimitWithSpeciesEntity>>

    @Transaction
    @Query("""
        SELECT limit_table.* FROM limit_table
        INNER JOIN trip_limit_table ON limit_table.id = trip_limit_table.limitId
        WHERE trip_limit_table.tripId = :tripId
    """)
    fun getLimitsForTrip(tripId: String): Flow<List<LimitWithSpeciesEntity>>

    @Transaction
    @Query("""
        SELECT limit_table.* FROM limit_table
    """)
    fun getAllLimitsWithSpecies(): Flow<List<LimitWithSpeciesEntity>>
    @Transaction
    @Query("""
        SELECT limit_table.* FROM limit_table 
        WHERE limit_table.id = :limitId
        """)
    suspend fun getLimitWithSpeciesById(limitId: String): LimitWithSpeciesEntity?
}