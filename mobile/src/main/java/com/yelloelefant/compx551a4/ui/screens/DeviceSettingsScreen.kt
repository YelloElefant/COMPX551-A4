package com.yelloelefant.compx551a4.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sensors
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yelloelefant.compx551a4.ui.components.DeviceConnectionCard
import com.yelloelefant.compx551a4.viewmodel.MainViewModel

@Composable
fun DeviceSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val sourceType by viewModel.sensorSourceType.collectAsState()
    val deviceId by viewModel.deviceId.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val simulatorIntensity by viewModel.simulatorIntensity.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Polar H10 Device Setup",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage Bluetooth connection and strap configuration",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Connection Card
        DeviceConnectionCard(
            sourceType = sourceType,
            deviceId = deviceId,
            connectionState = connectionState,
            simulatorIntensity = simulatorIntensity,
            isScanning = isScanning,
            discoveredDevices = discoveredDevices,
            onSourceTypeChange = { viewModel.setSensorSourceType(it) },
            onDeviceIdChange = { viewModel.setDeviceId(it) },
            onConnect = { viewModel.connectDevice() },
            onDisconnect = { viewModel.disconnectDevice() },
            onStartScan = { viewModel.startDeviceScan() },
            onStopScan = { viewModel.stopDeviceScan() },
            onIntensityChange = { viewModel.setSimulatorIntensity(it) }
        )

        // Polar H10 Device Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Polar BLE SDK Specifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                InfoRow("Hardware Model", "Polar H10 Chest Strap")
                InfoRow("Heart Rate Stream", "1 Hz (BPM + Beat-to-Beat RR ms)")
                InfoRow("Accelerometer Stream", "50 Hz 3-Axis (X, Y, Z milli-g)")
                InfoRow("Range & Resolution", "±8 g range, 16-bit resolution")
                InfoRow("SDK Library", "com.github.polarofficial:polar-ble-sdk:5.5.0")
            }
        }

        // Chest Strap Attachment Guidelines
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Strap Placement & Contact Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "1. Moistening Electrodes:\nMoisten the rubber electrode areas on the back of the strap with water or conductive gel before wearing.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "2. Strap Positioning:\nAttach the connector to the strap and wrap it around your chest just below the chest muscles. Ensure a snug fit.",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "3. Unclipping After Use:\nDetach the connector module from the strap after every session to preserve CR2032 battery life.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}
