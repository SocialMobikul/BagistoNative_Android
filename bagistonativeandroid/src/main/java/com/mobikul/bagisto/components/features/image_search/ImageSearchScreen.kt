package com.mobikul.bagisto.components.features.image_search

import android.util.Log
import android.view.ViewGroup
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.BackHandler
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

enum class SearchState { LIVE, CAPTURED, RESULT }

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnsafeOptInUsageError")
@Composable
fun ImageSearchScreen(
    onLabelSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var searchState by remember { mutableStateOf(SearchState.LIVE) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var labels by remember { mutableStateOf<List<String>>(emptyList()) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var isSuggestionsExpanded by remember { mutableStateOf(false) }

    val suggestionCountText = if (labels.size == 1) "1 Suggestion Found" else "${labels.size} Suggestions Found"

    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    val showResizedCaptured = isSuggestionsExpanded && searchState != SearchState.LIVE
    val sceneModifier = if (showResizedCaptured) {
        Modifier.fillMaxWidth().fillMaxHeight(0.35f)
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = Modifier.fillMaxSize().background(if (isSuggestionsExpanded) Color.White else Color.Black)) {
        Box(modifier = sceneModifier) {
            if (searchState == SearchState.LIVE) {
                AndroidView(
                    factory = { ctx ->
                        previewView.apply {
                            layoutParams = ViewGroup.LayoutParams(-1, -1)
                            val preview = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
                            ProcessCameraProvider.getInstance(ctx).addListener({
                                val cameraProvider = ProcessCameraProvider.getInstance(ctx).get()
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                                } catch (e: Exception) { Log.e("Search", "Err", e) }
                            }, ContextCompat.getMainExecutor(ctx))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                capturedBitmap?.let { bmp ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        val ratio = bmp.width.toFloat() / bmp.height.toFloat()
                        val imageModifier = if (showResizedCaptured) Modifier.fillMaxHeight().aspectRatio(ratio) else Modifier.fillMaxSize()
                        val scale = if (showResizedCaptured) ContentScale.Fit else ContentScale.Crop
                        Image(bmp.asImageBitmap(), null, imageModifier, contentScale = scale)
                    }
                }
            }
        }

        Surface(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 60.dp, end = 24.dp).size(44.dp),
            shape = CircleShape, color = Color.Black.copy(0.4f), contentColor = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Close, null, modifier = Modifier.size(24.dp)) }
        }

        AnimatedVisibility(
            visible = (searchState == SearchState.LIVE || searchState == SearchState.CAPTURED || (searchState == SearchState.RESULT && !isSuggestionsExpanded)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
             Column(modifier = Modifier.padding(start = 24.dp, top = 0.dp, end = 24.dp, bottom = 100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                 if (searchState == SearchState.LIVE) {
                    Box(modifier = Modifier.size(76.dp).border(4.5.dp, Color.White, CircleShape).padding(7.dp).clip(CircleShape).background(Color.White).clickable {
                        imageCapture.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                capturedBitmap = image.toBitmap().rotate(image.imageInfo.rotationDegrees)
                                image.close()
                                searchState = SearchState.CAPTURED
                            }
                            override fun onError(exc: ImageCaptureException) {}
                        })
                    })
                    Spacer(modifier = Modifier.height(36.dp))
                    Pill(text = suggestionCountText, expanded = isSuggestionsExpanded) {
                        if (searchState == SearchState.RESULT) isSuggestionsExpanded = !isSuggestionsExpanded
                    }
                } else if (searchState == SearchState.CAPTURED || searchState == SearchState.RESULT) {
                     Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                         Btn(text = "Retake", isBlue = false, modifier = Modifier.weight(1f)) { 
                             searchState = SearchState.LIVE 
                             labels = emptyList()
                             isSuggestionsExpanded = false
                         }
                         Btn(text = if (isAnalyzing) "Analysing..." else "Search", isBlue = true, modifier = Modifier.weight(1f)) {
                            if (!isAnalyzing) {
                                scope.launch {
                                    isAnalyzing = true
                                    capturedBitmap?.let { bmp ->
                                        labels = runSimplifiedHybridSearch(bmp)
                                        searchState = SearchState.RESULT
                                        isSuggestionsExpanded = labels.isNotEmpty()
                                    }
                                    isAnalyzing = false
                                }
                            }
                        }
                     }
                     Pill(text = suggestionCountText, expanded = isSuggestionsExpanded) {
                         if (searchState == SearchState.RESULT) isSuggestionsExpanded = !isSuggestionsExpanded
                     }
                }
             }
        }

        AnimatedVisibility(
            visible = isSuggestionsExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.65f).clip(RoundedCornerShape(28.dp, 28.dp, 0.dp, 0.dp)).background(Color.White)) {
                Surface(modifier = Modifier.fillMaxWidth().padding(20.dp), shape = RoundedCornerShape(12.dp), color = Color(0xFFF2F2F7)) {
                    Row(modifier = Modifier.padding(20.dp, 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Results Found: ${labels.size}", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.KeyboardArrowDown, null, tint = Color.Gray, modifier = Modifier.rotate(180f).clickable { isSuggestionsExpanded = false })
                    }
                }
                Spacer(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(labels) { _, label ->
                        Text(label, modifier = Modifier.fillMaxWidth().clickable { onLabelSelected(label) }.padding(24.dp, 18.dp), fontSize = 17.sp, color = Color.Black)
                        Spacer(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFFF2F2F7)).padding(horizontal = 24.dp))
                    }
                }
            }
        }
    }

    var collapsedFromExpanded by remember { mutableStateOf(false) }

    BackHandler {
        when {
            isSuggestionsExpanded -> {
                isSuggestionsExpanded = false
                collapsedFromExpanded = true
            }
            collapsedFromExpanded -> onBack()
            searchState == SearchState.RESULT -> searchState = SearchState.CAPTURED
            searchState == SearchState.CAPTURED -> searchState = SearchState.LIVE
            else -> onBack()
        }
    }
}

suspend fun runSimplifiedHybridSearch(bitmap: Bitmap): List<String> = withContext(Dispatchers.Default) {
    if (bitmap.isRecycled) return@withContext emptyList()
    
    val inputImage = InputImage.fromBitmap(bitmap, 0)
    val pool = mutableListOf<Pair<String, Float>>()

    val OCR_CONFIDENCE = 0.6f
    val LABEL_THRESHOLD = 0.45f
    val CLASSIFICATION_THRESHOLD = 0.4f
    val FINAL_MIN_CONFIDENCE = 0.4f


    try {
        coroutineScope {
            launch {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val result = try { recognizer.process(inputImage).await() } catch (e: Exception) { null }
                result?.textBlocks?.forEach { block ->
                    val text = block.text.trim()
                    if (isValidOCRText(text)) {
                        synchronized(pool) {
                            pool.add(text to OCR_CONFIDENCE)
                        }
                    }
                }
            }

            launch {
                val labeler = ImageLabeling.getClient(
                    ImageLabelerOptions.Builder()
                        .setConfidenceThreshold(LABEL_THRESHOLD)
                        .build()
                )
                val labels = try { labeler.process(inputImage).await() } catch (e: Exception) { emptyList() }
                synchronized(pool) {
                    labels
                        .filter { label -> label.confidence >= LABEL_THRESHOLD }
                        .forEach { pool.add(it.text to it.confidence) }
                }
            }

            launch {
                val detector = ObjectDetection.getClient(
                    ObjectDetectorOptions.Builder()
                        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .build()
                )
                val objects = try { detector.process(inputImage).await() } catch (e: Exception) { emptyList() }
                synchronized(pool) {
                    objects
                        .filter { it.labels.isNotEmpty() }
                        .forEach { detectedObject ->
                            detectedObject.labels
                                .filter { label -> label.confidence >= CLASSIFICATION_THRESHOLD }
                                .forEach { label -> pool.add(label.text to label.confidence) }
                        }
                }
            }
        }
    } catch (e: Exception) { Log.e("SimplifiedSearch", "Error during search", e) }

    return@withContext pool
        .asSequence()
        .groupBy { it.first.lowercase().trim() }
        .map { (text, pairs) ->
            text to (pairs.maxOfOrNull { it.second } ?: 0f)
        }
        .filter { (_, confidence) ->
            confidence >= FINAL_MIN_CONFIDENCE
        }
        .sortedByDescending { it.second }
        .take(12)
        .map { it.first }
        .toList()
}

private fun isValidOCRText(text: String): Boolean {
    if (text.length < 3) return false
    if (text.matches(Regex("^[^a-zA-Z0-9]*$"))) return false
    if (!text.any { it.isLetterOrDigit() }) return false
    if (text.matches(Regex("^(\\w)\\1{5,}$"))) return false
    if (text.matches(Regex("^[0-9]*$"))) return false

    return true
}

@Composable
fun Pill(text: String, expanded: Boolean, onToggle: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White.copy(0.96f), modifier = Modifier.fillMaxWidth().height(54.dp)) {
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, color = Color.Black, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.KeyboardArrowUp, null, tint = Color.LightGray, modifier = Modifier.size(22.dp).rotate(if (expanded) 180f else 0f).clickable { onToggle() } )
        }
    }
}

@Composable
fun Btn(text: String, isBlue: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, color = if (isBlue) Color(0xFF007AFF) else Color(0xFF333333), shape = RoundedCornerShape(14.dp), modifier = modifier.height(52.dp)) {
        Box(contentAlignment = Alignment.Center) { Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }
    }
}

fun ImageProxy.toBitmap(): Bitmap {
    val b = planes[0].buffer; val bytes = ByteArray(b.remaining()); b.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

fun Bitmap.rotate(deg: Int): Bitmap {
    if (deg == 0) return this
    val m = Matrix().apply { postRotate(deg.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, m, true)
}
