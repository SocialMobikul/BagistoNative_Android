package com.masilotti.demo.components.features.image_search

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@Composable
fun ImageSearchScreen(
    onLabelSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var labels by remember { mutableStateOf<List<String>>(emptyList()) }

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

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(ctx), ImageLabelAnalyzer { result ->
                                // Update only if a new label is detected
                                val newLabels = result.filterNot { labels.contains(it) }
                                if (newLabels.isNotEmpty()) {
                                    //val updated = (newLabels + labels).distinct().take(10)
                                    val updated = (newLabels + labels).distinct()
                                    labels = updated
                                }
                            })
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
                            Log.e("ImageSearch", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Vertical list of horizontally styled label cards on the left
        LazyColumn(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        ) {
            itemsIndexed(labels) { _, label ->
                LabelCard(label) {
                    onLabelSelected(label)
                }
            }
        }

    }

   BackHandler(onBack = onBack)
}

@Composable
fun LabelCard(label: String, onClick: () -> Unit) {
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
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

