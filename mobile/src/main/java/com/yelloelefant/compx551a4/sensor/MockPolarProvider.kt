package com.yelloelefant.compx551a4.sensor

import com.yelloelefant.compx551a4.data.AccelerometerData
import com.yelloelefant.compx551a4.data.HeartRateData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.sin
import kotlin.random.Random

/**
 * Provides simulated live data streams for Accelerometer and Heart Rate
 * to enable testing on macOS, emulators, or devices without a physical Polar H10.
 */
object MockPolarProvider {

    /**
     * Emits continuous mock 3-axis accelerometer data streams at ~20Hz (every 50ms).
     */
    fun accelerometerStream(): Flow<AccelerometerData> = flow {
        var counter = 0f
        while (true) {
            delay(50) // 20 updates per second
            counter += 0.2f
            val timestamp = System.currentTimeMillis()
            val x = sin(counter) * 1.5f + Random.nextFloat() * 0.1f
            val y = kotlin.math.cos(counter) * 1.5f + Random.nextFloat() * 0.1f
            val z = 9.81f + Random.nextFloat() * 0.2f
            emit(AccelerometerData(timestamp, x, y, z))
        }
    }

    /**
     * Emits continuous mock heart rate and RR interval data streams at 1Hz (every 1000ms).
     */
    fun heartRateStream(): Flow<HeartRateData> = flow {
        var baseBpm = 70
        while (true) {
            delay(1000) // 1 update per second
            baseBpm += Random.nextInt(-2, 3)
            baseBpm = baseBpm.coerceIn(60, 120)
            
            // Calculate simulated RR interval in milliseconds based on current BPM
            val intervalMs = (60000 / baseBpm) + Random.nextInt(-30, 31)
            val rrIntervals = listOf(intervalMs)

            emit(HeartRateData(System.currentTimeMillis(), baseBpm, rrIntervals))
        }
    }
}
