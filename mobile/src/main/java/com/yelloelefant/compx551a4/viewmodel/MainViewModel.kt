package com.yelloelefant.compx551a4.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yelloelefant.compx551a4.data.AccDataPoint
import com.yelloelefant.compx551a4.data.HrDataPoint
import com.yelloelefant.compx551a4.data.SessionEntity
import com.yelloelefant.compx551a4.data.SessionRepository
import com.yelloelefant.compx551a4.data.TrendStats
import com.yelloelefant.compx551a4.processing.LiveSessionStats
import com.yelloelefant.compx551a4.processing.ProcessedAccSample
import com.yelloelefant.compx551a4.processing.ProcessedHrSample
import com.yelloelefant.compx551a4.processing.SignalProcessor
import com.yelloelefant.compx551a4.sensor.ConnectionState
import com.yelloelefant.compx551a4.sensor.HeartSensorSource
import com.yelloelefant.compx551a4.sensor.MockPolarH10Source
import com.yelloelefant.compx551a4.sensor.PolarH10Source
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SensorSourceType(val displayName: String) {
    REAL_POLAR_H10("Polar H10 (Bluetooth)"),
    SIMULATOR("Polar H10 (Simulator)")
}

enum class SessionRecordingState {
    IDLE, RECORDING, PAUSED
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("polar_prefs", Context.MODE_PRIVATE)
    private val repository = SessionRepository(application)
    private val signalProcessor = SignalProcessor()

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

    private val _liveHrSample = MutableStateFlow<ProcessedHrSample?>(null)
    val liveHrSample: StateFlow<ProcessedHrSample?> = _liveHrSample.asStateFlow()

    private val _liveAccSample = MutableStateFlow<ProcessedAccSample?>(null)
    val liveAccSample: StateFlow<ProcessedAccSample?> = _liveAccSample.asStateFlow()

    private val _hrChartHistory = MutableStateFlow<List<ProcessedHrSample>>(emptyList())
    val hrChartHistory: StateFlow<List<ProcessedHrSample>> = _hrChartHistory.asStateFlow()

    private val _accChartHistory = MutableStateFlow<List<ProcessedAccSample>>(emptyList())
    val accChartHistory: StateFlow<List<ProcessedAccSample>> = _accChartHistory.asStateFlow()

    private val _liveStats = MutableStateFlow(LiveSessionStats())
    val liveStats: StateFlow<LiveSessionStats> = _liveStats.asStateFlow()

    private val _sessionHistory = MutableStateFlow<List<SessionEntity>>(emptyList())
    val sessionHistory: StateFlow<List<SessionEntity>> = _sessionHistory.asStateFlow()

    private val _selectedSession = MutableStateFlow<SessionEntity?>(null)
    val selectedSession: StateFlow<SessionEntity?> = _selectedSession.asStateFlow()

    private val _trendStats = MutableStateFlow(TrendStats())
    val trendStats: StateFlow<TrendStats> = _trendStats.asStateFlow()

    private val _simulatorIntensity = MutableStateFlow(MockPolarH10Source.ExerciseIntensity.RESTING)
    val simulatorIntensity: StateFlow<MockPolarH10Source.ExerciseIntensity> = _simulatorIntensity.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<com.polar.sdk.api.model.PolarDeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<com.polar.sdk.api.model.PolarDeviceInfo>> = _discoveredDevices.asStateFlow()

    private var hrCollectJob: Job? = null
    private var accCollectJob: Job? = null
    private var scanJob: Job? = null
    private var timerJob: Job? = null
    private var connectionStateJob: Job? = null

    // Session accumulation buffers
    private val sessionHrBuffer = mutableListOf<ProcessedHrSample>()
    private val sessionAccBuffer = mutableListOf<ProcessedAccSample>()
    private var sessionStartTimeMs: Long = 0
    private var sessionElapsedTimeSec: Long = 0

    init {
        loadSessionData()
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

    fun setSimulatorIntensity(intensity: MockPolarH10Source.ExerciseIntensity) {
        _simulatorIntensity.value = intensity
        mockPolarSource.simulateExerciseLevel(intensity)
    }

    private fun startStreamCollection() {
        hrCollectJob?.cancel()
        hrCollectJob = viewModelScope.launch {
            currentSource().hrStream().collect { rawHr ->
                val processed = signalProcessor.processHrSample(rawHr)
                _liveHrSample.value = processed

                // Chart rolling buffer (60 items)
                val currentHrList = _hrChartHistory.value.toMutableList()
                currentHrList.add(processed)
                if (currentHrList.size > 60) currentHrList.removeAt(0)
                _hrChartHistory.value = currentHrList

                if (_recordingState.value == SessionRecordingState.RECORDING) {
                    sessionHrBuffer.add(processed)
                    updateSessionStats()
                }
            }
        }

        accCollectJob?.cancel()
        accCollectJob = viewModelScope.launch {
            currentSource().accStream().collect { rawAcc ->
                val processed = signalProcessor.processAccSample(rawAcc)
                _liveAccSample.value = processed

                // Chart rolling buffer (80 items)
                val currentAccList = _accChartHistory.value.toMutableList()
                currentAccList.add(processed)
                if (currentAccList.size > 80) currentAccList.removeAt(0)
                _accChartHistory.value = currentAccList

                if (_recordingState.value == SessionRecordingState.RECORDING) {
                    sessionAccBuffer.add(processed)
                }
            }
        }
    }

    fun startSession() {
        if (_recordingState.value == SessionRecordingState.IDLE) {
            sessionHrBuffer.clear()
            sessionAccBuffer.clear()
            sessionStartTimeMs = System.currentTimeMillis()
            sessionElapsedTimeSec = 0
            _recordingState.value = SessionRecordingState.RECORDING
            startTimer()
        }
    }

    fun pauseSession() {
        if (_recordingState.value == SessionRecordingState.RECORDING) {
            _recordingState.value = SessionRecordingState.PAUSED
            timerJob?.cancel()
        }
    }

    fun resumeSession() {
        if (_recordingState.value == SessionRecordingState.PAUSED) {
            _recordingState.value = SessionRecordingState.RECORDING
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_recordingState.value == SessionRecordingState.RECORDING) {
                delay(1000)
                sessionElapsedTimeSec++
                updateSessionStats()
            }
        }
    }

    private fun updateSessionStats() {
        val stats = signalProcessor.computeSessionStats(
            hrSamples = sessionHrBuffer,
            accSamples = sessionAccBuffer,
            durationSeconds = sessionElapsedTimeSec
        )
        _liveStats.value = stats
    }

    fun stopAndSaveSession(title: String, notes: String) {
        val endTimeMs = System.currentTimeMillis()
        timerJob?.cancel()

        val finalStats = signalProcessor.computeSessionStats(
            hrSamples = sessionHrBuffer,
            accSamples = sessionAccBuffer,
            durationSeconds = sessionElapsedTimeSec
        )

        // Sample down HR and Acc series for compact historical charts
        val hrPoints = sessionHrBuffer.mapIndexedNotNull { index, sample ->
            if (index % 2 == 0) {
                HrDataPoint(
                    sec = (index * (sessionElapsedTimeSec.toDouble() / sessionHrBuffer.size.coerceAtLeast(1))).toInt(),
                    bpm = sample.smoothedBpm,
                    rmssd = sample.rmssd
                )
            } else null
        }

        val accPoints = sessionAccBuffer.mapIndexedNotNull { index, sample ->
            if (index % 25 == 0) { // sample 2 per second from 50Hz stream
                AccDataPoint(
                    sec = (index / 50),
                    magG = sample.magnitudeG
                )
            } else null
        }

        val sessionTitle = title.ifBlank { "Session ${System.currentTimeMillis() % 10000}" }

        val entity = SessionEntity(
            title = sessionTitle,
            startTimeMs = sessionStartTimeMs,
            endTimeMs = endTimeMs,
            durationSeconds = sessionElapsedTimeSec,
            avgBpm = finalStats.avgBpm,
            minBpm = finalStats.minBpm,
            maxBpm = finalStats.maxBpm,
            avgRmssd = finalStats.avgRmssd,
            maxMotionG = finalStats.maxMotionG,
            eventCount = finalStats.eventCount,
            zoneDistribution = finalStats.zonePercentages,
            motionDistribution = finalStats.motionPercentages,
            notes = notes,
            hrSeries = hrPoints,
            accSeries = accPoints
        )

        viewModelScope.launch {
            repository.saveSession(entity)
            loadSessionData()
        }

        _recordingState.value = SessionRecordingState.IDLE
        sessionHrBuffer.clear()
        sessionAccBuffer.clear()
        _liveStats.value = LiveSessionStats()
    }

    fun selectSession(session: SessionEntity?) {
        _selectedSession.value = session
    }

    fun deleteSession(id: String) {
        viewModelScope.launch {
            repository.deleteSession(id)
            if (_selectedSession.value?.id == id) {
                _selectedSession.value = null
            }
            loadSessionData()
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedSession.value = null
            loadSessionData()
        }
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            val list = repository.loadSessions()
            _sessionHistory.value = list
            _trendStats.value = repository.computeTrendStats(list)
        }
    }

    override fun onCleared() {
        super.onCleared()
        hrCollectJob?.cancel()
        accCollectJob?.cancel()
        timerJob?.cancel()
        connectionStateJob?.cancel()
        realPolarSource.shutdown()
        mockPolarSource.shutdown()
    }
}
