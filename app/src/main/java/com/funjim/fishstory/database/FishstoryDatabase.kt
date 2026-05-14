package com.funjim.fishstory.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.funjim.fishstory.model.*

@Database(
    entities = [
        Event::class,
        Fish::class,
        Fisherman::class,
        Lure::class,
        LureColor::class,
        Note::class,
        Photo::class,
        Species::class,
        TackleBox::class,
        Trip::class,
        EventFishermanCrossRef::class,
        TripFishermanCrossRef::class,
        TackleBoxLureCrossRef::class,
        NoteFishCrossRef::class,
        NoteEventCrossRef::class,
        NoteTripCrossRef::class,
        PhotoEventCrossRef::class,
        PhotoFishCrossRef::class,
        PhotoFishermanCrossRef::class,
        PhotoLureCrossRef::class,
        PhotoSpeciesCrossRef::class,
        PhotoTripCrossRef::class
    ],
    version = 1,
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
