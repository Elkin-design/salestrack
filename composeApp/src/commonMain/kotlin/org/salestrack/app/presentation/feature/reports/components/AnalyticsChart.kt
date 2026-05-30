package org.salestrack.app.presentation.feature.reports.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.salestrack.app.core.utils.formatMoney
import org.salestrack.app.domain.model.ReportPoint
import kotlin.math.abs

@Composable
fun AnalyticsLineChart(
    points: List<ReportPoint>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        fontSize = 10.sp
    )
    val tooltipStyle = TextStyle(
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    
    val animationProgress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    LaunchedEffect(points) {
        selectedIndex = null
        animationProgress.animateTo(1f, animationSpec = tween(1200))
    }

    BoxWithConstraints(modifier = modifier) {
        val boxWidth = maxWidth
        val leftPaddingDp = 50.dp
        val bottomPaddingDp = 30.dp

        // Background Layer: Y-Axis and Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.isEmpty()) return@Canvas

            val chartHeight = size.height - bottomPaddingDp.toPx()
            val maxAmount = points.maxOf { it.totalAmount }.toFloat().coerceAtLeast(1f)
            
            val gridLines = 4
            for (i in 0..gridLines) {
                val ratio = i.toFloat() / gridLines
                val y = chartHeight - (chartHeight * ratio)
                
                // Grid Line
                drawLine(
                    color = surfaceColor.copy(alpha = 0.2f),
                    start = Offset(leftPaddingDp.toPx(), y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Y Label
                val amountLabel = formatMoney((maxAmount * ratio).toDouble()).substringBefore(".")
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$$amountLabel",
                    style = labelStyle,
                    topLeft = Offset(5.dp.toPx(), y - 10.dp.toPx())
                )
            }
        }

        // Foreground Layer: Scrollable Chart
        val scrollState = rememberScrollState()
        // Decide width based on number of points to ensure minimum spacing
        val minSpacingPerPoint = 60.dp
        val neededWidth = minSpacingPerPoint * (points.size - 1).coerceAtLeast(1)
        val chartContentWidth = maxOf(boxWidth - leftPaddingDp, neededWidth)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = leftPaddingDp)
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(chartContentWidth)
                    .fillMaxHeight()
                    .pointerInput(points) {
                        detectTapGestures { tapOffset ->
                            if (points.isEmpty()) return@detectTapGestures
                            val chartWidth = size.width
                            val spacing = chartWidth / (points.size - 1).coerceAtLeast(1)
                            
                            var closestIndex = -1
                            var minDistance = Float.MAX_VALUE
                            
                            points.forEachIndexed { index, _ ->
                                val x = index * spacing
                                val dist = abs(tapOffset.x - x)
                                if (dist < minDistance && dist < 40.dp.toPx()) { // 40dp tap target radius
                                    minDistance = dist
                                    closestIndex = index
                                }
                            }
                            
                            selectedIndex = if (closestIndex != -1) closestIndex else null
                        }
                    }
            ) {
                if (points.isEmpty()) return@Canvas

                val chartWidth = size.width
                val chartHeight = size.height - bottomPaddingDp.toPx()
                val maxAmount = points.maxOf { it.totalAmount }.toFloat().coerceAtLeast(1f)
                val spacing = chartWidth / (points.size - 1).coerceAtLeast(1)
                
                val path = Path()
                val fillPath = Path()
                
                points.forEachIndexed { index, point ->
                    val x = index * spacing
                    val y = chartHeight - (point.totalAmount.toFloat() / maxAmount) * chartHeight * animationProgress.value
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, chartHeight)
                        fillPath.lineTo(x, y)
                    } else {
                        val prevX = (index - 1) * spacing
                        val prevY = chartHeight - (points[index - 1].totalAmount.toFloat() / maxAmount) * chartHeight * animationProgress.value
                        val controlX1 = prevX + (x - prevX) / 2
                        val controlX2 = prevX + (x - prevX) / 2
                        
                        path.cubicTo(controlX1, prevY, controlX2, y, x, y)
                        fillPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
                    }
                    
                    if (index == points.size - 1) {
                        fillPath.lineTo(x, chartHeight)
                        fillPath.close()
                    }
                }

                // Draw fill gradient
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = chartHeight
                    )
                )

                // Draw line
                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(listOf(primaryColor, primaryColor.copy(alpha = 0.7f))),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw points and X Labels
                points.forEachIndexed { index, point ->
                    val x = index * spacing
                    val y = chartHeight - (point.totalAmount.toFloat() / maxAmount) * chartHeight * animationProgress.value
                    val isSelected = index == selectedIndex

                    if (animationProgress.value > 0.8f) {
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                            center = Offset(x, y),
                            style = Stroke(width = if (isSelected) 3.dp.toPx() else 2.dp.toPx())
                        )
                    }

                    // For dense charts (like hourly/daily), skip some labels if not scrolling
                    // Since it scrolls, we can show more, but if it's too many still space them out
                    val shouldDrawLabel = neededWidth >= (boxWidth - leftPaddingDp) || points.size <= 7 || index == 0 || index == points.size / 2 || index == points.size - 1
                    
                    if (shouldDrawLabel) {
                        val labelWidth = textMeasurer.measure(point.label, labelStyle).size.width
                        drawText(
                            textMeasurer = textMeasurer,
                            text = point.label,
                            style = labelStyle,
                            topLeft = Offset(x - (labelWidth / 2f), chartHeight + 8.dp.toPx())
                        )
                    }
                }

                // Draw Tooltip for selected point
                selectedIndex?.let { index ->
                    val point = points[index]
                    val x = index * spacing
                    val y = chartHeight - (point.totalAmount.toFloat() / maxAmount) * chartHeight * animationProgress.value

                    val tooltipText = "${point.label}: $${formatMoney(point.totalAmount)}"
                    val textLayoutResult = textMeasurer.measure(tooltipText, tooltipStyle)
                    
                    val tooltipWidth = textLayoutResult.size.width + 24.dp.toPx()
                    val tooltipHeight = textLayoutResult.size.height + 16.dp.toPx()
                    
                    var tooltipX = x - (tooltipWidth / 2f)
                    if (tooltipX < 0f) tooltipX = 0f
                    if (tooltipX + tooltipWidth > size.width) tooltipX = size.width - tooltipWidth
                    
                    val tooltipY = (y - tooltipHeight - 12.dp.toPx()).coerceAtLeast(0f)

                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(tooltipX, tooltipY),
                        size = Size(tooltipWidth, tooltipHeight),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                    
                    drawText(
                        textLayoutResult = textLayoutResult,
                        color = onPrimaryColor,
                        topLeft = Offset(tooltipX + 12.dp.toPx(), tooltipY + 8.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun AnalyticsBarChart(
    points: List<ReportPoint>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        fontSize = 10.sp
    )
    val tooltipStyle = TextStyle(
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    
    val animationProgress = remember { Animatable(0f) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    LaunchedEffect(points) {
        selectedIndex = null
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }

    BoxWithConstraints(modifier = modifier) {
        val boxWidth = maxWidth
        val leftPaddingDp = 50.dp
        val bottomPaddingDp = 30.dp

        // Background Layer: Y-Axis and Grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.isEmpty()) return@Canvas

            val chartHeight = size.height - bottomPaddingDp.toPx()
            val maxAmount = points.maxOf { it.totalAmount }.toFloat().coerceAtLeast(1f)
            
            val gridLines = 3
            for (i in 0..gridLines) {
                val ratio = i.toFloat() / gridLines
                val y = chartHeight - (chartHeight * ratio)
                
                // Grid Line
                drawLine(
                    color = surfaceColor.copy(alpha = 0.2f),
                    start = Offset(leftPaddingDp.toPx(), y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                val amountLabel = formatMoney((maxAmount * ratio).toDouble()).substringBefore(".")
                drawText(
                    textMeasurer = textMeasurer,
                    text = "$$amountLabel",
                    style = labelStyle,
                    topLeft = Offset(5.dp.toPx(), y - 10.dp.toPx())
                )
            }
        }

        // Foreground Layer: Scrollable Chart
        val scrollState = rememberScrollState()
        val minWidthPerBar = 40.dp
        val neededWidth = minWidthPerBar * points.size
        val chartContentWidth = maxOf(boxWidth - leftPaddingDp, neededWidth)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = leftPaddingDp)
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(chartContentWidth)
                    .fillMaxHeight()
                    .pointerInput(points) {
                        detectTapGestures { tapOffset ->
                            if (points.isEmpty()) return@detectTapGestures
                            val chartWidth = size.width
                            val barWidth = (chartWidth / points.size) * 0.7f
                            val gap = (chartWidth / points.size) * 0.3f
                            
                            var closestIndex = -1
                            points.forEachIndexed { index, _ ->
                                val x = index * (barWidth + gap) + gap / 2
                                if (tapOffset.x in x..(x + barWidth)) {
                                    closestIndex = index
                                }
                            }
                            selectedIndex = if (closestIndex != -1) closestIndex else null
                        }
                    }
            ) {
                if (points.isEmpty()) return@Canvas

                val chartWidth = size.width
                val chartHeight = size.height - bottomPaddingDp.toPx()
                val maxAmount = points.maxOf { it.totalAmount }.toFloat().coerceAtLeast(1f)
                val barWidth = (chartWidth / points.size) * 0.7f
                val gap = (chartWidth / points.size) * 0.3f

                points.forEachIndexed { index, point ->
                    val x = index * (barWidth + gap) + gap / 2
                    val barHeight = (point.totalAmount.toFloat() / maxAmount) * chartHeight * animationProgress.value
                    val isSelected = index == selectedIndex
                    
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = if (isSelected) {
                                listOf(primaryColor.copy(alpha = 0.8f), primaryColor.copy(alpha = 0.5f))
                            } else {
                                listOf(primaryColor, primaryColor.copy(alpha = 0.6f))
                            }
                        ),
                        topLeft = Offset(x, chartHeight - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )

                    // Draw all labels since we can scroll now, or step them if too many
                    val step = if (points.size > 15 && neededWidth < (boxWidth - leftPaddingDp)) 2 else 1
                    if (index % step == 0) {
                        val labelWidth = textMeasurer.measure(point.label, labelStyle).size.width
                        drawText(
                            textMeasurer = textMeasurer,
                            text = point.label,
                            style = labelStyle,
                            topLeft = Offset(x + (barWidth / 2f) - (labelWidth / 2f), chartHeight + 8.dp.toPx())
                        )
                    }
                }
                
                // Draw Tooltip for selected bar
                selectedIndex?.let { index ->
                    val point = points[index]
                    val x = index * (barWidth + gap) + gap / 2
                    val barHeight = (point.totalAmount.toFloat() / maxAmount) * chartHeight * animationProgress.value

                    val tooltipText = "${point.label}: $${formatMoney(point.totalAmount)}"
                    val textLayoutResult = textMeasurer.measure(tooltipText, tooltipStyle)
                    
                    val tooltipWidth = textLayoutResult.size.width + 24.dp.toPx()
                    val tooltipHeight = textLayoutResult.size.height + 16.dp.toPx()
                    
                    var tooltipX = x + (barWidth / 2f) - (tooltipWidth / 2f)
                    if (tooltipX < 0f) tooltipX = 0f
                    if (tooltipX + tooltipWidth > size.width) tooltipX = size.width - tooltipWidth
                    
                    val tooltipY = (chartHeight - barHeight - tooltipHeight - 8.dp.toPx()).coerceAtLeast(0f)

                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(tooltipX, tooltipY),
                        size = Size(tooltipWidth, tooltipHeight),
                        cornerRadius = CornerRadius(8.dp.toPx())
                    )
                    
                    drawText(
                        textLayoutResult = textLayoutResult,
                        color = onPrimaryColor,
                        topLeft = Offset(tooltipX + 12.dp.toPx(), tooltipY + 8.dp.toPx())
                    )
                }
            }
        }
    }
}
