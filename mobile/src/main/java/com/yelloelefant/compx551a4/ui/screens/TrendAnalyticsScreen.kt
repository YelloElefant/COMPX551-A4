package com.yelloelefant.compx551a4.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yelloelefant.compx551a4.data.SessionEntity
import com.yelloelefant.compx551a4.data.TrendStats
import com.yelloelefant.compx551a4.viewmodel.MainViewModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TrendAnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val sessions by viewModel.sessionHistory.collectAsState()
    val trendStats by viewModel.trendStats.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Long-Term Health & Fitness Trends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Aggregated Polar H10 session analytics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Top Summary Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryStatCard(
                title = "Total Sessions",
                value = "${trendStats.totalSessions}",
                icon = Icons.Default.Assessment,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                title = "Total Time",
                value = formatHours(trendStats.totalDurationSeconds),
                icon = Icons.Default.Timer,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryStatCard(
                title = "Overall Avg HR",
                value = "${trendStats.overallAvgBpm} BPM",
                icon = Icons.Default.Favorite,
                color = Color(0xFFFF3B30),
                modifier = Modifier.weight(1f)
            )
            SummaryStatCard(
                title = "Overall Avg HRV",
                value = "${trendStats.overallAvgRmssd.roundToInt()} ms",
                icon = Icons.Default.MonitorHeart,
                color = Color(0xFFAF52DE),
                modifier = Modifier.weight(1f)
            )
        }

        if (sessions.size < 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier.padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Record at least 2 sessions to render multi-workout historical trend charts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Trend Chart 1: Session Avg Heart Rate Over Time
            TrendLineChartCard(
                title = "Heart Rate Trend Across Sessions",
                subtitle = "Average BPM per recorded workout session",
                sessions = sessions,
                valueExtractor = { it.avgBpm.toFloat() },
                lineColor = Color(0xFFFF3B30),
                unit = "BPM"
            )

            // Trend Chart 2: Session Avg HRV (RMSSD) Over Time
            TrendLineChartCard(
                title = "HRV (RMSSD) Trend Across Sessions",
                subtitle = "Average autonomic recovery score per session",
                sessions = sessions,
                valueExtractor = { it.avgRmssd.toFloat() },
                lineColor = Color(0xFFAF52DE),
                unit = "ms"
            )
        }
    }
}

@Composable
private fun SummaryStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TrendLineChartCard(
    title: String,
    subtitle: String,
    sessions: List<SessionEntity>,
    valueExtractor: (SessionEntity) -> Float,
    lineColor: Color,
    unit: String,
) {
    val chronologicalSessions = sessions.reversed() // oldest to newest
    val values = chronologicalSessions.map(valueExtractor)

    val minVal = (values.minOrNull() ?: 0f) * 0.85f
    val maxVal = (values.maxOfOrNull { it } ?: 100f) * 1.15f
    val range = (maxVal - minVal).coerceAtLeast(10f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val stepX = w / (values.size - 1).coerceAtLeast(1)
                    val path = Path()

                    val points = values.mapIndexed { index, valF ->
                        val x = index * stepX
                        val y = h - ((valF - minVal) / range * h)
                        Offset(x, y)
                    }

                    points.forEachIndexed { i, pt ->
                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                    }

                    drawPath(path, lineColor, style = Stroke(width = 3.dp.toPx()))

                    points.forEach { pt ->
                        drawCircle(color = lineColor, radius = 5.dp.toPx(), center = pt)
                        drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = pt)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Oldest Session",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Latest Session: ${values.lastOrNull()?.roundToInt() ?: 0} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = lineColor
                )
            }
        }
    }
}

private fun formatHours(seconds: Long): String {
    val hrs = seconds / 3600.0
    return String.format(Locale.getDefault(), "%.1f hrs", hrs)
}
