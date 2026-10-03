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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.yelloelefant.compx551a4.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SessionHistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val sessions by viewModel.sessionHistory.collectAsState()
    var selectedSession by remember { mutableStateOf<SessionEntity?>(null) }

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
                    text = "Saved Workout Sessions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${sessions.size} sessions saved to file storage • Tap card for details",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No saved sessions found.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Use the Session Control card on the Dashboard to record and save sessions.",
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
                        onClick = { selectedSession = session },
                        onDelete = { viewModel.deleteSession(session.id) }
                    )
                }
            }
        }
    }

    // Individual Session Detail Inspector Dialog
    if (selectedSession != null) {
        SessionDetailModal(
            session = selectedSession!!,
            onDismiss = { selectedSession = null }
        )
    }
}

@Composable
fun SessionHistoryCard(
    session: SessionEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dateStr = try {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(session.startTimeMs))
    } catch (e: Exception) {
        "Recent Session"
    }
    val durationText = formatDuration(session.durationSeconds)

    val avgMag = if (session.accSeries.isNotEmpty()) {
        val validMags = session.accSeries.map { it.magG }.filter { !it.isNaN() }
        if (validMags.isNotEmpty()) validMags.average() else 1.0
    } else {
        1.0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = durationText, fontWeight = FontWeight.Bold)
                    Text(text = "Duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${session.avgBpm} BPM", fontWeight = FontWeight.Bold)
                    Text(text = "Avg HR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${session.maxBpm} BPM", fontWeight = FontWeight.Bold, color = Color.Red)
                    Text(text = "Peak HR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = String.format(Locale.getDefault(), "%.2f g", avgMag), fontWeight = FontWeight.Bold, color = Color(0xFF0288D1))
                    Text(text = "Avg Mag", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (session.hrEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${session.hrEvents.size} High HR Alert(s) detected",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SessionDetailModal(
    session: SessionEntity,
    onDismiss: () -> Unit,
) {
    val dateStr = try {
        SimpleDateFormat("EEE, MMM dd yyyy 'at' HH:mm", Locale.getDefault()).format(Date(session.startTimeMs))
    } catch (e: Exception) {
        "Recent Session"
    }

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
                // Summary Metrics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = formatDuration(session.durationSeconds), fontWeight = FontWeight.Bold)
                        Text(text = "Duration", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${session.avgBpm}", fontWeight = FontWeight.Bold)
                        Text(text = "Avg BPM", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${session.maxBpm}", fontWeight = FontWeight.Bold, color = Color.Red)
                        Text(text = "Max BPM", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (session.notes.isNotBlank()) {
                    Text(text = "Notes:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = session.notes, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Heart Rate Timeline Chart with High HR Event Markers
                Text(text = "Heart Rate Timeline & High HR Events", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val hrPoints = session.hrSeries

                        if (hrPoints.size >= 2) {
                            val minB = hrPoints.minOf { it.bpm }.coerceAtMost(50)
                            val maxB = hrPoints.maxOf { it.bpm }.coerceAtLeast(160)
                            val range = (maxB - minB).coerceAtLeast(30).toFloat()

                            val stepX = w / (hrPoints.size - 1)
                            val path = Path()

                            hrPoints.forEachIndexed { i, pt ->
                                val x = i * stepX
                                val y = h - ((pt.bpm - minB) / range * h)
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }

                            drawPath(path, Color(0xFFFF3B30), style = Stroke(width = 2.dp.toPx()))

                            // Draw High HR Event markers (>150 BPM)
                            session.hrEvents.forEach { ev ->
                                val totalSec = session.durationSeconds.coerceAtLeast(1)
                                val xPos = (ev.sec.toFloat() / totalSec) * w
                                val yPos = h - ((ev.bpm - minB) / range * h)
                                drawCircle(color = Color(0xFFFF9800), radius = 6.dp.toPx(), center = Offset(xPos, yPos))
                                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(xPos, yPos))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High HR Events Log
                Text(text = "High HR Events Log (${session.hrEvents.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (session.hrEvents.isEmpty()) {
                    Text(text = "No high heart rate events (>150 BPM) recorded in this session.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    session.hrEvents.forEach { ev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "⏱️ At ${formatDuration(ev.sec.toLong())}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "${ev.bpm} BPM", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        }
                    }
                }
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
