package com.funjim.fishstory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.funjim.fishstory.model.*

@Database(
    entities = [
        Bait::class,
        BodyOfWater::class,                 // Backed up
        Event::class,                       // Backed up
        EventBait::class,
        EventBodyOfWater::class,
        EventFishermanCrossRef::class,      // Backed up
        EventTargetSpecies::class,
        Fish::class,                        // Backed up
        Fisherman::class,                   // Backed up
        Lure::class,                        // Backed up
        LureColor::class,                   // Backed up
        LureGlowColorCrossRef::class,       // Backed up
        LurePrimaryColorCrossRef::class,    // Backed up
        LureSecondaryColorCrossRef::class,  // Backed up
        Note::class,
        NoteFishCrossRef::class,
        NoteEventCrossRef::class,
        NoteTripCrossRef::class,
        Photo::class,                       // Backed up
        PhotoBaitCrossRef::class,
        PhotoBodyOfWaterCrossRef::class,
        PhotoEventCrossRef::class,
        PhotoFishCrossRef::class,
        PhotoFishermanCrossRef::class,
        PhotoLureCrossRef::class,
        PhotoSpeciesCrossRef::class,
        PhotoTripCrossRef::class,
        Species::class,                     // Backed up
        TackleBox::class,                   // Backed up
        TackleBoxLureCrossRef::class,
        Trip::class,                        // Backed up
        TripBait::class,
        TripBodyOfWater::class,
        TripFishermanCrossRef::class,
        TripTargetSpecies::class
    ],
    views = [
        EventDetailedSummary::class,
        TripDetailedSummary::class
    ],
    version = 9,
    exportSchema = false
)
abstract class FishstoryDatabase : RoomDatabase() {
    abstract fun baitDao(): BaitDao
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
