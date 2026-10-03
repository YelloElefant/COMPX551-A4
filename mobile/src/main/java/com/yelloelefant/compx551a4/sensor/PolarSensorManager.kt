package com.yelloelefant.compx551a4.sensor

import android.content.Context
import android.util.Log
import com.polar.sdk.api.model.PolarDeviceInfo
import com.yelloelefant.compx551a4.data.AccelerometerData
import com.yelloelefant.compx551a4.data.HeartRateData
import com.yelloelefant.compx551a4.processing.LowPassFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
        Log.d(tag, "PolarSensorManager initialized for live Bluetooth sensor.")
    }

    fun connect(deviceId: String = "C6230415") {
        Log.d(tag, "Connecting to physical Polar H10 device: $deviceId")
        _isConnected.value = true
        startRealPolarStreaming(deviceId)
    }

    fun disconnect(deviceId: String = "") {
        Log.d(tag, "Disconnecting sensor")
        _isConnected.value = false
        realPolarSource.disconnect()
        stopStreaming()
    }

    fun searchForDevices(): Flow<PolarDeviceInfo> {
        return realPolarSource.searchForDevices()
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
                    val accFilter = LowPassFilter(0.2f)
                    realPolarSource.accStream().collect { sample ->
                        val (smoothedX, smoothedY, smoothedZ) = accFilter.filter(
                            sample.x / 1000f,
                            sample.y / 1000f,
                            sample.z / 1000f
                        )
                        _accelerometerData.value = AccelerometerData(
                            timestamp = System.currentTimeMillis(),
                            x = smoothedX,
                            y = smoothedY,
                            z = smoothedZ
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
