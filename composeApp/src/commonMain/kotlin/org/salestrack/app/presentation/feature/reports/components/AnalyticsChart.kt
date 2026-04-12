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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import org.salestrack.app.domain.model.ReportPoint

@Composable
fun AnalyticsLineChart(
    points: List<ReportPoint>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.animateTo(1f, animationSpec = tween(1200))
    }

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas

        val maxAmount = points.maxOf { it.totalAmount }.toFloat().coerceAtLeast(1f)
        val spacing = size.width / (points.size - 1).coerceAtLeast(1)
        
        // Draw grid lines (simplified)
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = size.height - (size.height / gridLines) * i
            drawLine(
                color = surfaceColor.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val path = Path()
        val fillPath = Path()
        
        points.forEachIndexed { index, point ->
            val x = index * spacing
            val y = size.height - (point.totalAmount.toFloat() / maxAmount) * size.height * animationProgress.value
            
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                // Cubic bezier for smoothness
                val prevX = (index - 1) * spacing
                val prevY = size.height - (points[index - 1].totalAmount.toFloat() / maxAmount) * size.height * animationProgress.value
                val controlX1 = prevX + (x - prevX) / 2
                val controlX2 = prevX + (x - prevX) / 2
                
                path.cubicTo(controlX1, prevY, controlX2, y, x, y)
                fillPath.cubicTo(controlX1, prevY, controlX2, y, x, y)
            }
            
            if (index == points.size - 1) {
                fillPath.lineTo(x, size.height)
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
                )
            )
        )

        // Draw line
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun AnalyticsBarChart(
    points: List<ReportPoint>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(240.dp)
) {
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(points) {
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }

    Canvas(modifier = modifier) {
        if (points.isEmpty()) return@Canvas

        val maxAmount = points.maxOf { it.totalAmount }.toFloat().coerceAtLeast(1f)
        val barWidth = (size.width / points.size) * 0.7f
        val gap = (size.width / points.size) * 0.3f

        points.forEachIndexed { index, point ->
            val x = index * (barWidth + gap) + gap / 2
            val barHeight = (point.totalAmount.toFloat() / maxAmount) * size.height * animationProgress.value
            
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        secondaryColor,
                        secondaryColor.copy(alpha = 0.6f)
                    )
                ),
                topLeft = Offset(x, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
            )
        }
    }
}
