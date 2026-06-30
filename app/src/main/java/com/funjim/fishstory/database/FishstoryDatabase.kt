package com.funjim.fishstory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.funjim.fishstory.model.*

@Database(
    entities = [
        BodyOfWater::class,                 // Backed up
        Event::class,                       // Backed up
        EventBodyOfWater::class,
        EventTargetSpecies::class,
        Fish::class,                        // Backed up
        Fisherman::class,                   // Backed up
        Lure::class,                        // Backed up
        LureColor::class,                   // Backed up
        Note::class,
        Photo::class,                       // Backed up
        Species::class,                     // Backed up
        TackleBox::class,                   // Backed up
        Trip::class,                        // Backed up
        TripBodyOfWater::class,
        TripTargetSpecies::class,
        EventFishermanCrossRef::class,      // Backed up
        LureGlowColorCrossRef::class,       // Backed up
        LurePrimaryColorCrossRef::class,    // Backed up
        LureSecondaryColorCrossRef::class,  // Backed up
        NoteFishCrossRef::class,
        NoteEventCrossRef::class,
        NoteTripCrossRef::class,
        PhotoBodyOfWaterCrossRef::class,
        PhotoEventCrossRef::class,
        PhotoFishCrossRef::class,
        PhotoFishermanCrossRef::class,
        PhotoLureCrossRef::class,
        PhotoSpeciesCrossRef::class,
        PhotoTripCrossRef::class,
        TackleBoxLureCrossRef::class,       // Backed up
        TripFishermanCrossRef::class        // Backed up
    ],
    views = [
        EventDetailedSummary::class,
        TripDetailedSummary::class
    ],
    version = 8,
    exportSchema = false
)
abstract class FishstoryDatabase : RoomDatabase() {
    abstract fun bodyOfWaterDao(): BodyOfWaterDao
    abstract fun eventDao(): EventDao
    abstract fun fishDao(): FishDao
    abstract fun fishermanDao(): FishermanDao
    abstract fun lureDao(): LureDao
    abstract fun noteDao(): NoteDao
    abstract fun photoDao(): PhotoDao
    abstract fun tackleBoxDao(): TackleBoxDao
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: FishstoryDatabase? = null

        fun getDatabase(context: Context): FishstoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FishstoryDatabase::class.java,
                    "fishstory_db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
