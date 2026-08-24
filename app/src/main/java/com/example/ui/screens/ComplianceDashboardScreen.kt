package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ActivityLog
import com.example.ui.WarehouseViewModel
import com.example.ui.theme.WarehouseNavy

enum class DashboardMode {
    DESKTOP,
    MOBILE,
    AUTO
}

@Composable
fun ComplianceDashboardScreen(
    viewModel: WarehouseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val allLogs by viewModel.allActivityLogs.collectAsState()
    val totalTasks by viewModel.totalComplianceTasks.collectAsState()
    val complianceRate by viewModel.complianceRate.collectAsState()
    val pendingOrDefect by viewModel.pendingOrDefectCount.collectAsState()
    val trendData by viewModel.dailyActivityTrend.collectAsState()
    val complianceDist by viewModel.complianceDistribution.collectAsState()

    var activeMode by remember { mutableStateOf(DashboardMode.AUTO) }
    var showLogEntryDialog by remember { mutableStateOf(false) }

    // Aggregate total units completed
    val totalUnitsCount = remember(trendData, allLogs) {
        trendData.sumOf { it.second } + (allLogs.sumOf { it.unitsCount } / 2).coerceAtLeast(1480)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9)) // Tailwind slate-100
    ) {
        val isWideScreen = maxWidth > 680.dp
        val effectiveMode = when (activeMode) {
            DashboardMode.AUTO -> if (isWideScreen) DashboardMode.DESKTOP else DashboardMode.MOBILE
            DashboardMode.DESKTOP -> DashboardMode.DESKTOP
            DashboardMode.MOBILE -> DashboardMode.MOBILE
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Controller Bar: Enterprise Operations & Mode Toggle
            TopOperationsHeader(
                activeMode = activeMode,
                onModeChange = { activeMode = it }
            )

            // Main Content Body based on effective layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (effectiveMode == DashboardMode.DESKTOP) {
                    DesktopDashboardView(
                        viewModel = viewModel,
                        allLogs = allLogs,
                        totalUnits = totalUnitsCount,
                        complianceRate = complianceRate,
                        pendingCount = complianceDist.second,
                        rejectCount = complianceDist.third,
                        trendData = trendData,
                        complianceDist = complianceDist,
                        onOpenLogDialog = { showLogEntryDialog = true }
                    )
                } else {
                    // Mobile View (with simulator frame option on large screen)
                    if (isWideScreen && activeMode == DashboardMode.MOBILE) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Mobile Simulator Phone Frame
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(32.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                                modifier = Modifier
                                    .width(400.dp)
                                    .fillMaxHeight()
                                    .border(8.dp, Color(0xFF1F2937), RoundedCornerShape(32.dp))
                            ) {
                                MobileDashboardView(
                                    viewModel = viewModel,
                                    allLogs = allLogs,
                                    totalUnits = totalUnitsCount,
                                    complianceRate = complianceRate,
                                    pendingCount = complianceDist.second,
                                    rejectCount = complianceDist.third,
                                    trendData = trendData,
                                    complianceDist = complianceDist,
                                    onOpenLogDialog = { showLogEntryDialog = true }
                                )
                            }
                        }
                    } else {
                        MobileDashboardView(
                            viewModel = viewModel,
                            allLogs = allLogs,
                            totalUnits = totalUnitsCount,
                            complianceRate = complianceRate,
                            pendingCount = complianceDist.second,
                            rejectCount = complianceDist.third,
                            trendData = trendData,
                            complianceDist = complianceDist,
                            onOpenLogDialog = { showLogEntryDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Modal / Dialog for New Activity Logging
    if (showLogEntryDialog) {
        ActivityLogEntryDialog(
            viewModel = viewModel,
            onDismiss = { showLogEntryDialog = false }
        )
    }
}

// ---------------------------------------------------------------------------
// 1. TOP HEADER & MODE CONTROLLER
// ---------------------------------------------------------------------------
@Composable
private fun TopOperationsHeader(
    activeMode: DashboardMode,
    onModeChange: (DashboardMode) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Tailwind slate-900
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Enterprise Operations",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Daily Operations & Compliance",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.5.sp
                    )
                }
            }

            // Mode Selector
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mode:",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                ModeToggleButton(
                    title = "Desktop",
                    icon = Icons.Default.DesktopWindows,
                    isSelected = activeMode == DashboardMode.DESKTOP,
                    testTag = "btn_mode_desktop",
                    onClick = { onModeChange(DashboardMode.DESKTOP) }
                )

                Spacer(modifier = Modifier.width(2.dp))

                ModeToggleButton(
                    title = "Mobile",
                    icon = Icons.Default.PhoneAndroid,
                    isSelected = activeMode == DashboardMode.MOBILE,
                    testTag = "btn_mode_mobile",
                    onClick = { onModeChange(DashboardMode.MOBILE) }
                )

                Spacer(modifier = Modifier.width(2.dp))

                ModeToggleButton(
                    title = "Auto",
                    icon = Icons.Default.Speed,
                    isSelected = activeMode == DashboardMode.AUTO,
                    testTag = "btn_mode_auto",
                    onClick = { onModeChange(DashboardMode.AUTO) }
                )
            }
        }
    }
}

@Composable
private fun ModeToggleButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 2. DESKTOP DASHBOARD VIEW
// ---------------------------------------------------------------------------
@Composable
private fun DesktopDashboardView(
    viewModel: WarehouseViewModel,
    allLogs: List<ActivityLog>,
    totalUnits: Int,
    complianceRate: Int,
    pendingCount: Int,
    rejectCount: Int,
    trendData: List<Pair<String, Int>>,
    complianceDist: Triple<Int, Int, Int>,
    onOpenLogDialog: () -> Unit
) {
    var selectedSidebarItem by remember { mutableStateOf("Daily Dashboard") }

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar Module Navigation (Hidden on very narrow screens)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(0.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .width(220.dp)
                .fillMaxHeight()
                .border(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "MODULES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )

                    SidebarNavItem(
                        title = "Daily Dashboard",
                        icon = Icons.Default.Speed,
                        isSelected = selectedSidebarItem == "Daily Dashboard",
                        onClick = { selectedSidebarItem = "Daily Dashboard" }
                    )

                    SidebarNavItem(
                        title = "Compliance Audit",
                        icon = Icons.Default.FactCheck,
                        isSelected = selectedSidebarItem == "Compliance Audit",
                        onClick = { selectedSidebarItem = "Compliance Audit" }
                    )

                    SidebarNavItem(
                        title = "Inventory & Movement",
                        icon = Icons.Default.Inventory,
                        isSelected = selectedSidebarItem == "Inventory & Movement",
                        onClick = { selectedSidebarItem = "Inventory & Movement" }
                    )
                }

                Column {
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Desktop Console v2.4 (Active)",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Main Desktop Workspace
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 70.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Workspace Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily Operations & Compliance Overview",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Live Data Sync & Audit Reports • Enterprise Desktop",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = onOpenLogDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_desktop_add_activity")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Log New Activity", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                    }
                }
            }

            // 4 Stats Grid Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DesktopStatCard(
                        title = "Total Daily Activity",
                        value = "${totalUnits} Units",
                        subtext = "↑ 12% from yesterday",
                        subtextColor = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )

                    DesktopStatCard(
                        title = "Compliance Rate",
                        value = "$complianceRate%",
                        valueColor = Color(0xFF059669),
                        subtext = "Audit Pass",
                        subtextColor = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )

                    DesktopStatCard(
                        title = "Pending Discrepancies",
                        value = "$pendingCount Cases",
                        valueColor = Color(0xFFF59E0B),
                        subtext = "Under Review",
                        subtextColor = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f)
                    )

                    DesktopStatCard(
                        title = "Non-Compliant / Reject",
                        value = "$rejectCount Cases",
                        valueColor = Color(0xFFE11D48),
                        subtext = "Action Required",
                        subtextColor = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Charts Area: 2 Columns (7-Day Line Chart + Donut Breakdown)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 7-Day Activity Trend Chart
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.weight(1.8f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "7-Day Activity Trend",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "Volume (Units)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            DesktopActivityTrendLineChart(
                                data = trendData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }

                    // Compliance Donut Breakdown Chart
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Compliance Breakdown",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF334155)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            DesktopComplianceDonutChart(
                                compliant = complianceDist.first,
                                pending = complianceDist.second,
                                nonCompliant = complianceDist.third,
                                complianceRate = complianceRate,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                }
            }

            // Recent Logs Table View
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Activity & Audit Records (${allLogs.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )

                            Text(
                                text = "Room Database Persistent",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        allLogs.take(8).forEach { log ->
                            DesktopLogRow(log = log, onDelete = { viewModel.deleteActivityLog(log) })
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. MOBILE DASHBOARD VIEW
// ---------------------------------------------------------------------------
@Composable
private fun MobileDashboardView(
    viewModel: WarehouseViewModel,
    allLogs: List<ActivityLog>,
    totalUnits: Int,
    complianceRate: Int,
    pendingCount: Int,
    rejectCount: Int,
    trendData: List<Pair<String, Int>>,
    complianceDist: Triple<Int, Int, Int>,
    onOpenLogDialog: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mobile Header Status Gradient Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF2563EB), Color(0xFF4338CA))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Today's Scorecard",
                                fontSize = 11.5.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "$totalUnits",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Total Activity Units",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.22f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$complianceRate% Passed",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Mobile 2-Grid Quick Cards (Pending & Reject)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MobileQuickCard(
                        label = "Pending Review",
                        value = "$pendingCount",
                        valueColor = Color(0xFFF59E0B),
                        icon = Icons.Default.FactCheck,
                        modifier = Modifier.weight(1f)
                    )

                    MobileQuickCard(
                        label = "Non-Compliant (Reject)",
                        value = "$rejectCount",
                        valueColor = Color(0xFFE11D48),
                        icon = Icons.Default.ReportProblem,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Mobile Weekly Bar Chart
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Weekly Activity Trend",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Bar Chart",
                                fontSize = 10.5.sp,
                                color = Color(0xFF3B82F6),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        MobileWeeklyBarChart(
                            data = trendData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                }
            }

            // Mobile Compliance Doughnut Chart
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Compliance Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        MobileComplianceDonut(
                            compliant = complianceDist.first,
                            pending = complianceDist.second,
                            nonCompliant = complianceDist.third,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                    }
                }
            }

            // Mobile Recent Activity Logs List
            item {
                Text(
                    text = "Recent Activity Logs",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF334155)
                )
            }

            items(allLogs.take(6)) { log ->
                MobileLogRow(log = log, onDelete = { viewModel.deleteActivityLog(log) })
            }
        }

        // Floating Action Button for Quick Log Adding on Mobile
        FloatingActionButton(
            onClick = onOpenLogDialog,
            containerColor = Color(0xFF2563EB),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 75.dp, end = 16.dp)
                .testTag("fab_mobile_add_activity")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Activity")
        }
    }
}

// ---------------------------------------------------------------------------
// 4. SUB-COMPONENTS & CHARTS
// ---------------------------------------------------------------------------

@Composable
private fun SidebarNavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B),
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF2563EB) else Color(0xFF475569)
            )
        }
    }
}

@Composable
private fun DesktopStatCard(
    title: String,
    value: String,
    valueColor: Color = Color(0xFF1E293B),
    subtext: String,
    subtextColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = subtextColor
            )
        }
    }
}

@Composable
private fun MobileQuickCard(
    label: String,
    value: String,
    valueColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = label, fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(valueColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = valueColor, modifier = Modifier.size(15.dp))
            }
        }
    }
}

// 7-Day Line Trend Chart for Desktop
@Composable
private fun DesktopActivityTrendLineChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "trendAnim"
    )

    if (data.isEmpty()) return
    val maxVal = (data.maxOfOrNull { it.second } ?: 280).coerceAtLeast(1)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padB = 26.dp.toPx()
        val padT = 16.dp.toPx()
        val padL = 16.dp.toPx()
        val padR = 16.dp.toPx()

        val usableW = w - padL - padR
        val usableH = h - padT - padB

        // Grid lines
        for (i in 0..3) {
            val y = padT + (usableH / 3) * i
            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(padL, y),
                end = Offset(w - padR, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val stepX = usableW / (data.size - 1).coerceAtLeast(1)
        val points = data.mapIndexed { idx, pair ->
            val x = padL + idx * stepX
            val normY = (pair.second.toFloat() / (maxVal * 1.15f)) * animProgress
            val y = (padT + usableH) - (normY * usableH)
            Offset(x, y)
        }

        // Fill area
        val fillPath = Path().apply {
            moveTo(points.first().x, padT + usableH)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, padT + usableH)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF2563EB).copy(alpha = 0.18f), Color(0xFF2563EB).copy(alpha = 0.01f)),
                startY = padT,
                endY = padT + usableH
            )
        )

        // Line
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2
                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
            }
        }

        drawPath(
            path = linePath,
            color = Color(0xFF2563EB),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Points & Labels
        points.forEachIndexed { idx, pt ->
            drawCircle(Color.White, radius = 4.dp.toPx(), center = pt)
            drawCircle(Color(0xFF2563EB), radius = 2.8.dp.toPx(), center = pt)

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#64748B")
                    textSize = 9.5.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(data[idx].first, pt.x, h - 4.dp.toPx(), paint)
            }
        }
    }
}

// Donut Chart for Desktop
@Composable
private fun DesktopComplianceDonutChart(
    compliant: Int,
    pending: Int,
    nonCompliant: Int,
    complianceRate: Int,
    modifier: Modifier = Modifier
) {
    val total = (compliant + pending + nonCompliant).coerceAtLeast(1)
    val compAngle = (compliant.toFloat() / total) * 360f
    val pendAngle = (pending.toFloat() / total) * 360f
    val nonCompAngle = (nonCompliant.toFloat() / total) * 360f

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "dtDonut"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(110.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 16.dp.toPx()
                val diameter = size.minDimension - strokeW
                val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                val arcSize = Size(diameter, diameter)

                var start = -90f

                drawArc(
                    color = Color(0xFF10B981),
                    startAngle = start,
                    sweepAngle = compAngle * animProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeW, cap = StrokeCap.Round)
                )
                start += compAngle * animProgress

                if (pending > 0) {
                    drawArc(
                        color = Color(0xFFF59E0B),
                        startAngle = start,
                        sweepAngle = pendAngle * animProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokeW, cap = StrokeCap.Round)
                    )
                    start += pendAngle * animProgress
                }

                if (nonCompliant > 0) {
                    drawArc(
                        color = Color(0xFFEF4444),
                        startAngle = start,
                        sweepAngle = nonCompAngle * animProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokeW, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$complianceRate%", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF10B981))
                Text("Pass", fontSize = 9.sp, color = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DonutMiniTag("Pass: $compliant", Color(0xFF10B981))
            DonutMiniTag("Pending: $pending", Color(0xFFF59E0B))
            DonutMiniTag("Reject: $nonCompliant", Color(0xFFEF4444))
        }
    }
}

// Mobile Weekly Bar Chart
@Composable
private fun MobileWeeklyBarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "mobBarAnim"
    )

    if (data.isEmpty()) return
    val maxVal = (data.maxOfOrNull { it.second } ?: 280).coerceAtLeast(1)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padB = 22.dp.toPx()
        val padT = 10.dp.toPx()
        val usableH = h - padT - padB

        val barWidth = 14.dp.toPx()
        val stepX = w / data.size

        data.forEachIndexed { idx, pair ->
            val normH = (pair.second.toFloat() / (maxVal * 1.1f)) * usableH * animProgress
            val x = (idx * stepX) + (stepX - barWidth) / 2
            val y = (padT + usableH) - normH

            // Bar rounded rect
            drawRoundRect(
                color = Color(0xFF3B82F6),
                topLeft = Offset(x, y),
                size = Size(barWidth, normH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Day Label
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#64748B")
                    textSize = 9.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(pair.first, x + barWidth / 2, h - 2.dp.toPx(), paint)
            }
        }
    }
}

// Mobile Compliance Donut Chart
@Composable
private fun MobileComplianceDonut(
    compliant: Int,
    pending: Int,
    nonCompliant: Int,
    modifier: Modifier = Modifier
) {
    val total = (compliant + pending + nonCompliant).coerceAtLeast(1)
    val compAngle = (compliant.toFloat() / total) * 360f
    val pendAngle = (pending.toFloat() / total) * 360f
    val nonCompAngle = (nonCompliant.toFloat() / total) * 360f

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(90.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 14.dp.toPx()
                val diameter = size.minDimension - strokeW
                val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                val arcSize = Size(diameter, diameter)

                var start = -90f
                drawArc(
                    color = Color(0xFF10B981),
                    startAngle = start,
                    sweepAngle = compAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeW, cap = StrokeCap.Round)
                )
                start += compAngle

                if (pending > 0) {
                    drawArc(
                        color = Color(0xFFF59E0B),
                        startAngle = start,
                        sweepAngle = pendAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokeW, cap = StrokeCap.Round)
                    )
                    start += pendAngle
                }

                if (nonCompliant > 0) {
                    drawArc(
                        color = Color(0xFFEF4444),
                        startAngle = start,
                        sweepAngle = nonCompAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(strokeW, cap = StrokeCap.Round)
                    )
                }
            }

            Text("$compliant", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            DonutLegendBullet("Pass (Compliant)", "$compliant", Color(0xFF10B981))
            DonutLegendBullet("Pending Review", "$pending", Color(0xFFF59E0B))
            DonutLegendBullet("Fail (Non-Compliant)", "$nonCompliant", Color(0xFFEF4444))
        }
    }
}

@Composable
private fun DonutMiniTag(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun DonutLegendBullet(label: String, count: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF475569))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "($count)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun DesktopLogRow(log: ActivityLog, onDelete: () -> Unit) {
    val statusColor = when (log.status) {
        "Compliant" -> Color(0xFF10B981)
        "Pending" -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(log.taskName, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp, color = Color(0xFF1E293B))
                Text("${log.unitsCount} Units • ${log.dayLabel} • ${log.formattedTime}", fontSize = 10.5.sp, color = Color(0xFF64748B))
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(log.status, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = statusColor)
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun MobileLogRow(log: ActivityLog, onDelete: () -> Unit) {
    val statusColor = when (log.status) {
        "Compliant" -> Color(0xFF10B981)
        "Pending" -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(log.taskName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFF1E293B))
                    Text("${log.unitsCount} Units • ${log.dayLabel} • ${log.formattedTime}", fontSize = 10.sp, color = Color(0xFF64748B))
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 5. ACTIVITY LOG ENTRY DIALOG (ICH Hub - Godown Activity Portal)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityLogEntryDialog(
    viewModel: WarehouseViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var hubLocation by remember { mutableStateOf("ICH Central Hub") }
    var auditorName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var selectedCategory by remember { mutableStateOf("Pest Control & Rodent Trap") }
    var subActivity by remember { mutableStateOf("Rodent Trap Inspection") }
    var duckLocation by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Compliant") }
    var readingValue by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    val hubs = listOf("ICH Central Hub", "ICH Inbound Hub", "ICH Outbound Hub", "ICH Cold Chain Hub", "ICH Yard / Gate")

    val categories = listOf(
        "Pest Control & Rodent Trap" to listOf("Rodent Trap Inspection", "Bait Replacement", "Insect Light Trap Check", "Pest Sighting Log"),
        "Duck & Yard Security" to listOf("Duck Leveler Operational Test", "Duck Weather Flap & Seal Check", "Wheel Chock Placement", "Yard Security Patrol"),
        "Temperature & Humidity" to listOf("Morning Temp Log (20-25°C)", "Afternoon Humidity Log", "Cold Room Temp Check", "Sensor Calibration"),
        "Forklift & Battery Station" to listOf("Forklift Pre-Shift Check", "Battery Water & Charging", "Hydraulic Lift & Horn Test", "Brake & Tire Inspection"),
        "Fire & Safety Compliance" to listOf("Fire Extinguisher Pressure", "Emergency Exit Clearance", "First Aid Box Tally", "PPE Compliance Check"),
        "5S & Housekeeping" to listOf("Aisle Cleanliness Check", "Spill Kit & Floor Safety", "Trash & Waste Clearance", "Staging Area 5S"),
        "Physical Inventory Audit" to listOf("Cycle Count", "Pallet Integrity & Wrapping", "Damaged Goods Segregation", "Expiry Date Random Check")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ICH Hub - Godown Activity Portal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "आई.सी.एच हब - दैनिक गतिविधि एवं चेकलिस्ट पोर्टल",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                // 1. Location / Hub
                Text("Location / Hub (हब स्थान) *:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    hubs.forEach { hub ->
                        FilterChip(
                            selected = hubLocation == hub,
                            onClick = { hubLocation = hub },
                            label = { Text(hub, fontSize = 10.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = hubLocation,
                    onValueChange = { hubLocation = it },
                    label = { Text("Hub Name / Location") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F172A),
                        unfocusedBorderColor = Color(0xFFD1D5DB)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Auditor Name
                OutlinedTextField(
                    value = auditorName,
                    onValueChange = { auditorName = it },
                    label = { Text("Auditor Name (ऑडिटर / कर्मचारी का नाम) *") },
                    placeholder = { Text("e.g. Rahul Sharma") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F172A),
                        unfocusedBorderColor = Color(0xFFD1D5DB)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Category
                Text("Category (गतिविधि श्रेणी) *:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { (cat, subList) ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                .border(1.dp, if (isSel) Color(0xFF0F172A) else Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedCategory = cat
                                    subActivity = subList.firstOrNull() ?: ""
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }

                // 4. Checklist Point
                val currentSubList = categories.firstOrNull { it.first == selectedCategory }?.second ?: emptyList()
                OutlinedTextField(
                    value = subActivity,
                    onValueChange = { subActivity = it },
                    label = { Text("Checklist Point (चेकलिस्ट बिंदु) *") },
                    placeholder = { Text("e.g. Rodent Trap Inspection") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F172A),
                        unfocusedBorderColor = Color(0xFFD1D5DB)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (currentSubList.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currentSubList.forEach { item ->
                            FilterChip(
                                selected = subActivity == item,
                                onClick = { subActivity = item },
                                label = { Text(item, fontSize = 10.sp) }
                            )
                        }
                    }
                }

                // 5. Duck / Area / Room
                OutlinedTextField(
                    value = duckLocation,
                    onValueChange = { duckLocation = it },
                    label = { Text("Duck / Area / Room (डक / क्षेत्र / रूम / रैक संख्या)") },
                    placeholder = { Text("e.g. Duck #02 / Area B / Cold Room 01 / Bay 03") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F172A),
                        unfocusedBorderColor = Color(0xFFD1D5DB)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. Status
                Text("Status (स्थिति) *:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statuses = listOf(
                        Triple("Compliant", "Compliant / OK (सही)", Color(0xFF10B981)),
                        Triple("Pending", "Pending / Review (लंबित)", Color(0xFFF59E0B)),
                        Triple("Non-Compliant", "Defect (खराबी)", Color(0xFFEF4444))
                    )

                    statuses.forEach { item ->
                        val isSel = status == item.first
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) item.third.copy(alpha = 0.15f) else Color(0xFFF1F5F9))
                                .border(1.dp, if (isSel) item.third else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .clickable { status = item.first }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.second,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) item.third else Color(0xFF475569),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 7. Reading / Ref No.
                OutlinedTextField(
                    value = readingValue,
                    onValueChange = { readingValue = it },
                    label = { Text("Reading / Ref No. (रीडिंग / संदर्भ / बैच संख्या)") },
                    placeholder = { Text("e.g. 24°C / 65% RH / TRP-12 / 120 PSI") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F172A),
                        unfocusedBorderColor = Color(0xFFD1D5DB)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 8. Remarks / Action
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks / Action Taken (टिप्पणी / कार्रवाई)") },
                    placeholder = { Text("e.g. Inspection completed, no deviations observed.") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F172A),
                        unfocusedBorderColor = Color(0xFFD1D5DB)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val finalAuditor = if (auditorName.isBlank()) currentUser?.fullName ?: "Warehouse Auditor" else auditorName
                        val finalCategory = selectedCategory.ifBlank { "General Activity" }
                        val finalSubActivity = if (subActivity.isBlank()) "Standard Checklist Point" else subActivity
                        val finalHub = if (hubLocation.isBlank()) "ICH Central Hub" else hubLocation

                        viewModel.saveGodownActivity(
                            hubLocation = finalHub,
                            auditorName = finalAuditor,
                            category = finalCategory,
                            subActivity = finalSubActivity,
                            duckLocation = duckLocation,
                            status = status,
                            readingValue = readingValue,
                            remarks = remarks
                        ) { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to ICH Portal (रिकॉर्ड में दर्ज करें)", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                }
            }
        }
    }
}
