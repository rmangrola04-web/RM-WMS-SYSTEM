package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleConstants
import com.example.data.VehicleEntry
import com.example.ui.WarehouseViewModel
import com.example.ui.components.ActivityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.WarehouseAccentAmber
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseSteelBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockBaysScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val allEntries by viewModel.allEntries.collectAsState()
    
    // Filter active docked vehicles
    val activeDockedMap = remember(allEntries) {
        allEntries
            .filter { it.status != "Gate Out" && it.dockBay.isNotBlank() && it.dockBay != "None" }
            .associateBy { it.dockBay }
    }

    val unassignedVehicles = remember(allEntries) {
        allEntries.filter { it.status == "Gate In" && (it.dockBay.isBlank() || it.dockBay == "None") }
    }

    var selectedBayForAssign by remember { mutableStateOf<String?>(null) }
    var selectedVehicleForAssign by remember { mutableStateOf<VehicleEntry?>(null) }
    var vehicleSelectExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Dock Header Overview Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarehouseSteelBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = WarehouseSteelBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Loading / Unloading Dock Bays",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${activeDockedMap.size} Occupied • ${10 - activeDockedMap.size} Available",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WarehouseEmerald))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Free", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WarehouseAccentAmber))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Occupied", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dock Bay Grid (Bay 01 to Bay 10)
        val bays = (1..10).map { String.format("Bay %02d", it) }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(bays) { bayName ->
                val vehicle = activeDockedMap[bayName]
                val isOccupied = vehicle != null

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOccupied) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isOccupied) {
                                selectedBayForAssign = bayName
                                selectedVehicleForAssign = unassignedVehicles.firstOrNull()
                            }
                        }
                        .testTag("dock_bay_card_${bayName.replace(" ", "_")}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isOccupied) WarehouseNavy else MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOccupied) WarehouseAccentAmber else WarehouseEmerald)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (vehicle != null) {
                            Text(
                                text = vehicle.vehicleNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = WarehouseNavy
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            ActivityBadge(activity = vehicle.activityType)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Placed: ${vehicle.placedTime.ifBlank { vehicle.inTime }}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    viewModel.advanceStatus(vehicle)
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WarehouseNavy),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Advance (${vehicle.status.take(8)})", fontSize = 10.sp)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Available",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = WarehouseEmerald
                                    )
                                    Text(
                                        text = "Tap to Dock",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog to Assign an in-yard vehicle to the selected empty dock
    selectedBayForAssign?.let { bay ->
        AlertDialog(
            onDismissRequest = { selectedBayForAssign = null },
            title = { Text("Assign Vehicle to $bay") },
            text = {
                if (unassignedVehicles.isEmpty()) {
                    Text("No unassigned Gate In vehicles in yard. Please gate-in a new vehicle first.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Select Vehicle in Yard:")
                        ExposedDropdownMenuBox(
                            expanded = vehicleSelectExpanded,
                            onExpandedChange = { vehicleSelectExpanded = !vehicleSelectExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedVehicleForAssign?.let { "${it.vehicleNumber} (${it.activityType})" } ?: "Select Vehicle",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleSelectExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = vehicleSelectExpanded,
                                onDismissRequest = { vehicleSelectExpanded = false }
                            ) {
                                unassignedVehicles.forEach { entry ->
                                    DropdownMenuItem(
                                        text = { Text("${entry.vehicleNumber} - ${entry.activityType} (${entry.vehicleType})") },
                                        onClick = {
                                            selectedVehicleForAssign = entry
                                            vehicleSelectExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (unassignedVehicles.isNotEmpty()) {
                    Button(
                        onClick = {
                            selectedVehicleForAssign?.let { v ->
                                viewModel.assignBay(v.id, bay)
                            }
                            selectedBayForAssign = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WarehouseNavy)
                    ) {
                        Text("Assign Dock")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBayForAssign = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
