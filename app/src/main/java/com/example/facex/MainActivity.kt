package com.example.facex

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.example.facex.ui.helpers.CpuCollector
import com.example.facex.ui.screens.AppNavigation
import com.example.facex.ui.theme.FaceXTheme
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.android.OpenCVLoader
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initOpenCV()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            FaceXTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                ) { _ ->
                    AppNavigation(navController)
                }
            }
        }
    }

    private fun initOpenCV() {
        if (OpenCVLoader.initLocal()) {
            try {
                Log.d("OpenCV", "Library loaded successfully! Core.NATIVE_LIBRARY_NAME")
            } catch (e: UnsatisfiedLinkError) {
                Log.e("OpenCV", "Failed to load OpenCV library: ${e.message}")
            }

        } else {
            Log.d(TAG, "initOpenCV: OpenCV loading failure")
        }
    }

    override fun onPause() {
        super.onPause()
        CpuCollector.stopCollecting()
    }

    override fun onResume() {
        super.onResume()
        CpuCollector.startCollecting(
            interval = 500.milliseconds,
            onError = { error ->
                Log.e("CPU", "Collection error", error)
            }
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        CpuCollector.shutdown()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

