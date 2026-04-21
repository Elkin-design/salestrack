package org.salestrack.app.presentation.feature.reports.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.salestrack.app.core.utils.formatMoney
import org.salestrack.app.domain.model.ReportPoint

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
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.animateTo(1f, animationSpec = tween(1200))
    }

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas

        val leftPadding = 50.dp.toPx()
        val bottomPadding = 30.dp.toPx()
        val chartWidth = size.width - leftPadding
        val chartHeight = size.height - bottomPadding

        val maxAmount = points.maxOf { it.totalAmount }.toFloat().coerceAtLeast(1f)
        val spacing = chartWidth / (points.size - 1).coerceAtLeast(1)
        
        // Draw Y-Axis Labels and Grid
        val gridLines = 4
        for (i in 0..gridLines) {
            val ratio = i.toFloat() / gridLines
            val y = chartHeight - (chartHeight * ratio)
            
            // Grid Line
            drawLine(
                color = surfaceColor.copy(alpha = 0.2f),
                start = Offset(leftPadding, y),
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

        val path = Path()
        val fillPath = Path()
        
        points.forEachIndexed { index, point ->
            val x = leftPadding + (index * spacing)
            val y = chartHeight - (point.totalAmount.toFloat() / maxAmount) * chartHeight * animationProgress.value
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, chartHeight)
                fillPath.lineTo(x, y)
            } else {
                val prevX = leftPadding + ((index - 1) * spacing)
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

        // Draw line (Single gamma gradient)
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(primaryColor, primaryColor.copy(alpha = 0.7f))),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points (nodes) and X Labels
        points.forEachIndexed { index, point ->
            val x = leftPadding + (index * spacing)
            val y = chartHeight - (point.totalAmount.toFloat() / maxAmount) * chartHeight * animationProgress.value

            // Node
            if (animationProgress.value > 0.8f) {
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // X Label (only first, middle, last to avoid overlap)
            if (index == 0 || index == points.size / 2 || index == points.size - 1) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = point.label,
                    style = labelStyle,
                    topLeft = Offset(x - 15.dp.toPx(), chartHeight + 8.dp.toPx())
                )
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
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas

        val leftPadding = 50.dp.toPx()
        val bottomPadding = 30.dp.toPx()
        val chartWidth = size.width - leftPadding
        val chartHeight = size.height - bottomPadding

        val maxAmount = points.maxOf { it.totalAmount }.toFloat().coerceAtLeast(1f)
        val barWidth = (chartWidth / points.size) * 0.7f
        val gap = (chartWidth / points.size) * 0.3f

        // Y Labels
        val gridLines = 3
        for (i in 0..gridLines) {
            val ratio = i.toFloat() / gridLines
            val y = chartHeight - (chartHeight * ratio)
            val amountLabel = formatMoney((maxAmount * ratio).toDouble()).substringBefore(".")
            drawText(
                textMeasurer = textMeasurer,
                text = "$$amountLabel",
                style = labelStyle,
                topLeft = Offset(5.dp.toPx(), y - 10.dp.toPx())
            )
        }

        points.forEachIndexed { index, point ->
            val x = leftPadding + index * (barWidth + gap) + gap / 2
            val barHeight = (point.totalAmount.toFloat() / maxAmount) * chartHeight * animationProgress.value
            
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.6f))
                ),
                topLeft = Offset(x, chartHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )

            // X Label
            if (points.size <= 7 || index % (points.size / 5).coerceAtLeast(1) == 0) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = point.label,
                    style = labelStyle,
                    topLeft = Offset(x, chartHeight + 8.dp.toPx())
                )
            }
        }
    }
}
