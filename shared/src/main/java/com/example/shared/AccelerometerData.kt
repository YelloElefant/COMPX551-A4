package com.example.shared

/**
 * Data class representing accelerometer sensor readings along three spatial axes (X, Y, Z)
 * along with the sensor event timestamp.
 */
data class AccelerometerData(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val timestamp: Long = 0L
)
