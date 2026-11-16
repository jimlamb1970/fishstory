package com.funjim.fishstory.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.funjim.fishstory.database.entity.EventTargetSpeciesEntity
import com.funjim.fishstory.database.entity.LimitSpeciesEntity
import com.funjim.fishstory.database.entity.SpeciesEntity
import com.funjim.fishstory.database.entity.TripTargetSpeciesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeciesDao {
    // CRUD Operations for Species
    @Query("DELETE FROM species_table")
    suspend fun deleteAllSpecies()
    @Delete
    suspend fun deleteSpecies(species: SpeciesEntity)
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insertSpecies(species: SpeciesEntity)
    @Update
    suspend fun updateSpecies(species: SpeciesEntity)

    // CRUD Operations for EventTargetSpecies
    @Query("DELETE FROM event_target_species")
    suspend fun deleteAllEventTargetSpecies()
    @Query("DELETE FROM event_target_species WHERE eventId = :eventId AND speciesId = :speciesId")
    suspend fun deleteEventTargetSpecies(eventId: String, speciesId: String)
    @Query("DELETE FROM event_target_species WHERE eventId IN (:eventIds) AND speciesId = :speciesId")
    suspend fun deleteTargetSpeciesForEvents(eventIds: List<String>, speciesId: String)
    @Upsert
    suspend fun upsertEventTargetSpecies(crossRef: EventTargetSpeciesEntity)
    @Upsert
    suspend fun upsertTargetSpeciesForEvents(targets: List<EventTargetSpeciesEntity>)

    // CRUD Operations for LimitSpecies
    @Query("DELETE FROM limit_species_table WHERE limitId = :limitId")
    suspend fun deleteAllSpeciesForLimit(limitId: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimitSpecies(item: LimitSpeciesEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimitSpecies(xRefs: List<LimitSpeciesEntity>)

    // CRUD Operations for TripTargetSpecies
    @Query("DELETE FROM trip_target_species")
    suspend fun deleteAllTripTargetSpecies()
    @Query("DELETE FROM trip_target_species WHERE tripId = :tripId AND speciesId = :speciesId")
    suspend fun deleteTripTargetSpecies(tripId: String, speciesId: String)
    @Upsert
    suspend fun upsertTripTargetSpecies(crossRef: TripTargetSpeciesEntity)

    // Get Operations for Species
    @Transaction
    @Query("SELECT * FROM species_table WHERE id = :speciesId")
    fun getSpecies(speciesId: String): Flow<SpeciesEntity?>
    @Query("SELECT * FROM species_table WHERE id = :id")
    suspend fun getSpeciesById(id: String): SpeciesEntity?
    @Query("SELECT * FROM species_table WHERE name = :name")
    suspend fun getSpeciesByName(name: String): SpeciesEntity?

    @Query("SELECT * FROM species_table ORDER BY name ASC")
    fun getAllSpecies(): Flow<List<SpeciesEntity>>
    @Query("SELECT * FROM species_table ORDER BY name ASC")
    suspend fun getAllSpeciesList(): List<SpeciesEntity>
    @Query("""
        SELECT species_table.* FROM species_table
        INNER JOIN event_target_species ON species_table.id = event_target_species.speciesId
        WHERE event_target_species.eventId = :eventId
        UNION
        SELECT species_table.* FROM species_table
        INNER JOIN limit_species_table ON species_table.id = limit_species_table.speciesId
        INNER JOIN event_limit_table ON limit_species_table.limitId = event_limit_table.limitId
        WHERE event_limit_table.eventId = :eventId
        UNION
        SELECT species_table.* FROM species_table 
        INNER JOIN fish_table ON species_table.id = fish_table.speciesId  
        WHERE fish_table.eventId = :eventId
    """)
    suspend fun getAllSpeciesForEvent(eventId: String): List<SpeciesEntity>
    @Query("""
        SELECT species_table.* FROM species_table
        INNER JOIN event_target_species ON species_table.id = event_target_species.speciesId
        WHERE event_target_species.eventId = :eventId
        GROUP BY species_table.id
        """)
    fun getEventTargetSpecies(eventId: String): Flow<List<SpeciesEntity>>
    @Query("""
        SELECT species_table.* FROM species_table
        INNER JOIN trip_target_species ON species_table.id = trip_target_species.speciesId
        WHERE trip_target_species.tripId = :tripId
        GROUP BY species_table.id
        """)
    fun getTripTargetSpecies(tripId: String): Flow<List<SpeciesEntity>>

    // Get Operations for EventTargetSpecies
    @Query("SELECT * FROM event_target_species")
    fun getAllEventTargetSpecies(): Flow<List<EventTargetSpeciesEntity>>
    @Query("SELECT * FROM event_target_species")
    suspend fun getAllEventTargetSpeciesList(): List<EventTargetSpeciesEntity>
    @Query("""
        SELECT event_target_species.* FROM event_target_species
        WHERE event_target_species.eventId = :eventId
    """)
    suspend fun getEventTargetSpeciesForEvent(eventId: String): List<EventTargetSpeciesEntity>

    // Get Operations for LimitSpecies
    @Query("SELECT * FROM limit_species_table")
    suspend fun getAllLimitSpeciesList(): List<LimitSpeciesEntity>
    @Query("""
        SELECT limit_species_table.* FROM limit_species_table
        INNER JOIN event_limit_table ON limit_species_table.limitId = event_limit_table.limitId
        WHERE event_limit_table.eventId = :eventId
    """)
    suspend fun getLimitSpeciesForEvent(eventId: String): List<LimitSpeciesEntity>

    // Get Operation for TripTargetSpecies
    @Query("SELECT * FROM trip_target_species")
    fun getAllTripTargetSpecies(): Flow<List<TripTargetSpeciesEntity>>
    @Query("SELECT * FROM trip_target_species")
    suspend fun getAllTripTargetSpeciesList(): List<TripTargetSpeciesEntity>

    // Special queries for Species
    @Transaction
    @Query("""
        SELECT DISTINCT species_table.* FROM species_table 
        INNER JOIN fish_table ON species_table.id = fish_table.speciesId 
        WHERE (:bodyOfWaterId IS NULL OR fish_table.bodyOfWaterId = :bodyOfWaterId)
          AND (:eventId IS NULL OR fish_table.eventId = :eventId)
          AND (:fishermanId IS NULL OR fish_table.fishermanId = :fishermanId)
          AND (:lureId IS NULL OR fish_table.lureId = :lureId)
          AND (:tripId IS NULL OR fish_table.tripId = :tripId)
        GROUP BY species_table.id
    """)
    fun getSpeciesWithFish(
        bodyOfWaterId: String? = null,
        eventId: String? = null,
        fishermanId: String? = null,
        lureId: String? = null,
        tripId: String? = null
    ): Flow<List<SpeciesEntity>>
}
