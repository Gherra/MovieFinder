package com.ramankumar.moviefinder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ramankumar.moviefinder.data.local.dao.FavoriteDao
import com.ramankumar.moviefinder.data.local.dao.MovieDao
import com.ramankumar.moviefinder.data.local.dao.SwipeHistoryDao
import com.ramankumar.moviefinder.data.local.entities.FavoriteEntity
import com.ramankumar.moviefinder.data.local.entities.MovieEntity
import com.ramankumar.moviefinder.data.local.entities.SwipeHistoryEntity

@Database(
    entities = [
        MovieEntity::class,
        SwipeHistoryEntity::class,
        FavoriteEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao
    abstract fun swipeHistoryDao(): SwipeHistoryDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moviefinder_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}