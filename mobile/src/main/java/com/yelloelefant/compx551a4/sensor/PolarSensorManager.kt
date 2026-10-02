package com.yelloelefant.compx551a4.sensor

import android.content.Context
import android.util.Log
import com.polar.sdk.api.model.PolarDeviceInfo
import com.yelloelefant.compx551a4.data.AccelerometerData
import com.yelloelefant.compx551a4.data.AppConfig
import com.yelloelefant.compx551a4.data.HeartRateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages connection lifecycle and sensor data streams from the physical Polar H10 chest strap
 * via [PolarH10Source], or falls back to [MockPolarProvider] when [AppConfig.USE_MOCK_DATA] is enabled.
 */
class PolarSensorManager(val context: Context) {

    private val tag = "PolarSensorManager"

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _accelerometerData = MutableStateFlow<AccelerometerData?>(null)
    val accelerometerData: StateFlow<AccelerometerData?> = _accelerometerData.asStateFlow()

    private val _heartRateData = MutableStateFlow<HeartRateData?>(null)
    val heartRateData: StateFlow<HeartRateData?> = _heartRateData.asStateFlow()

    private var streamJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private val realPolarSource = PolarH10Source(context)

    init {
        Log.d(tag, "PolarSensorManager initialized. Mock mode: ${AppConfig.USE_MOCK_DATA}")
    }

    /**
     * Connects to the physical Polar H10 device or starts mock streaming.
     */
    fun connect(deviceId: String = "C38E221A") {
        Log.d(tag, "Connecting sensor (Mock mode: ${AppConfig.USE_MOCK_DATA}, DeviceId: $deviceId)")
        _isConnected.value = true
        if (AppConfig.USE_MOCK_DATA) {
            startMockStreaming()
        } else {
            startRealPolarStreaming(deviceId)
        }
    }

    /**
     * Disconnects from the sensor device and halts streaming.
     */
    fun disconnect(deviceId: String = "") {
        Log.d(tag, "Disconnecting sensor")
        _isConnected.value = false
        if (!AppConfig.USE_MOCK_DATA) {
            realPolarSource.disconnect()
        }
        stopStreaming()
    }

    /**
     * Scans nearby Polar BLE devices.
     */
    fun searchForDevices(): Flow<PolarDeviceInfo> {
        return realPolarSource.searchForDevices()
    }

    private fun startMockStreaming() {
        streamJob?.cancel()
        streamJob = scope.launch {
            launch {
                MockPolarProvider.heartRateStream().collect { hr ->
                    _heartRateData.value = hr
                }
            }
            launch {
                MockPolarProvider.accelerometerStream().collect { acc ->
                    _accelerometerData.value = acc
                }
            }
        }
    }

    private fun startRealPolarStreaming(deviceId: String) {
        realPolarSource.connect(deviceId)
        streamJob?.cancel()
        streamJob = scope.launch {
            launch {
                try {
                    realPolarSource.hrStream().collect { sample ->
                        _heartRateData.value = HeartRateData(
                            timestamp = sample.timestampMs,
                            bpm = sample.bpm,
                            rrIntervals = sample.rrMs
                        )
                    }
                } catch (e: Exception) {
                    Log.e(tag, "HR Stream error: ${e.message}")
                }
            }
            launch {
                try {
                    realPolarSource.accStream().collect { sample ->
                        _accelerometerData.value = AccelerometerData(
                            timestamp = System.currentTimeMillis(),
                            x = sample.x / 1000f,
                            y = sample.y / 1000f,
                            z = sample.z / 1000f
                        )
                    }
                } catch (e: Exception) {
                    Log.e(tag, "ACC Stream error: ${e.message}")
                }
            }
        }
    }

    private fun stopStreaming() {
        streamJob?.cancel()
        streamJob = null
        _accelerometerData.value = null
        _heartRateData.value = null
    }
}
