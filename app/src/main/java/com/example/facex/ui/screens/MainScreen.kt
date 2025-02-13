package com.example.facex.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.facex.ui.screens.camera_face_recognition.RecognitionRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen() {
    val permissions = remember {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            listOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            listOf(android.Manifest.permission.CAMERA)
        }
    }
    val cameraPermissionState = rememberMultiplePermissionsState(permissions = permissions)

    MainContent(
        hasPermission = cameraPermissionState.allPermissionsGranted,
        onRequestPermission = cameraPermissionState::launchMultiplePermissionRequest
    )
}

@RequiresApi(Build.VERSION_CODES.P)
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
