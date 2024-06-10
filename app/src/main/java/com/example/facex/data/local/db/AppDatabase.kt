package com.example.facex.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PersonDTO::class], version = 1, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {
    abstract fun personDao(): PersonDao
}
