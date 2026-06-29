package com.example

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.print.PrintHelper
import coil.compose.AsyncImage
import com.example.ui.theme.MyApplicationTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkTheme by remember { mutableStateOf(true) }
            MyApplicationTheme(darkTheme = darkTheme) {
                MainAppScreen(
                    darkTheme = darkTheme,
                    onThemeToggle = { darkTheme = !darkTheme }
                )
            }
        }
    }
}

// Global standards definitions
data class SizingPreset(
    val name: String,
    val widthValue: Float,
    val heightValue: Float,
    val description: String
)

val SIZING_PRESETS = listOf(
    SizingPreset("US & India (2\" x 2\")", 51f, 51f, "Standard visa/passport photo. 1:1 Square aspect ratio."),
    SizingPreset("UK & Schengen (35x45 mm)", 35f, 45f, "Standard biometric passport size used in Europe/UK."),
    SizingPreset("Canada (50x70 mm)", 50f, 70f, "Standard Canadian visa, passport, or ID specifications."),
    SizingPreset("PAN Card / ID (25x35 mm)", 25f, 35f, "Compact size common for identity cards and profiles.")
)

sealed interface AppStep {
    object Welcome : AppStep
    object CropAlign : AppStep
    object BackgroundRemoval : AppStep
    object LayoutExport : AppStep
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    darkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Navigation and workflow states
    var currentStep by remember { mutableStateOf<AppStep>(AppStep.Welcome) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Image loading states
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Crop / Alignment states
    var cropWidthUnitVal by remember { mutableStateOf(300f) }
    var cropHeightUnitVal by remember { mutableStateOf(400f) }
    var selectedUnit by remember { mutableStateOf("px") } // mm, inch, px
    var dpiValue by remember { mutableStateOf(300) }

    // Transforms
    var imageScale by remember { mutableStateOf(1f) }
    var imageOffset by remember { mutableStateOf(Offset.Zero) }
    var imageRotation by remember { mutableStateOf(0f) }

    // Segmented / Background replaced bitmap
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var segmentedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var bgRemovedRawMask by remember { mutableStateOf<com.google.mlkit.vision.segmentation.SegmentationMask?>(null) }
    var selectedBgColor by remember { mutableStateOf(android.graphics.Color.WHITE) }
    var isSegmentationRunning by remember { mutableStateOf(false) }

    // Print & Layout sheet options
    var selectedPageType by remember { mutableStateOf("a4") } // a4, 4r, custom
    var customPageWidth by remember { mutableStateOf(210f) }
    var customPageHeight by remember { mutableStateOf(297f) }
    var gapHorizontalVal by remember { mutableStateOf(40f) }
    var gapVerticalVal by remember { mutableStateOf(40f) }
    var marginTopVal by remember { mutableStateOf(40f) }
    var marginLeftVal by remember { mutableStateOf(70f) }
    var photoCountInput by remember { mutableStateOf(14) }
    var showCutlines by remember { mutableStateOf(true) }
    var addBorder by remember { mutableStateOf(true) }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            scope.launch(Dispatchers.IO) {
                val bitmap = loadAndNormalizeBitmap(context, uri)
                if (bitmap != null) {
                    withContext(Dispatchers.Main) {
                        loadedBitmap = bitmap
                        // Reset layout and transforms
                        imageScale = 1f
                        imageOffset = Offset.Zero
                        imageRotation = 0f
                        currentStep = AppStep.CropAlign
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedImageUri = tempCameraUri
            scope.launch(Dispatchers.IO) {
                val bitmap = loadAndNormalizeBitmap(context, tempCameraUri!!)
                if (bitmap != null) {
                    withContext(Dispatchers.Main) {
                        loadedBitmap = bitmap
                        imageScale = 1f
                        imageOffset = Offset.Zero
                        imageRotation = 0f
                        currentStep = AppStep.CropAlign
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Camera permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                val uri = createTempImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Camera permission required to capture photo", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ComposeCanvas(modifier = Modifier.size(24.dp)) {
                            // Minimalist biometric scan vector logo
                            drawRoundRect(
                                color = ComposeColor(0xFF38BDF8),
                                size = size,
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawCircle(
                                color = ComposeColor(0xFF818CF8),
                                radius = size.minDimension / 4f,
                                center = center
                            )
                            drawLine(
                                color = ComposeColor(0xFF38BDF8),
                                start = Offset(0f, size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Passport Photo PRO",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onThemeToggle,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text(
                            text = if (darkTheme) "☀️" else "🌙",
                            fontSize = 24.sp
                        )
                    }
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Show Guidelines"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
        ) {
            when (currentStep) {
                AppStep.Welcome -> {
                    WelcomeView(
                        onSelectFromGallery = {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onCapturePhoto = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                scope.launch {
                                    val uri = createTempImageUri(context)
                                    tempCameraUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        onUsePreset = { preset ->
                            cropWidthUnitVal = preset.widthValue
                            cropHeightUnitVal = preset.heightValue
                            selectedUnit = "mm"
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                }

                AppStep.CropAlign -> {
                    if (loadedBitmap != null) {
                        CropAlignView(
                            bitmap = loadedBitmap!!,
                            selectedUnit = selectedUnit,
                            cropWidthVal = cropWidthUnitVal,
                            cropHeightVal = cropHeightUnitVal,
                            dpi = dpiValue,
                            scale = imageScale,
                            offset = imageOffset,
                            rotation = imageRotation,
                            onScaleChange = { imageScale = it },
                            onOffsetChange = { imageOffset = it },
                            onRotate = { degrees ->
                                scope.launch(Dispatchers.IO) {
                                    val rotated = rotateBitmap(loadedBitmap!!, degrees)
                                    withContext(Dispatchers.Main) {
                                        loadedBitmap = rotated
                                        imageScale = 1f
                                        imageOffset = Offset.Zero
                                    }
                                }
                            },
                            onReset = {
                                imageScale = 1f
                                imageOffset = Offset.Zero
                            },
                            onUnitChange = { newUnit ->
                                // Convert raw input values to match new unit
                                val factor = getUnitConversionFactor(selectedUnit, newUnit, dpiValue)
                                cropWidthUnitVal = (cropWidthUnitVal * factor)
                                cropHeightUnitVal = (cropHeightUnitVal * factor)
                                selectedUnit = newUnit
                            },
                            onWidthChange = { cropWidthUnitVal = it },
                            onHeightChange = { cropHeightUnitVal = it },
                            onApplyCrop = { wPx, hPx, relativeX, relativeY, relativeW, relativeH ->
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val cropped = cropBitmapExact(
                                            loadedBitmap!!,
                                            relativeX,
                                            relativeY,
                                            relativeW,
                                            relativeH,
                                            wPx,
                                            hPx
                                        )
                                        withContext(Dispatchers.Main) {
                                            croppedBitmap = cropped
                                            segmentedBitmap = cropped // set as fallback initially
                                            bgRemovedRawMask = null   // reset background removal mask
                                            currentStep = AppStep.BackgroundRemoval
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Failed to crop image. Try zooming out.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onCancel = {
                                loadedBitmap = null
                                selectedImageUri = null
                                currentStep = AppStep.Welcome
                            }
                        )
                    }
                }

                AppStep.BackgroundRemoval -> {
                    if (croppedBitmap != null) {
                        BackgroundRemovalView(
                            croppedBitmap = croppedBitmap!!,
                            segmentedBitmap = segmentedBitmap!!,
                            bgColor = selectedBgColor,
                            isProcessing = isSegmentationRunning,
                            hasMask = bgRemovedRawMask != null,
                            onRunAI = {
                                isSegmentationRunning = true
                                scope.launch(Dispatchers.IO) {
                                    val options = SelfieSegmenterOptions.Builder()
                                        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                                        .build()
                                    val segmenter = Segmentation.getClient(options)
                                    val inputImage = InputImage.fromBitmap(croppedBitmap!!, 0)
                                    segmenter.process(inputImage)
                                        .addOnSuccessListener { mask ->
                                            scope.launch(Dispatchers.Default) {
                                                bgRemovedRawMask = mask
                                                val replaced = replaceBackgroundWithColor(
                                                    croppedBitmap!!,
                                                    mask,
                                                    selectedBgColor
                                                )
                                                withContext(Dispatchers.Main) {
                                                    segmentedBitmap = replaced
                                                    isSegmentationRunning = false
                                                    Toast.makeText(context, "AI Background removed successfully!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            isSegmentationRunning = false
                                            Toast.makeText(context, "AI failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            },
                            onBgColorChange = { color ->
                                selectedBgColor = color
                                scope.launch(Dispatchers.Default) {
                                    val mask = bgRemovedRawMask
                                    val replaced = if (mask != null) {
                                        replaceBackgroundWithColor(croppedBitmap!!, mask, color)
                                    } else {
                                        // Simple fill if mask not processed yet
                                        croppedBitmap!!
                                    }
                                    withContext(Dispatchers.Main) {
                                        segmentedBitmap = replaced
                                    }
                                }
                            },
                            onNext = {
                                currentStep = AppStep.LayoutExport
                            },
                            onBack = {
                                currentStep = AppStep.CropAlign
                            }
                        )
                    }
                }

                AppStep.LayoutExport -> {
                    if (segmentedBitmap != null) {
                        LayoutExportView(
                            segmentedBitmap = segmentedBitmap!!,
                            selectedUnit = selectedUnit,
                            cropWidthVal = cropWidthUnitVal,
                            cropHeightVal = cropHeightUnitVal,
                            dpi = dpiValue,
                            selectedPageType = selectedPageType,
                            customPageWidth = customPageWidth,
                            customPageHeight = customPageHeight,
                            gapHorizontal = gapHorizontalVal,
                            gapVertical = gapVerticalVal,
                            marginTop = marginTopVal,
                            marginLeft = marginLeftVal,
                            photoCount = photoCountInput,
                            showCutlines = showCutlines,
                            addBorder = addBorder,
                            onPageTypeChange = { type ->
                                selectedPageType = type
                                if (type == "4r") {
                                    selectedUnit = "px"
                                    photoCountInput = 12
                                    gapHorizontalVal = 40f
                                    gapVerticalVal = 40f
                                    marginTopVal = 40f
                                    marginLeftVal = 110f
                                } else if (type == "a4") {
                                    selectedUnit = "px"
                                    photoCountInput = 14
                                    gapHorizontalVal = 40f
                                    gapVerticalVal = 40f
                                    marginTopVal = 40f
                                    marginLeftVal = 70f
                                }
                            },
                            onCustomPageWidthChange = { customPageWidth = it },
                            onCustomPageHeightChange = { customPageHeight = it },
                            onGapHorizontalChange = { gapHorizontalVal = it },
                            onGapVerticalChange = { gapVerticalVal = it },
                            onMarginTopChange = { marginTopVal = it },
                            onMarginLeftChange = { marginLeftVal = it },
                            onPhotoCountChange = { photoCountInput = it },
                            onShowCutlinesChange = { showCutlines = it },
                            onAddBorderChange = { addBorder = it },
                            onDpiChange = { dpiValue = it },
                            onDownloadSheet = { sheetBitmap, format ->
                                scope.launch(Dispatchers.IO) {
                                    val uri = saveBitmapToGallery(context, sheetBitmap, format)
                                    withContext(Dispatchers.Main) {
                                        if (uri != null) {
                                            Toast.makeText(context, "Saved sheet to Gallery/Pictures!", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onShareSheet = { sheetBitmap ->
                                scope.launch(Dispatchers.IO) {
                                    val file = saveBitmapToCache(context, sheetBitmap)
                                    if (file != null) {
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        withContext(Dispatchers.Main) {
                                            shareFileUri(context, uri)
                                        }
                                    }
                                }
                            },
                            onPrintSheet = { sheetBitmap ->
                                val printHelper = PrintHelper(context).apply {
                                    scaleMode = PrintHelper.SCALE_MODE_FIT
                                }
                                printHelper.printBitmap("Passport Photo Sheet", sheetBitmap)
                            },
                            onStartOver = {
                                loadedBitmap = null
                                croppedBitmap = null
                                segmentedBitmap = null
                                bgRemovedRawMask = null
                                selectedImageUri = null
                                currentStep = AppStep.Welcome
                            },
                            onBack = {
                                currentStep = AppStep.BackgroundRemoval
                            }
                        )
                    }
                }
            }
        }
    }

    if (showHelpDialog) {
        Dialog(onDismissRequest = { showHelpDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Guidelines & Standards",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { showHelpDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Official Biometric Requirements",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Facial Expression: Maintain a neutral face. Do not smile, frown, or tilt your head. Both eyes must be fully open.\n" +
                                "• Positioning: Position your face directly in the center of the crop circle. Your hair, chin, and ears should be completely visible.\n" +
                                "• Lighting: Natural lighting is best. Shadows on your face or behind your neck are the number one reason photo documents are rejected by government agencies.\n" +
                                "• Attire: Wear casual, everyday clothing. Uniforms, military wear, head coverings (except for religious reasons), and eyeglasses are strictly prohibited.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "How to print at home:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Customize your layout in Step 4. Select your paper size (A4 is standard printer paper, 4R is 4x6 inch photographic paper).\n" +
                                "2. Click 'Print / PDF' to print wirelessly from your phone or save as a layout PDF.\n" +
                                "3. Print at exactly 100% scale (no fit, no fill margin borders) on glossy photographic paper for perfect alignment.\n" +
                                "4. Cut cleanly along the dotted cutline separators using a straight ruler and razor cutter.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showHelpDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Got it")
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeView(
    onSelectFromGallery: () -> Unit,
    onCapturePhoto: () -> Unit,
    onUsePreset: (SizingPreset) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Banner Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(bottom = 8.dp)
                .clickable {
                    try {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            Uri.parse("https://www.shader7.com/photopassportsizepro/")
                        )
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                    }
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_banner),
                contentDescription = "App Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }


        Text(
            text = "Create Passport Photos Free",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Convert, crop, replace backgrounds with AI, and export print-ready sheets instantly.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSelectFromGallery,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("gallery_picker_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick Photo", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCapturePhoto,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("camera_picker_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Presets List
        Text(
            text = "Standard Passport Presets",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SIZING_PRESETS.forEach { preset ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUsePreset(preset) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = preset.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowForward,
                            contentDescription = "Apply Preset",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Credits / Support section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Need Help or Have Issues?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Contact the developer for support or feedback",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = LocalContext.current

                    // Email Option
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:nxdecore@gmail.com")
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Passport Photo PRO Support")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open email app", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_dialog_email),
                            contentDescription = "Email support",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Email",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Instagram Option
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://instagram.com/nishix_vamp"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Could not open Instagram", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📸",
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "@nishix_vamp",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CropAlignView(
    bitmap: Bitmap,
    selectedUnit: String,
    cropWidthVal: Float,
    cropHeightVal: Float,
    dpi: Int,
    scale: Float,
    offset: Offset,
    rotation: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onRotate: (Float) -> Unit,
    onReset: () -> Unit,
    onUnitChange: (String) -> Unit,
    onWidthChange: (Float) -> Unit,
    onHeightChange: (Float) -> Unit,
    onApplyCrop: (wPx: Int, hPx: Int, relativeX: Float, relativeY: Float, relativeW: Float, relativeH: Float) -> Unit,
    onCancel: () -> Unit
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var scrollEnabled by remember { mutableStateOf(true) }
    val currentScale by rememberUpdatedState(scale)
    val currentOffset by rememberUpdatedState(offset)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState(), enabled = scrollEnabled),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Step 1: Crop & Align",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Zoom and drag your photo to align your chin and eyes with the biometric guides.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Sizing preset settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = if (cropWidthVal % 1f == 0f) cropWidthVal.toInt().toString() else cropWidthVal.toString(),
                onValueChange = { val value = it.toFloatOrNull() ?: 1f; onWidthChange(value) },
                label = { Text("Width") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = if (cropHeightVal % 1f == 0f) cropHeightVal.toInt().toString() else cropHeightVal.toString(),
                onValueChange = { val value = it.toFloatOrNull() ?: 1f; onHeightChange(value) },
                label = { Text("Height") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            Column(modifier = Modifier.weight(1.2f)) {
                Text(text = "Unit", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("px", "mm", "inch").forEach { u ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedUnit == u) MaterialTheme.colorScheme.primary else ComposeColor.Transparent)
                                .clickable { onUnitChange(u) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = u,
                                color = if (selectedUnit == u) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Aspect Ratio Calculation
        val unitMultiplier = when (selectedUnit) {
            "mm" -> dpi / 25.4f
            "inch" -> dpi.toFloat()
            else -> 1f
        }
        val cropW_px = max(10, (cropWidthVal * unitMultiplier).toInt())
        val cropH_px = max(10, (cropHeightVal * unitMultiplier).toInt())
        val aspectRatio = cropW_px.toFloat() / cropH_px.toFloat()

        // Cropping Canvas Area
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .background(ComposeColor.Black)
                .onGloballyPositioned { containerSize = it.size }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        scrollEnabled = false
                        do {
                            val event = awaitPointerEvent()
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            if (zoomChange != 1f || panChange != Offset.Zero) {
                                onScaleChange((currentScale * zoomChange).coerceIn(0.5f, 6.0f))
                                onOffsetChange(currentOffset + panChange)
                            }
                            
                            event.changes.forEach {
                                if (it.positionChanged()) {
                                    it.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })
                        
                        scrollEnabled = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val viewWidth = constraints.maxWidth.toFloat()
            val viewHeight = constraints.maxHeight.toFloat()

            // Calculate the crop box layout centered inside the viewport
            val boxW: Float
            val boxH: Float
            if (viewWidth / viewHeight > aspectRatio) {
                // Sizing based on height
                boxH = viewHeight * 0.72f
                boxW = boxH * aspectRatio
            } else {
                // Sizing based on width
                boxW = viewWidth * 0.72f
                boxH = boxW / aspectRatio
            }

            val imgW = bitmap.width.toFloat()
            val imgH = bitmap.height.toFloat()

            // Calculate base scale to fit the image inside the crop area nicely
            val baseScale = max(boxW / imgW, boxH / imgH)
            val totalScale = baseScale * scale

            // Raw bitmap display with graphics transformation
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .requiredSize((imgW / LocalDensity.current.density).dp, (imgH / LocalDensity.current.density).dp)
                    .graphicsLayer {
                        scaleX = totalScale
                        scaleY = totalScale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.None
            )

            // Stylized Guide Overlay
            ComposeCanvas(modifier = Modifier.fillMaxSize()) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val rectL = (size.width - boxW) / 2f
                val rectT = (size.height - boxH) / 2f

                // 1. Darken the region outside the crop frame
                val path = androidx.compose.ui.graphics.Path().apply {
                    addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
                }
                val innerPath = androidx.compose.ui.graphics.Path().apply {
                    addRect(androidx.compose.ui.geometry.Rect(rectL, rectT, rectL + boxW, rectT + boxH))
                }
                val outsidePath = androidx.compose.ui.graphics.Path.combine(
                    androidx.compose.ui.graphics.PathOperation.Difference,
                    path,
                    innerPath
                )
                drawPath(outsidePath, color = ComposeColor.Black.copy(alpha = 0.65f))

                // 2. Draw border around the crop frame
                drawRect(
                    color = ComposeColor.White,
                    topLeft = Offset(rectL, rectT),
                    size = Size(boxW, boxH),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 3. Draw Biometric oval guide representing head outline
                drawOval(
                    color = ComposeColor(0xFF38BDF8).copy(alpha = 0.75f),
                    topLeft = Offset(rectL + boxW * 0.25f, rectT + boxH * 0.15f),
                    size = Size(boxW * 0.5f, boxH * 0.62f),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                // 4. Draw horizontal eye line helper
                drawLine(
                    color = ComposeColor(0xFF38BDF8).copy(alpha = 0.5f),
                    start = Offset(rectL, rectT + boxH * 0.45f),
                    end = Offset(rectL + boxW, rectT + boxH * 0.45f),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )

                // Labels
                // For simplicity, we keep it visual and neat
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tool operations Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onRotate(-90f) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.graphicsLayer { rotationZ = -90f })
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rotate L", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { onRotate(90f) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rotate R", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1.1f)
            ) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset Crop", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Confirm buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    // Calculate exact crop box rectangle to crop on original image pixels
                    val viewWidth = containerSize.width.toFloat()
                    val viewHeight = containerSize.height.toFloat()

                    val boxW: Float
                    val boxH: Float
                    if (viewWidth / viewHeight > aspectRatio) {
                        boxH = viewHeight * 0.72f
                        boxW = boxH * aspectRatio
                    } else {
                        boxW = viewWidth * 0.72f
                        boxH = boxW / aspectRatio
                    }

                    val imgW = bitmap.width.toFloat()
                    val imgH = bitmap.height.toFloat()

                    val baseScale = max(boxW / imgW, boxH / imgH)
                    val totalScale = baseScale * scale

                    // Image coordinate corners inside the viewport
                    val imageLeftPx = viewWidth / 2f + offset.x - (imgW * totalScale) / 2f
                    val imageTopPx = viewHeight / 2f + offset.y - (imgH * totalScale) / 2f

                    val cropLeftPx = (viewWidth - boxW) / 2f
                    val cropTopPx = (viewHeight - boxH) / 2f

                    // Map crop box coordinate back to relative ratio of image
                    val relativeX = (cropLeftPx - imageLeftPx) / (imgW * totalScale)
                    val relativeY = (cropTopPx - imageTopPx) / (imgH * totalScale)
                    val relativeW = boxW / (imgW * totalScale)
                    val relativeH = boxH / (imgH * totalScale)

                    onApplyCrop(cropW_px, cropH_px, relativeX, relativeY, relativeW, relativeH)
                },
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("apply_crop_button")
            ) {
                Text("Crop & Apply", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BackgroundRemovalView(
    croppedBitmap: Bitmap,
    segmentedBitmap: Bitmap,
    bgColor: Int,
    isProcessing: Boolean,
    hasMask: Boolean,
    onRunAI: () -> Unit,
    onBgColorChange: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val swatches = listOf(
        Pair(Color.WHITE, "White"),
        Pair(Color.parseColor("#E8F0FF"), "Light Blue"),
        Pair(Color.parseColor("#DBEAFE"), "Passport Blue"),
        Pair(Color.parseColor("#F3F4F6"), "Light Gray"),
        Pair(Color.parseColor("#000000"), "Black"),
        Pair(Color.TRANSPARENT, "Transparent")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Step 2: AI Background Removal",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Run local AI segmentation to extract subject and swap background colors instantly.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Photo display card
        Box(
            modifier = Modifier
                .size(200.dp, 260.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .background(if (bgColor == Color.TRANSPARENT) ComposeColor.LightGray else ComposeColor(bgColor)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = segmentedBitmap.asImageBitmap(),
                contentDescription = "Segmented selfie",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ComposeColor.Black.copy(alpha = 0.72f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "AI extracting subject...\n(100% private & offline)",
                            color = ComposeColor.White,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI processing trigger
        if (!hasMask) {
            Button(
                onClick = onRunAI,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("run_ai_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Magic AI: Remove Background", fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                text = "✓ AI Extraction Active! Select a background color:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ComposeColor(0xFF10B981),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Swatches row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                swatches.forEach { (color, label) ->
                    val isSelected = bgColor == color
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (color == Color.TRANSPARENT) ComposeColor.LightGray else ComposeColor(color))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else ComposeColor.Gray,
                                shape = CircleShape
                            )
                            .clickable { onBgColorChange(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (color == Color.WHITE || color == Color.TRANSPARENT) ComposeColor.Black else ComposeColor.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Navigation controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back")
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1.5f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Next: Sizing & Layout", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
fun LayoutExportView(
    segmentedBitmap: Bitmap,
    selectedUnit: String,
    cropWidthVal: Float,
    cropHeightVal: Float,
    dpi: Int,
    selectedPageType: String,
    customPageWidth: Float,
    customPageHeight: Float,
    gapHorizontal: Float,
    gapVertical: Float,
    marginTop: Float,
    marginLeft: Float,
    photoCount: Int,
    showCutlines: Boolean,
    addBorder: Boolean,
    onPageTypeChange: (String) -> Unit,
    onCustomPageWidthChange: (Float) -> Unit,
    onCustomPageHeightChange: (Float) -> Unit,
    onGapHorizontalChange: (Float) -> Unit,
    onGapVerticalChange: (Float) -> Unit,
    onMarginTopChange: (Float) -> Unit,
    onMarginLeftChange: (Float) -> Unit,
    onPhotoCountChange: (Int) -> Unit,
    onShowCutlinesChange: (Boolean) -> Unit,
    onAddBorderChange: (Boolean) -> Unit,
    onDpiChange: (Int) -> Unit,
    onDownloadSheet: (Bitmap, String) -> Unit,
    onShareSheet: (Bitmap) -> Unit,
    onPrintSheet: (Bitmap) -> Unit,
    onStartOver: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var dpiInputText by remember { mutableStateOf(dpi.toString()) }
    LaunchedEffect(dpi) {
        if (dpiInputText.toIntOrNull() != dpi) {
            dpiInputText = dpi.toString()
        }
    }

    // Convert dimensions from current unit to pixels based on DPI
    val unitMultiplier = when (selectedUnit) {
        "mm" -> dpi / 25.4f
        "inch" -> dpi.toFloat()
        else -> 1f
    }

    val photoWidthPx = max(10, (cropWidthVal * unitMultiplier).toInt())
    val photoHeightPx = max(10, (cropHeightVal * unitMultiplier).toInt())
    val gapHPx = (gapHorizontal * unitMultiplier).toInt()
    val gapVPx = (gapVertical * unitMultiplier).toInt()
    val marginTPx = (marginTop * unitMultiplier).toInt()
    val marginLPx = (marginLeft * unitMultiplier).toInt()

    val pageDimensions = when (selectedPageType) {
        "a4" -> Pair((210 * (dpi / 25.4f)).toInt(), (297 * (dpi / 25.4f)).toInt())
        "4r" -> Pair((4 * dpi), (6 * dpi))
        else -> Pair((customPageWidth * unitMultiplier).toInt(), (customPageHeight * unitMultiplier).toInt())
    }
    val pageWidthPx = pageDimensions.first
    val pageHeightPx = pageDimensions.second

    // Calculate maximum fit count
    val colCount = floor((pageWidthPx - marginLPx + gapHPx).toFloat() / (photoWidthPx + gapHPx)).toInt().coerceAtLeast(1)
    val rowCount = floor((pageHeightPx - marginTPx + gapVPx).toFloat() / (photoHeightPx + gapVPx)).toInt().coerceAtLeast(1)
    val maxPossiblePhotos = colCount * rowCount

    // Create the printable layout bitmap dynamically
    val sheetBitmap = remember(
        segmentedBitmap, pageWidthPx, pageHeightPx, photoWidthPx, photoHeightPx,
        gapHPx, gapVPx, marginTPx, marginLPx, photoCount, showCutlines, addBorder, maxPossiblePhotos
    ) {
        generatePrintSheet(
            segmentedBitmap,
            pageWidthPx,
            pageHeightPx,
            photoWidthPx,
            photoHeightPx,
            gapHPx,
            gapVPx,
            marginTPx,
            marginLPx,
            photoCount.coerceAtMost(maxPossiblePhotos),
            showCutlines,
            addBorder,
            Color.WHITE
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Step 3: Print Layout & Export",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Fine-tune spacing, review print preview sheet, and print or download your photos.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Sheet Preview display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = sheetBitmap.asImageBitmap(),
                    contentDescription = "Print layout preview",
                    modifier = Modifier.fillMaxHeight(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Layout Options Accordion or Scroll section
        var showSettings by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showSettings = !showSettings }
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Adjust Margins, Gaps & Count",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Icon(
                imageVector = if (showSettings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }

        AnimatedVisibility(visible = showSettings) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                // Photo count slider
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Photo Count: ${photoCount.coerceAtMost(maxPossiblePhotos)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onPhotoCountChange(maxPossiblePhotos) }) {
                        Text("Fill Max (${maxPossiblePhotos})", fontSize = 12.sp)
                    }
                }
                Slider(
                    value = photoCount.coerceIn(1, maxPossiblePhotos).toFloat(),
                    onValueChange = { onPhotoCountChange(it.toInt()) },
                    valueRange = 1f..maxPossiblePhotos.toFloat(),
                    steps = if (maxPossiblePhotos > 1) maxPossiblePhotos - 2 else 0,
                    modifier = Modifier.fillMaxWidth()
                )

                // Paper templates
                Text(
                    text = "Paper Template",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("a4" to "A4 Sheet", "4r" to "4R (4\"x6\")", "custom" to "Custom").forEach { (type, label) ->
                        val isSelected = selectedPageType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onPageTypeChange(type) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (selectedPageType == "custom") {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = customPageWidth.toString(),
                            onValueChange = { onCustomPageWidthChange(it.toFloatOrNull() ?: 10f) },
                            label = { Text("Page Width") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = customPageHeight.toString(),
                            onValueChange = { onCustomPageHeightChange(it.toFloatOrNull() ?: 10f) },
                            label = { Text("Page Height") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Margin settings
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = gapHorizontal.toString(),
                        onValueChange = { onGapHorizontalChange(it.toFloatOrNull() ?: 0f) },
                        label = { Text("Gap Horiz ($selectedUnit)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = gapVertical.toString(),
                        onValueChange = { onGapVerticalChange(it.toFloatOrNull() ?: 0f) },
                        label = { Text("Gap Vert ($selectedUnit)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = marginTop.toString(),
                        onValueChange = { onMarginTopChange(it.toFloatOrNull() ?: 0f) },
                        label = { Text("Top Margin ($selectedUnit)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = marginLeft.toString(),
                        onValueChange = { onMarginLeftChange(it.toFloatOrNull() ?: 0f) },
                        label = { Text("Left Margin ($selectedUnit)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Print quality DPI settings
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = dpiInputText,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty()) {
                                dpiInputText = ""
                            } else if (newValue.all { it.isDigit() }) {
                                dpiInputText = newValue
                                val parsed = newValue.toIntOrNull()
                                if (parsed != null) {
                                    onDpiChange(parsed)
                                }
                            }
                        },
                        label = { Text("Print DPI") },
                        modifier = Modifier
                            .width(120.dp)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused) {
                                    val clamped = (dpiInputText.toIntOrNull() ?: 300).coerceIn(72, 1200)
                                    dpiInputText = clamped.toString()
                                    onDpiChange(clamped)
                                }
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text(
                        text = "Higher DPI (e.g. 300 or 600) yields crisp photographic quality. 300 is standard.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cutlines and borders toggle switches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Switch(checked = showCutlines, onCheckedChange = onShowCutlinesChange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show Cutlines", fontSize = 12.sp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Switch(checked = addBorder, onCheckedChange = onAddBorderChange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Photo Border", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Direct Native Print Button (Primary CTA)
        Button(
            onClick = { onPrintSheet(sheetBitmap) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("print_sheet_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Print Wirelessly / Save PDF", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Watch Ad for HD Download
        Button(
            onClick = {
                try {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        Uri.parse("https://emotionallytonightintelligent.com/x8fvr0fu?key=a0df723f661db51d2b97818a0f27ea09")
                    )
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open link", Toast.LENGTH_SHORT).show()
                }

                // Trigger HD Download (600 DPI)
                try {
                    val hdDpi = 300
                    val hdUnitMultiplier = when (selectedUnit) {
                        "mm" -> hdDpi / 25.4f
                        "inch" -> hdDpi.toFloat()
                        else -> 1f
                    }
                    val hdPhotoWidthPx = max(10, (cropWidthVal * hdUnitMultiplier).toInt())
                    val hdPhotoHeightPx = max(10, (cropHeightVal * hdUnitMultiplier).toInt())
                    val hdGapHPx = (gapHorizontal * hdUnitMultiplier).toInt()
                    val hdGapVPx = (gapVertical * hdUnitMultiplier).toInt()
                    val hdMarginTPx = (marginTop * hdUnitMultiplier).toInt()
                    val hdMarginLPx = (marginLeft * hdUnitMultiplier).toInt()

                    val hdPageDimensions = when (selectedPageType) {
                        "a4" -> Pair((210 * (hdDpi / 25.4f)).toInt(), (297 * (hdDpi / 25.4f)).toInt())
                        "4r" -> Pair((4 * hdDpi), (6 * hdDpi))
                        else -> Pair((customPageWidth * hdUnitMultiplier).toInt(), (customPageHeight * hdUnitMultiplier).toInt())
                    }
                    val hdPageWidthPx = hdPageDimensions.first
                    val hdPageHeightPx = hdPageDimensions.second

                    val hdSheet = generatePrintSheet(
                        segmentedBitmap,
                        hdPageWidthPx,
                        hdPageHeightPx,
                        hdPhotoWidthPx,
                        hdPhotoHeightPx,
                        hdGapHPx,
                        hdGapVPx,
                        hdMarginTPx,
                        hdMarginLPx,
                        photoCount,
                        showCutlines,
                        addBorder,
                        Color.WHITE
                    )
                    onDownloadSheet(hdSheet, "png")
                } catch (e: Exception) {
                    Toast.makeText(context, "HD rendering failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("📺 Watch Ad for HD Quality Download", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Download Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onDownloadSheet(sheetBitmap, "jpg") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("download_jpg_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Save JPG", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { onDownloadSheet(sheetBitmap, "png") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("download_png_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Save PNG", fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = { onShareSheet(sheetBitmap) },
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share sheet layout"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Back / Reset controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }

            OutlinedButton(
                onClick = onStartOver,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Start Over", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// IMAGE & BITMAP UTILITY HELPERS
// ==========================================

fun loadAndNormalizeBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, null, options)
        }

        // Downscale image if it is extremely high resolution to avoid memory crashes
        val reqSize = 1200
        var scale = 1
        while (options.outWidth / scale / 2 >= reqSize && options.outHeight / scale / 2 >= reqSize) {
            scale *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = scale
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decoded = context.contentResolver.openInputStream(uri).use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        } ?: return null

        val rotation = getExifRotationDegrees(context, uri)
        if (rotation != 0) {
            rotateBitmap(decoded, rotation.toFloat())
        } else {
            decoded
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getExifRotationDegrees(context: Context, uri: Uri): Int {
    try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val exifInterface = ExifInterface(stream)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return 0
}

fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
    if (degrees == 0f) return source
    val matrix = Matrix().apply { postRotate(degrees) }
    val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    if (rotated != source) {
        source.recycle()
    }
    return rotated
}

fun cropBitmapExact(
    source: Bitmap,
    relativeX: Float,
    relativeY: Float,
    relativeW: Float,
    relativeH: Float,
    targetW_px: Int,
    targetH_px: Int
): Bitmap {
    val srcW = source.width
    val srcH = source.height

    val cropX = (relativeX * srcW).toInt().coerceIn(0, srcW - 1)
    val cropY = (relativeY * srcH).toInt().coerceIn(0, srcH - 1)
    val cropW = (relativeW * srcW).toInt().coerceIn(1, srcW - cropX)
    val cropH = (relativeH * srcH).toInt().coerceIn(1, srcH - cropY)

    val cropped = Bitmap.createBitmap(source, cropX, cropY, cropW, cropH)
    val scaled = Bitmap.createScaledBitmap(cropped, targetW_px, targetH_px, true)
    if (scaled != cropped) {
        cropped.recycle()
    }
    return scaled
}

fun replaceBackgroundWithColor(
    original: Bitmap,
    mask: com.google.mlkit.vision.segmentation.SegmentationMask,
    backgroundColor: Int
): Bitmap {
    val width = original.width
    val height = original.height
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val pixels = IntArray(width * height)
    original.getPixels(pixels, 0, width, 0, 0, width, height)

    val buffer = mask.buffer
    buffer.rewind()

    val bgA = (backgroundColor shr 24) and 0xFF
    val bgR = (backgroundColor shr 16) and 0xFF
    val bgG = (backgroundColor shr 8) and 0xFF
    val bgB = backgroundColor and 0xFF

    for (y in 0 until height) {
        for (x in 0 until width) {
            val index = y * width + x
            val confidence = buffer.float.coerceIn(0f, 1f)

            val pixel = pixels[index]
            val a = (pixel shr 24) and 0xFF
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF

            // Blend foreground face selfie with background color
            val fgAlpha = confidence
            val bgAlpha = 1f - confidence

            val blendedA = (a * fgAlpha + bgA * bgAlpha).toInt().coerceIn(0, 255)
            val blendedR = (r * fgAlpha + bgR * bgAlpha).toInt().coerceIn(0, 255)
            val blendedG = (g * fgAlpha + bgG * bgAlpha).toInt().coerceIn(0, 255)
            val blendedB = (b * fgAlpha + bgB * bgAlpha).toInt().coerceIn(0, 255)

            pixels[index] = (blendedA shl 24) or (blendedR shl 16) or (blendedG shl 8) or blendedB
        }
    }

    output.setPixels(pixels, 0, width, 0, 0, width, height)
    return output
}

fun generatePrintSheet(
    croppedBitmap: Bitmap,
    pageWidthPx: Int,
    pageHeightPx: Int,
    photoWidthPx: Int,
    photoHeightPx: Int,
    gapHPx: Int,
    gapVPx: Int,
    marginTPx: Int,
    marginLPx: Int,
    photoCount: Int,
    showCutlines: Boolean,
    addBorder: Boolean,
    backgroundColor: Int
): Bitmap {
    val sheet = Bitmap.createBitmap(pageWidthPx, pageHeightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(sheet)
    canvas.drawColor(backgroundColor)

    val colCount = floor((pageWidthPx - marginLPx + gapHPx).toFloat() / (photoWidthPx + gapHPx)).toInt().coerceAtLeast(1)
    val rowCount = floor((pageHeightPx - marginTPx + gapVPx).toFloat() / (photoHeightPx + gapVPx)).toInt().coerceAtLeast(1)
    val totalPossible = colCount * rowCount
    val drawCount = photoCount.coerceAtMost(totalPossible)

    val photoPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    val borderPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = max(2f, (pageWidthPx / 600f)) // dynamic border width scaled with sheet resolution
    }

    val cutlinePaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = max(1.5f, (pageWidthPx / 800f))
        pathEffect = DashPathEffect(floatArrayOf(15f, 15f), 0f)
    }

    val destRect = Rect()

    for (i in 0 until drawCount) {
        val col = i % colCount
        val row = i / colCount
        val x = marginLPx + col * (photoWidthPx + gapHPx)
        val y = marginTPx + row * (photoHeightPx + gapVPx)

        destRect.set(x, y, x + photoWidthPx, y + photoHeightPx)
        canvas.drawBitmap(croppedBitmap, null, destRect, photoPaint)

        if (addBorder) {
            canvas.drawRect(destRect, borderPaint)
        }
    }

    // Cutlines between grid items
    if (showCutlines && drawCount > 0) {
        val rowsNeeded = ceil(drawCount.toFloat() / colCount).toInt()

        // Vertical separators
        for (col in 0 until colCount - 1) {
            var minY = -1
            var maxY = -1
            for (row in 0 until rowsNeeded) {
                val indexL = row * colCount + col
                val indexR = indexL + 1
                if (indexL < drawCount && indexR < drawCount) {
                    val y = marginTPx + row * (photoHeightPx + gapVPx)
                    if (minY == -1) minY = y
                    maxY = y + photoHeightPx
                }
            }
            if (minY != -1) {
                val x = marginLPx + (col + 1) * photoWidthPx + col * gapHPx + gapHPx / 2
                canvas.drawLine(x.toFloat(), minY.toFloat(), x.toFloat(), maxY.toFloat(), cutlinePaint)
            }
        }

        // Horizontal separators
        for (row in 0 until rowsNeeded - 1) {
            var minX = -1
            var maxX = -1
            for (col in 0 until colCount) {
                val indexT = row * colCount + col
                val indexB = indexT + colCount
                if (indexT < drawCount && indexB < drawCount) {
                    val x = marginLPx + col * (photoWidthPx + gapHPx)
                    if (minX == -1) minX = x
                    maxX = x + photoWidthPx
                }
            }
            if (minX != -1) {
                val y = marginTPx + (row + 1) * photoHeightPx + row * gapVPx + gapVPx / 2
                canvas.drawLine(minX.toFloat(), y.toFloat(), maxX.toFloat(), y.toFloat(), cutlinePaint)
            }
        }
    }

    return sheet
}

// Convert input preset dimensions between mm, inch, and raw px based on layout DPI
fun getUnitConversionFactor(fromUnit: String, toUnit: String, dpi: Int): Float {
    if (fromUnit == toUnit) return 1f
    // Convert from source to inches
    val toInchesFactor = when (fromUnit) {
        "mm" -> 1f / 25.4f
        "px" -> 1f / dpi.toFloat()
        else -> 1f
    }
    // Convert from inches to target
    val fromInchesFactor = when (toUnit) {
        "mm" -> 25.4f
        "px" -> dpi.toFloat()
        else -> 1f
    }
    return toInchesFactor * fromInchesFactor
}

// File systems temp capture files creation helper
fun createTempImageUri(context: Context): Uri {
    val directory = File(context.cacheDir, "Pictures")
    directory.mkdirs()
    val file = File.createTempFile("camera_capture_", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap): File? {
    return try {
        val directory = File(context.cacheDir, "Pictures")
        directory.mkdirs()
        val file = File(directory, "shared_passport_sheet.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun shareFileUri(context: Context, uri: Uri) {
    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Photo Sheet"))
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap, format: String): Uri? {
    val filename = "passport_photo_sheet_${System.currentTimeMillis()}.$format"
    val mimeType = if (format == "png") "image/png" else "image/jpeg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PassportPhotoMaker")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    if (uri != null) {
        try {
            resolver.openOutputStream(uri).use { outStream ->
                if (outStream != null) {
                    val compressFormat = if (format == "png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                    bitmap.compress(compressFormat, 100, outStream)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    return uri
}
