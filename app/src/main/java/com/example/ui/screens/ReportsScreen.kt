package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ForwardToInbox
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleConstants
import com.example.ui.WarehouseViewModel
import com.example.ui.components.DownloadReportDialog
import com.example.ui.components.EmailReportDialog
import com.example.ui.components.StatusBadge
import com.example.ui.components.SummaryDashboardVisualizer
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseSteelBlue

@Composable
fun ReportsScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val allEntries by viewModel.allEntries.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val dockOccupancy by viewModel.dockOccupancy.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showEmailDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    val loadingCount = allEntries.count { it.activityType == "Loading" }
    val unloadingCount = allEntries.count { it.activityType == "Unloading" }
    val crossDockCount = allEntries.count { it.activityType == "Cross-Docking" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = WarehouseNavy),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Daily Activity Report",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Date: ${VehicleConstants.currentFormattedDateTime()}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ReportMetric("Total", stats.total.toString(), Color.White)
                        ReportMetric("In Yard", stats.activeInYard.toString(), Color(0xFF90CAF9))
                        ReportMetric("At Dock", stats.docked.toString(), Color(0xFFFFE082))
                        ReportMetric("Dispatched", stats.completedOrOut.toString(), Color(0xFFA7F3D0))
                    }
                }
            }
        }

        // Detailed Summary Dashboard Visualization Component
        item {
            SummaryDashboardVisualizer(
                stats = stats,
                dockOccupancy = dockOccupancy,
                entries = allEntries
            )
        }

        // Primary Download Report & Export Action Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = WarehouseEmerald,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Download Report",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Export all vehicle movement logs from Room DB to CSV",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showDownloadDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WarehouseEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("btn_download_report")
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(17.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download Report", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.Black)
                        }

                        Button(
                            onClick = { showEmailDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WarehouseSteelBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(44.dp)
                                .testTag("btn_open_email_report")
                        ) {
                            Icon(Icons.Default.ForwardToInbox, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Email", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // User Activity & Operator Audit Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = WarehouseNavy,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "User Activity Report",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WarehouseNavy.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${allUsers.size} Users",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarehouseNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    allUsers.forEach { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(WarehouseSteelBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = WarehouseSteelBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(user.fullName, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                                    Text("${user.email} • ID: ${user.userId}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(WarehouseEmerald.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(user.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WarehouseEmerald)
                                }
                                Text("Login: ${user.lastLogin?.take(10) ?: "N/A"}", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Activity Breakdown Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Activity Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ActivityRow("📦 Loading (Outbound Dispatch)", loadingCount, allEntries.size, Color(0xFFC2410C))
                    Spacer(modifier = Modifier.height(8.dp))
                    ActivityRow("📥 Unloading (Inbound Receiving)", unloadingCount, allEntries.size, Color(0xFF1D4ED8))
                    Spacer(modifier = Modifier.height(8.dp))
                    ActivityRow("🔄 Cross-Docking", crossDockCount, allEntries.size, Color(0xFF0F766E))
                }
            }
        }

        // Export Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val reportText = viewModel.generateFullReportExport()
                        clipboardManager.setText(AnnotatedString(reportText))
                        Toast.makeText(context, "Warehouse report copied!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarehouseNavy),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_copy_report")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Report", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val reportText = viewModel.generateFullReportExport()
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, reportText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Warehouse Movement Report")
                        context.startActivity(shareIntent)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_share_report")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontSize = 12.sp)
                }
            }
        }

        // Recent Completed Vehicles
        item {
            Text(
                text = "Recent Completed / Dispatched Vehicles",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val completedEntries = allEntries.filter { it.status == "Completed" || it.status == "Gate Out" }
        if (completedEntries.isEmpty()) {
            item {
                Text(
                    text = "No Gate Out or Completed vehicles yet.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(completedEntries) { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = entry.vehicleNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${entry.fromLocation} ➜ ${entry.toLocation}",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "In: ${entry.inTime} | Out: ${entry.outTime.ifBlank { "N/A" }}",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = entry.status)
                    }
                }
            }
        }
    }

    if (showDownloadDialog) {
        DownloadReportDialog(
            viewModel = viewModel,
            onDismiss = { showDownloadDialog = false }
        )
    }

    if (showEmailDialog) {
        EmailReportDialog(
            viewModel = viewModel,
            onDismiss = { showEmailDialog = false }
        )
    }
}

@Composable
private fun ReportMetric(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = title, fontSize = 10.5.sp, color = color.copy(alpha = 0.8f))
    }
}

@Composable
private fun ActivityRow(label: String, count: Int, total: Int, color: Color) {
    val percentage = if (total > 0) (count * 100 / total) else 0
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = "$count ($percentage%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = if (total > 0) count.toFloat() / total else 0f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}
