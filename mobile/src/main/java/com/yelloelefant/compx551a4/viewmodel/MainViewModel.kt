package com.yelloelefant.compx551a4.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polar.sdk.api.model.PolarDeviceInfo
import com.yelloelefant.compx551a4.sensor.AccSample
import com.yelloelefant.compx551a4.sensor.ConnectionState
import com.yelloelefant.compx551a4.sensor.HeartSensorSource
import com.yelloelefant.compx551a4.sensor.HrSample
import com.yelloelefant.compx551a4.sensor.MockPolarH10Source
import com.yelloelefant.compx551a4.sensor.PolarH10Source
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

enum class SensorSourceType(val displayName: String) {
    REAL_POLAR_H10("Polar H10 (Bluetooth)"),
    SIMULATOR("Polar H10 (Simulator)")
}

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

    private val prefs = application.getSharedPreferences("polar_prefs", Context.MODE_PRIVATE)

    private val realPolarSource = PolarH10Source(application)
    private val mockPolarSource = MockPolarH10Source()

    private val _sensorSourceType = MutableStateFlow(SensorSourceType.REAL_POLAR_H10)
    val sensorSourceType: StateFlow<SensorSourceType> = _sensorSourceType.asStateFlow()

    private val _deviceId = MutableStateFlow(prefs.getString("device_id", "C38E221A") ?: "C38E221A")
    val deviceId: StateFlow<String> = _deviceId.asStateFlow()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _recordingState = MutableStateFlow(SessionRecordingState.IDLE)
    val recordingState: StateFlow<SessionRecordingState> = _recordingState.asStateFlow()

    private val _liveHrSample = MutableStateFlow<HrSample?>(null)
    val liveHrSample: StateFlow<HrSample?> = _liveHrSample.asStateFlow()

    private val _liveAccSample = MutableStateFlow<AccSample?>(null)
    val liveAccSample: StateFlow<AccSample?> = _liveAccSample.asStateFlow()

    private val _hrChartHistory = MutableStateFlow<List<HrSample>>(emptyList())
    val hrChartHistory: StateFlow<List<HrSample>> = _hrChartHistory.asStateFlow()

    private val _accChartHistory = MutableStateFlow<List<AccSample>>(emptyList())
    val accChartHistory: StateFlow<List<AccSample>> = _accChartHistory.asStateFlow()

    private val _liveStats = MutableStateFlow(LiveStats())
    val liveStats: StateFlow<LiveStats> = _liveStats.asStateFlow()

    private val _simulatorIntensity = MutableStateFlow(MockPolarH10Source.ExerciseIntensity.RESTING)
    val simulatorIntensity: StateFlow<MockPolarH10Source.ExerciseIntensity> = _simulatorIntensity.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<PolarDeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<PolarDeviceInfo>> = _discoveredDevices.asStateFlow()

    private var hrCollectJob: Job? = null
    private var accCollectJob: Job? = null
    private var scanJob: Job? = null
    private var timerJob: Job? = null
    private var connectionStateJob: Job? = null

    private val sessionHrBuffer = mutableListOf<HrSample>()
    private val sessionAccBuffer = mutableListOf<AccSample>()
    private var sessionElapsedTimeSec: Long = 0

    init {
        observeActiveSourceState()
    }

    private fun currentSource(): HeartSensorSource {
        return if (_sensorSourceType.value == SensorSourceType.REAL_POLAR_H10) {
            realPolarSource
        } else {
            mockPolarSource
        }
    }

    private fun observeActiveSourceState() {
        connectionStateJob?.cancel()
        connectionStateJob = viewModelScope.launch {
            currentSource().connectionState.collect { state ->
                _connectionState.value = state
            }
        }
    }

    fun setSensorSourceType(type: SensorSourceType) {
        if (_sensorSourceType.value == type) return
        disconnectDevice()
        _sensorSourceType.value = type
        observeActiveSourceState()
    }

    fun setDeviceId(id: String) {
        val clean = id.uppercase().trim()
        _deviceId.value = clean
        prefs.edit().putString("device_id", clean).apply()
    }

    fun connectDevice() {
        currentSource().connect(_deviceId.value)
        startStreamCollection()
    }

    fun disconnectDevice() {
        hrCollectJob?.cancel()
        accCollectJob?.cancel()
        currentSource().disconnect()
    }

    fun startDeviceScan() {
        if (_isScanning.value) return
        _isScanning.value = true
        _discoveredDevices.value = emptyList()
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            try {
                realPolarSource.searchForDevices().collect { info ->
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

    fun setSimulatorIntensity(intensity: MockPolarH10Source.ExerciseIntensity) {
        _simulatorIntensity.value = intensity
        mockPolarSource.simulateExerciseLevel(intensity)
    }

    private fun startStreamCollection() {
        hrCollectJob?.cancel()
        hrCollectJob = viewModelScope.launch {
            currentSource().hrStream().collect { rawHr ->
                _liveHrSample.value = rawHr

                val currentHrList = _hrChartHistory.value.toMutableList()
                currentHrList.add(rawHr)
                if (currentHrList.size > 60) currentHrList.removeAt(0)
                _hrChartHistory.value = currentHrList

                if (_recordingState.value == SessionRecordingState.RECORDING) {
                    sessionHrBuffer.add(rawHr)
                    updateStats()
                }
            }
        }

        accCollectJob?.cancel()
        accCollectJob = viewModelScope.launch {
            currentSource().accStream().collect { rawAcc ->
                _liveAccSample.value = rawAcc

                val currentAccList = _accChartHistory.value.toMutableList()
                currentAccList.add(rawAcc)
                if (currentAccList.size > 80) currentAccList.removeAt(0)
                _accChartHistory.value = currentAccList

                if (_recordingState.value == SessionRecordingState.RECORDING) {
                    sessionAccBuffer.add(rawAcc)
                }
            }
        }
    }

    fun startLiveSession() {
        if (_recordingState.value == SessionRecordingState.IDLE) {
            sessionHrBuffer.clear()
            sessionAccBuffer.clear()
            sessionElapsedTimeSec = 0
            _recordingState.value = SessionRecordingState.RECORDING
            startTimer()
        }
    }

    fun stopLiveSession() {
        _recordingState.value = SessionRecordingState.IDLE
        timerJob?.cancel()
        sessionHrBuffer.clear()
        sessionAccBuffer.clear()
        _liveStats.value = LiveStats()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_recordingState.value == SessionRecordingState.RECORDING) {
                delay(1000)
                sessionElapsedTimeSec++
                updateStats()
            }
        }
    }

    private fun updateStats() {
        if (sessionHrBuffer.isEmpty()) return
        val bpms = sessionHrBuffer.map { it.bpm }
        val latestAcc = sessionAccBuffer.lastOrNull()
        val latestMag = if (latestAcc != null) {
            sqrt(latestAcc.x.toDouble().pow(2) + latestAcc.y.toDouble().pow(2) + latestAcc.z.toDouble().pow(2)) / 1000.0
        } else 1.0

        _liveStats.value = LiveStats(
            durationSeconds = sessionElapsedTimeSec,
            sampleCount = sessionHrBuffer.size,
            avgBpm = bpms.average().toInt(),
            minBpm = bpms.minOrNull() ?: 0,
            maxBpm = bpms.maxOrNull() ?: 0,
            latestBpm = bpms.last(),
            latestMagnitudeG = latestMag
        )
    }

    override fun onCleared() {
        super.onCleared()
        hrCollectJob?.cancel()
        accCollectJob?.cancel()
        scanJob?.cancel()
        timerJob?.cancel()
        connectionStateJob?.cancel()
        realPolarSource.shutdown()
        mockPolarSource.shutdown()
    }
}
