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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun HrvGaugeCard(
    rmssd: Double,
    avgRmssd: Double,
    modifier: Modifier = Modifier,
) {
    val rmssdInt = rmssd.roundToInt().coerceIn(0, 150)
    val sweepAngle = (rmssdInt / 100f * 180f).coerceIn(0f, 180f)

    val (statusLabel, statusColor) = when {
        rmssdInt >= 50 -> "High HRV • Optimal Recovery" to Color(0xFF4CAF50)
        rmssdInt >= 25 -> "Moderate HRV • Balanced" to Color(0xFF0288D1)
        rmssdInt > 0 -> "Low HRV • Fatigue / High Stress" to Color(0xFFFF5722)
        else -> "Awaiting RR Data..." to Color.Gray
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Heart Rate Variability (HRV)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arc Gauge Canvas
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp, 100.dp)) {
                Canvas(modifier = Modifier.size(170.dp, 90.dp)) {
                    val strokeWidthPx = 16.dp.toPx()
                    val arcSize = Size(size.width - strokeWidthPx, size.height * 2 - strokeWidthPx)
                    val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)

                    // Track arc
                    drawArc(
                        color = Color.Gray.copy(alpha = 0.2f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    // Value arc
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = statusColor,
                            startAngle = 180f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text(
                        text = "$rmssdInt",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                    Text(
                        text = "RMSSD (ms)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Text(
                    text = "Current: $rmssdInt ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Session Avg: ${avgRmssd.roundToInt()} ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
