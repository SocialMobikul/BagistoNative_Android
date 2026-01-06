package com.masilotti.demo.components.features.image_search

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class ImageLabelAnalyzer(
    private val onLabelsDetected: (List<String>) -> Unit
) : ImageAnalysis.Analyzer {

    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

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
