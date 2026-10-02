package com.yelloelefant.compx551a4.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yelloelefant.compx551a4.ui.components.BluetoothPermissionHandler
import com.yelloelefant.compx551a4.ui.components.DeviceConnectionCard
import com.yelloelefant.compx551a4.ui.components.HrZoneDistributionCard
import com.yelloelefant.compx551a4.ui.components.HrvGaugeCard
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
    val sourceType by viewModel.sensorSourceType.collectAsState()
    val deviceId by viewModel.deviceId.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val recordingState by viewModel.recordingState.collectAsState()
    val liveHrSample by viewModel.liveHrSample.collectAsState()
    val hrChartHistory by viewModel.hrChartHistory.collectAsState()
    val accChartHistory by viewModel.accChartHistory.collectAsState()
    val liveStats by viewModel.liveStats.collectAsState()
    val simulatorIntensity by viewModel.simulatorIntensity.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()

    BluetoothPermissionHandler(
        onPermissionsGranted = {
            // Automatically connect on permission granted if disconnected
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Device Connection Card
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

            // Session Controls Bar
            SessionControlsCard(
                recordingState = recordingState,
                liveStats = liveStats,
                onStart = { viewModel.startSession() },
                onPause = { viewModel.pauseSession() },
                onResume = { viewModel.resumeSession() },
                onStopAndSave = { title, notes -> viewModel.stopAndSaveSession(title, notes) }
            )

            // Live Pulsing Heart Rate & Vitals Card
            PulsingHeartCard(hrSample = liveHrSample)

            // Live HR Trend Line Chart
            LiveHrChartCard(hrHistory = hrChartHistory)

            // Live 3-Axis Accelerometer Motion Chart
            LiveAccChartCard(accHistory = accChartHistory)

            // Live Heart Rate Variability (HRV) Arc Gauge
            HrvGaugeCard(
                rmssd = liveHrSample?.rmssd ?: 0.0,
                avgRmssd = liveStats.avgRmssd
            )

            // Live HR Zone Distribution Breakdown
            HrZoneDistributionCard(zonePercentages = liveStats.zonePercentages)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
