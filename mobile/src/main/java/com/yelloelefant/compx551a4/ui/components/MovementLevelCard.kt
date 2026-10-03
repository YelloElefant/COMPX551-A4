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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yelloelefant.compx551a4.data.AccelerometerData
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

enum class MovementLevel(val label: String, val threshold: Double, val color: Color) {
    STATIONARY("Stationary / Rest", 1.08, Color(0xFF9E9E9E)),
    LIGHT("Light Motion", 1.25, Color(0xFF00BCD4)),
    MODERATE("Moderate Activity", 1.65, Color(0xFFFF9800)),
    HIGH("High Impact / Intense", 3.0, Color(0xFFF44336));

    companion object {
        fun fromMagnitude(mag: Double): MovementLevel {
            return when {
                mag >= MODERATE.threshold -> HIGH
                mag >= LIGHT.threshold -> MODERATE
                mag >= STATIONARY.threshold -> LIGHT
                else -> STATIONARY
            }
        }
    }
}

@Composable
fun MovementLevelCard(
    accHistory: List<AccelerometerData>,
    modifier: Modifier = Modifier,
) {
    val samples = accHistory.takeLast(60)
    val latest = samples.lastOrNull()
    val latestMag = if (latest != null) {
        sqrt(latest.x.toDouble().pow(2) + latest.y.toDouble().pow(2) + latest.z.toDouble().pow(2))
    } else {
        1.0
    }

    val movementLevel = MovementLevel.fromMagnitude(latestMag)

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
                        text = "Movement Level & Intensity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time activity classification from accelerometer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(movementLevel.color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = movementLevel.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = movementLevel.color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Movement Magnitude Chart over time
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(104.dp)) {
                    val w = size.width
                    val h = size.height
                    val baselineY = h * 0.8f

                    if (samples.size >= 2) {
                        val stepX = w / (samples.size - 1)
                        val path = Path()

                        val mags = samples.map { sample ->
                            sqrt(sample.x.toDouble().pow(2) + sample.y.toDouble().pow(2) + sample.z.toDouble().pow(2))
                        }

                        mags.forEachIndexed { i, mag ->
                            val x = i * stepX
                            val delta = mag - 1.0
                            val y = (baselineY - (delta * (h * 1.5f))).toFloat().coerceIn(4f, h - 4f)

                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(path, movementLevel.color, style = Stroke(width = 2.5.dp.toPx()))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vector Magnitude (|Mag|):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.3f g", latestMag),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = movementLevel.color
                )
            }
        }
    }
}
