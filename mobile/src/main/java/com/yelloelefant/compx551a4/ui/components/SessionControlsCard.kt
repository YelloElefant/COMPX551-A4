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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yelloelefant.compx551a4.viewmodel.LiveStats
import com.yelloelefant.compx551a4.viewmodel.SessionRecordingState
import java.util.Locale

@Composable
fun SessionControlsCard(
    recordingState: SessionRecordingState,
    liveStats: LiveStats,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            Text(
                text = durationText,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (recordingState == SessionRecordingState.RECORDING) "● Live Data Capture Active" else "Capture Ready",
                style = MaterialTheme.typography.bodyMedium,
                color = if (recordingState == SessionRecordingState.RECORDING) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (recordingState == SessionRecordingState.IDLE) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Live Capture", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Stop Live Capture", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
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
