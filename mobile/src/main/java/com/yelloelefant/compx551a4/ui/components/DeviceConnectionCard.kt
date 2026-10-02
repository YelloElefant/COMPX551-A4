package com.yelloelefant.compx551a4.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.polar.sdk.api.model.PolarDeviceInfo
import com.yelloelefant.compx551a4.sensor.ConnectionState
import com.yelloelefant.compx551a4.sensor.MockPolarH10Source
import com.yelloelefant.compx551a4.viewmodel.SensorSourceType

@Composable
fun DeviceConnectionCard(
    sourceType: SensorSourceType,
    deviceId: String,
    connectionState: ConnectionState,
    simulatorIntensity: MockPolarH10Source.ExerciseIntensity,
    isScanning: Boolean = false,
    discoveredDevices: List<PolarDeviceInfo> = emptyList(),
    onSourceTypeChange: (SensorSourceType) -> Unit,
    onDeviceIdChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onStartScan: () -> Unit = {},
    onStopScan: () -> Unit = {},
    onIntensityChange: (MockPolarH10Source.ExerciseIntensity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isConnected = connectionState is ConnectionState.Connected
    var showTroubleshootingHelp by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Source Selector Segmented Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sensor Source Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = { showTroubleshootingHelp = !showTroubleshootingHelp }) {
                    Icon(Icons.Default.HelpOutline, contentDescription = "Troubleshooting", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showTroubleshootingHelp) "Hide Help" else "Connection Help", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Troubleshooting help box
            AnimatedVisibility(visible = showTroubleshootingHelp) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFFFB300), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "💡 Why iPhone sees Polar H10 but Android doesn't:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Active iPhone Connection: Polar H10 only supports ONE Bluetooth connection at a time. If connected to your iPhone or Polar Beat app on iOS, turn OFF iPhone Bluetooth so Android can find it!\n" +
                                    "2. Android System GPS Location: Ensure Location Services (GPS) is turned ON in your Android phone settings dropdown.\n" +
                                    "3. Wet Electrodes: Wet the strap rubber pads and ensure the strap is worn on your chest.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF3E2723)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SensorSourceType.entries.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = sourceType == type,
                        onClick = { onSourceTypeChange(type) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = SensorSourceType.entries.size)
                    ) {
                        Text(type.displayName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sourceType == SensorSourceType.REAL_POLAR_H10) {
                // Real Polar BLE Device Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = deviceId,
                        onValueChange = onDeviceIdChange,
                        label = { Text("Polar H10 Device ID (8 digits)") },
                        placeholder = { Text("e.g. C38E221A") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !isConnected
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isConnected) {
                        OutlinedButton(
                            onClick = onDisconnect,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F))
                        ) {
                            Icon(Icons.Default.BluetoothDisabled, contentDescription = "Disconnect")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Disconnect")
                        }
                    } else {
                        Button(onClick = onConnect) {
                            Icon(Icons.Default.BluetoothConnected, contentDescription = "Connect")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Connect")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scan for nearby devices button
                if (!isConnected) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = if (isScanning) onStopScan else onStartScan,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scanning for Nearby Polar H10...")
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Scan")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Nearby Bluetooth Devices")
                            }
                        }
                    }

                    // Display discovered devices list
                    if (discoveredDevices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Discovered Polar Devices:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            discoveredDevices.forEach { device ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .clickable {
                                            onDeviceIdChange(device.deviceId)
                                            onConnect()
                                        }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Polar ${device.name} [ID: ${device.deviceId}]", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(text = "Tap to Connect", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Connection Status Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (statusText, statusColor) = when (connectionState) {
                        is ConnectionState.Connected -> "Connected to ${connectionState.deviceId}" to Color(0xFF2E7D32)
                        is ConnectionState.Connecting -> "Connecting to ${connectionState.deviceId}..." to Color(0xFFF57C00)
                        is ConnectionState.Failed -> "Failed: ${connectionState.message}" to Color(0xFFD32F2F)
                        ConnectionState.Disconnected -> "Disconnected" to Color.Gray
                    }

                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Status: $statusText",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Simulator Controls
                Text(
                    text = "Simulated Exercise Intensity Level:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MockPolarH10Source.ExerciseIntensity.entries.forEach { intensity ->
                        FilterChip(
                            selected = simulatorIntensity == intensity,
                            onClick = { onIntensityChange(intensity) },
                            label = { Text(intensity.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!isConnected) {
                    Button(
                        onClick = onConnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start Polar H10 Simulator Stream")
                    }
                } else {
                    OutlinedButton(
                        onClick = onDisconnect,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Stop Simulator Stream")
                    }
                }
            }
        }
    }
}
