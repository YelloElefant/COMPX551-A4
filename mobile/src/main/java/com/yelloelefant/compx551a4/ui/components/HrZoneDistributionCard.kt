package com.yelloelefant.compx551a4.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yelloelefant.compx551a4.processing.HrZone
import kotlin.math.roundToInt

@Composable
fun HrZoneDistributionCard(
    zonePercentages: Map<HrZone, Float>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Heart Rate Zones Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment horizontal progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                HrZone.entries.forEach { zone ->
                    val pct = zonePercentages[zone] ?: 0f
                    if (pct > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(pct.coerceAtLeast(1f))
                                .fillMaxHeight()
                                .background(zone.color)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Zone Rows
            HrZone.entries.forEach { zone ->
                val pct = zonePercentages[zone] ?: 0f
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(130.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(12.dp)
                                .background(zone.color, RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = zone.title,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (pct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = zone.color,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Text(
                        text = "${pct.roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp)
                    )
                }
            }
        }
    }
}
