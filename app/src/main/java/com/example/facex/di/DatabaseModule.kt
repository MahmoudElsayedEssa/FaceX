package com.example.facex.di

import android.content.Context
import androidx.room.Room
import com.example.facex.data.local.db.AppDatabase
import com.example.facex.data.local.db.PersonDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "face-recognition.db"
        ).build()
    }

    @Singleton
    @Provides
    fun providePersonDao(database: AppDatabase): PersonDao {
        return database.personDao()
    }


}
