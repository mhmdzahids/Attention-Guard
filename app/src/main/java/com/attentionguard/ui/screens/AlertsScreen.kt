package com.attentionguard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attentionguard.AlertLog
import com.attentionguard.ui.theme.*

@Composable
fun AlertsScreen(alerts: List<AlertLog>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Attention Logs",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceDark
            )
            Text(
                text = "Monitors Active",
                fontSize = 12.sp,
                color = SecondaryGray
            )
        }

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No alerts logged yet. Passive monitoring running smoothly.",
                    color = SecondaryGray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(alerts) { alert ->
                    AlertItem(alert = alert)
                }
            }
        }
    }
}

@Composable
fun AlertItem(alert: AlertLog) {
    val (badgeColor, tierLabel) = when (alert.riskTier) {
        "low" -> Pair(RiskLow, "Low Risk")
        "moderate" -> Pair(RiskModerate, "Moderate Risk")
        else -> Pair(RiskHigh, "High Risk")
    }

    val (icon, iconColor) = when (alert.riskTier) {
        "low" -> Pair(Icons.Default.CheckCircle, RiskLow)
        "moderate" -> Pair(Icons.Default.Warning, RiskModerate)
        else -> Pair(Icons.Default.Error, RiskHigh)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, HairlineSoft),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.timestamp,
                    fontSize = 12.sp,
                    color = SecondaryGray,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = String.format("API: %.2f", alert.apiScore),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tierLabel,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = alert.title,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceDark,
                        fontSize = 15.sp
                    )
                    Text(
                        text = alert.description,
                        color = OnSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
