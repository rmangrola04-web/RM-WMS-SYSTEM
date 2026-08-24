package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WarehouseAccentAmber
import com.example.ui.theme.WarehouseEmerald
import com.example.ui.theme.WarehouseRed
import com.example.ui.theme.WarehouseSteelBlue
import com.example.ui.theme.WarehouseTeal

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when (status) {
        "Gate In" -> Triple(Color(0xFFE0F2FE), Color(0xFF0369A1), Color(0xFF0284C7))
        "Dock Assigned / Placed" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), WarehouseAccentAmber)
        "In-Progress" -> Triple(Color(0xFFEDE9FE), Color(0xFF6D28D9), Color(0xFF7C3AED))
        "Completed" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), WarehouseEmerald)
        "Gate Out" -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFF64748B))
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF334155), Color(0xFF94A3B8))
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = status,
            color = textColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ActivityBadge(
    activity: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (activity) {
        "Loading" -> Pair(Color(0xFFFFF7ED), Color(0xFFC2410C))
        "Unloading" -> Pair(Color(0xFFEFF6FF), Color(0xFF1D4ED8))
        "Cross-Docking" -> Pair(Color(0xFFF0FDFA), Color(0xFF0F766E))
        else -> Pair(Color(0xFFF8FAFC), Color(0xFF334155))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = activity,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
