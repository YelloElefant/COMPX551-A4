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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yelloelefant.compx551a4.data.HeartRateData

@Composable
fun LiveHrChartCard(
    hrHistory: List<HeartRateData>,
    modifier: Modifier = Modifier,
) {
    val samples = hrHistory.takeLast(60)
    val latestSample = samples.lastOrNull()
    val latestBpm = latestSample?.bpm ?: 0

    // Green if between 80 and 120, Red otherwise
    val chartColor = if (latestBpm in 80..120) {
        Color(0xFF4CAF50) // Green
    } else {
        Color(0xFFF44336) // Red
    }

    val minBpm = (samples.minOfOrNull { it.bpm } ?: 60).coerceAtMost(50)
    val maxBpm = (samples.maxOfOrNull { it.bpm } ?: 140).coerceAtLeast(160)
    val bpmRange = (maxBpm - minBpm).coerceAtLeast(40).toFloat()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        text = "Green: 80-120 BPM • Red: Outside range",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(chartColor, RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = "${if (latestBpm > 0) latestBpm else "--"} BPM",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = chartColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

                    if (samples.size >= 2) {
                        val stepX = width / (samples.size - 1)
                        val strokePath = Path()
                        val fillPath = Path()

                        val points = samples.mapIndexed { index, sample ->
                            val x = index * stepX
                            val y = height - ((sample.bpm - minBpm) / bpmRange * height)
                            Offset(x, y)
                        }

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

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    chartColor.copy(alpha = 0.35f),
                                    chartColor.copy(alpha = 0.02f)
                                )
                            )
                        )

                        drawPath(
                            path = strokePath,
                            color = chartColor,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        val lastPoint = points.last()
                        drawCircle(
                            color = chartColor,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Min: ${samples.minOfOrNull { it.bpm } ?: "--"} BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Avg: ${if (samples.isNotEmpty()) samples.map { it.bpm }.average().toInt() else "--"} BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Max: ${samples.maxOfOrNull { it.bpm } ?: "--"} BPM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
