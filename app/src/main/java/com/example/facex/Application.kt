package com.example.facex

import android.app.Application
import android.util.Log
import com.example.facex.data.local.ml.liteRT.modelhandling.DefaultModelsInitializer
import com.example.facex.ui.helpers.CpuCollector
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltAndroidApp
class FaceXApplication : Application() {
    @Inject
    lateinit var defaultModelsInitializer: DefaultModelsInitializer

    override fun onCreate() {
        super.onCreate()


        CoroutineScope(Dispatchers.IO).launch {
            defaultModelsInitializer.initializeDefaultModels()
        }
    }


}
