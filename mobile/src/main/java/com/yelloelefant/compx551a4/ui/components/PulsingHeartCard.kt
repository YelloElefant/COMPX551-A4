package com.yelloelefant.compx551a4.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yelloelefant.compx551a4.processing.ProcessedHrSample
import kotlin.math.roundToInt

@Composable
fun PulsingHeartCard(
    hrSample: ProcessedHrSample?,
    modifier: Modifier = Modifier,
) {
    val bpm = hrSample?.smoothedBpm ?: 0
    val rawBpm = hrSample?.bpm ?: 0
    val isConnected = hrSample != null
    val zone = hrSample?.zone

    // Dynamic heart rate pulse animation speed (ms per beat = 60,000 / BPM)
    val pulseDurationMs = if (bpm > 30) (60000 / bpm).coerceIn(300, 1500) else 1000

    val infiniteTransition = rememberInfiniteTransition(label = "heartPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isConnected) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDurationMs / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Contact Status & Zone Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Contact Tag
                if (hrSample?.contact == true) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2E7D32).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "● Strap Contact OK",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFD32F2F).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isConnected) "⚠️ Check Strap Contact" else "Disconnected",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // HR Zone Chip
                if (zone != null) {
                    Box(
                        modifier = Modifier
                            .background(zone.color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = zone.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = zone.color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Heart Icon & Live BPM
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = (zone?.color ?: Color.Red).copy(alpha = 0.15f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart Rate",
                        tint = zone?.color ?: Color.Red,
                        modifier = Modifier
                            .size(44.dp)
                            .scale(scale)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = if (isConnected) "$bpm" else "--",
                        fontSize = 54.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 54.sp
                    )
                    Text(
                        text = "BPM (Smoothed)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Metrics Bar: Instant BPM, RR Interval, RMSSD
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isConnected) "$rawBpm" else "--",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instant BPM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isConnected) "${hrSample?.rrMs ?: 0} ms" else "--",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RR Interval",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isConnected) "${hrSample?.rmssd?.roundToInt() ?: 0} ms" else "--",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "HRV (RMSSD)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // High HR Alert Warning Banner
            if (hrSample?.isHighHrEvent == true) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "High HR Alert",
                        tint = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "High Heart Rate Alert (> 165 BPM)",
                        color = Color(0xFFD32F2F),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
