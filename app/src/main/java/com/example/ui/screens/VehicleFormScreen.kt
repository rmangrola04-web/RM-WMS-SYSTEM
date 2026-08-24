package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleConstants
import com.example.ui.WarehouseViewModel
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.DocFormData
import com.example.ui.components.LogisticsDocUpload
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseRed
import com.example.ui.theme.WarehouseSteelBlue

private val LoadingBlue = Color(0xFF0284C7)
private val LoadingBgLight = Color(0xFFF0F9FF)
private val UnloadingGreen = Color(0xFF059669)
private val UnloadingBgLight = Color(0xFFF0FDF4)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VehicleFormScreen(
    viewModel: WarehouseViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val activityType by viewModel.formActivityType.collectAsState()
    val vehicleNumber by viewModel.formVehicleNumber.collectAsState()
    val vehicleType by viewModel.formVehicleType.collectAsState()
    val transporter by viewModel.formTransporter.collectAsState()
    val placedTime by viewModel.formPlacedTime.collectAsState()
    val fromLocation by viewModel.formFromLocation.collectAsState()
    val toLocation by viewModel.formToLocation.collectAsState()
    val nextDestination by viewModel.formNextDestination.collectAsState()
    val inTime by viewModel.formInTime.collectAsState()
    val outTime by viewModel.formOutTime.collectAsState()
    val status by viewModel.formStatus.collectAsState()
    val remarks by viewModel.formRemarks.collectAsState()
    val dockBay by viewModel.formDockBay.collectAsState()
    val driverName by viewModel.formDriverName.collectAsState()
    val driverPhone by viewModel.formDriverPhone.collectAsState()
    val cartonsCount by viewModel.formCartonsCount.collectAsState()
    val sealNumber by viewModel.formSealNumber.collectAsState()
    val opStartTime by viewModel.formOpStartTime.collectAsState()
    val opEndTime by viewModel.formOpEndTime.collectAsState()
    val grnNumber by viewModel.formGrnNumber.collectAsState()
    val grnTime by viewModel.formGrnTime.collectAsState()
    val lrNumber by viewModel.formLrNumber.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    val formInvoiceFile by viewModel.formInvoiceFile.collectAsState()
    val formLrFile by viewModel.formLrFile.collectAsState()
    val formChecklistDone by viewModel.formChecklistDone.collectAsState()
    val formHasDiscrepancy by viewModel.formHasDiscrepancy.collectAsState()
    val formDiscrepancyType by viewModel.formDiscrepancyType.collectAsState()
    val formDiscrepancyFile by viewModel.formDiscrepancyFile.collectAsState()
    val formDiscrepancyRemarks by viewModel.formDiscrepancyRemarks.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var vehicleTypeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var dockBayExpanded by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var sourceHubExpanded by remember { mutableStateOf(false) }
    var destHubExpanded by remember { mutableStateOf(false) }

    val isLoadingMode = activityType == "Loading"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Form Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = WarehouseNavy),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (viewModel.editingEntryId != null) "Edit Vehicle Movement" else "Warehouse Vehicle Movement Tracker",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "गाड़ियों की लोडिंग और अनलोडिंग की सटीक एंट्री",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        currentUser?.let { user ->
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_user_info_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👤 ${user.fullName} (${user.category})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = Color(0xFF1E293B)
                        )
                        if (user.mobileNumber.isNotBlank()) {
                            Text(
                                text = "  |  📱 +91 ${user.mobileNumber}",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                    Text(
                        text = "Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF059669),
                        modifier = Modifier
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Activity Type (गतिविधि चुनें)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "1. एक्टिविटी का प्रकार (Activity Type - गतिविधि चुनें) *",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarehouseNavy
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Unloading Card Option (आवक)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isLoadingMode) UnloadingBgLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .border(
                                width = if (!isLoadingMode) 2.dp else 1.dp,
                                color = if (!isLoadingMode) UnloadingGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setActivityType("Unloading") }
                            .padding(12.dp)
                            .testTag("radio_operation_unloading")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = !isLoadingMode,
                                onClick = { viewModel.setActivityType("Unloading") }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "📦 Unloading",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (!isLoadingMode) UnloadingGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "(आवक / खाली हो रही है)",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Loading Card Option (जावक)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isLoadingMode) LoadingBgLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .border(
                                width = if (isLoadingMode) 2.dp else 1.dp,
                                color = if (isLoadingMode) LoadingBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setActivityType("Loading") }
                            .padding(12.dp)
                            .testTag("radio_operation_loading")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isLoadingMode,
                                onClick = { viewModel.setActivityType("Loading") }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "🚚 Loading",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isLoadingMode) LoadingBlue else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "(जावक / माल लोड हो रहा है)",
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 2: Common Vehicle Details
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "2. गाड़ी की सामान्य जानकारी (Common Details)",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarehouseNavy
                )

                // Vehicle Number with Barcode scanner
                Column {
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { viewModel.formVehicleNumber.value = it.uppercase() },
                        label = { Text("गाड़ी नंबर (Vehicle Number) *") },
                        placeholder = { Text("उदा. MP09 AB 1234") },
                        leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                        trailingIcon = {
                            IconButton(
                                onClick = { showBarcodeScanner = true },
                                modifier = Modifier.testTag("btn_scan_vehicle_barcode")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan Barcode/QR",
                                    tint = WarehouseEmerald
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_vehicle_number")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showBarcodeScanner = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarehouseEmerald.copy(alpha = 0.12f),
                            contentColor = WarehouseEmerald
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_open_gatepass_scanner")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "गेट पास / बारकोड स्कैन करें (Scan Barcode/QR)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Vehicle Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = vehicleTypeExpanded,
                    onExpandedChange = { vehicleTypeExpanded = !vehicleTypeExpanded }
                ) {
                    OutlinedTextField(
                        value = vehicleType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("गाड़ी का प्रकार (Vehicle Type) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleTypeExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("form_vehicle_type")
                    )
                    ExposedDropdownMenu(
                        expanded = vehicleTypeExpanded,
                        onDismissRequest = { vehicleTypeExpanded = false }
                    ) {
                        VehicleConstants.VEHICLE_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    viewModel.formVehicleType.value = type
                                    vehicleTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Transporter Name
                OutlinedTextField(
                    value = transporter,
                    onValueChange = { viewModel.formTransporter.value = it },
                    label = { Text("ट्रांसपोर्टर (Transporter Name) *") },
                    placeholder = { Text("उदा. V-Trans, TCI, Safechem, Blue Dart") },
                    leadingIcon = { Icon(Icons.Default.LocalShipping, contentDescription = null, tint = WarehouseSteelBlue) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_transporter")
                )

                // Driver Name & Phone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = driverName,
                        onValueChange = { viewModel.formDriverName.value = it },
                        label = { Text("ड्राइवर का नाम") },
                        placeholder = { Text("उदा. रमेश") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_driver_name")
                    )
                    OutlinedTextField(
                        value = driverPhone,
                        onValueChange = { viewModel.formDriverPhone.value = it },
                        label = { Text("संपर्क नंबर (Phone)") },
                        placeholder = { Text("10 अंक") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_driver_phone")
                    )
                }

                // Gate In-Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inTime,
                        onValueChange = { viewModel.formInTime.value = it },
                        label = { Text("गेट इन समय (Gate In-Time) *") },
                        placeholder = { Text("YYYY-MM-DD HH:mm") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_in_time")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.formInTime.value = VehicleConstants.currentFormattedDateTime() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_in_time_now")
                    ) {
                        Text("Now (अभी)", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 3: Dynamic Location Details (Source & Destination)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isLoadingMode) LoadingBlue else UnloadingGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "3. लोकेशन विवरण (Source & Destination Locations)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarehouseNavy
                    )
                }

                // 2. SOURCE LOCATION (Dynamic: Text input for Unloading, Dropdown for Loading)
                Column {
                    val sourceLabel = if (isLoadingMode) {
                        "Loading From (किस हब से लोड हो रही है) *"
                    } else {
                        "Loading From (कहाँ से लोड होकर आई) *"
                    }

                    Text(
                        text = "2. $sourceLabel",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (isLoadingMode) {
                        // Loading -> Source is Hub Dropdown (ICH Indore, AIL ICH Indore, HPL)
                        ExposedDropdownMenuBox(
                            expanded = sourceHubExpanded,
                            onExpandedChange = { sourceHubExpanded = !sourceHubExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = fromLocation,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("सोर्स हब चुनें (Select Source Hub) *") },
                                placeholder = { Text("-- हब चुनें --") },
                                leadingIcon = { Icon(Icons.Default.Warehouse, contentDescription = null, tint = LoadingBlue) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceHubExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("form_source_hub_select")
                            )
                            ExposedDropdownMenu(
                                expanded = sourceHubExpanded,
                                onDismissRequest = { sourceHubExpanded = false }
                            ) {
                                VehicleConstants.HUB_LOCATIONS.forEach { hub ->
                                    DropdownMenuItem(
                                        text = { Text(hub, fontWeight = if (fromLocation == hub) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            viewModel.formFromLocation.value = hub
                                            sourceHubExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            VehicleConstants.HUB_LOCATIONS.forEach { hub ->
                                FilterChip(
                                    selected = fromLocation == hub,
                                    onClick = { viewModel.formFromLocation.value = hub },
                                    label = { Text(hub, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LoadingBlue.copy(alpha = 0.2f))
                                )
                            }
                        }
                    } else {
                        // Unloading -> Source is Text Input (पार्टी/प्लांट का नाम लिखें)
                        OutlinedTextField(
                            value = fromLocation,
                            onValueChange = { viewModel.formFromLocation.value = it },
                            label = { Text("पार्टी / सप्लायर / प्लांट का नाम *") },
                            placeholder = { Text("पार्टी / प्लांट / शहर का नाम दर्ज करें") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = UnloadingGreen) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_from_location")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Pithampur Plant", "Ahmedabad Gateway", "Delhi NCR DC", "Bhopal Hub", "Kandla Port").forEach { loc ->
                                FilterChip(
                                    selected = fromLocation == loc,
                                    onClick = { viewModel.formFromLocation.value = loc },
                                    label = { Text(loc, fontSize = 10.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UnloadingGreen.copy(alpha = 0.2f))
                                )
                            }
                        }
                    }
                }

                // 3. DESTINATION LOCATION (Dynamic: Dropdown for Unloading, Text input for Loading)
                Column {
                    val destLabel = if (isLoadingMode) {
                        "Dispatch To (कहाँ जा रही है / पार्टी का नाम) *"
                    } else {
                        "Unloading At (कहाँ खाली हो रही है) *"
                    }

                    Text(
                        text = "3. $destLabel",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (isLoadingMode) {
                        // Loading -> Destination is Text Input (पार्टी / शहर का नाम)
                        OutlinedTextField(
                            value = toLocation,
                            onValueChange = { viewModel.formToLocation.value = it },
                            label = { Text("डेस्टिनेशन पार्टी / डिपो / ब्रांच *") },
                            placeholder = { Text("पार्टी / प्लांट / शहर का नाम दर्ज करें") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = LoadingBlue) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_to_location")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Bhiwandi Hub Mumbai", "Delhi Logistics Park", "Pune DC", "Jaipur Depot", "Dewas Hub").forEach { loc ->
                                FilterChip(
                                    selected = toLocation == loc,
                                    onClick = { viewModel.formToLocation.value = loc },
                                    label = { Text(loc, fontSize = 10.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = LoadingBlue.copy(alpha = 0.2f))
                                )
                            }
                        }
                    } else {
                        // Unloading -> Destination is Hub Dropdown (ICH Indore, AIL ICH Indore, HPL)
                        ExposedDropdownMenuBox(
                            expanded = destHubExpanded,
                            onExpandedChange = { destHubExpanded = !destHubExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = toLocation,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("अनलोडिंग हब चुनें (Select Unloading Hub) *") },
                                placeholder = { Text("-- हब चुनें --") },
                                leadingIcon = { Icon(Icons.Default.Warehouse, contentDescription = null, tint = UnloadingGreen) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = destHubExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("form_destination_hub_select")
                            )
                            ExposedDropdownMenu(
                                expanded = destHubExpanded,
                                onDismissRequest = { destHubExpanded = false }
                            ) {
                                VehicleConstants.HUB_LOCATIONS.forEach { hub ->
                                    DropdownMenuItem(
                                        text = { Text(hub, fontWeight = if (toLocation == hub) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            viewModel.formToLocation.value = hub
                                            destHubExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            VehicleConstants.HUB_LOCATIONS.forEach { hub ->
                                FilterChip(
                                    selected = toLocation == hub,
                                    onClick = { viewModel.formToLocation.value = hub },
                                    label = { Text(hub, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = UnloadingGreen.copy(alpha = 0.2f))
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 4: Conditional Operation Details (Loading Details OR Unloading Details)
        if (isLoadingMode) {
            // LOADING SECTION (Outbound)
            Card(
                colors = CardDefaults.cardColors(containerColor = LoadingBgLight.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, LoadingBlue.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = LoadingBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "4. लोडिंग विवरण (Loading Details - Outbound)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = LoadingBlue
                        )
                    }

                    // Dock Number & Dock Reaching Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dock Bay Dropdown
                        ExposedDropdownMenuBox(
                            expanded = dockBayExpanded,
                            onExpandedChange = { dockBayExpanded = !dockBayExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = dockBay,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("डक नंबर (Dock No.) *") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dockBayExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("form_dock_bay")
                            )
                            ExposedDropdownMenu(
                                expanded = dockBayExpanded,
                                onDismissRequest = { dockBayExpanded = false }
                            ) {
                                VehicleConstants.DOCK_BAYS.forEach { bay ->
                                    DropdownMenuItem(
                                        text = { Text(bay) },
                                        onClick = {
                                            viewModel.formDockBay.value = bay
                                            dockBayExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dock Reaching Time
                        OutlinedTextField(
                            value = placedTime,
                            onValueChange = { viewModel.formPlacedTime.value = it },
                            label = { Text("डक समय (Placed)") },
                            placeholder = { Text("HH:mm") },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_placed_time")
                        )
                    }

                    // Number of Cartons & Seal Number
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cartonsCount,
                            onValueChange = { viewModel.formCartonsCount.value = it.filter { char -> char.isDigit() } },
                            label = { Text("कुल कार्टन (Cartons) *") },
                            placeholder = { Text("उदा. 450") },
                            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_cartons_count")
                        )

                        OutlinedTextField(
                            value = sealNumber,
                            onValueChange = { viewModel.formSealNumber.value = it.uppercase() },
                            label = { Text("सील नंबर (Seal No.)") },
                            placeholder = { Text("उदा. SL-98234") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_seal_number")
                        )
                    }

                    // Loading Start & End Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = opStartTime,
                            onValueChange = { viewModel.formOpStartTime.value = it },
                            label = { Text("लोडिंग शुरू समय (Start)") },
                            placeholder = { Text("HH:mm") },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_op_start_time")
                        )

                        OutlinedTextField(
                            value = opEndTime,
                            onValueChange = { viewModel.formOpEndTime.value = it },
                            label = { Text("लोडिंग खत्म समय (End)") },
                            placeholder = { Text("HH:mm") },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_op_end_time")
                        )
                    }
                }
            }
        } else {
            // UNLOADING SECTION (Inbound)
            Card(
                colors = CardDefaults.cardColors(containerColor = UnloadingBgLight.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, UnloadingGreen.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = UnloadingGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "4. अनलोडिंग विवरण (Unloading Details - Inbound)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = UnloadingGreen
                        )
                    }

                    // Next Destination (गाड़ी कहाँ जाएगी?)
                    OutlinedTextField(
                        value = nextDestination,
                        onValueChange = { viewModel.formNextDestination.value = it },
                        label = { Text("गाड़ी कहाँ जाएगी? (Next Destination)") },
                        placeholder = { Text("उदा. रिटर्न ब्रांच, खाली गेट आउट") },
                        leadingIcon = { Icon(Icons.Default.ArrowForward, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_next_destination")
                    )

                    // Dock Number & Dock Reaching Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = dockBayExpanded,
                            onExpandedChange = { dockBayExpanded = !dockBayExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = dockBay,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("डक नंबर (Dock No.)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dockBayExpanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("form_dock_bay")
                            )
                            ExposedDropdownMenu(
                                expanded = dockBayExpanded,
                                onDismissRequest = { dockBayExpanded = false }
                            ) {
                                VehicleConstants.DOCK_BAYS.forEach { bay ->
                                    DropdownMenuItem(
                                        text = { Text(bay) },
                                        onClick = {
                                            viewModel.formDockBay.value = bay
                                            dockBayExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = placedTime,
                            onValueChange = { viewModel.formPlacedTime.value = it },
                            label = { Text("डक समय (Placed)") },
                            placeholder = { Text("HH:mm") },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_placed_time")
                        )
                    }

                    // Unloading Start & End Time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = opStartTime,
                            onValueChange = { viewModel.formOpStartTime.value = it },
                            label = { Text("अनलोडिंग शुरू (Start)") },
                            placeholder = { Text("HH:mm") },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_op_start_time")
                        )

                        OutlinedTextField(
                            value = opEndTime,
                            onValueChange = { viewModel.formOpEndTime.value = it },
                            label = { Text("अनलोडिंग खत्म (End)") },
                            placeholder = { Text("HH:mm") },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_op_end_time")
                        )
                    }

                    // GRN Number, GRN Time & LR Number
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = grnNumber,
                            onValueChange = { viewModel.formGrnNumber.value = it.uppercase() },
                            label = { Text("GRN No. (जीआरएन)") },
                            placeholder = { Text("उदा. GRN-102") },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_grn_number")
                        )

                        OutlinedTextField(
                            value = grnTime,
                            onValueChange = { viewModel.formGrnTime.value = it },
                            label = { Text("GRN समय (Time)") },
                            placeholder = { Text("HH:mm") },
                            leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_grn_time")
                        )
                    }

                    // LR / Bilty Number
                    OutlinedTextField(
                        value = lrNumber,
                        onValueChange = { viewModel.formLrNumber.value = it.uppercase() },
                        label = { Text("11 No./LR No. (बिल्टी नंबर)") },
                        placeholder = { Text("उदा. LR-IND-4501") },
                        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_lr_number")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 5: Status & Gate Out Details
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "5. स्टेटस और अतिरिक्त विवरण (Status & Remarks)",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarehouseNavy
                )

                // Current Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = status,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("वर्तमान स्टेटस (Current Status) *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("form_status")
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        VehicleConstants.STATUS_OPTIONS.forEach { (st, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.formStatus.value = st
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Gate Out Time (Optional or for completed movements)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = outTime,
                        onValueChange = { viewModel.formOutTime.value = it },
                        label = { Text("गेट आउट समय (Gate Out-Time)") },
                        placeholder = { Text("YYYY-MM-DD HH:mm") },
                        leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_out_time")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { viewModel.formOutTime.value = VehicleConstants.currentFormattedDateTime() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_out_time_now")
                    ) {
                        Text("Now (अभी)", fontSize = 11.sp)
                    }
                }

                // Remarks / Any Other Point (अन्य विवरण)
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { viewModel.formRemarks.value = it },
                    label = { Text("अन्य विवरण (Remarks / Any Other Point)") },
                    placeholder = { Text("गाड़ी में कोई डैमेज, देरी का कारण, स्पेशल निर्देश...") },
                    leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_remarks")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section 5: Logistics Verification Checklist & Discrepancy Doc Upload
        LogisticsDocUpload(
            processType = activityType,
            initialVehicleNo = vehicleNumber,
            initialFormData = DocFormData(
                vehicleNo = vehicleNumber,
                invoiceFile = formInvoiceFile,
                lrFile = formLrFile,
                checklistDone = formChecklistDone,
                hasDiscrepancy = formHasDiscrepancy,
                discrepancyType = formDiscrepancyType,
                discrepancyFile = formDiscrepancyFile,
                remarks = formDiscrepancyRemarks
            ),
            onFormDataChange = { updated ->
                viewModel.formInvoiceFile.value = updated.invoiceFile
                viewModel.formLrFile.value = updated.lrFile
                viewModel.formChecklistDone.value = updated.checklistDone
                viewModel.formHasDiscrepancy.value = updated.hasDiscrepancy
                viewModel.formDiscrepancyType.value = updated.discrepancyType
                viewModel.formDiscrepancyFile.value = updated.discrepancyFile
                viewModel.formDiscrepancyRemarks.value = updated.remarks
            },
            onSubmit = { updated ->
                viewModel.formInvoiceFile.value = updated.invoiceFile
                viewModel.formLrFile.value = updated.lrFile
                viewModel.formChecklistDone.value = updated.checklistDone
                viewModel.formHasDiscrepancy.value = updated.hasDiscrepancy
                viewModel.formDiscrepancyType.value = updated.discrepancyType
                viewModel.formDiscrepancyFile.value = updated.discrepancyFile
                viewModel.formDiscrepancyRemarks.value = updated.remarks
                viewModel.submitForm(onComplete = onNavigateBack)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = {
                viewModel.submitForm(onComplete = onNavigateBack)
            },
            enabled = !isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = WarehouseNavy),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("form_submit_button")
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("सबमिट हो रहा है...")
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (viewModel.editingEntryId != null) "लॉग अपडेट करें (Update Log)" else "लॉग सबमिट करें (Submit Vehicle Log)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (viewModel.editingEntryId != null) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    viewModel.startNewEntry()
                    onNavigateBack()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancel Editing")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismiss = { showBarcodeScanner = false },
            onScanned = { scanned ->
                viewModel.formVehicleNumber.value = scanned.vehicleNumber
                scanned.origin?.let { viewModel.formFromLocation.value = it }
                scanned.destination?.let { viewModel.formToLocation.value = it }
                scanned.activityType?.let { viewModel.formActivityType.value = it }
                scanned.vehicleType?.let { viewModel.formVehicleType.value = it }
                scanned.dockBay?.let { viewModel.formDockBay.value = it }
                scanned.driverName?.let { viewModel.formDriverName.value = it }
                scanned.driverPhone?.let { viewModel.formDriverPhone.value = it }
                scanned.remarks?.let { viewModel.formRemarks.value = it }
                if (viewModel.formInTime.value.isBlank()) {
                    viewModel.formInTime.value = VehicleConstants.currentFormattedDateTime()
                }
                showBarcodeScanner = false
            }
        )
    }
}
