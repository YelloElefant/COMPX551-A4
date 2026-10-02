package com.yelloelefant.compx551a4

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yelloelefant.compx551a4.data.AppConfig
import com.yelloelefant.compx551a4.ui.theme.COMPX551A4Theme
import com.yelloelefant.compx551a4.viewmodel.AccelerometerViewModel
import com.yelloelefant.compx551a4.viewmodel.HeartRateViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main Activity handling Bluetooth permissions and setting up the Jetpack Compose UI
 * for streaming and displaying raw Heart Rate, RR Intervals, and Accelerometer data.
 */
class MainActivity : ComponentActivity() {

    // Launcher for requesting runtime Bluetooth and Location permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Handle permission results
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            COMPX551A4Theme {
                // Outer Scaffold removed to prevent double padding/clipping conflict
                PolarStreamerScreen()
            }
        }
    }

    /**
     * Checks and requests necessary runtime permissions for BLE scanning/connection and location.
     */
    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}

/**
 * Session summary data class for historical logging.
 */
data class SessionSummary(
    val sessionId: Int,
    val startTime: String,
    val durationSeconds: Long,
    val totalSamples: Int
)

/**
 * Main Jetpack Compose screen rendering connection controls, device ID input, session recording,
 * historical summary log, and real-time sensor streams.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolarStreamerScreen(
    modifier: Modifier = Modifier,
    accViewModel: AccelerometerViewModel = viewModel(),
    hrViewModel: HeartRateViewModel = viewModel()
) {
    val isConnected by hrViewModel.isConnected.collectAsState()
    val hrData by hrViewModel.heartRateData.collectAsState()
    val accData by accViewModel.accelerometerData.collectAsState()

    // Device ID state for physical Polar H10 connection
    var deviceIdInput by remember { mutableStateOf("") }

    // Session Recording States
    var isRecording by remember { mutableStateOf(false) }
    var sessionSampleCount by remember { mutableStateOf(0) }
    var sessionStartTime by remember { mutableStateOf(0L) }
    var sessionDurationSeconds by remember { mutableStateOf(0L) }
    val sessionHistory = remember { mutableStateListOf<SessionSummary>() }

    val timeFormatter = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    // Accumulate samples when recording is active
    LaunchedEffect(hrData, accData) {
        if (isRecording) {
            if (hrData != null || accData != null) {
                sessionSampleCount++
            }
        }
    }

    // Session timer ticker
    LaunchedEffect(isRecording) {
        if (isRecording) {
            sessionStartTime = System.currentTimeMillis()
            sessionDurationSeconds = 0L
            while (isRecording) {
                kotlinx.coroutines.delay(1000L)
                sessionDurationSeconds = (System.currentTimeMillis() - sessionStartTime) / 1000L
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Polar H10 Data Streamer") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues) // Respect TopAppBar padding
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Added top spacer to prevent any clipping below TopAppBar
            Spacer(modifier = Modifier.height(4.dp))

            // Status & Mode Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (AppConfig.USE_MOCK_DATA) "Mode: MOCK DATA STREAM" else "Mode: LIVE BLE SENSOR",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status: ${if (isConnected) "CONNECTED" else "DISCONNECTED"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // FIXED: Device ID Input Field label updated (removed "Optional" to reflect mandatory hardware requirement)
            OutlinedTextField(
                value = deviceIdInput,
                onValueChange = { deviceIdInput = it },
                label = { Text("Polar H10 Device ID") },
                placeholder = { Text("e.g. C6230415") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Connection & Disconnection Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        hrViewModel.connect()
                        accViewModel.connect()
                    },
                    enabled = !isConnected,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Connect")
                }

                Button(
                    onClick = {
                        hrViewModel.disconnect()
                        accViewModel.disconnect()
                        if (isRecording) {
                            isRecording = false
                        }
                    },
                    enabled = isConnected,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Disconnect")
                }
            }

            // Session Recording Controls (Start / Stop Session)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isRecording) "🔴 Session Recording Active" else "⏸️ Session Recorder Ready",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Duration: ${sessionDurationSeconds}s | Samples Captured: $sessionSampleCount",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                sessionSampleCount = 0
                                isRecording = true
                            },
                            enabled = isConnected && !isRecording,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start Session")
                        }

                        Button(
                            onClick = {
                                isRecording = false
                                if (sessionSampleCount > 0) {
                                    sessionHistory.add(
                                        SessionSummary(
                                            sessionId = sessionHistory.size + 1,
                                            startTime = dateFormatter.format(Date(sessionStartTime)),
                                            durationSeconds = sessionDurationSeconds,
                                            totalSamples = sessionSampleCount
                                        )
                                    )
                                }
                            },
                            enabled = isRecording,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Stop Session")
                        }
                    }
                }
            }

            // Heart Rate & RR Intervals Stream Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Heart Rate (HR) & RR Intervals",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val bpmText = hrData?.bpm?.toString() ?: "--"
                    val rrText = hrData?.rrIntervals?.joinToString(", ") { "$it ms" } ?: "--"
                    val timeText = hrData?.timestamp?.let { timeFormatter.format(Date(it)) } ?: "--:--:--.---"

                    Text(
                        text = "$bpmText BPM",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RR Intervals: $rrText",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Timestamp: $timeText",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Accelerometer Stream Card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Accelerometer (ACC)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val xVal = accData?.x?.let { String.format(Locale.getDefault(), "%.3f", it) } ?: "--"
                    val yVal = accData?.y?.let { String.format(Locale.getDefault(), "%.3f", it) } ?: "--"
                    val zVal = accData?.z?.let { String.format(Locale.getDefault(), "%.3f", it) } ?: "--"
                    val accTimeText = accData?.timestamp?.let { timeFormatter.format(Date(it)) } ?: "--:--:--.---"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "X: $xVal", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Y: $yVal", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Z: $zVal", style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Timestamp: $accTimeText",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Historical / Summary Data Log Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Historical Sessions Log (${sessionHistory.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (sessionHistory.isEmpty()) {
                        Text(
                            text = "No completed sessions yet. Start and stop a session to record historical logs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        sessionHistory.forEach { summary ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Session #${summary.sessionId} (${summary.startTime}) - ${summary.durationSeconds}s - ${summary.totalSamples} samples",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
