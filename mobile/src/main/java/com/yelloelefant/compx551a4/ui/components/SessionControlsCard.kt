package com.yelloelefant.compx551a4.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yelloelefant.compx551a4.processing.LiveSessionStats
import com.yelloelefant.compx551a4.viewmodel.SessionRecordingState

@Composable
fun SessionControlsCard(
    recordingState: SessionRecordingState,
    liveStats: LiveSessionStats,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStopAndSave: (title: String, notes: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var sessionTitle by remember { mutableStateOf("") }
    var sessionNotes by remember { mutableStateOf("") }

    val durationText = formatDuration(liveStats.durationSeconds)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (recordingState == SessionRecordingState.RECORDING) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Session Timer Display
            Text(
                text = durationText,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when (recordingState) {
                    SessionRecordingState.IDLE -> "Session Ready"
                    SessionRecordingState.RECORDING -> "● Recording Live Polar Data..."
                    SessionRecordingState.PAUSED -> "Session Paused"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (recordingState == SessionRecordingState.RECORDING) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (recordingState) {
                    SessionRecordingState.IDLE -> {
                        Button(
                            onClick = onStart,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Workout Session", fontWeight = FontWeight.Bold)
                        }
                    }

                    SessionRecordingState.RECORDING -> {
                        Button(
                            onClick = onPause,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pause")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { showSaveDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stop & Save")
                        }
                    }

                    SessionRecordingState.PAUSED -> {
                        Button(
                            onClick = onResume,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = { showSaveDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Finish")
                        }
                    }
                }
            }
        }
    }

    // Save Session Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Session Summary") },
            text = {
                Column {
                    Text(
                        text = "Session Duration: $durationText | Avg HR: ${liveStats.avgBpm} BPM",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = sessionTitle,
                        onValueChange = { sessionTitle = it },
                        label = { Text("Session Title (e.g. Morning Run)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sessionNotes,
                        onValueChange = { sessionNotes = it },
                        label = { Text("Notes / Felt Experience") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        onStopAndSave(sessionTitle, sessionNotes)
                        sessionTitle = ""
                        sessionNotes = ""
                    }
                ) {
                    Text("Save Session")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatDuration(seconds: Long): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hrs > 0) {
        String.format("%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}
