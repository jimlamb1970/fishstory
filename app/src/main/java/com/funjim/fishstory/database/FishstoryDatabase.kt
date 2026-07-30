package com.funjim.fishstory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.funjim.fishstory.model.*

@Database(
    entities = [
        Bait::class,
        BodyOfWater::class,
        Event::class,
        EventBait::class,
        EventBodyOfWater::class,
        EventFishermanCrossRef::class,
        EventTargetSpecies::class,
        Fish::class,
        Fisherman::class,
        Lure::class,
        LureColor::class,
        LureGlowColorCrossRef::class,
        LurePrimaryColorCrossRef::class,
        LureSecondaryColorCrossRef::class,
        Note::class,
        NoteFishCrossRef::class,
        NoteEventCrossRef::class,
        NoteTripCrossRef::class,
        Photo::class,
        PhotoBaitCrossRef::class,
        PhotoBodyOfWaterCrossRef::class,
        PhotoEventCrossRef::class,
        PhotoFishCrossRef::class,
        PhotoFishermanCrossRef::class,
        PhotoLureCrossRef::class,
        PhotoSpeciesCrossRef::class,
        PhotoTripCrossRef::class,
        Species::class,
        TackleBox::class,
        TackleBoxLureCrossRef::class,
        Trip::class,
        TripBait::class,
        TripBodyOfWater::class,
        TripFishermanCrossRef::class,
        TripTargetSpecies::class,
        Water::class,
        WaterClarity::class
    ],
    views = [
        EventDetailedSummary::class,
        TripDetailedSummary::class
    ],
    version = 10,
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
    abstract fun waterDao(): WaterDao

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
