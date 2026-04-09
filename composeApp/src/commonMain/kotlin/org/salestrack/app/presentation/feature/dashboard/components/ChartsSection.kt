package org.salestrack.app.presentation.feature.dashboard.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.salestrack.app.core.utils.formatMoney
import org.salestrack.app.presentation.feature.dashboard.DashboardCategoryShare
import org.salestrack.app.presentation.feature.dashboard.DashboardTrendPoint
import kotlin.math.roundToInt

@Composable
fun WeeklyTrendCard(
    trend: List<DashboardTrendPoint>,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Tendencia 7 Días",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            
            val maxAmount = trend.maxOfOrNull { it.amount }?.takeIf { it > 0.0 } ?: 1.0
            
            if (trend.isEmpty()) {
                Text(
                    text = "Sin datos suficientes para tendencia", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                trend.forEachIndexed { index, point ->
                    val ratio = (point.amount / maxAmount).toFloat().coerceIn(0f, 1f)
                    var targetRatio by remember { mutableStateOf(0f) }
                    
                    LaunchedEffect(ratio) {
                        targetRatio = ratio
                    }
                    
                    val animatedRatio by animateFloatAsState(
                        targetValue = targetRatio,
                        animationSpec = tween(durationMillis = 500, delayMillis = index * 50, easing = FastOutSlowInEasing),
                        label = "trendRatio"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(36.dp),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedRatio)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Text(
                            text = "$${formatMoney(point.amount)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(84.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownCard(
    breakdown: List<DashboardCategoryShare>,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Mix por Categoría",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            val total = breakdown.sumOf { it.amount }
            if (breakdown.isEmpty() || total <= 0.0) {
                Text(
                    text = "Sin ventas categorizadas todavía", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                breakdown.forEachIndexed { index, item ->
                    val ratio = (item.amount / total).toFloat().coerceIn(0f, 1f)
                    var targetRatio by remember { mutableStateOf(0f) }
                    
                    LaunchedEffect(ratio) {
                        targetRatio = ratio
                    }
                    
                    val animatedRatio by animateFloatAsState(
                        targetValue = targetRatio,
                        animationSpec = tween(durationMillis = 500, delayMillis = index * 50, easing = FastOutSlowInEasing),
                        label = "categoryRatio"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = item.category, 
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatRatio(ratio), 
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedRatio)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.tertiary)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatRatio(value: Float): String {
    return "${(value * 100).roundToInt()}%"
}
