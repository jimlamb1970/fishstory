package com.funjim.fishstory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.funjim.fishstory.model.*

@Database(
    entities = [
        Trip::class, 
        Fisherman::class, 
        TripFishermanCrossRef::class, 
        Segment::class, 
        Lure::class, 
        LureColor::class,
        Fish::class,
        Species::class,
        Photo::class,
        TackleBox::class,
        TackleBoxLureCrossRef::class,
        SegmentFishermanCrossRef::class
    ], 
    version = 21,
    exportSchema = false
)
abstract class FishstoryDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun fishermanDao(): FishermanDao
    abstract fun segmentDao(): SegmentDao
    abstract fun lureDao(): LureDao
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
