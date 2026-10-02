package com.yelloelefant.compx551a4.sensor

import android.content.Context
import android.util.Log
import com.yelloelefant.compx551a4.data.AccelerometerData
import com.yelloelefant.compx551a4.data.AppConfig
import com.yelloelefant.compx551a4.data.HeartRateData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages the connection lifecycle and sensor data streams from the Polar H10 chest strap
 * (or falls back to [MockPolarProvider] when [AppConfig.USE_MOCK_DATA] is enabled).
 */
class PolarSensorManager(context: Context) {

    private val tag = "PolarSensorManager"

    // Connection state flow
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Accelerometer sensor data stream
    private val _accelerometerData = MutableStateFlow<AccelerometerData?>(null)
    val accelerometerData: StateFlow<AccelerometerData?> = _accelerometerData.asStateFlow()

    // Heart rate & RR interval sensor data stream
    private val _heartRateData = MutableStateFlow<HeartRateData?>(null)
    val heartRateData: StateFlow<HeartRateData?> = _heartRateData.asStateFlow()

    private var mockJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    init {
        Log.d(tag, "PolarSensorManager initialized. Mock mode: ${AppConfig.USE_MOCK_DATA}")
    }

    /**
     * Connects to the sensor device or starts mock streaming.
     */
    fun connect(deviceId: String = "") {
        Log.d(tag, "Connecting sensor (Mock mode: ${AppConfig.USE_MOCK_DATA})")
        _isConnected.value = true
        startMockStreaming()
    }

    /**
     * Disconnects from the sensor device and halts streaming.
     */
    fun disconnect(deviceId: String = "") {
        Log.d(tag, "Disconnecting sensor")
        _isConnected.value = false
        stopMockStreaming()
    }

    /**
     * Starts collecting mock ACC and HR flows.
     */
    private fun startMockStreaming() {
        mockJob?.cancel()
        mockJob = scope.launch {
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

    /**
     * Stops mock data collection and resets sensor states.
     */
    private fun stopMockStreaming() {
        mockJob?.cancel()
        mockJob = null
        _accelerometerData.value = null
        _heartRateData.value = null
    }
}
