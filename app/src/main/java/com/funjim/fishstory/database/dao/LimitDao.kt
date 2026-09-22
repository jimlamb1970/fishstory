package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.funjim.fishstory.database.EventLimitEntity
import com.funjim.fishstory.database.LimitEntity
import com.funjim.fishstory.database.LimitSpeciesEntity
import com.funjim.fishstory.database.LimitWithSpeciesEntity
import com.funjim.fishstory.database.SpeciesEntity
import com.funjim.fishstory.database.TripLimitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LimitDao {
    // Lure queries
    @Query("SELECT * FROM limit_table")
    fun getAllLimits(): Flow<List<LimitEntity>>

    @Query("DELETE FROM limit_table")
    suspend fun deleteAllLimits()

    @Query("SELECT * FROM limit_table WHERE id = :id")
    suspend fun geLimitById(id: String): LimitEntity?
    @Query("SELECT * FROM limit_table WHERE id = :id")
    fun getLimit(id: String): Flow<LimitEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(entity: LimitEntity)

    @Upsert
    suspend fun upsertLimit(entity: LimitEntity)

    @Delete
    suspend fun deleteLimit(entity: LimitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimitSpeciesXRefs(xRefs: List<LimitSpeciesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventLimit(entity: EventLimitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripLimit(entity: TripLimitEntity)

    @Transaction
    suspend fun insertLimitForEvent(
        limit: LimitEntity,
        species: List<SpeciesEntity>,
        eventId: String
    ) {
        upsertLimit(limit)

        if (species.isNotEmpty()) {
            val speciesXRefs = species.map { species ->
                LimitSpeciesEntity(
                    limitId = limit.id,
                    speciesId = species.id
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
        species: List<SpeciesEntity>,
        tripId: String
    ) {
        upsertLimit(limit)

        if (species.isNotEmpty()) {
            val speciesXRefs = species.map { species ->
                LimitSpeciesEntity(
                    limitId = limit.id,
                    speciesId = species.id
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
}