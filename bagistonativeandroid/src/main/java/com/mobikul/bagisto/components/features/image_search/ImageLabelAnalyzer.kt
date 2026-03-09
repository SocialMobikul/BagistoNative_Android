package com.mobikul.bagisto.components.features.image_search

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

/**
 * Image labeling analyzer using Google ML Kit.
 * 
 * This class provides on-device image labeling capabilities to
 * identify products in images for reverse image search.
 * 
 * Features:
 * - On-device ML processing
 * - Confidence scoring
 * - Product label identification
 * 
 * @property classifier ML Kit image labeler instance
 * 
 * @param onLabelsDetected Callback invoked with detected labels
 * 
 * @see ImageSearchComponent
 * @see ImageSearchScreen
 * 
 * @constructor
 * @param context Android application context
 */
class ImageLabelAnalyzer(
    private val onLabelsDetected: (List<String>) -> Unit
) : ImageAnalysis.Analyzer {

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    /**
     * Analyze a camera frame for image labels.
     * 
     * Processes the image using ML Kit and invokes callback with
     * detected labels on success.
     * 
     * @param imageProxy The camera frame to analyze
     */
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        labeler.process(inputImage)
            .addOnSuccessListener { labels ->
                val labelNames = labels.mapNotNull { it.text }.distinct()
                onLabelsDetected(labelNames)
            }
            .addOnFailureListener { e ->
                Log.e("ImageLabelAnalyzer", "Labeling failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
