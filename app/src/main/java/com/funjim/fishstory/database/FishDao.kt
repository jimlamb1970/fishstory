package com.funjim.fishstory.database

import androidx.room.*
import com.funjim.fishstory.model.Fish
import com.funjim.fishstory.model.FishWithDetails
import com.funjim.fishstory.model.Species
import kotlinx.coroutines.flow.Flow

@Dao
interface FishDao {
    @Query("""
        SELECT 
            fish_table.id, 
            species_table.name AS speciesName, 
            (CASE 
                WHEN fisherman_table.nickname IS NOT NULL AND fisherman_table.nickname != '' 
                THEN fisherman_table.firstName || ' "' || fisherman_table.nickname || '" ' || fisherman_table.lastName 
                ELSE fisherman_table.firstName || ' ' || fisherman_table.lastName 
            END) AS fishermanName, 
            lure_table.name AS lureName,
            primary_color.name AS lurePrimaryColorName, 
            secondary_color.name AS lureSecondaryColorName,
            lure_table.glows AS lureGlows,
            glow_color_table.name AS lureGlowColorName,
            fish_table.length, 
            fish_table.isReleased,
            fish_table.timestamp, 
            fish_table.latitude, 
            fish_table.longitude,
            fish_table.segmentId,
            fish_table.tripId,
            fish_table.holeNumber
        FROM fish_table
        INNER JOIN species_table ON fish_table.speciesId = species_table.id
        INNER JOIN fisherman_table ON fish_table.fishermanId = fisherman_table.id
        LEFT JOIN lure_table ON fish_table.lureId = lure_table.id
        LEFT JOIN lure_color_table AS primary_color ON lure_table.primaryColorId = primary_color.id
        LEFT JOIN lure_color_table AS secondary_color ON lure_table.secondaryColorId = secondary_color.id
        LEFT JOIN lure_color_table AS glow_color_table ON lure_table.glowColorId = glow_color_table.id
        ORDER BY fish_table.timestamp DESC
    """)
    fun getAllFishWithDetails(): Flow<List<FishWithDetails>>

    @Query("""
        SELECT 
            fish_table.id, 
            species_table.name AS speciesName, 
            (CASE 
                WHEN fisherman_table.nickname IS NOT NULL AND fisherman_table.nickname != '' 
                THEN fisherman_table.firstName || ' "' || fisherman_table.nickname || '" ' || fisherman_table.lastName 
                ELSE fisherman_table.firstName || ' ' || fisherman_table.lastName 
            END) AS fishermanName, 
            lure_table.name AS lureName,
            primary_color.name AS lurePrimaryColorName, 
            secondary_color.name AS lureSecondaryColorName,
            lure_table.glows AS lureGlows,
            glow_color_table.name AS lureGlowColorName,
            fish_table.length, 
            fish_table.isReleased,
            fish_table.timestamp, 
            fish_table.latitude, 
            fish_table.longitude,
            fish_table.segmentId,
            fish_table.tripId,
            fish_table.holeNumber
        FROM fish_table
        INNER JOIN species_table ON fish_table.speciesId = species_table.id
        INNER JOIN fisherman_table ON fish_table.fishermanId = fisherman_table.id
        LEFT JOIN lure_table ON fish_table.lureId = lure_table.id
        LEFT JOIN lure_color_table AS primary_color ON lure_table.primaryColorId = primary_color.id
        LEFT JOIN lure_color_table AS secondary_color ON lure_table.secondaryColorId = secondary_color.id
        LEFT JOIN lure_color_table AS glow_color_table ON lure_table.glowColorId = glow_color_table.id
        WHERE fish_table.tripId = :tripId
        ORDER BY fish_table.timestamp DESC
    """)
    fun getFishForTrip(tripId: String): Flow<List<FishWithDetails>>

    @Query("""
        SELECT 
            fish_table.id, 
            species_table.name AS speciesName, 
            (CASE 
                WHEN fisherman_table.nickname IS NOT NULL AND fisherman_table.nickname != '' 
                THEN fisherman_table.firstName || ' "' || fisherman_table.nickname || '" ' || fisherman_table.lastName 
                ELSE fisherman_table.firstName || ' ' || fisherman_table.lastName 
            END) AS fishermanName,
            lure_table.name AS lureName,
            primary_color.name AS lurePrimaryColorName, 
            secondary_color.name AS lureSecondaryColorName,
            lure_table.glows AS lureGlows,
            glow_color_table.name AS lureGlowColorName,
            fish_table.length, 
            fish_table.isReleased,
            fish_table.timestamp, 
            fish_table.latitude, 
            fish_table.longitude,
            fish_table.segmentId,
            fish_table.tripId,
            fish_table.holeNumber
        FROM fish_table
        INNER JOIN species_table ON fish_table.speciesId = species_table.id
        INNER JOIN fisherman_table ON fish_table.fishermanId = fisherman_table.id
        LEFT JOIN lure_table ON fish_table.lureId = lure_table.id
        LEFT JOIN lure_color_table AS primary_color ON lure_table.primaryColorId = primary_color.id
        LEFT JOIN lure_color_table AS secondary_color ON lure_table.secondaryColorId = secondary_color.id
        LEFT JOIN lure_color_table AS glow_color_table ON lure_table.glowColorId = glow_color_table.id
        WHERE fish_table.segmentId = :segmentId
        ORDER BY fish_table.timestamp DESC
    """)
    fun getFishForSegment(segmentId: String): Flow<List<FishWithDetails>>

    @Query("SELECT * FROM fish_table ORDER BY timestamp DESC")
    fun getAllFish(): Flow<List<Fish>>

    @Query("DELETE FROM fish_table")
    suspend fun deleteAllFish()

    @Query("SELECT * FROM fish_table WHERE id = :id")
    suspend fun getFishById(id: String): Fish?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(fish: Fish)

    @Update
    suspend fun upsertFish(fish: Fish)

    @Update
    suspend fun updateFish(fish: Fish)

    @Delete
    suspend fun deleteFish(fish: Fish)

    @Query("SELECT * FROM species_table ORDER BY name ASC")
    fun getAllSpecies(): Flow<List<Species>>

    @Query("DELETE FROM species_table")
    suspend fun deleteAllSpecies()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSpecies(species: Species)

    @Delete
    suspend fun deleteSpecies(species: Species)
}
