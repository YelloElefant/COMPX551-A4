package com.yelloelefant.compx551a4.data

/**
 * Data class representing raw 3-axis accelerometer sensor measurements.
 *
 * @property timestamp Epoch timestamp in milliseconds when the sample was captured.
 * @property x Acceleration along the X axis (g).
 * @property y Acceleration along the Y axis (g).
 * @property z Acceleration along the Z axis (g).
 */
data class AccelerometerData(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float
)
