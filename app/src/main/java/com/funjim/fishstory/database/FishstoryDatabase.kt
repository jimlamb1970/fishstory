package com.funjim.fishstory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.funjim.fishstory.model.*

@Database(
    entities = [
        Event::class,                       // Backed up
        Fish::class,                        // Backed up
        Fisherman::class,                   // Backed up
        Lure::class,                        // Backed up
        LureColor::class,                   // Backed up
        Note::class,
        Photo::class,                       // Backed up
        Species::class,                     // Backed up
        TackleBox::class,                   // Backed up
        TargetSpecies::class,
        Trip::class,                        // Backed up
        EventFishermanCrossRef::class,      // Backed up
        LureGlowColorCrossRef::class,       // Backed up
        LurePrimaryColorCrossRef::class,    // Backed up
        LureSecondaryColorCrossRef::class,  // Backed up
        NoteFishCrossRef::class,
        NoteEventCrossRef::class,
        NoteTripCrossRef::class,
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
    version = 4,
    exportSchema = false
)
abstract class FishstoryDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun fishermanDao(): FishermanDao
    abstract fun eventDao(): EventDao
    abstract fun lureDao(): LureDao
    abstract fun noteDao(): NoteDao
    abstract fun fishDao(): FishDao
    abstract fun photoDao(): PhotoDao
    abstract fun tackleBoxDao(): TackleBoxDao

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
