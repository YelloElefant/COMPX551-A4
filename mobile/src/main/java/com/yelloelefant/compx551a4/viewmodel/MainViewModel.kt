package com.yelloelefant.compx551a4.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.polar.sdk.api.model.PolarDeviceInfo
import com.yelloelefant.compx551a4.data.AccDataPoint
import com.yelloelefant.compx551a4.data.AccelerometerData
import com.yelloelefant.compx551a4.data.HeartRateData
import com.yelloelefant.compx551a4.data.HrDataPoint
import com.yelloelefant.compx551a4.data.HrEventPoint
import com.yelloelefant.compx551a4.data.SessionEntity
import com.yelloelefant.compx551a4.data.SessionRepository
import com.yelloelefant.compx551a4.sensor.PolarSensorManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sqrt

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
    val latestMagnitudeG: Double = 0.0,
    val currentContextAlert: String = "Normal"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sensorManager = PolarSensorManager(application)
    private val sessionRepository = SessionRepository(application)

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

    private val _sessionHistory = MutableStateFlow<List<SessionEntity>>(emptyList())
    val sessionHistory: StateFlow<List<SessionEntity>> = _sessionHistory.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<PolarDeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<PolarDeviceInfo>> = _discoveredDevices.asStateFlow()

    private var scanJob: Job? = null
    private var timerJob: Job? = null

    private val sessionHrBuffer = mutableListOf<HeartRateData>()
    private val sessionAccBuffer = mutableListOf<AccelerometerData>()
    private val sessionEventBuffer = mutableListOf<HrEventPoint>()
    private var sessionStartTimeMs: Long = 0
    private var sessionElapsedTimeSec: Long = 0

    init {
        loadSessions()

        viewModelScope.launch {
            sensorManager.heartRateData.collect { hr ->
                if (hr != null) {
                    val currentList = _hrChartHistory.value.toMutableList()
                    currentList.add(hr)
                    if (currentList.size > 60) currentList.removeAt(0)
                    _hrChartHistory.value = currentList

                    // Real-time context evaluation (Exercise vs Stress)
                    val latestAcc = sessionAccBuffer.lastOrNull() ?: _accChartHistory.value.lastOrNull()
                    val mag = if (latestAcc != null) {
                        sqrt(latestAcc.x.toDouble().pow(2) + latestAcc.y.toDouble().pow(2) + latestAcc.z.toDouble().pow(2))
                    } else 1.0
                    val isMoving = mag >= 1.25

                    val contextAlert = if (hr.bpm > 135) {
                        if (isMoving) "💪 Exercise (High HR + Movement)" else "⚠️ Stress Alert (High HR while Still)"
                    } else {
                        "Normal"
                    }

                    _liveStats.value = _liveStats.value.copy(currentContextAlert = contextAlert)

                    if (_recordingState.value == SessionRecordingState.RECORDING) {
                        sessionHrBuffer.add(hr)

                        // Sensor Fusion Event Detection (> 135 BPM)
                        if (hr.bpm > 135) {
                            val lastEventSec = sessionEventBuffer.lastOrNull()?.sec ?: -10
                            if (sessionElapsedTimeSec.toInt() - lastEventSec > 5) {
                                val label = if (isMoving) {
                                    "Exercise (${hr.bpm} BPM)"
                                } else {
                                    "⚠️ Stress / Rest HR Alert (${hr.bpm} BPM)"
                                }
                                sessionEventBuffer.add(
                                    HrEventPoint(
                                        sec = sessionElapsedTimeSec.toInt(),
                                        bpm = hr.bpm,
                                        label = label
                                    )
                                )
                            }
                        }
                    }
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

                    if (_recordingState.value == SessionRecordingState.RECORDING) {
                        sessionAccBuffer.add(acc)
                    }
                }
            }
        }
    }

    fun connectDevice(deviceId: String = "C6230415") {
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
        if (_recordingState.value == SessionRecordingState.IDLE) {
            sessionHrBuffer.clear()
            sessionAccBuffer.clear()
            sessionEventBuffer.clear()
            sessionStartTimeMs = System.currentTimeMillis()
            sessionElapsedTimeSec = 0
            _recordingState.value = SessionRecordingState.RECORDING
            startTimer()
        }
    }

    fun stopAndSaveSession(title: String, notes: String) {
        timerJob?.cancel()

        val bpms = sessionHrBuffer.map { it.bpm }
        val avgBpm = if (bpms.isNotEmpty()) bpms.average().toInt() else 0
        val minBpm = bpms.minOrNull() ?: 0
        val maxBpm = bpms.maxOrNull() ?: 0

        val sessionTitle = title.ifBlank { "Workout Session ${System.currentTimeMillis() % 10000}" }

        val accPoints = sessionAccBuffer.mapIndexedNotNull { index, sample ->
            if (index % 25 == 0) {
                val mag = sqrt(sample.x.toDouble().pow(2) + sample.y.toDouble().pow(2) + sample.z.toDouble().pow(2))
                AccDataPoint(
                    sec = index / 50,
                    magG = mag
                )
            } else null
        }

        val hrPoints = sessionHrBuffer.mapIndexed { index, sample ->
            HrDataPoint(
                sec = (index * (sessionElapsedTimeSec.toDouble() / sessionHrBuffer.size.coerceAtLeast(1))).toInt(),
                bpm = sample.bpm
            )
        }

        val entity = SessionEntity(
            title = sessionTitle,
            startTimeMs = sessionStartTimeMs,
            endTimeMs = System.currentTimeMillis(),
            durationSeconds = sessionElapsedTimeSec,
            avgBpm = avgBpm,
            minBpm = minBpm,
            maxBpm = maxBpm,
            sampleCount = sessionHrBuffer.size,
            notes = notes,
            hrSeries = hrPoints,
            accSeries = accPoints,
            hrEvents = sessionEventBuffer.toList()
        )

        viewModelScope.launch {
            sessionRepository.saveSession(entity)
            loadSessions()
        }

        _recordingState.value = SessionRecordingState.IDLE
        sessionHrBuffer.clear()
        sessionAccBuffer.clear()
        sessionEventBuffer.clear()
        _liveStats.value = LiveStats()
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            sessionRepository.deleteSession(id)
            loadSessions()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_recordingState.value == SessionRecordingState.RECORDING) {
                delay(1000)
                sessionElapsedTimeSec++
                val bpms = sessionHrBuffer.map { it.bpm }
                val latestAcc = sessionAccBuffer.lastOrNull()
                val latestMag = if (latestAcc != null) {
                    sqrt(latestAcc.x.toDouble().pow(2) + latestAcc.y.toDouble().pow(2) + latestAcc.z.toDouble().pow(2))
                } else 1.0

                _liveStats.value = _liveStats.value.copy(
                    durationSeconds = sessionElapsedTimeSec,
                    sampleCount = sessionHrBuffer.size,
                    avgBpm = if (bpms.isNotEmpty()) bpms.average().toInt() else 0,
                    minBpm = bpms.minOrNull() ?: 0,
                    maxBpm = bpms.maxOrNull() ?: 0,
                    latestBpm = bpms.lastOrNull() ?: 0,
                    latestMagnitudeG = latestMag
                )
            }
        }
    }

    private fun loadSessions() {
        viewModelScope.launch {
            val list = sessionRepository.loadSessions()
            _sessionHistory.value = list
        }
    }

    override fun onCleared() {
        super.onCleared()
        scanJob?.cancel()
        timerJob?.cancel()
    }
}
