package com.dnfapps.arrmatey.ui.screens.settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dnfapps.arrmatey.shared.*
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanQrCodeSheet(
    onDismiss: () -> Unit,
    onQrScanned: (String) -> Unit,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        },
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp),
                    )
                }

                Column {
                    Text(
                        text = mokoString(MR.strings.scan_qr_code),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = mokoString(MR.strings.scan_qr_code_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                    ) {
                        CameraQrScannerView(
                            onQrCodeScanned = { scannedString ->
                                onQrScanned(scannedString)
                            },
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    ) {
                        Text(
                            text = mokoString(MR.strings.camera_permission_required),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text(mokoString(MR.strings.grant_camera_permission))
                        }
                    }
                }
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text(mokoString(MR.strings.cancel))
            }
        }
    }
}

@Composable
private fun CameraQrScannerView(
    onQrCodeScanned: (String) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScanned by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            val mainExecutor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImageProxy(imageProxy) { qrValue ->
                        if (!isScanned) {
                            isScanned = true
                            mainExecutor.execute {
                                onQrCodeScanned(qrValue)
                            }
                        }
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, mainExecutor)

            previewView
        },
        modifier = Modifier.fillMaxWidth().height(320.dp),
    )
}

private val qrCodeHints = mapOf(
    DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
    DecodeHintType.TRY_HARDER to true,
)

@SuppressLint("UnsafeOptInUsageError")
private fun processImageProxy(
    imageProxy: ImageProxy,
    onSuccess: (String) -> Unit,
) {
    try {
        val plane = imageProxy.planes[0]
        val buffer = plane.buffer
        val rowStride = plane.rowStride
        val width = imageProxy.width
        val height = imageProxy.height

        val yBytes = ByteArray(width * height)
        if (rowStride == width) {
            buffer.get(yBytes, 0, width * height)
        } else {
            val initialPos = buffer.position()
            for (y in 0 until height) {
                buffer.position(initialPos + y * rowStride)
                val lengthToCopy = minOf(width, buffer.remaining())
                if (lengthToCopy > 0) {
                    buffer.get(yBytes, y * width, lengthToCopy)
                }
            }
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val source = createLuminanceSource(yBytes, width, height, rotationDegrees)

        val qrReader = QRCodeReader()
        var decodedText: String? = tryDecode(qrReader, source)

        if (decodedText == null && rotationDegrees != 0) {
            val baseSource = PlanarYUVLuminanceSource(
                yBytes,
                width,
                height,
                0,
                0,
                width,
                height,
                false,
            )
            decodedText = tryDecode(qrReader, baseSource)
        }

        if (decodedText != null) {
            onSuccess(decodedText)
        }
    } catch (e: Exception) {
        Log.e("ScanQrCodeSheet", "Error processing image frame", e)
    } finally {
        imageProxy.close()
    }
}

private fun createLuminanceSource(
    yBytes: ByteArray,
    width: Int,
    height: Int,
    rotationDegrees: Int,
): PlanarYUVLuminanceSource = when (rotationDegrees % 360) {
    90 -> {
        val rotatedData = ByteArray(width * height)
        for (y in 0 until height) {
            val yWidth = y * width
            for (x in 0 until width) {
                rotatedData[x * height + (height - 1 - y)] = yBytes[yWidth + x]
            }
        }
        PlanarYUVLuminanceSource(
            rotatedData,
            height,
            width,
            0,
            0,
            height,
            width,
            false,
        )
    }
    180 -> {
        val rotatedData = ByteArray(width * height)
        for (y in 0 until height) {
            val yWidth = y * width
            val targetYWidth = (height - 1 - y) * width
            for (x in 0 until width) {
                rotatedData[targetYWidth + (width - 1 - x)] = yBytes[yWidth + x]
            }
        }
        PlanarYUVLuminanceSource(
            rotatedData,
            width,
            height,
            0,
            0,
            width,
            height,
            false,
        )
    }
    270 -> {
        val rotatedData = ByteArray(width * height)
        for (y in 0 until height) {
            val yWidth = y * width
            for (x in 0 until width) {
                rotatedData[(width - 1 - x) * height + y] = yBytes[yWidth + x]
            }
        }
        PlanarYUVLuminanceSource(
            rotatedData,
            height,
            width,
            0,
            0,
            height,
            width,
            false,
        )
    }
    else -> {
        PlanarYUVLuminanceSource(
            yBytes,
            width,
            height,
            0,
            0,
            width,
            height,
            false,
        )
    }
}

private fun tryDecode(reader: QRCodeReader, source: LuminanceSource): String? = try {
    val bitmap = BinaryBitmap(HybridBinarizer(source))
    reader.decode(bitmap, qrCodeHints).text
} catch (_: Exception) {
    null
} finally {
    reader.reset()
}
