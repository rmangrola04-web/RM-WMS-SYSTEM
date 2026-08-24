@file:OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalGetImage::class
)

package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseSteelBlue
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.util.concurrent.Executors

data class ScannedPassData(
    val rawValue: String,
    val vehicleNumber: String,
    val origin: String? = null,
    val destination: String? = null,
    val activityType: String? = null,
    val vehicleType: String? = null,
    val dockBay: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val remarks: String? = null
)

object BarcodePassParser {
    fun parse(raw: String): ScannedPassData {
        val trimmed = raw.trim()

        var vehicle = ""
        var from: String? = null
        var to: String? = null
        var act: String? = null
        var vType: String? = null
        var bay: String? = null
        var driver: String? = null
        var phone: String? = null
        var rem: String? = null

        // 1. Check if payload is URL formatted: e.g. https://domain.com/pass?v=MP09AB1234&from=Indore...
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.contains("?")
        ) {
            try {
                val query = if (trimmed.contains("?")) trimmed.substringAfter("?") else trimmed
                val pairs = query.split("&")
                for (pair in pairs) {
                    val kv = pair.split("=")
                    if (kv.size == 2) {
                        val key = URLDecoder.decode(kv[0], "UTF-8").trim().lowercase()
                        val value = URLDecoder.decode(kv[1], "UTF-8").trim()
                        when {
                            key in listOf("v", "vehicle", "vehicleno", "plate", "truck", "reg") -> vehicle = value
                            key in listOf("from", "origin", "src", "source", "plant") -> from = value
                            key in listOf("to", "dest", "destination", "hub", "warehouse") -> to = value
                            key in listOf("act", "activity", "type", "operation") -> act = normalizeActivity(value)
                            key in listOf("vtype", "vehicletype", "model", "body") -> vType = value
                            key in listOf("bay", "dock", "dockbay", "bayno") -> bay = normalizeBay(value)
                            key in listOf("driver", "drivername", "pilot") -> driver = value
                            key in listOf("phone", "mobile", "contact", "tel") -> phone = value
                            key in listOf("rem", "remark", "remarks", "seal", "lr", "note") -> rem = value
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("BarcodeParser", "Error parsing URL query parameters", e)
            }
        }

        // 2. Check Key-Value patterns separated by colon, equals, semicolon, pipe, comma, or newline
        if (vehicle.isBlank() && (trimmed.contains(":") || trimmed.contains("=") || trimmed.contains(";") || trimmed.contains("|") || trimmed.contains("\n"))) {
            val parts = trimmed.split(Regex("[;||\n]"))
            for (part in parts) {
                val kv = part.split(Regex("[:=]"), limit = 2)
                if (kv.size == 2) {
                    val key = kv[0].trim().uppercase()
                    val value = kv[1].trim()
                    when {
                        key.contains("VEHICLE") || key.contains("TRUCK") || key.contains("REG") || key.contains("PLATE") || key.contains("V_NO") || key == "V" -> vehicle = value
                        key.contains("FROM") || key.contains("ORIGIN") || key.contains("PLANT") || key.contains("SOURCE") -> from = value
                        key.contains("TO") || key.contains("DEST") || key.contains("HUB") || key.contains("WH") -> to = value
                        key.contains("ACT") || key.contains("OPERATION") || (key.contains("TYPE") && (value.contains("Load", true) || value.contains("Unload", true))) -> act = normalizeActivity(value)
                        key.contains("VTYPE") || key.contains("VEHICLE_TYPE") || key.contains("BODY") || key.contains("MODEL") -> vType = value
                        key.contains("DOCK") || key.contains("BAY") -> bay = normalizeBay(value)
                        key.contains("DRIVER") || key.contains("PILOT") -> driver = value
                        key.contains("PHONE") || key.contains("MOBILE") || key.contains("CONTACT") || key.contains("TEL") -> phone = value
                        key.contains("LR") || key.contains("SEAL") || key.contains("REMARK") || key.contains("NOTE") || key.contains("BILL") -> rem = value
                    }
                }
            }
        }

        // 3. Fallback: Extract Vehicle Number using Indian / Global Plate Regex if not found yet
        if (vehicle.isBlank()) {
            val plateRegex = Regex("""\b([A-Z]{2}\s?[0-9]{1,2}\s?[A-Z]{1,3}\s?[0-9]{4})\b""", RegexOption.IGNORE_CASE)
            val match = plateRegex.find(trimmed)
            if (match != null) {
                vehicle = match.value
            } else {
                vehicle = trimmed
            }
        }

        // Clean vehicle number format (e.g. "MP 09 AB 1234" or "MP09AB1234")
        val cleanVehicle = vehicle.replace(Regex("""[^A-Za-z0-9\s-]"""), "").trim().uppercase()

        return ScannedPassData(
            rawValue = trimmed,
            vehicleNumber = if (cleanVehicle.isNotBlank()) cleanVehicle else trimmed.uppercase(),
            origin = from,
            destination = to,
            activityType = act,
            vehicleType = vType,
            dockBay = bay,
            driverName = driver,
            driverPhone = phone,
            remarks = rem
        )
    }

    private fun normalizeActivity(act: String): String {
        return when {
            act.contains("Unload", ignoreCase = true) -> "Unloading"
            act.contains("Load", ignoreCase = true) -> "Loading"
            act.contains("Trans", ignoreCase = true) || act.contains("Cross", ignoreCase = true) -> "Transshipment"
            act.contains("Audit", ignoreCase = true) || act.contains("Check", ignoreCase = true) -> "Yard Inspection"
            else -> act
        }
    }

    private fun normalizeBay(bay: String): String {
        val trimmed = bay.trim()
        return if (trimmed.matches(Regex("""\d+"""))) {
            "Bay ${trimmed.padStart(2, '0')}"
        } else if (trimmed.startsWith("Bay", ignoreCase = true)) {
            val num = trimmed.replace(Regex("[^0-9]"), "")
            if (num.isNotBlank()) "Bay ${num.padStart(2, '0')}" else trimmed
        } else {
            trimmed
        }
    }
}

@Composable
fun BarcodeScannerDialog(
    onDismiss: () -> Unit,
    onScanned: (ScannedPassData) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var scannedResult by remember { mutableStateOf<ScannedPassData?>(null) }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (hasCameraPermission) {
                    CameraPreviewScanner(
                        isScanningEnabled = scannedResult == null,
                        onBarcodeDetected = { scannedData ->
                            triggerHapticFeedback(context)
                            scannedResult = scannedData
                        }
                    )
                } else {
                    // Camera permission denied state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Permission Required",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please allow camera access to scan gate pass QR codes and vehicle barcodes.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = WarehouseEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_request_camera_perm")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Allow Camera")
                        }
                    }
                }

                // Top Floating Bar with Header and Close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = WarehouseEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "CameraX Scanner",
                                    color = Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "QR & Barcode Logistics Reader",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .testTag("btn_close_scanner")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Bottom Content: either Scanned Result Confirmation Sheet or Testing Presets
                AnimatedVisibility(
                    visible = scannedResult != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    scannedResult?.let { result ->
                        ScannedResultPreviewCard(
                            scannedData = result,
                            onConfirm = {
                                onScanned(result)
                                onDismiss()
                            },
                            onRescan = {
                                scannedResult = null
                            }
                        )
                    }
                }

                if (scannedResult == null) {
                    // Quick Testing Presets and Instructions Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xEE111827)),
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CenterFocusWeak,
                                    contentDescription = null,
                                    tint = WarehouseEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Hold vehicle QR code / barcode inside the frame",
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Quick Sample Passes for Testing:",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                val demoPasses = listOf(
                                    "MP09 AB 1234" to "MP09 AB 1234 (Tata Ace)",
                                    "MH12 RN 8899" to "MH12 RN 8899 (32ft Container)",
                                    "DL01 AX 4567" to "DL01 AX 4567 (Trailer)",
                                    "VEHICLE:GJ03 XY 7711;FROM:Surat;TO:Indore;ACT:Loading;DRIVER:Ramesh;BAY:Bay 02" to "GJ03 (Surat ➜ Indore)",
                                    "https://gate.wh.com/pass?v=KA01MJ9988&act=Unloading&from=Bangalore&to=Indore&dock=Bay 04&driver=Vijay&phone=9876501234" to "KA01 (URL Pass with Bay & Phone)"
                                )
                                demoPasses.forEach { (raw, label) ->
                                    FilterChip(
                                        selected = false,
                                        onClick = {
                                            val parsed = BarcodePassParser.parse(raw)
                                            scannedResult = parsed
                                        },
                                        label = {
                                            Text(
                                                label,
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = Color.White.copy(alpha = 0.12f)
                                        ),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                        modifier = Modifier.testTag("chip_sample_pass_${label.take(6)}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannedResultPreviewCard(
    scannedData: ScannedPassData,
    onConfirm: () -> Unit,
    onRescan: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        border = BorderStroke(1.dp, WarehouseEmerald.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scanned_result_preview_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WarehouseEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WarehouseEmerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pass Detected!",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Review details and apply to form",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Vehicle Number Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(WarehouseNavy)
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = scannedData.vehicleNumber,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Extracted Details Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (scannedData.activityType != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Activity: ", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        Text(scannedData.activityType, color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                if (scannedData.origin != null || scannedData.destination != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = WarehouseEmerald, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${scannedData.origin ?: "Warehouse"} ➔ ${scannedData.destination ?: "Dest"}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (scannedData.dockBay != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warehouse, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dock Bay: ${scannedData.dockBay}", color = Color.White, fontSize = 12.sp)
                    }
                }
                if (scannedData.driverName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Driver: ${scannedData.driverName}${scannedData.driverPhone?.let { " ($it)" } ?: ""}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
                if (scannedData.remarks != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Remarks: ", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        Text(scannedData.remarks, color = Color.White.copy(alpha = 0.9f), fontSize = 11.5.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Fill Form and Rescan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRescan,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_rescan_barcode")
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rescan", fontSize = 13.sp)
                }

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = WarehouseEmerald),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("btn_confirm_scanned_pass")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply to Form", fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CameraPreviewScanner(
    isScanningEnabled: Boolean,
    onBarcodeDetected: (ScannedPassData) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val coroutineScope = rememberCoroutineScope()

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var isAnalyzing by remember { mutableStateOf(true) }

    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_CODABAR
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    // Scanning Reticle Laser Animation
    val infiniteTransition = rememberInfiniteTransition(label = "ScannerLaser")
    val scanLineOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScanLaserOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    camera?.let { cam ->
                        val currentZoom = cam.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                        val newZoom = (currentZoom * zoom).coerceIn(1f, 5f)
                        cam.cameraControl.setZoomRatio(newZoom)
                        zoomRatio = newZoom
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    focusPoint = tapOffset
                    coroutineScope.launch {
                        delay(1200)
                        focusPoint = null
                    }
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy: ImageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && isScanningEnabled && isAnalyzing) {
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue
                                        if (!rawValue.isNullOrBlank() && isScanningEnabled && isAnalyzing) {
                                            isAnalyzing = false
                                            val parsed = BarcodePassParser.parse(rawValue)
                                            onBarcodeDetected(parsed)
                                            break
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("CameraXScanner", "MLKit Barcode analysis failure", e)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraXScanner", "Camera binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            update = { previewView ->
                // Handle lens flip or re-bind if lensFacing changed
                val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy: ImageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null && isScanningEnabled) {
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        val rawValue = barcode.rawValue
                                        if (!rawValue.isNullOrBlank() && isScanningEnabled) {
                                            val parsed = BarcodePassParser.parse(rawValue)
                                            onBarcodeDetected(parsed)
                                            break
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("CameraXScanner", "Analysis error", e)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (exc: Exception) {
                        Log.e("CameraXScanner", "Camera re-bind error", exc)
                    }
                }, ContextCompat.getMainExecutor(previewView.context))
            },
            modifier = Modifier.fillMaxSize()
        )

        // Viewfinder Frame Overlay with Corner Accents
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.Center)
                .border(2.dp, WarehouseEmerald.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                .testTag("camera_viewfinder_box")
        ) {
            // Animated Laser Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .offset(y = scanLineOffset.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                WarehouseEmerald.copy(alpha = 0.1f),
                                WarehouseEmerald,
                                Color.White,
                                WarehouseEmerald,
                                WarehouseEmerald.copy(alpha = 0.1f)
                            )
                        )
                    )
            )
        }

        // Tap-to-Focus visual indicator
        focusPoint?.let { pt ->
            Box(
                modifier = Modifier
                    .offset { IntOffset((pt.x - 24).toInt(), (pt.y - 24).toInt()) }
                    .size(48.dp)
                    .border(2.dp, Color(0xFFFBBF24), CircleShape)
            )
        }

        // Camera Quick Controls (Torch, Zoom, Camera Switch)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 95.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Torch Button
            IconButton(
                onClick = {
                    camera?.let { cam ->
                        if (cam.cameraInfo.hasFlashUnit()) {
                            isTorchOn = !isTorchOn
                            cam.cameraControl.enableTorch(isTorchOn)
                        }
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .testTag("btn_toggle_torch")
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashlightOff,
                    contentDescription = "Toggle Torch",
                    tint = if (isTorchOn) Color(0xFFFBBF24) else Color.White
                )
            }

            // Lens Switch Button
            IconButton(
                onClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .testTag("btn_switch_camera")
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White
                )
            }

            // Zoom 2x Toggle Button
            IconButton(
                onClick = {
                    camera?.let { cam ->
                        val targetZoom = if (zoomRatio > 1.5f) 1.0f else 2.0f
                        cam.cameraControl.setZoomRatio(targetZoom)
                        zoomRatio = targetZoom
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .testTag("btn_toggle_zoom")
            ) {
                Text(
                    text = if (zoomRatio > 1.5f) "2x" else "1x",
                    color = WarehouseEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun triggerHapticFeedback(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(80)
        }
    } catch (e: Exception) {
        Log.w("BarcodeScanner", "Vibration unavailable", e)
    }
}
