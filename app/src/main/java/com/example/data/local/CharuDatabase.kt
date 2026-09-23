package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CommandLogEntity::class], version = 1, exportSchema = false)
abstract class CharuDatabase : RoomDatabase() {
    abstract fun commandLogDao(): CommandLogDao

    companion object {
        @Volatile
        private var INSTANCE: CharuDatabase? = null

        fun getInstance(context: Context): CharuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CharuDatabase::class.java,
                    "charu_assistant_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
