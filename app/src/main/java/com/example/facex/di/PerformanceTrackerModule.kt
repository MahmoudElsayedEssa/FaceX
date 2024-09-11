package com.example.facex.di

import com.example.facex.domain.usecase.PerformanceTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PerformanceTrackerModule {

    @Provides
    @Singleton
    fun providePerformanceTracker(): PerformanceTracker {
        return PerformanceTracker()
    }
}