package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleEntry
import com.example.ui.DockOccupancySummary
import com.example.ui.YardStats
import com.example.ui.theme.WarehouseAccentAmber
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseRed
import com.example.ui.theme.WarehouseSteelBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SummaryDashboardVisualizer(
    stats: YardStats,
    dockOccupancy: DockOccupancySummary,
    entries: List<VehicleEntry>,
    modifier: Modifier = Modifier,
    onFilterStatus: (String) -> Unit = {},
    onFilterDock: (String) -> Unit = {}
) {
    var selectedViewMode by remember { mutableIntStateOf(0) }

    // Calculated metrics
    val totalEntries = stats.total
    val processedCount = stats.completedOrOut
    val activeInYard = stats.activeInYard
    val dockedCount = stats.docked
    val gateInPendingDock = (activeInYard - dockedCount).coerceAtLeast(0)

    val processedRatePercent = if (totalEntries > 0) {
        ((processedCount.toFloat() / totalEntries.toFloat()) * 100f).toInt()
    } else 0

    val totalDocks = dockOccupancy.totalDocks.coerceAtLeast(1)
    val occupiedDocks = dockOccupancy.occupiedCount
    val occupancyRatePercent = ((occupiedDocks.toFloat() / totalDocks.toFloat()) * 100f).toInt()

    // Activity breakdown
    val loadingEntries = entries.count { it.activityType.equals("Loading", ignoreCase = true) }
    val unloadingEntries = entries.count { it.activityType.equals("Unloading", ignoreCase = true) }
    val crossDockEntries = entries.count { it.activityType.equals("Cross-Docking", ignoreCase = true) }

    // Dock occupancy level color
    val occupancyColor = when {
        occupancyRatePercent >= 80 -> WarehouseRed
        occupancyRatePercent >= 50 -> WarehouseAccentAmber
        else -> WarehouseEmerald
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("summary_dashboard_visualizer"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Bar
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
                            .background(WarehouseNavy.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Analytics Summary",
                            tint = WarehouseNavy,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Warehouse Summary Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Processed Entries & Dock Occupancy Analytics",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live Pulse Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = WarehouseEmerald.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarehouseEmerald.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(WarehouseEmerald)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE METRICS",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarehouseEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs for Visualizations
            TabRow(
                selectedTabIndex = selectedViewMode,
                containerColor = Color(0xFFF1F5F9),
                contentColor = WarehouseNavy,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedViewMode]),
                        color = WarehouseNavy,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .testTag("summary_view_tab_row")
            ) {
                Tab(
                    selected = selectedViewMode == 0,
                    onClick = { selectedViewMode = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Summary Gauges", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("tab_summary_gauges")
                )
                Tab(
                    selected = selectedViewMode == 1,
                    onClick = { selectedViewMode = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Entries Throughput", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("tab_entries_throughput")
                )
                Tab(
                    selected = selectedViewMode == 2,
                    onClick = { selectedViewMode = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warehouse, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dock Bay Matrix", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    modifier = Modifier.testTag("tab_dock_matrix")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedViewMode) {
                0 -> {
                    // Dual Gauge Overview: Total Processed Entries & Current Dock Occupancy Rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Processed Vehicle Entries Visualization
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("processed_entries_chart"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Processed Entries",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarehouseNavy
                                )
                                Text(
                                    text = "पूर्ण एवं रवाना गाड़ियां",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Donut / Circular Progress Chart
                                DonutMetricChart(
                                    primaryValue = processedCount,
                                    totalValue = totalEntries,
                                    percentage = processedRatePercent,
                                    primaryColor = WarehouseEmerald,
                                    secondaryColor = Color(0xFFCBD5E1),
                                    centerLabel = "$processedCount / $totalEntries",
                                    subLabel = "Done ($processedRatePercent%)"
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Breakdown Pills
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MetricPill(
                                        label = "Done",
                                        value = "$processedCount",
                                        color = WarehouseEmerald,
                                        onClick = { onFilterStatus("Completed") }
                                    )
                                    MetricPill(
                                        label = "In Yard",
                                        value = "$activeInYard",
                                        color = WarehouseSteelBlue,
                                        onClick = { onFilterStatus("Gate In") }
                                    )
                                    MetricPill(
                                        label = "Docked",
                                        value = "$dockedCount",
                                        color = WarehouseAccentAmber,
                                        onClick = { onFilterStatus("Dock Assigned / Placed") }
                                    )
                                }
                            }
                        }

                        // Card 2: Current Dock Occupancy Rate Visualization
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dock_occupancy_chart"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Dock Occupancy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarehouseNavy
                                )
                                Text(
                                    text = "डॉक ऑक्यूपेंसी दर",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Radial Arc Meter for Dock Utilization
                                ArcGaugeChart(
                                    occupied = occupiedDocks,
                                    total = totalDocks,
                                    percentage = occupancyRatePercent,
                                    gaugeColor = occupancyColor,
                                    centerLabel = "$occupiedDocks / $totalDocks Bays",
                                    subLabel = "$occupancyRatePercent% Full"
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Breakdown Pills
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MetricPill(
                                        label = "Occupied",
                                        value = "$occupiedDocks",
                                        color = occupancyColor,
                                        onClick = { onFilterDock("Occupied") }
                                    )
                                    MetricPill(
                                        label = "Available",
                                        value = "${totalDocks - occupiedDocks}",
                                        color = WarehouseEmerald,
                                        onClick = { onFilterDock("Empty") }
                                    )
                                    MetricPill(
                                        label = "Capacity",
                                        value = "$totalDocks",
                                        color = WarehouseNavy,
                                        onClick = { onFilterDock("All") }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Processed Entries Throughput & Operation Distribution
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("entries_throughput_view")
                    ) {
                        Text(
                            text = "Activity & Movement Distribution",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontal Stacked Proportion Bar
                        val safeTotal = (loadingEntries + unloadingEntries + crossDockEntries).coerceAtLeast(1).toFloat()
                        val loadFrac = loadingEntries / safeTotal
                        val unloadFrac = unloadingEntries / safeTotal
                        val crossFrac = crossDockEntries / safeTotal

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE2E8F0))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                if (loadingEntries > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(loadFrac.coerceAtLeast(0.01f))
                                            .background(WarehouseEmerald)
                                    )
                                }
                                if (unloadingEntries > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(unloadFrac.coerceAtLeast(0.01f))
                                            .background(WarehouseNavy)
                                    )
                                }
                                if (crossDockEntries > 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .weight(crossFrac.coerceAtLeast(0.01f))
                                            .background(WarehouseAccentAmber)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Multi-Bar Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThroughputBarItem(
                                title = "Loading (Outbound)",
                                count = loadingEntries,
                                total = entries.size,
                                color = WarehouseEmerald,
                                modifier = Modifier.weight(1f)
                            )
                            ThroughputBarItem(
                                title = "Unloading (Inbound)",
                                count = unloadingEntries,
                                total = entries.size,
                                color = WarehouseNavy,
                                modifier = Modifier.weight(1f)
                            )
                            ThroughputBarItem(
                                title = "Cross-Docking",
                                count = crossDockEntries,
                                total = entries.size,
                                color = WarehouseAccentAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Funnel Overview
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Gate Processing Pipeline Status:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF475569)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusPipelineStep(
                                        step = "1. Gate In",
                                        count = gateInPendingDock,
                                        color = WarehouseSteelBlue
                                    )
                                    Text("➔", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    StatusPipelineStep(
                                        step = "2. At Dock",
                                        count = dockedCount,
                                        color = WarehouseAccentAmber
                                    )
                                    Text("➔", color = Color(0xFF94A3B8), fontSize = 12.sp)
                                    StatusPipelineStep(
                                        step = "3. Processed",
                                        count = processedCount,
                                        color = WarehouseEmerald
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Dock Bay Matrix & Occupancy Detail
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dock_matrix_view")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Real-Time Dock Bay Status (${dockOccupancy.occupiedCount} Occupied / ${dockOccupancy.emptyCount} Empty)",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$occupancyRatePercent% Occupied",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = occupancyColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Grid of dock bays
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dockOccupancy.docks.forEach { dock ->
                                val isOcc = dock.isOccupied
                                Surface(
                                    modifier = Modifier
                                        .widthIn(min = 72.dp)
                                        .clickable {
                                            onFilterDock(if (isOcc) dock.bayNumber else "Empty")
                                        }
                                        .testTag("dock_chip_${dock.bayNumber.replace(" ", "_")}"),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isOcc) occupancyColor.copy(alpha = 0.12f) else Color(0xFFF1F5F9),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isOcc) occupancyColor.copy(alpha = 0.6f) else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = dock.bayNumber,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isOcc) occupancyColor else Color(0xFF475569)
                                        )
                                        Text(
                                            text = if (isOcc) dock.vehicleNumber.ifBlank { "Occupied" } else "Available",
                                            fontSize = 9.5.sp,
                                            fontWeight = if (isOcc) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isOcc) Color(0xFF1E293B) else Color(0xFF059669),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(8.dp))

            // Quick Stats Footnote
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = WarehouseSteelBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Turnaround: ~42 mins avg dock time",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Text(
                    text = "Total Entries Logged: $totalEntries",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = WarehouseNavy
                )
            }
        }
    }
}

@Composable
private fun DonutMetricChart(
    primaryValue: Int,
    totalValue: Int,
    percentage: Int,
    primaryColor: Color,
    secondaryColor: Color,
    centerLabel: String,
    subLabel: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (totalValue > 0) (primaryValue.toFloat() / totalValue.toFloat()).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "donutProgress"
    )

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // Background Track
            drawArc(
                color = secondaryColor.copy(alpha = 0.4f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(primaryColor.copy(alpha = 0.7f), primaryColor)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = subLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = primaryColor
            )
        }
    }
}

@Composable
private fun ArcGaugeChart(
    occupied: Int,
    total: Int,
    percentage: Int,
    gaugeColor: Color,
    centerLabel: String,
    subLabel: String,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (total > 0) (occupied.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "arcGaugeProgress"
    )

    Box(
        modifier = modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val arcSize = Size(diameter, diameter)

            // 240 degree arc gauge starting at 150 deg
            val startAngle = 150f
            val totalSweep = 240f

            // Background Track
            drawArc(
                color = Color(0xFFCBD5E1).copy(alpha = 0.5f),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Gauge Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.linearGradient(
                        listOf(gaugeColor.copy(alpha = 0.7f), gaugeColor)
                    ),
                    startAngle = startAngle,
                    sweepAngle = animatedProgress * totalSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = subLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = gaugeColor
            )
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier.padding(horizontal = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label: $value",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
private fun ThroughputBarItem(
    title: String,
    count: Int,
    total: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val pct = if (total > 0) ((count.toFloat() / total.toFloat()) * 100f).toInt() else 0
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(title, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569), maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text("$count", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
                Text("$pct%", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun StatusPipelineStep(
    step: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(step, fontSize = 10.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$count Vehicles",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
