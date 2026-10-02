package com.yelloelefant.compx551a4.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yelloelefant.compx551a4.data.AccelerometerData
import com.yelloelefant.compx551a4.sensor.PolarSensorManager
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel managing the Accelerometer sensor state and lifecycle for UI observation.
 */
class AccelerometerViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorManager = PolarSensorManager(application)

    /** State flow exposing raw accelerometer data samples. */
    val accelerometerData: StateFlow<AccelerometerData?> = sensorManager.accelerometerData
    
    /** State flow indicating whether the sensor is currently connected. */
    val isConnected: StateFlow<Boolean> = sensorManager.isConnected

    /** Connects to the accelerometer sensor. */
    fun connect() {
        sensorManager.connect()
    }

    /** Disconnects from the accelerometer sensor. */
    fun disconnect() {
        sensorManager.disconnect()
    }
}
