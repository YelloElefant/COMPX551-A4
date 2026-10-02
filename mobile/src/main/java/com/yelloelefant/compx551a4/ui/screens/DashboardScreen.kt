package com.yelloelefant.compx551a4.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yelloelefant.compx551a4.data.AppConfig
import com.yelloelefant.compx551a4.ui.components.BluetoothPermissionHandler
import com.yelloelefant.compx551a4.ui.components.LiveAccChartCard
import com.yelloelefant.compx551a4.ui.components.LiveHrChartCard
import com.yelloelefant.compx551a4.ui.components.PulsingHeartCard
import com.yelloelefant.compx551a4.ui.components.SessionControlsCard
import com.yelloelefant.compx551a4.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val isConnected by viewModel.isConnected.collectAsState()
    val heartRateData by viewModel.heartRateData.collectAsState()
    val hrChartHistory by viewModel.hrChartHistory.collectAsState()
    val accChartHistory by viewModel.accChartHistory.collectAsState()
    val recordingState by viewModel.recordingState.collectAsState()
    val liveStats by viewModel.liveStats.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()

    var deviceIdInput by remember { mutableStateOf("C6230415") }

    BluetoothPermissionHandler(
        onPermissionsGranted = {}
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PolarSensorManager Connection Card with Bluetooth Scan
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Polar H10 Connection & BLE Scanner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mode: ${if (AppConfig.USE_MOCK_DATA) "Mock Data" else "Live Bluetooth Sensor"} | Status: ${if (isConnected) "Connected" else "Disconnected"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isConnected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = deviceIdInput,
                        onValueChange = { deviceIdInput = it },
                        label = { Text("Polar H10 Device ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isConnected
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.connectDevice(deviceIdInput) },
                            enabled = !isConnected,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Connect")
                        }

                        OutlinedButton(
                            onClick = { viewModel.disconnectDevice() },
                            enabled = isConnected,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Disconnect")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bluetooth Scan Button
                    if (!isConnected) {
                        OutlinedButton(
                            onClick = {
                                if (isScanning) viewModel.stopDeviceScan() else viewModel.startDeviceScan()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scanning for Nearby Polar Devices...")
                            } else {
                                Icon(Icons.Default.Search, contentDescription = "Scan")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan Nearby Bluetooth Devices")
                            }
                        }

                        if (discoveredDevices.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Discovered Polar Devices:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Column(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                discoveredDevices.forEach { device ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                            .clickable {
                                                deviceIdInput = device.deviceId
                                                viewModel.connectDevice(device.deviceId)
                                            }
                                            .padding(12.dp),
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
                }
            }

            // Session Controls Card (Start / Stop & Save Session to file)
            SessionControlsCard(
                recordingState = recordingState,
                liveStats = liveStats,
                onStart = { viewModel.startLiveSession() },
                onStopAndSave = { title, notes -> viewModel.stopAndSaveSession(title, notes) }
            )

            // Live Vitals
            PulsingHeartCard(heartRateData = heartRateData, isConnected = isConnected)

            LiveHrChartCard(hrHistory = hrChartHistory)

            LiveAccChartCard(accHistory = accChartHistory)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
