package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.YardStats
import com.example.ui.theme.WarehouseAccentAmber
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseNavy
import com.example.ui.theme.WarehouseSteelBlue

@Composable
fun StatCards(
    stats: YardStats,
    onFilterSelect: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItem(
            title = "Total",
            value = stats.total.toString(),
            icon = Icons.Default.LocalShipping,
            accentColor = WarehouseNavy,
            bgColor = Color(0xFFEBF2FA),
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelect("All") },
            testTag = "stat_total_card"
        )
        StatItem(
            title = "In Yard",
            value = stats.activeInYard.toString(),
            icon = Icons.Default.DirectionsCar,
            accentColor = WarehouseSteelBlue,
            bgColor = Color(0xFFE0F2FE),
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelect("Gate In") },
            testTag = "stat_yard_card"
        )
        StatItem(
            title = "At Dock",
            value = stats.docked.toString(),
            icon = Icons.Default.Warehouse,
            accentColor = WarehouseAccentAmber,
            bgColor = Color(0xFFFEF3C7),
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelect("Dock Assigned / Placed") },
            testTag = "stat_dock_card"
        )
        StatItem(
            title = "Done",
            value = stats.completedOrOut.toString(),
            icon = Icons.Default.CheckCircle,
            accentColor = WarehouseEmerald,
            bgColor = Color(0xFFDCFCE7),
            modifier = Modifier.weight(1f),
            onClick = { onFilterSelect("Completed") },
            testTag = "stat_done_card"
        )
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = value,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
