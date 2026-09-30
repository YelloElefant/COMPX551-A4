package com.example.shared

data class GyroscopeData(
    val x: Float = 0f, // Angular speed around the X axis (rad/s)
    val y: Float = 0f, // Angular speed around the Y axis (rad/s)
    val z: Float = 0f, // Angular speed around the Z axis (rad/s)
    val timestamp: Long = 0L // Sensor event timestamp in nanoseconds
)