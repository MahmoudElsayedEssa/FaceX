package com.example.facex.di

import android.content.Context
import com.example.facex.data.local.ml.ModelStorageManager
import com.example.facex.data.local.ml.liteRT.ModelStorageManagerImpl
import com.example.facex.domain.logger.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun provideModelStorageManager(
        @ApplicationContext context: Context, logger: Logger
    ): ModelStorageManager = ModelStorageManagerImpl(context, logger)
}
