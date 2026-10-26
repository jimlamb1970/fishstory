package com.funjim.fishstory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.funjim.fishstory.database.dao.BaitDao
import com.funjim.fishstory.database.dao.BodyOfWaterDao
import com.funjim.fishstory.database.dao.EventDao
import com.funjim.fishstory.database.dao.FishDao
import com.funjim.fishstory.database.dao.FishermanDao
import com.funjim.fishstory.database.dao.LimitDao
import com.funjim.fishstory.database.dao.LureDao
import com.funjim.fishstory.database.dao.NoteDao
import com.funjim.fishstory.database.dao.PhotoDao
import com.funjim.fishstory.database.dao.TackleBoxDao
import com.funjim.fishstory.database.dao.TripDao
import com.funjim.fishstory.database.dao.WaterDao
import com.funjim.fishstory.database.dao.WeatherDao
import com.funjim.fishstory.database.entity.BaitEntity
import com.funjim.fishstory.database.entity.BodyOfWaterEntity
import com.funjim.fishstory.database.entity.EventBaitEntity
import com.funjim.fishstory.database.entity.EventBodyOfWaterEntity
import com.funjim.fishstory.database.entity.EventEntity
import com.funjim.fishstory.database.entity.EventEntityDetailedSummary
import com.funjim.fishstory.database.entity.EventFishermanEntity
import com.funjim.fishstory.database.entity.EventLimitEntity
import com.funjim.fishstory.database.entity.EventTargetSpeciesEntity
import com.funjim.fishstory.database.entity.FishEntity
import com.funjim.fishstory.database.entity.FishermanEntity
import com.funjim.fishstory.database.entity.LimitEntity
import com.funjim.fishstory.database.entity.LimitSpeciesEntity
import com.funjim.fishstory.database.entity.LureColorEntity
import com.funjim.fishstory.database.entity.LureEntity
import com.funjim.fishstory.database.entity.LureGlowColorEntity
import com.funjim.fishstory.database.entity.LurePrimaryColorEntity
import com.funjim.fishstory.database.entity.LureSecondaryColorEntity
import com.funjim.fishstory.database.entity.NoteEntity
import com.funjim.fishstory.database.entity.NoteEventEntity
import com.funjim.fishstory.database.entity.NoteFishEntity
import com.funjim.fishstory.database.entity.NoteTripEntity
import com.funjim.fishstory.database.entity.PhotoBaitEntity
import com.funjim.fishstory.database.entity.PhotoBodyOfWaterEntity
import com.funjim.fishstory.database.entity.PhotoEntity
import com.funjim.fishstory.database.entity.PhotoEventEntity
import com.funjim.fishstory.database.entity.PhotoFishEntity
import com.funjim.fishstory.database.entity.PhotoFishermanEntity
import com.funjim.fishstory.database.entity.PhotoLureEntity
import com.funjim.fishstory.database.entity.PhotoSkyConditionEntity
import com.funjim.fishstory.database.entity.PhotoSpeciesEntity
import com.funjim.fishstory.database.entity.PhotoTripEntity
import com.funjim.fishstory.database.entity.PhotoWaterClarityEntity
import com.funjim.fishstory.database.entity.SkyConditionEntity
import com.funjim.fishstory.database.entity.SpeciesEntity
import com.funjim.fishstory.database.entity.TackleBoxEntity
import com.funjim.fishstory.database.entity.TackleBoxLureEntity
import com.funjim.fishstory.database.entity.TripBaitEntity
import com.funjim.fishstory.database.entity.TripBodyOfWaterEntity
import com.funjim.fishstory.database.entity.TripEntity
import com.funjim.fishstory.database.entity.TripEntityDetailedSummary
import com.funjim.fishstory.database.entity.TripFishermanEntity
import com.funjim.fishstory.database.entity.TripLimitEntity
import com.funjim.fishstory.database.entity.TripTargetSpeciesEntity
import com.funjim.fishstory.database.entity.WaterClarityEntity
import com.funjim.fishstory.database.entity.WaterEntity
import com.funjim.fishstory.database.entity.WeatherEntity

@Database(
    entities = [
        BaitEntity::class,
        BodyOfWaterEntity::class,
        EventEntity::class,
        EventBaitEntity::class,
        EventBodyOfWaterEntity::class,
        EventFishermanEntity::class,
        EventLimitEntity::class,
        EventTargetSpeciesEntity::class,
        FishEntity::class,
        FishermanEntity::class,
        LimitEntity::class,
        LimitSpeciesEntity::class,
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
        PhotoSkyConditionEntity::class,
        PhotoSpeciesEntity::class,
        PhotoTripEntity::class,
        PhotoWaterClarityEntity::class,
        SkyConditionEntity::class,
        SpeciesEntity::class,
        TackleBoxEntity::class,
        TackleBoxLureEntity::class,
        TripEntity::class,
        TripBaitEntity::class,
        TripBodyOfWaterEntity::class,
        TripFishermanEntity::class,
        TripLimitEntity::class,
        TripTargetSpeciesEntity::class,
        WaterEntity::class,
        WaterClarityEntity::class,
        WeatherEntity::class
    ],
    views = [
        EventEntityDetailedSummary::class,
        TripEntityDetailedSummary::class
    ],
    version = 11,
    exportSchema = false
)
@TypeConverters(WeatherConverters::class, LimitTypeConverter::class)
abstract class FishstoryDatabase : RoomDatabase() {
    abstract fun baitDao(): BaitDao
    abstract fun bodyOfWaterDao(): BodyOfWaterDao
    abstract fun eventDao(): EventDao
    abstract fun fishDao(): FishDao
    abstract fun fishermanDao(): FishermanDao
    abstract fun limitDao(): LimitDao
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
