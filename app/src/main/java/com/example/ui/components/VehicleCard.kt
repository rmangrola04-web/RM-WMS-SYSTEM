package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleConstants
import com.example.data.VehicleEntry
import com.example.ui.theme.WarehouseAccentAmber
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseRed
import com.example.ui.theme.WarehouseSteelBlue

import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Warning
import com.example.ui.components.DocFormData
import com.example.ui.components.DocumentAttachment
import com.example.ui.components.LogisticsDocUploadDialog

@Composable
fun VehicleCard(
    entry: VehicleEntry,
    onAdvanceStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAssignDock: () -> Unit,
    onSaveDocs: ((DocFormData) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var showDocDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val nextStatusAction = when (entry.status) {
        "Gate In" -> "Assign Dock"
        "Dock Assigned / Placed" -> "Start Operation"
        "In-Progress" -> "Mark Completed"
        "Completed" -> "Gate Out"
        else -> null
    }

    if (showDocDialog) {
        LogisticsDocUploadDialog(
            processType = if (entry.activityType.isNotBlank()) entry.activityType else "Unloading",
            initialVehicleNo = entry.vehicleNumber,
            initialFormData = DocFormData(
                vehicleNo = entry.vehicleNumber,
                invoiceFile = if (entry.invoiceFile.isNotBlank()) DocumentAttachment(fileName = entry.invoiceFile, isPdf = entry.invoiceFile.endsWith(".pdf", ignoreCase = true)) else null,
                lrFile = if (entry.lrFile.isNotBlank()) DocumentAttachment(fileName = entry.lrFile, isPdf = entry.lrFile.endsWith(".pdf", ignoreCase = true)) else null,
                checklistDone = entry.checklistDone,
                hasDiscrepancy = entry.hasDiscrepancy,
                discrepancyType = entry.discrepancyType,
                discrepancyFile = if (entry.discrepancyFile.isNotBlank()) DocumentAttachment(fileName = entry.discrepancyFile, isPdf = entry.discrepancyFile.endsWith(".pdf", ignoreCase = true)) else null,
                remarks = entry.discrepancyRemarks
            ),
            onSave = { docData ->
                onSaveDocs?.invoke(docData)
                showDocDialog = false
            },
            onDismiss = { showDocDialog = false }
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("vehicle_card_${entry.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Vehicle Number & Badges & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WarehouseNavy.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = WarehouseNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = entry.vehicleNumber,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (entry.transporter.isNotBlank()) "${entry.vehicleType} • ${entry.transporter}" else entry.vehicleType,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.testTag("vehicle_card_menu_${entry.id}")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Details") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Assign Dock Bay") },
                            leadingIcon = { Icon(Icons.Default.Warehouse, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                onAssignDock()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Docs & Checklist") },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = WarehouseNavy) },
                            onClick = {
                                menuOpen = false
                                showDocDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Copy Pass") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                val passText = "Vehicle: ${entry.vehicleNumber}\nActivity: ${entry.activityType}\nRoute: ${entry.fromLocation} -> ${entry.toLocation}\nGate In: ${entry.inTime}\nStatus: ${entry.status}\nDock: ${entry.dockBay.ifBlank { "N/A" }}"
                                clipboardManager.setText(AnnotatedString(passText))
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete Entry", color = WarehouseRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = WarehouseRed) },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges Row: Activity & Status & Dock Bay & Discrepancy/Doc Flags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActivityBadge(activity = entry.activityType)
                StatusBadge(status = entry.status)
                if (entry.dockBay.isNotBlank() && entry.dockBay != "None") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(WarehouseSteelBlue.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warehouse,
                                contentDescription = null,
                                tint = WarehouseSteelBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = entry.dockBay,
                                color = WarehouseSteelBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (entry.hasDiscrepancy) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(WarehouseRed.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = WarehouseRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (entry.discrepancyType.isNotBlank()) entry.discrepancyType else "Discrepancy",
                                color = WarehouseRed,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (entry.checklistDone) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(WarehouseEmerald.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "✓ Verified",
                            color = WarehouseEmerald,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Route: Origin -> Destination
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = WarehouseEmerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = entry.fromLocation,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = entry.toLocation,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timestamps brief
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "In: ${entry.inTime.ifBlank { "--" }}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (entry.placedTime.isNotBlank()) {
                    Text(
                        text = "Docked: ${entry.placedTime}",
                        fontSize = 11.5.sp,
                        color = WarehouseSteelBlue,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (entry.outTime.isNotBlank()) {
                    Text(
                        text = "Out: ${entry.outTime}",
                        fontSize = 11.5.sp,
                        color = WarehouseRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Expanded details
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Transporter & Specifics
                    if (entry.transporter.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Transporter: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = entry.transporter,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (entry.cartonsCount > 0 || entry.sealNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (entry.cartonsCount > 0) {
                                Text(
                                    text = "Cartons: ${entry.cartonsCount}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarehouseNavy
                                )
                            }
                            if (entry.sealNumber.isNotBlank()) {
                                Text(
                                    text = "Seal: ${entry.sealNumber}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarehouseEmerald
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (entry.grnNumber.isNotBlank() || entry.lrNumber.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (entry.grnNumber.isNotBlank()) {
                                Text(
                                    text = "GRN: ${entry.grnNumber}${if (entry.grnTime.isNotBlank()) " (${entry.grnTime})" else ""}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarehouseSteelBlue
                                )
                            }
                            if (entry.lrNumber.isNotBlank()) {
                                Text(
                                    text = "LR: ${entry.lrNumber}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (entry.operationStartTime.isNotBlank() || entry.operationEndTime.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Op Duration: ${entry.operationStartTime.ifBlank { "--" }} ➜ ${entry.operationEndTime.ifBlank { "Ongoing" }}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (entry.nextDestination.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Next Dest: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = entry.nextDestination,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (entry.remarks.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Remarks: ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = entry.remarks,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    if (entry.driverName.isNotBlank() || entry.driverPhone.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Driver: ${entry.driverName.ifBlank { "N/A" }}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (entry.driverPhone.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${entry.driverPhone}"))
                                            context.startActivity(intent)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Call,
                                        contentDescription = null,
                                        tint = WarehouseEmerald,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = entry.driverPhone,
                                        fontSize = 12.sp,
                                        color = WarehouseEmerald,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Documentation & Discrepancy Status row
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Documentation Status:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val docStatusSummary = buildList {
                                if (entry.invoiceFile.isNotBlank()) add("Invoice: ${entry.invoiceFile}")
                                if (entry.lrFile.isNotBlank()) add("LR: ${entry.lrFile}")
                                if (entry.checklistDone) add("Checklist: Verified")
                                if (entry.hasDiscrepancy) add("Discrepancy: ${entry.discrepancyType} (${entry.discrepancyRemarks.ifBlank { "Damage/Shortage" }})")
                            }
                            if (docStatusSummary.isEmpty()) {
                                Text(
                                    text = "Pending Upload / Verification",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                docStatusSummary.forEach { item ->
                                    Text(
                                        text = item,
                                        fontSize = 11.sp,
                                        color = if (item.startsWith("Discrepancy")) WarehouseRed else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = { showDocDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(13.dp), tint = WarehouseNavy)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Docs", fontSize = 11.sp, color = WarehouseNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Advance Status or Expand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (expanded) "Show Less" else "View Details",
                        fontSize = 12.sp,
                        color = WarehouseSteelBlue,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = WarehouseSteelBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (nextStatusAction != null) {
                    Button(
                        onClick = onAdvanceStatus,
                        colors = ButtonDefaults.buttonColors(containerColor = WarehouseNavy),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        modifier = Modifier.testTag("advance_status_button_${entry.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = nextStatusAction,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
