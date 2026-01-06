package com.masilotti.demo.components.features

import android.annotation.SuppressLint
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


import androidx.camera.core.TorchState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.delay
import kotlin.math.min


@Composable
fun QrScannerScreen(
    onScanComplete: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var scanned by remember { mutableStateOf(false) }
    var hasFlash by remember { mutableStateOf(false) }

    // Animation state
    var isMovingDown by remember { mutableStateOf(true) }
    var lineY by remember { mutableStateOf(0f) }
    val frameDuration = 16L // ~60fps
    val animationDuration = 3000L // 3 seconds for full sweep

    // Animation controller
    LaunchedEffect(Unit) {
        while (!scanned) {
            val startTime = System.currentTimeMillis()
            val endTime = startTime + animationDuration
            val startY = lineY
            val targetY = if (isMovingDown) 1f else 0f

            while (System.currentTimeMillis() < endTime && !scanned) {
                val progress = min(
                    1f,
                    (System.currentTimeMillis() - startTime).toFloat() / animationDuration
                )
                lineY = startY + (targetY - startY) * progress
                delay(frameDuration)
            }

            if (!scanned) {
                lineY = targetY
                isMovingDown = !isMovingDown
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(android.util.Size(1080, 1920)) // Add target resolution
                        .build()
                        .also {
                            it.setAnalyzer(
                                ContextCompat.getMainExecutor(ctx),
                                QrCodeAnalyzer(
                                    onBarcodeDetected = { barcode ->
                                        if (!scanned) {
                                            scanned = true
                                            onScanComplete(barcode)
                                            onBack()
                                        }
                                    },
                                    isAlreadyScanned = { scanned } // Pass the state
                                )
                            )
                        }

                    ProcessCameraProvider.getInstance(ctx).addListener({
                        val cameraProvider = ProcessCameraProvider.getInstance(ctx).get()
                        try {
                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            camera.cameraInfo.torchState.observe(lifecycleOwner) { torchState ->
                                hasFlash = torchState == TorchState.ON
                            }
                        } catch (e: Exception) {
                            Log.e("QrScanner", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Scanner Frame UI
        Box(modifier = Modifier.fillMaxSize()) {
            // Dark overlay outside scanning area
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val frameSize = minOf(width, height) * 0.7f
                val left = (width - frameSize) / 2
                val top = (height - frameSize) / 2

                // Draw dark overlay
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    size = Size(width, top)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    topLeft = Offset(0f, top),
                    size = Size(left, frameSize)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    topLeft = Offset(left + frameSize, top),
                    size = Size(width - left - frameSize, frameSize)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    topLeft = Offset(0f, top + frameSize),
                    size = Size(width, height - top - frameSize)
                )
            }

            // Single Animated scanning line
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val frameSize = minOf(width, height) * 0.7f
                val left = (width - frameSize) / 2
                val top = (height - frameSize) / 2
                val currentLineY = top + (frameSize * lineY)

                drawLine(
                    color = Color.Red,
                    start = Offset(left, currentLineY),
                    end = Offset(left + frameSize, currentLineY),
                    strokeWidth = 8f
                )
            }

            // Flash toggle button at bottom center
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                IconButton(
                    onClick = {
                        ProcessCameraProvider.getInstance(context).addListener({
                            val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                            try {
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                ).cameraControl.enableTorch(!hasFlash)
                            } catch (e: Exception) {
                                Log.e("QrScanner", "Flash toggle failed", e)
                            }
                        }, ContextCompat.getMainExecutor(context))
                    }
                ) {
                    Icon(
                        imageVector = if (hasFlash) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                        contentDescription = "Toggle Flash",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }

    BackHandler(onBack = onBack)
}

private class QrCodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit,
    private val isAlreadyScanned: () -> Boolean
) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (isAlreadyScanned()) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            Log.d("QrScanner", "Starting barcode scan")
            val scanner = BarcodeScanning.getClient()
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    Log.d("QrScanner", "Scan completed with ${barcodes.size} codes")
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes.first()
                        Log.d("QrScanner", "Detected barcode: ${barcode.rawValue}")
                        barcode.rawValue?.let { onBarcodeDetected(it) }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QrScanner", "Barcode scan failed", e)
                }
                .addOnCompleteListener {
                    Log.d("QrScanner", "Scan process completed")
                    imageProxy.close()
                }
        } else {
            Log.d("QrScanner", "Image is null")
            imageProxy.close()
            return
        }
    }
}