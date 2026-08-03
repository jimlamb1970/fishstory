package com.funjim.fishstory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.funjim.fishstory.model.*

@Database(
    entities = [
        BaitEntity::class,
        BodyOfWaterEntity::class,
        EventEntity::class,
        EventBaitEntity::class,
        EventBodyOfWaterEntity::class,
        EventFishermanEntity::class,
        EventTargetSpeciesEntity::class,
        FishEntity::class,
        FishermanEntity::class,
        LureEntity::class,
        LureColorEntity::class,
        LureGlowColorEntity::class,
        LurePrimaryColorEntity::class,
        LureSecondaryColorEntity::class,
        NoteEntity::class,
        NoteFishEntity::class,
        NoteEventEntity::class,
        NoteTripEntity::class,
        PhotoEntity::class,
        PhotoBaitEntity::class,
        PhotoBodyOfWaterEntity::class,
        PhotoEventEntity::class,
        PhotoFishEntity::class,
        PhotoFishermanEntity::class,
        PhotoLureEntity::class,
        PhotoSpeciesEntity::class,
        PhotoTripEntity::class,
        SkyConditionEntity::class,
        SpeciesEntity::class,
        TackleBoxEntity::class,
        TackleBoxLureEntity::class,
        TripEntity::class,
        TripBaitEntity::class,
        TripBodyOfWaterEntity::class,
        TripFishermanEntity::class,
        TripTargetSpeciesEntity::class,
        WaterEntity::class,
        WaterClarityEntity::class,
        WeatherEntity::class
    ],
    views = [
        EventEntityDetailedSummary::class,
        TripEntityDetailedSummary::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(WeatherConverters::class)
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
    abstract fun weatherDao(): WeatherDao

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
