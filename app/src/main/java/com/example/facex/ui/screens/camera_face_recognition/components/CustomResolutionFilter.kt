package com.example.facex.ui.screens.camera_face_recognition.components

import android.util.Size
import androidx.camera.core.resolutionselector.ResolutionFilter

class CustomResolutionFilter : ResolutionFilter {
    override fun filter(
        supportedSizes: List<Size>,
        rotationDegrees: Int
    ): List<Size> {
        // Apply custom filtering and sorting logic here
        val sortedSizes = supportedSizes
            .filter { it.width == 1280 && it.height == 720 }
            .sortedByDescending { it.width * it.height }

        return sortedSizes
    }
}
