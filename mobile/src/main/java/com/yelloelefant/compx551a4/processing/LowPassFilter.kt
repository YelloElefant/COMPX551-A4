package com.yelloelefant.compx551a4.processing

/**
 * Exponential Moving Average (EMA) Low-Pass Filter for smoothing noisy sensor data
 * such as 3-axis accelerometer readings.
 */
class LowPassFilter(private val alpha: Float = 0.2f) {
    private var filteredX: Float = 0f
    private var filteredY: Float = 0f
    private var filteredZ: Float = 0f
    private var initialized = false

    fun filter(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
        if (!initialized) {
            filteredX = x
            filteredY = y
            filteredZ = z
            initialized = true
        } else {
            filteredX = alpha * x + (1 - alpha) * filteredX
            filteredY = alpha * y + (1 - alpha) * filteredY
            filteredZ = alpha * z + (1 - alpha) * filteredZ
        }
        return Triple(filteredX, filteredY, filteredZ)
    }

    fun reset() {
        initialized = false
    }
}
