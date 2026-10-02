package com.yelloelefant.compx551a4.sensor

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Simulated Polar H10 sensor source for testing, Compose previews,
 * and demonstration without requiring a physical strap attached.
 */
class MockPolarH10Source : HeartSensorSource {

    override val name: String = "Polar H10 (Simulator)"
    override val accSampleRateHz: Int = 50
    override val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    private var isConnected = false
    private var baseBpm = 75.0
    private var phase = 0.0

    override fun connect(deviceId: String) {
        val id = deviceId.ifBlank { "DEMO-H10" }.uppercase()
        connectionState.value = ConnectionState.Connecting(id)
        isConnected = true
        connectionState.value = ConnectionState.Connected(id)
    }

    override fun disconnect() {
        isConnected = false
        connectionState.value = ConnectionState.Disconnected
    }

    override fun hrStream(): Flow<HrSample> = flow {
        while (true) {
            if (isConnected) {
                // Simulate natural breathing sinus arrhythmia and slight HR drift
                phase += 0.1
                val breathingVar = sin(phase) * 4.0
                val randomNoise = Random.nextDouble(-2.0, 2.0)
                val currentBpm = (baseBpm + breathingVar + randomNoise).coerceIn(50.0, 180.0).toInt()

                // Calculate realistic RR interval in ms (~60000 / BPM with small beat-to-beat variability)
                val baseRr = (60000.0 / currentBpm).toInt()
                val rrVariation = Random.nextInt(-25, 25)
                val rrMs = baseRr + rrVariation

                emit(
                    HrSample(
                        bpm = currentBpm,
                        rrMs = listOf(rrMs),
                        contact = true,
                        timestampMs = System.currentTimeMillis()
                    )
                )
            }
            delay(1000) // ~1 reading per second like real H10
        }
    }

    override fun accStream(): Flow<AccSample> = flow {
        var stepCounter = 0.0
        while (true) {
            if (isConnected) {
                // 50Hz accelerometer stream
                stepCounter += 0.1
                // Gravity ~ 1000 milli-g on Z + motion oscillation
                val motionX = (sin(stepCounter * 2) * 150 + Random.nextInt(-30, 30)).toInt()
                val motionY = (sin(stepCounter * 2.5) * 200 + Random.nextInt(-40, 40)).toInt()
                val motionZ = (980 + sin(stepCounter) * 100 + Random.nextInt(-20, 20)).toInt()

                emit(AccSample(x = motionX, y = motionY, z = motionZ))
            }
            delay(20) // 50 Hz -> 20ms period
        }
    }

    /**
     * Helper to simulate increased physical activity in simulator mode.
     */
    fun simulateExerciseLevel(intensity: ExerciseIntensity) {
        baseBpm = when (intensity) {
            ExerciseIntensity.RESTING -> 65.0
            ExerciseIntensity.LIGHT -> 95.0
            ExerciseIntensity.MODERATE -> 135.0
            ExerciseIntensity.INTENSE -> 165.0
        }
    }

    override fun shutdown() {
        disconnect()
    }

    enum class ExerciseIntensity {
        RESTING, LIGHT, MODERATE, INTENSE
    }
}
