package com.example.facex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.facex.ui.screens.camera_face_recognition.RecognitionRoute
import com.example.facex.ui.theme.FaceXTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FaceXTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    MainScreen()
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {

    val cameraPermissionState: MultiplePermissionsState =
        rememberMultiplePermissionsState(
            permissions = listOf(
                android.Manifest.permission.CAMERA
            )
        )

    MainContent(
        hasPermission = cameraPermissionState.allPermissionsGranted,
        onRequestPermission = cameraPermissionState::launchMultiplePermissionRequest
    )

}

@Composable
private fun MainContent(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
) {
    if (hasPermission) {
        RecognitionRoute()
    } else {
        LaunchedEffect(key1 = "requestPermission") {
            onRequestPermission()
        }
    }
}
