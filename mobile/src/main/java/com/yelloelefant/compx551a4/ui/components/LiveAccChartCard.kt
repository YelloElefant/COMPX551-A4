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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yelloelefant.compx551a4.sensor.AccSample
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun LiveAccChartCard(
    accHistory: List<AccSample>,
    modifier: Modifier = Modifier,
) {
    val samples = accHistory.takeLast(80)
    val latestSample = samples.lastOrNull()

    val latestMag = if (latestSample != null) {
        sqrt(latestSample.x.toDouble().pow(2) + latestSample.y.toDouble().pow(2) + latestSample.z.toDouble().pow(2)) / 1000.0
    } else {
        1.0
    }

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
                        text = "3-Axis Motion & Acceleration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "50Hz Stream • Vector Magnitude",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(134.dp)) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f

                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(0f, centerY),
                        end = Offset(width, centerY),
                        strokeWidth = 1f
                    )

                    if (samples.size >= 2) {
                        val stepX = width / (samples.size - 1)

                        val pathX = Path()
                        val pathY = Path()
                        val pathZ = Path()
                        val pathMag = Path()

                        val scaleY = (height / 2f) / 2000f

                        samples.forEachIndexed { i, sample ->
                            val xPos = i * stepX
                            val yX = centerY - (sample.x * scaleY).coerceIn(-centerY, centerY)
                            val yY = centerY - (sample.y * scaleY).coerceIn(-centerY, centerY)
                            val yZ = centerY - (sample.z * scaleY).coerceIn(-centerY, centerY)

                            val magG = sqrt(sample.x.toDouble().pow(2) + sample.y.toDouble().pow(2) + sample.z.toDouble().pow(2)) / 1000.0
                            val magDeltaMilliG = (magG - 1.0) * 1000.0
                            val magOffset = (magDeltaMilliG * scaleY).coerceIn((-centerY).toDouble(), centerY.toDouble()).toFloat()
                            val yMag = centerY - magOffset

                            if (i == 0) {
                                pathX.moveTo(xPos, yX)
                                pathY.moveTo(xPos, yY)
                                pathZ.moveTo(xPos, yZ)
                                pathMag.moveTo(xPos, yMag)
                            } else {
                                pathX.lineTo(xPos, yX)
                                pathY.lineTo(xPos, yY)
                                pathZ.lineTo(xPos, yZ)
                                pathMag.lineTo(xPos, yMag)
                            }
                        }

                        drawPath(pathX, Color(0xFF00BCD4), style = Stroke(width = 1.5.dp.toPx()))
                        drawPath(pathY, Color(0xFF4CAF50), style = Stroke(width = 1.5.dp.toPx()))
                        drawPath(pathZ, Color(0xFF2196F3), style = Stroke(width = 1.5.dp.toPx()))
                        drawPath(pathMag, Color(0xFFFFC107), style = Stroke(width = 2.5.dp.toPx()))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendItem("X", Color(0xFF00BCD4))
                    Spacer(modifier = Modifier.width(8.dp))
                    LegendItem("Y", Color(0xFF4CAF50))
                    Spacer(modifier = Modifier.width(8.dp))
                    LegendItem("Z", Color(0xFF2196F3))
                    Spacer(modifier = Modifier.width(8.dp))
                    LegendItem("|Mag|", Color(0xFFFFC107))
                }

                Text(
                    text = String.format(Locale.getDefault(), "%.2f g", latestMag),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFC107)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
