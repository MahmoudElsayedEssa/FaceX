package com.example.facex.di

import android.content.Context
import androidx.room.Room
import com.example.facex.data.local.db.AppDatabase
import com.example.facex.data.local.db.ModelConfigDao
import com.example.facex.data.local.db.PersonDao
import com.example.facex.data.repository.PersonRepositoryImpl
import com.example.facex.domain.repository.PersonRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "face-recognition.db",
    ).build()

    @Singleton
    @Provides
    fun providePersonDao(database: AppDatabase): PersonDao = database.personDao()

    @Provides
    @Singleton
    fun provideModelConfigDao(database: AppDatabase): ModelConfigDao = database.modelConfigDao()

    @Provides
    @Singleton
    fun providePersonRepository(
        personDao: PersonDao,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): PersonRepository = PersonRepositoryImpl(
        personDao = personDao,
        dispatcher = dispatcher
    )


}


