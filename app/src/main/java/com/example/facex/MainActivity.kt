package com.example.facex

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.facex.ui.screens.camera_face_recognition.RecognitionRoute
import com.example.facex.ui.theme.FaceXTheme
import dagger.hilt.android.AndroidEntryPoint
import org.opencv.android.OpenCVLoader

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initOpenCV()
        enableEdgeToEdge()
        setContent {
            FaceXTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    Log.d(TAG, "onCreate: ")
                    RecognitionRoute()
                }
            }
        }
    }

    private fun initOpenCV() {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "initOpenCV: OpenCV loaded successfully")
        } else {
            Log.d(TAG, "initOpenCV: OpenCV loading failure")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    RecognitionRoute()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FaceXTheme {
        Greeting("Android")
    }
}