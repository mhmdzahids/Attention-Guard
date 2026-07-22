package com.attentionguard.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attentionguard.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.layout.layout

data class ChartPoint(val label: String, val value: Float)

@Composable
fun InsightsScreen(
    apiScore: Float,
    riskTier: String,
    sessionDuration: Float,
    scrollVelocity: Float,
    switchFreq: Float,
    nightRatio: Float,
    skipFrequency: Float,
    youtubeDuration: Float,
    instagramDuration: Float,
    tiktokDuration: Float,
    isYoutubeInstalled: Boolean,
    isInstagramInstalled: Boolean,
    isTiktokInstalled: Boolean,
    dbLogs: List<com.attentionguard.data.AttentionLog>,
    useSimulatedData: Boolean,
    onNavigateToMeditate: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    // Active chart type state
    var chartType by remember { mutableStateOf("hourly") }
    var currentRenderType by remember { mutableStateOf("hourly") }
    val chartProgress = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    var chartScale by remember { mutableStateOf(1f) }

    // Smoothly rise chart on first load
    LaunchedEffect(Unit) {
        chartProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    // 1. Prepare Hourly Data (Hour of Day)
    val todayStart = remember {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    
    val todayLogs = remember(dbLogs) {
        dbLogs.filter { it.timestamp >= todayStart }
    }
    
    val hourlyBaseline = listOf(0.45f, 0.25f, 0.15f, 0.45f, 0.58f, 0.42f, 0.65f, 0.52f, 0.45f)
    val hoursList = listOf(0, 3, 6, 9, 12, 15, 18, 21, 24)
    val scaleFactor = if (apiScore > 0f) apiScore / 0.52f else 1f
    
    val hourlyPoints = remember(todayLogs, scaleFactor) {
        hoursList.mapIndexed { index, hour ->
            val start = todayStart + ((hour - 1.5) * 3600000).toLong()
            val end = todayStart + ((hour + 1.5) * 3600000).toLong()
            val logsInInterval = todayLogs.filter { it.timestamp in start..end }
            
            if (logsInInterval.isNotEmpty()) {
                logsInInterval.map { it.apiScore }.average().toFloat()
            } else {
                Math.min(1.0f, hourlyBaseline[index] * scaleFactor)
            }
        }
    }

    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val visibleHoursCount = remember(currentHour) {
        Math.max(1, hoursList.filter { it <= currentHour }.size)
    }
    
    val peakIndex = remember(hourlyPoints, visibleHoursCount) {
        hourlyPoints.take(visibleHoursCount).indices.maxByOrNull { hourlyPoints[it] } ?: 0
    }
    val peakHoursList = listOf("12:00 AM", "03:00 AM", "06:00 AM", "09:00 AM", "12:00 PM", "03:00 PM", "06:00 PM", "09:00 PM", "12:00 AM")
    val peakHourText = peakHoursList[peakIndex]

    // 2. Prepare Weekly Data (Day by Day)
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val fullDayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    
    val weeklyBaselines = listOf(2.4f, 3.1f, 1.8f, 2.9f, 4.2f, 3.5f, sessionDuration)
    
    val weeklyPoints = remember(dbLogs, sessionDuration) {
        (0..6).map { i ->
            val c = java.util.Calendar.getInstance()
            c.add(java.util.Calendar.DAY_OF_YEAR, -i)
            c
        }.reversed().mapIndexed { index, c ->
            val dayStart = c.apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }.timeInMillis
            val dayEnd = dayStart + 86400000L - 1000L
            
            val logsForDay = dbLogs.filter { it.timestamp in dayStart..dayEnd }
            val value = if (logsForDay.isNotEmpty()) {
                logsForDay.maxOf { it.sessionDuration }
            } else {
                weeklyBaselines[index]
            }
            ChartPoint(
                label = dayNames[c.get(java.util.Calendar.DAY_OF_WEEK) - 1],
                value = value
            )
        }
    }
    
    val maxDayIndex = remember(weeklyPoints) {
        weeklyPoints.indices.maxByOrNull { weeklyPoints[it].value } ?: 4
    }
    val peakDayText = remember(maxDayIndex) {
        val c = java.util.Calendar.getInstance()
        c.add(java.util.Calendar.DAY_OF_YEAR, -(6 - maxDayIndex))
        fullDayNames[c.get(java.util.Calendar.DAY_OF_WEEK) - 1]
    }

    // Diagnostic logs for developers
    android.util.Log.d("AttentionGuardChart", "ChartType: $chartType | DB Logs: ${dbLogs.size} entries | Today's Logs: ${todayLogs.size} entries")
    android.util.Log.d("AttentionGuardChart", "Hourly Points: $hourlyPoints")
    android.util.Log.d("AttentionGuardChart", "Weekly Points: ${weeklyPoints.map { "${it.label}=${it.value}" }}")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Behavioral Patterns Insights",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceDark
            )

            Card(
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, HairlineSoft),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (currentRenderType == "hourly") "PEAK ACTIVITY" else "MOST ACTIVE DAY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryGray,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (currentRenderType == "hourly") peakHourText else peakDayText,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .background(SurfaceSoft, RoundedCornerShape(100.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val activeTabColor = CommerceCobalt
                            val inactiveTabColor = Color.Transparent
                            
                            val tabs = listOf("hourly" to "Hourly", "weekly" to "Weekly")
                            tabs.forEach { (type, label) ->
                                val selected = chartType == type
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(if (selected) activeTabColor else inactiveTabColor)
                                        .clickable {
                                            if (chartType != type) {
                                                coroutineScope.launch {
                                                    // 1. Smoothly shrink current chart down
                                                    chartProgress.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = tween(
                                                            durationMillis = 250,
                                                            easing = FastOutSlowInEasing
                                                        )
                                                    )
                                                    // 2. Swap render types at flat level
                                                    chartType = type
                                                    currentRenderType = type
                                                    chartScale = 1f // Reset scale on switch
                                                    // 3. Smoothly rise new chart up
                                                    chartProgress.animateTo(
                                                        targetValue = 1f,
                                                        animationSpec = tween(
                                                            durationMillis = 350,
                                                            easing = FastOutSlowInEasing
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else SecondaryGray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val scrollStateChart = rememberScrollState()

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, _, zoom, _ ->
                                    chartScale = (chartScale * zoom).coerceIn(1f, 3.5f)
                                }
                            }
                    ) {
                        val availableWidth = maxWidth - 44.dp
                        val chartWidth = availableWidth * chartScale

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Y Labels Column (Fixed width)
                            Column(
                                modifier = Modifier
                                    .height(150.dp)
                                    .width(36.dp)
                                    .padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.End
                            ) {
                                if (currentRenderType == "hourly") {
                                    Text("High", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryGray)
                                    Text("Mod", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryGray)
                                    Text("Low", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryGray)
                                } else {
                                    Text("8.0h", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryGray)
                                    Text("4.0h", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryGray)
                                    Text("0.0h", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryGray)
                                }
                            }

                            // Scrollable area for the Graph Canvas & X Labels
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(scrollStateChart, enabled = chartScale > 1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(chartWidth)
                                        .height(150.dp)
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height

                                        // Horizontal gridlines aligning with the Y Labels
                                        // Bottom line (Low / 0.0)
                                        drawLine(
                                            color = HairlineSoft,
                                            start = Offset(0f, h),
                                            end = Offset(w, h),
                                            strokeWidth = 2f
                                        )
                                        // Middle line (Mod / 4.0)
                                        drawLine(
                                            color = SurfaceSoft,
                                            start = Offset(0f, h / 2f),
                                            end = Offset(w, h / 2f),
                                            strokeWidth = 2f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                                        )
                                        // Top line (High / 8.0)
                                        drawLine(
                                            color = SurfaceSoft,
                                            start = Offset(0f, 0f),
                                            end = Offset(w, 0f),
                                            strokeWidth = 2f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                                        )

                                        val themeColor = when (riskTier) {
                                            "low" -> RiskLow
                                            "moderate" -> RiskModerate
                                            else -> RiskHigh
                                        }

                                        if (currentRenderType == "hourly") {
                                            // Scale X dynamically based on visibleHoursCount so the last visible hour sits at the rightmost edge w
                                            val points = hourlyPoints.take(visibleHoursCount).mapIndexed { index, valRaw ->
                                                val x = if (visibleHoursCount > 1) {
                                                    index * w / (visibleHoursCount - 1).toFloat()
                                                } else {
                                                    w / 2f
                                                }
                                                Offset(x, h - (valRaw * h * chartProgress.value))
                                            }

                                            if (points.isNotEmpty()) {
                                                val path = Path().apply {
                                                    moveTo(points[0].x, points[0].y)
                                                    for (i in 0 until points.size - 1) {
                                                        val pStart = points[i]
                                                        val pEnd = points[i + 1]
                                                        val controlX = (pStart.x + pEnd.x) / 2f
                                                        cubicTo(controlX, pStart.y, controlX, pEnd.y, pEnd.x, pEnd.y)
                                                    }
                                                }

                                                drawPath(
                                                    path = path,
                                                    color = themeColor,
                                                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                                                )

                                                val areaPath = Path().apply {
                                                    addPath(path)
                                                    lineTo(points.last().x, h)
                                                    lineTo(points.first().x, h)
                                                    close()
                                                }

                                                drawPath(
                                                    path = areaPath,
                                                    brush = Brush.verticalGradient(
                                                        colors = listOf(themeColor.copy(alpha = 0.25f * chartProgress.value), Color.Transparent)
                                                    )
                                                )

                                                // Draw highlight indicator only if it's within visible points range
                                                if (peakIndex < points.size) {
                                                    val highlightX = if (visibleHoursCount > 1) {
                                                        peakIndex * w / (visibleHoursCount - 1).toFloat()
                                                    } else {
                                                        w / 2f
                                                    }
                                                    val highlightY = h - (hourlyPoints[peakIndex] * h * chartProgress.value)
                                                    drawCircle(
                                                        color = themeColor,
                                                        radius = 15f * chartProgress.value,
                                                        center = Offset(highlightX, highlightY),
                                                        style = Stroke(width = 4f * chartProgress.value)
                                                    )
                                                    drawCircle(
                                                        color = themeColor,
                                                        radius = 6f * chartProgress.value,
                                                        center = Offset(highlightX, highlightY)
                                                    )
                                                }
                                            }
                                        } else {
                                            val numBars = weeklyPoints.size
                                            val barWidth = w / (numBars * 1.8f - 0.8f)
                                            val spacing = barWidth * 0.8f
                                            val maxVal = 8.0f 

                                            for (i in 0 until numBars) {
                                                val x = i * (barWidth + spacing)
                                                val barVal = weeklyPoints[i].value
                                                val barH = Math.max(12f, (barVal / maxVal) * h) * chartProgress.value
                                                val y = h - barH
                                                
                                                val barColor = if (barVal >= 5.0f) RiskHigh else if (barVal >= 2.8f) RiskModerate else CommerceCobalt
                                                
                                                drawRoundRect(
                                                    color = barColor,
                                                    topLeft = Offset(x, y),
                                                    size = androidx.compose.ui.geometry.Size(barWidth, barH),
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // X Labels Row placed in custom offsets to match the graph points
                                Box(
                                    modifier = Modifier
                                        .width(chartWidth)
                                        .padding(top = 4.dp)
                                ) {
                                    if (currentRenderType == "hourly") {
                                        val formatHourLabel: (Int) -> String = { hourVal ->
                                            when {
                                                hourVal == 0 -> "12 AM"
                                                hourVal == 12 -> "12 PM"
                                                hourVal == 24 -> "12 AM"
                                                hourVal > 12 -> "${hourVal - 12} PM"
                                                else -> "$hourVal AM"
                                            }
                                        }
                                        val visibleHours = hoursList.take(visibleHoursCount)
                                        
                                        // Zoomed: show all visible indices. Unzoomed: show even indices and the last index.
                                        val labelList = if (chartScale > 1.5f) {
                                            visibleHours.mapIndexed { index, hour -> index to formatHourLabel(hour) }
                                        } else {
                                            visibleHours.mapIndexedNotNull { index, hour ->
                                                if (index % 2 == 0 || index == visibleHoursCount - 1) {
                                                    index to formatHourLabel(hour)
                                                } else {
                                                    null
                                                }
                                            }
                                        }

                                        labelList.forEach { (index, time) ->
                                            Text(
                                                text = time,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SecondaryGray,
                                                modifier = Modifier.layout { measurable, constraints ->
                                                    val placeable = measurable.measure(constraints)
                                                    layout(placeable.width, placeable.height) {
                                                        // Center label under its graph point
                                                        val wPx = chartWidth.toPx()
                                                        val xPos = if (visibleHoursCount > 1) {
                                                            (index * (wPx / (visibleHoursCount - 1).toFloat())) - (placeable.width / 2f)
                                                        } else {
                                                            (wPx / 2f) - (placeable.width / 2f)
                                                        }
                                                        val clampedX = xPos.coerceIn(0f, wPx - placeable.width)
                                                        placeable.placeRelative(clampedX.toInt(), 0)
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        val numBars = weeklyPoints.size
                                        weeklyPoints.forEachIndexed { index, point ->
                                            Text(
                                                text = point.label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SecondaryGray,
                                                modifier = Modifier.layout { measurable, constraints ->
                                                    val placeable = measurable.measure(constraints)
                                                    layout(placeable.width, placeable.height) {
                                                        val wPx = chartWidth.toPx()
                                                        val barWidth = wPx / (numBars * 1.8f - 0.8f)
                                                        val spacing = barWidth * 0.8f
                                                        val x = index * (barWidth + spacing)
                                                        val center = x + barWidth / 2f
                                                        val xPos = center - (placeable.width / 2f)
                                                        val clampedX = xPos.coerceIn(0f, wPx - placeable.width)
                                                        placeable.placeRelative(clampedX.toInt(), 0)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = SurfaceSoft,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (useSimulatedData) {
                                        "Source: Simulated Data Mode"
                                    } else {
                                        if (currentRenderType == "hourly") {
                                            if (todayLogs.isEmpty()) "Source: Circadian Baseline (No logs)" 
                                            else "Source: Real-world Database (${todayLogs.size} logs today)"
                                        } else {
                                            val dbLogsInWeek = dbLogs.filter { it.timestamp >= System.currentTimeMillis() - 7 * 86400000L }
                                            if (dbLogsInWeek.isEmpty()) "Source: Estimated Weekly Baseline"
                                            else "Source: Real-world Database (${dbLogsInWeek.size} logs this week)"
                                        }
                                    },
                                    color = SecondaryGray,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // User visual hint for pinch gesture
                            Text(
                                text = "💡 Tip: Pinch graph to zoom & view detailed hour information",
                                color = SecondaryGray.copy(alpha = 0.8f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Signal Breakdown Stack (16.dp corner rounding xl cards)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // 1. Session Dynamics
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = SurfaceSoft,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassEmpty,
                                        contentDescription = "Session",
                                        tint = CommerceCobalt,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Session Dynamics",
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceDark,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Average usage duration today",
                                    color = SecondaryGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        val sessionRatio = sessionDuration / 8f
                        var barProgress by remember { mutableStateOf(0f) }
                        
                        LaunchedEffect(sessionDuration) {
                            barProgress = sessionRatio
                        }
                        
                        val animWidth by animateFloatAsState(
                            targetValue = barProgress,
                            animationSpec = tween(1000, easing = FastOutSlowInEasing)
                        )
                        
                        // Custom Linear Progress bar with dynamic color
                        val barColor = when {
                            animWidth > 0.65f -> RiskHigh
                            animWidth > 0.35f -> RiskModerate
                            else -> RiskLow
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(SurfaceSoft)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(animWidth)
                                    .background(barColor)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format("%d%% Intensity", (sessionRatio * 100).toInt()),
                                color = SecondaryGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = String.format("%.1fh Total", sessionDuration),
                                color = OnSurfaceDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 1b. Target App Exposure Details
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
                        Text(
                            text = "Target App Exposure Details",
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Exposure time breakdown for targeted short-form content apps",
                            color = SecondaryGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        val showYoutube = isYoutubeInstalled && youtubeDuration > 0.01f
                        val showInstagram = isInstagramInstalled && instagramDuration > 0.01f
                        val showTiktok = isTiktokInstalled && tiktokDuration > 0.01f

                        if (showYoutube) {
                            AppUsageBar(
                                appName = "YouTube (incl. Shorts)",
                                duration = youtubeDuration,
                                maxDuration = 4.0f,
                                color = Color(0xFFE41E3F),
                                icon = { YouTubeIcon() }
                            )
                        }

                        if (showInstagram) {
                            AppUsageBar(
                                appName = "Instagram (incl. Reels)",
                                duration = instagramDuration,
                                maxDuration = 4.0f,
                                color = Color(0xFFA121CE),
                                icon = { InstagramIcon() }
                            )
                        }

                        if (showTiktok) {
                            AppUsageBar(
                                appName = "TikTok",
                                duration = tiktokDuration,
                                maxDuration = 4.0f,
                                color = Color(0xFF000000),
                                icon = { TikTokIcon() }
                            )
                        }

                        if (!showYoutube && !showInstagram && !showTiktok) {
                            Text(
                                text = "No active screen time recorded today for monitored apps.",
                                color = SecondaryGray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                // 2. Interaction Micro-Behaviors
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
                        Text(
                            text = "Interaction Micro-Behaviors",
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark,
                            fontSize = 15.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceSoft),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Scroll Speed",
                                        color = SecondaryGray,
                                        fontSize = 12.sp
                                    )
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = String.format("%.0f", scrollVelocity),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OnSurfaceDark
                                        )
                                        Text(
                                            text = " px/s",
                                            fontSize = 12.sp,
                                            color = SecondaryGray,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                }
                            }

                            val skipColor = if (skipFrequency > 55f) RiskHigh else OnSurfaceDark
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SurfaceSoft),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Skip Rate",
                                        color = SecondaryGray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = String.format("%.0f%%", skipFrequency),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = skipColor,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Task Switching
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = SurfaceSoft,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SyncAlt,
                                        contentDescription = "Switches",
                                        tint = CommerceCobalt,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                textLabel(text = "Task Switching")
                                Text(
                                    text = "Rapid Inter-app Switches",
                                    color = SecondaryGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Text(
                            text = String.format("%.1f", switchFreq),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceDark
                        )
                    }
                }

                // 4. Temporal Distribution
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HairlineSoft),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(4.dp, CommerceCobalt), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            textLabel(text = "Temporal Distribution")
                            Text(
                                text = "Post-midnight Session Ratio",
                                color = SecondaryGray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SyncAlt,
                                    contentDescription = "Midnight",
                                    tint = CommerceCobalt,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Midnight usage detected",
                                    color = CommerceCobalt,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Circular Progress indicator
                        Box(
                            modifier = Modifier.size(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = SurfaceSoft,
                                    style = Stroke(width = 10f)
                                )
                                drawArc(
                                    color = CommerceCobalt,
                                    startAngle = -90f,
                                    sweepAngle = nightRatio * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 10f, cap = StrokeCap.Round)
                                )
                            }
                            Text(
                                text = String.format("%d%%", (nightRatio * 100).toInt()),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceDark
                            )
                        }
                    }
                }
            }
        }

        // Sticky Bottom Button Area (using Meta palette primary CTA: 100px Black Pill Button shape)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onNavigateToMeditate,
                    colors = ButtonDefaults.buttonColors(containerColor = InkButton),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "View Prevention Plan",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun textLabel(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.Bold,
        color = OnSurfaceDark,
        fontSize = 15.sp
    )
}

@Composable
private fun YouTubeIcon() {
    Surface(
        modifier = Modifier.size(28.dp),
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFE41E3F)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "YouTube",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun InstagramIcon() {
    Surface(
        modifier = Modifier.size(28.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFA121CE)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = "Instagram",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun TikTokIcon() {
    Surface(
        modifier = Modifier.size(28.dp),
        shape = RoundedCornerShape(100.dp),
        color = Color.Black
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "TikTok",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AppUsageBar(
    appName: String,
    duration: Float,
    maxDuration: Float,
    color: Color,
    icon: @Composable () -> Unit
) {
    val ratio = if (maxDuration > 0f) Math.min(1.0f, Math.max(0.0f, duration / maxDuration)) else 0f
    var barProgress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(duration) {
        barProgress = ratio
    }
    
    val animWidth by animateFloatAsState(
        targetValue = barProgress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appName,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark,
                    fontSize = 13.sp
                )
                Text(
                    text = String.format("%.1f hrs", duration),
                    color = SecondaryGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(SurfaceSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animWidth)
                        .background(color)
                )
            }
        }
    }
}
