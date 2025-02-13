package com.example.facex.di

import com.example.facex.data.local.DefaultLogger
import com.example.facex.data.local.DefaultPerformanceTracker
import com.example.facex.domain.logger.Logger
import com.example.facex.domain.performancetracking.PerformanceTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PerformanceModule {
    @Provides
    @Singleton
    fun providePerformanceTracker(): PerformanceTracker = DefaultPerformanceTracker

    @Singleton
    @Provides
    fun provideLogger(): Logger = DefaultLogger()

}
