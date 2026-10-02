package com.yelloelefant.compx551a4.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yelloelefant.compx551a4.processing.HrZone
import com.yelloelefant.compx551a4.processing.ProcessedHrSample

@Composable
fun LiveHrChartCard(
    hrHistory: List<ProcessedHrSample>,
    modifier: Modifier = Modifier,
) {
    val samples = hrHistory.takeLast(60)
    val latestSample = samples.lastOrNull()
    val activeZone = latestSample?.zone ?: HrZone.REST

    val minBpm = (samples.minOfOrNull { it.smoothedBpm } ?: 60).coerceAtMost(50)
    val maxBpm = (samples.maxOfOrNull { it.smoothedBpm } ?: 140).coerceAtLeast(160)
    val bpmRange = (maxBpm - minBpm).coerceAtLeast(40).toFloat()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Live Heart Rate Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time 60s window • Polar H10",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(activeZone.color, RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "${latestSample?.smoothedBpm ?: "--"} BPM",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = activeZone.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(164.dp)) {
                    val width = size.width
                    val height = size.height

                    // Draw Horizontal Zone Grid Lines (60, 100, 130, 155, 175)
                    val zoneLines = listOf(60, 100, 130, 155, 175)
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    zoneLines.forEach { zoneBpm ->
                        if (zoneBpm in minBpm..maxBpm) {
                            val y = height - ((zoneBpm - minBpm) / bpmRange * height)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.3f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f,
                                pathEffect = dashPathEffect
                            )
                        }
                    }

                    if (samples.size >= 2) {
                        val stepX = width / (samples.size - 1)
                        val strokePath = Path()
                        val fillPath = Path()

                        val points = samples.mapIndexed { index, sample ->
                            val x = index * stepX
                            val y = height - ((sample.smoothedBpm - minBpm) / bpmRange * height)
                            Offset(x, y)
                        }

                        // Build path
                        strokePath.moveTo(points.first().x, points.first().y)
                        fillPath.moveTo(points.first().x, height)
                        fillPath.lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val controlPointX = (p1.x + p2.x) / 2f
                            strokePath.cubicTo(
                                controlPointX, p1.y,
                                controlPointX, p2.y,
                                p2.x, p2.y
                            )
                            fillPath.cubicTo(
                                controlPointX, p1.y,
                                controlPointX, p2.y,
                                p2.x, p2.y
                            )
                        }

                        fillPath.lineTo(points.last().x, height)
                        fillPath.close()

                        // Gradient fill under the curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    activeZone.color.copy(alpha = 0.35f),
                                    activeZone.color.copy(alpha = 0.02f)
                                )
                            )
                        )

                        // Main curve line
                        drawPath(
                            path = strokePath,
                            color = activeZone.color,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Draw last point pulsing dot
                        val lastPoint = points.last()
                        drawCircle(
                            color = activeZone.color,
                            radius = 6.dp.toPx(),
                            center = lastPoint
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = lastPoint
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer info: Min, Max, Avg BPM in the current window
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Min: ${samples.minOfOrNull { it.smoothedBpm } ?: "--"} BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Avg: ${if (samples.isNotEmpty()) samples.map { it.smoothedBpm }.average().toInt() else "--"} BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Max: ${samples.maxOfOrNull { it.smoothedBpm } ?: "--"} BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
