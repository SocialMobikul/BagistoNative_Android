package com.masilotti.demo.components.features.image_search

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@androidx.camera.core.ExperimentalGetImage
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun TextSearchScreen(
    onTextDetected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var detectedWords by remember { mutableStateOf<Set<String>>(emptySet()) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }

                    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                val mediaImage = imageProxy.image ?: return@setAnalyzer
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )

                                textRecognizer.process(image)
                                    .addOnSuccessListener { visionText ->
                                        val words = visionText.text
                                            .split("\\s+".toRegex()) // Split by whitespace
                                            .filter { it.length > 2 } // Filter out short words
                                            .map { it.trim().lowercase() } // Normalize
                                            .filterNot { word ->
                                                word.isEmpty() || detectedWords.contains(word)
                                            }

                                        if (words.isNotEmpty()) {
                                            detectedWords = detectedWords + words
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("TextSearch", "Text recognition failed", e)
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            }
                        }

                    ProcessCameraProvider.getInstance(ctx).addListener({
                        val cameraProvider = ProcessCameraProvider.getInstance(ctx).get()
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("TextSearch", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Vertical list of detected words on the left
        LazyColumn(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        ) {
            itemsIndexed(detectedWords.toList()) { _, word ->
                WordCard(word) {
                    onTextDetected(word)
                }
            }
        }
    }

    BackHandler(onBack = onBack)
}

@Composable
fun WordCard(word: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .width(160.dp)
            .border(1.dp, Color.White, shape = RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            text = word,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}