package com.yelloelefant.compx551a4.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polar.sdk.api.model.PolarDeviceInfo
import com.yelloelefant.compx551a4.data.AccelerometerData
import com.yelloelefant.compx551a4.data.HeartRateData
import com.yelloelefant.compx551a4.sensor.PolarSensorManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SessionRecordingState {
    IDLE, RECORDING
}

data class LiveStats(
    val durationSeconds: Long = 0,
    val sampleCount: Int = 0,
    val avgBpm: Int = 0,
    val minBpm: Int = 0,
    val maxBpm: Int = 0,
    val latestBpm: Int = 0,
    val latestMagnitudeG: Double = 0.0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = PolarSensorManager(application)

    val isConnected: StateFlow<Boolean> = sensorManager.isConnected
    val heartRateData: StateFlow<HeartRateData?> = sensorManager.heartRateData
    val accelerometerData: StateFlow<AccelerometerData?> = sensorManager.accelerometerData

    private val _recordingState = MutableStateFlow(SessionRecordingState.IDLE)
    val recordingState: StateFlow<SessionRecordingState> = _recordingState.asStateFlow()

    private val _hrChartHistory = MutableStateFlow<List<HeartRateData>>(emptyList())
    val hrChartHistory: StateFlow<List<HeartRateData>> = _hrChartHistory.asStateFlow()

    private val _accChartHistory = MutableStateFlow<List<AccelerometerData>>(emptyList())
    val accChartHistory: StateFlow<List<AccelerometerData>> = _accChartHistory.asStateFlow()

    private val _liveStats = MutableStateFlow(LiveStats())
    val liveStats: StateFlow<LiveStats> = _liveStats.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<PolarDeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<PolarDeviceInfo>> = _discoveredDevices.asStateFlow()

    private var scanJob: Job? = null

    init {
        viewModelScope.launch {
            sensorManager.heartRateData.collect { hr ->
                if (hr != null) {
                    val currentList = _hrChartHistory.value.toMutableList()
                    currentList.add(hr)
                    if (currentList.size > 60) currentList.removeAt(0)
                    _hrChartHistory.value = currentList
                }
            }
        }
        viewModelScope.launch {
            sensorManager.accelerometerData.collect { acc ->
                if (acc != null) {
                    val currentList = _accChartHistory.value.toMutableList()
                    currentList.add(acc)
                    if (currentList.size > 80) currentList.removeAt(0)
                    _accChartHistory.value = currentList
                }
            }
        }
    }

    fun connectDevice(deviceId: String = "C38E221A") {
        sensorManager.connect(deviceId)
    }

    fun disconnectDevice() {
        sensorManager.disconnect()
    }

    fun startDeviceScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        _discoveredDevices.value = emptyList()
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            try {
                sensorManager.searchForDevices().collect { info ->
                    val current = _discoveredDevices.value.toMutableList()
                    if (current.none { it.deviceId == info.deviceId }) {
                        current.add(info)
                        _discoveredDevices.value = current
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun stopDeviceScan() {
        scanJob?.cancel()
        _isScanning.value = false
    }

    fun startLiveSession() {
        _recordingState.value = SessionRecordingState.RECORDING
    }

    fun stopLiveSession() {
        _recordingState.value = SessionRecordingState.IDLE
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
    }
}
