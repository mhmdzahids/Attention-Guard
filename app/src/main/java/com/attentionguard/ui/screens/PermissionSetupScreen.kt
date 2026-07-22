package com.attentionguard.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attentionguard.ui.theme.*
import com.attentionguard.service.AttentionAccessibilityService

object PermissionChecker {
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedService = android.content.ComponentName(context, AttentionAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(expectedService)
    }

    fun isOverlayPermissionGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }
}

@Composable
fun PermissionSetupScreen(
    isAccessibilityGranted: Boolean,
    isOverlayGranted: Boolean,
    onFixAccessibility: () -> Unit,
    onFixOverlay: () -> Unit,
    onRemindMeLater: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .padding(bottom = 160.dp), // Space for sticky bottom buttons
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attention Guard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = CommerceCobalt
                )
            }

            // Warning Badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(RiskModerate)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "SETUP INCOMPLETE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    letterSpacing = 1.sp
                )
            }

            // Hero title & subtitle
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Enable Essential Services",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark,
                    lineHeight = 38.sp
                )
                Text(
                    text = "Attention Guard requires two special permissions to passively measure attention metrics without reading your private content.",
                    fontSize = 16.sp,
                    color = OnSurfaceVariant,
                    lineHeight = 22.sp
                )
            }

            // Permission cards
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PermissionCard(
                    title = "Accessibility Service",
                    description = "Required to calculate scroll velocity (px/sec) and skip rates during short-form video viewing.",
                    icon = Icons.Default.TouchApp,
                    isGranted = isAccessibilityGranted,
                    onClick = onFixAccessibility
                )

                PermissionCard(
                    title = "Display Over Other Apps",
                    description = "Required to trigger non-intrusive behavioral nudges and focus overlays when high risk is detected.",
                    icon = Icons.Default.Layers,
                    isGranted = isOverlayGranted,
                    onClick = onFixOverlay
                )
            }

            // Privacy Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceSoft)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Privacy Lock",
                    tint = SecondaryGray,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Your privacy is protected. These permissions are strictly used for calculating interaction metadata locally. No video content, camera data, or personal text is ever recorded or transmitted.",
                    fontSize = 13.sp,
                    color = SecondaryGray,
                    lineHeight = 18.sp
                )
            }

            // Subtle Visual Flourish (Custom Canvas Drawing)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, HairlineSoft, RoundedCornerShape(16.dp))
                    .background(SurfaceSoft.copy(alpha = 0.3f))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    
                    // Draw abstract clean lines and circles representing geometric interface precision
                    drawCircle(
                        color = CommerceCobalt.copy(alpha = 0.05f),
                        radius = h * 0.8f,
                        center = Offset(w * 0.85f, h * 0.5f)
                    )
                    
                    drawCircle(
                        color = CommerceCobalt.copy(alpha = 0.08f),
                        radius = h * 0.4f,
                        center = Offset(w * 0.85f, h * 0.5f)
                    )
                    
                    // Curved aesthetic path
                    val path = Path().apply {
                        moveTo(0f, h * 0.7f)
                        quadraticBezierTo(w * 0.35f, h * 0.2f, w * 0.7f, h * 0.6f)
                        cubicTo(w * 0.8f, h * 0.75f, w * 0.9f, h * 0.4f, w, h * 0.3f)
                    }
                    
                    drawPath(
                        path = path,
                        color = CommerceCobalt.copy(alpha = 0.25f),
                        style = Stroke(width = 3f)
                    )
                    
                    // Axis/grid lines
                    drawLine(
                        color = HairlineSoft,
                        start = Offset(0f, h * 0.8f),
                        end = Offset(w, h * 0.8f),
                        strokeWidth = 1f
                    )
                    
                    drawCircle(
                        color = CommerceCobalt,
                        radius = 8f,
                        center = Offset(w * 0.7f, h * 0.6f)
                    )
                }
            }
        }

        // Sticky Bottom Actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .drawBehind {
                    drawLine(
                        color = HairlineSoft,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 2f
                    )
                }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (!isAccessibilityGranted) {
                        onFixAccessibility()
                    } else if (!isOverlayGranted) {
                        onFixOverlay()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CommerceCobalt),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text = "Fix in Settings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onRemindMeLater,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                border = BorderStroke(2.dp, Color(0xFF0A1317)),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0A1317))
            ) {
                Text(
                    text = "Remind Me Later",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, HairlineSoft, RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CommerceCobalt,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark
                )
                
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isGranted) RiskLow else RiskHigh)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isGranted) "ENABLED" else "NOT GRANTED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Text(
                text = description,
                fontSize = 13.sp,
                color = OnSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
