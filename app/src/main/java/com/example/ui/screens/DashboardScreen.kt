package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleConstants
import com.example.data.VehicleEntry
import com.example.ui.WarehouseViewModel
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.StatCards
import com.example.ui.components.SummaryDashboardVisualizer
import com.example.ui.components.VehicleCard
import com.example.ui.theme.WarehouseAccentAmber
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseRed
import com.example.ui.theme.WarehouseSteelBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WarehouseViewModel,
    onNavigateToNewEntry: () -> Unit,
    onEditEntry: (VehicleEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.filteredEntries.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    val selectedActivity by viewModel.selectedActivityFilter.collectAsState()
    val selectedDock by viewModel.selectedDockFilter.collectAsState()
    val dockOccupancy by viewModel.dockOccupancy.collectAsState()

    var showDockAssignDialogForEntry by remember { mutableStateOf<VehicleEntry?>(null) }
    var selectedBayForDialog by remember { mutableStateOf("Bay 01") }
    var showDeleteConfirmForEntry by remember { mutableStateOf<VehicleEntry?>(null) }
    var showAssignVehicleForEmptyBay by remember { mutableStateOf<String?>(null) }
    var showScannerOnDashboard by remember { mutableStateOf(false) }

    val isFilterActive = searchQuery.isNotBlank() || selectedStatus != "All" || selectedActivity != "All" || selectedDock != "All"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Top KPI Stat Cards
            item {
                Spacer(modifier = Modifier.height(12.dp))
                StatCards(
                    stats = stats,
                    onFilterSelect = { status ->
                        viewModel.selectedStatusFilter.value = status
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Visual Summary Dashboard Component
                SummaryDashboardVisualizer(
                    stats = stats,
                    dockOccupancy = dockOccupancy,
                    entries = allEntries,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onFilterStatus = { status ->
                        viewModel.selectedStatusFilter.value = status
                    },
                    onFilterDock = { dock ->
                        viewModel.setDockFilter(dock)
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Search Bar & CameraX Quick Scan Action
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search (Vehicle, LR, Bay)...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = WarehouseSteelBlue) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_search_input")
                    )

                    Button(
                        onClick = { showScannerOnDashboard = true },
                        colors = ButtonDefaults.buttonColors(containerColor = WarehouseEmerald),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                        modifier = Modifier.testTag("btn_dashboard_scan_qr")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR/Barcode",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Dock Bay Filter Section Header & Quick Status Strip
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = WarehouseNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dock Bay Filter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Occupancy Summary Indicators
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WarehouseRed.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🔴 ${dockOccupancy.occupiedCount} Occupied",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarehouseRed
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WarehouseEmerald.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🟢 ${dockOccupancy.emptyCount} Empty",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarehouseEmerald
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Dock Number Filter Chips Row
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // All Docks Chip
                    item {
                        FilterChip(
                            selected = selectedDock == "All",
                            onClick = { viewModel.setDockFilter("All") },
                            label = { Text("🏢 All Docks", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarehouseNavy,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_dock_all")
                        )
                    }

                    // Occupied Docks Chip
                    item {
                        FilterChip(
                            selected = selectedDock == "Occupied",
                            onClick = { viewModel.setDockFilter("Occupied") },
                            label = { Text("🔴 Occupied Docks (${dockOccupancy.occupiedCount})", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF991B1B),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_dock_occupied")
                        )
                    }

                    // Unassigned / In-Yard Chip
                    item {
                        val unassignedCount = allEntries.count { it.status != "Gate Out" && (it.dockBay.isBlank() || it.dockBay == "None") }
                        FilterChip(
                            selected = selectedDock == "Empty",
                            onClick = { viewModel.setDockFilter("Empty") },
                            label = { Text("⚪ No Dock / Yard ($unassignedCount)", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarehouseSteelBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_dock_unassigned")
                        )
                    }

                    // Individual Bays (Bay 01 to Bay 10)
                    items(dockOccupancy.docks) { dockInfo ->
                        val isBaySelected = selectedDock.equals(dockInfo.bayNumber, ignoreCase = true)
                        FilterChip(
                            selected = isBaySelected,
                            onClick = {
                                if (isBaySelected) {
                                    viewModel.setDockFilter("All")
                                } else {
                                    viewModel.setDockFilter(dockInfo.bayNumber)
                                }
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (dockInfo.isOccupied) WarehouseRed else WarehouseEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (dockInfo.isOccupied) "${dockInfo.bayNumber} (${dockInfo.vehicleNumber.take(6)})" else "${dockInfo.bayNumber} (Free)",
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (dockInfo.isOccupied) WarehouseNavy else WarehouseEmerald,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_dock_${dockInfo.bayNumber.replace(" ", "_")}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Status Filter Chips Row
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statusChips = listOf(
                        "All" to "All Status",
                        "Gate In" to "Gate In",
                        "Dock Assigned / Placed" to "Dock Placed",
                        "In-Progress" to "In-Progress",
                        "Completed" to "Completed",
                        "Gate Out" to "Gate Out"
                    )

                    items(statusChips) { (statusKey, label) ->
                        FilterChip(
                            selected = selectedStatus == statusKey,
                            onClick = { viewModel.selectedStatusFilter.value = statusKey },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarehouseNavy,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_status_${statusKey.replace(" ", "_")}")
                        )
                    }
                }
            }

            // Activity Filter Chips Row (Loading / Unloading / Cross-Docking)
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val actChips = listOf(
                        "All" to "All Activities",
                        "Loading" to "📦 Loading (Outbound)",
                        "Unloading" to "📥 Unloading (Inbound)",
                        "Cross-Docking" to "🔄 Cross-Docking"
                    )

                    items(actChips) { (actKey, label) ->
                        FilterChip(
                            selected = selectedActivity == actKey,
                            onClick = { viewModel.selectedActivityFilter.value = actKey },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarehouseSteelBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_activity_${actKey.replace(" ", "_")}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Entries Count & Title Header + Reset filter button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedDock != "All") "Dock Filter Results ($selectedDock)" else "Live Vehicle Movements",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${entries.size} Vehicles found",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isFilterActive) {
                        TextButton(
                            onClick = { viewModel.resetFilters() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = WarehouseNavy)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset All", fontSize = 11.5.sp, color = WarehouseNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Vehicle Movement Cards List / Empty State
            if (entries.isEmpty()) {
                item {
                    val isSpecificDockEmpty = selectedDock.startsWith("Bay", ignoreCase = true)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isSpecificDockEmpty) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(WarehouseEmerald.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warehouse,
                                        contentDescription = null,
                                        tint = WarehouseEmerald,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Dock $selectedDock is Empty & Available",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Currently no vehicle assigned to $selectedDock (Dock is Empty).",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { showAssignVehicleForEmptyBay = selectedDock },
                                        colors = ButtonDefaults.buttonColors(containerColor = WarehouseNavy),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Assign Vehicle", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.setDockFilter("All") },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("All Docks", fontSize = 12.sp)
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Vehicles Found",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try clearing filters or add a new vehicle entry",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = onNavigateToNewEntry,
                                        colors = ButtonDefaults.buttonColors(containerColor = WarehouseNavy),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Add Vehicle")
                                    }
                                    if (isFilterActive) {
                                        OutlinedButton(
                                            onClick = { viewModel.resetFilters() },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Clear Filters")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        VehicleCard(
                            entry = entry,
                            onAdvanceStatus = { viewModel.advanceStatus(entry) },
                            onEdit = {
                                viewModel.populateForEdit(entry)
                                onEditEntry(entry)
                            },
                            onDelete = { showDeleteConfirmForEntry = entry },
                            onAssignDock = {
                                selectedBayForDialog = if (entry.dockBay.isNotBlank() && entry.dockBay != "None") entry.dockBay else "Bay 01"
                                showDockAssignDialogForEntry = entry
                            },
                            onSaveDocs = { docData ->
                                viewModel.updateDocumentation(entry, docData)
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button to Add New Vehicle Gate Entry
        FloatingActionButton(
            onClick = {
                viewModel.startNewEntry()
                onNavigateToNewEntry()
            },
            containerColor = WarehouseNavy,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_vehicle")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Vehicle Entry", modifier = Modifier.size(28.dp))
        }
    }

    // Quick Assign to Empty Bay Dialog
    showAssignVehicleForEmptyBay?.let { targetBay ->
        val unassignedList = remember(allEntries) {
            allEntries.filter { it.status == "Gate In" && (it.dockBay.isBlank() || it.dockBay == "None") }
        }
        var selectedVehicleToAssign by remember { mutableStateOf(unassignedList.firstOrNull()) }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAssignVehicleForEmptyBay = null },
            title = { Text("Assign Vehicle to $targetBay") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Target Bay: $targetBay", fontWeight = FontWeight.Bold, color = WarehouseNavy)
                    if (unassignedList.isEmpty()) {
                        Text(
                            text = "No unassigned Gate In vehicles in yard. Please gate-in a new vehicle first.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.5.sp
                        )
                    } else {
                        Text("Select Gate-In Vehicle from Yard:", fontSize = 12.5.sp)
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedVehicleToAssign?.let { "${it.vehicleNumber} (${it.activityType})" } ?: "Select Vehicle",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                unassignedList.forEach { v ->
                                    DropdownMenuItem(
                                        text = { Text("${v.vehicleNumber} - ${v.activityType} (${v.fromLocation})") },
                                        onClick = {
                                            selectedVehicleToAssign = v
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (unassignedList.isNotEmpty()) {
                    Button(
                        onClick = {
                            selectedVehicleToAssign?.let { v ->
                                viewModel.assignBay(v.id, targetBay)
                            }
                            showAssignVehicleForEmptyBay = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarehouseNavy)
                    ) {
                        Text("Confirm Assign")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignVehicleForEmptyBay = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dock Assignment Dialog
    showDockAssignDialogForEntry?.let { entry ->
        var dialogBayExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDockAssignDialogForEntry = null },
            title = { Text("Assign Dock Bay") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Vehicle: ${entry.vehicleNumber} (${entry.activityType})")
                    ExposedDropdownMenuBox(
                        expanded = dialogBayExpanded,
                        onExpandedChange = { dialogBayExpanded = !dialogBayExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedBayForDialog,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Bay") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dialogBayExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dialogBayExpanded,
                            onDismissRequest = { dialogBayExpanded = false }
                        ) {
                            VehicleConstants.DOCK_BAYS.forEach { bay ->
                                DropdownMenuItem(
                                    text = { Text(bay) },
                                    onClick = {
                                        selectedBayForDialog = bay
                                        dialogBayExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.assignBay(entry.id, selectedBayForDialog)
                        showDockAssignDialogForEntry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarehouseNavy)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDockAssignDialogForEntry = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    showDeleteConfirmForEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmForEntry = null },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete the record for ${entry.vehicleNumber}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEntry(entry)
                        showDeleteConfirmForEntry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmForEntry = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showScannerOnDashboard) {
        BarcodeScannerDialog(
            onDismiss = { showScannerOnDashboard = false },
            onScanned = { scanned ->
                showScannerOnDashboard = false
                val matching = allEntries.find { it.vehicleNumber.equals(scanned.vehicleNumber, ignoreCase = true) }
                if (matching != null) {
                    viewModel.searchQuery.value = scanned.vehicleNumber
                } else {
                    viewModel.startNewEntry()
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
                    onNavigateToNewEntry()
                }
            }
        )
    }
}
