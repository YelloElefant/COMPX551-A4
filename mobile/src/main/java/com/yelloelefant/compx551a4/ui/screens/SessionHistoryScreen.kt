package com.yelloelefant.compx551a4.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yelloelefant.compx551a4.data.SessionEntity
import com.yelloelefant.compx551a4.ui.components.HrZoneDistributionCard
import com.yelloelefant.compx551a4.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SessionHistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val sessions by viewModel.sessionHistory.collectAsState()
    val selectedSession by viewModel.selectedSession.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Session Log & History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${sessions.size} recorded workouts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (sessions.isNotEmpty()) {
                OutlinedButton(onClick = { showClearDialog = true }) {
                    Text("Clear Log", color = Color(0xFFD32F2F))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No recorded sessions yet.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start a workout session on the Live Dashboard to capture Polar H10 data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    SessionHistoryCard(
                        session = session,
                        onClick = { viewModel.selectSession(session) },
                        onDelete = { viewModel.deleteSession(session.id) }
                    )
                }
            }
        }
    }

    // Detailed Session Inspector Modal Dialog
    if (selectedSession != null) {
        SessionDetailModal(
            session = selectedSession!!,
            onDismiss = { viewModel.selectSession(null) }
        )
    }

    // Clear All Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Sessions?") },
            text = { Text("Are you sure you want to delete all historical session logs? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllSessions()
                        showClearDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SessionHistoryCard(
    session: SessionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(session.startTimeMs))
    val durationText = formatDuration(session.durationSeconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Session",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill("Duration", durationText)
                MetricPill("Avg HR", "${session.avgBpm} BPM")
                MetricPill("Max HR", "${session.maxBpm} BPM")
                MetricPill("Avg HRV", "${session.avgRmssd.roundToInt()} ms")
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SessionDetailModal(
    session: SessionEntity,
    onDismiss: () -> Unit,
) {
    val dateStr = SimpleDateFormat("EEE, MMM dd yyyy 'at' HH:mm", Locale.getDefault()).format(Date(session.startTimeMs))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = session.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Key Stats Summary Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MetricPill("Duration", formatDuration(session.durationSeconds))
                    MetricPill("Avg HR", "${session.avgBpm} BPM")
                    MetricPill("Peak HR", "${session.maxBpm} BPM")
                    MetricPill("HRV (RMSSD)", "${session.avgRmssd.roundToInt()} ms")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes section if present
                if (session.notes.isNotBlank()) {
                    Text(text = "Notes:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = session.notes, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // HR Graph throughout session
                if (session.hrSeries.size >= 2) {
                    Text(text = "Session HR Timeline", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val minB = session.hrSeries.minOf { it.bpm }.coerceAtMost(50)
                            val maxB = session.hrSeries.maxOf { it.bpm }.coerceAtLeast(160)
                            val range = (maxB - minB).coerceAtLeast(30).toFloat()

                            val stepX = w / (session.hrSeries.size - 1)
                            val path = Path()

                            session.hrSeries.forEachIndexed { i, pt ->
                                val x = i * stepX
                                val y = h - ((pt.bpm - minB) / range * h)
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }

                            drawPath(path, Color(0xFFFF3B30), style = Stroke(width = 2.dp.toPx()))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // HR Zone Breakdown
                HrZoneDistributionCard(zonePercentages = session.zoneDistribution)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close Inspector")
            }
        }
    )
}

private fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }
}
