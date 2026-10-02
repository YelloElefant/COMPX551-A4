package com.yelloelefant.compx551a4.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.yelloelefant.compx551a4.data.HeartRateData
import com.yelloelefant.compx551a4.sensor.PolarSensorManager
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel managing the Heart Rate & RR Interval sensor state and lifecycle for UI observation.
 */
class HeartRateViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorManager = PolarSensorManager(application)

    /** State flow exposing heart rate and RR interval samples. */
    val heartRateData: StateFlow<HeartRateData?> = sensorManager.heartRateData
    
    /** State flow indicating whether the sensor is currently connected. */
    val isConnected: StateFlow<Boolean> = sensorManager.isConnected

    /** Connects to the heart rate sensor. */
    fun connect() {
        sensorManager.connect()
    }

    /** Disconnects from the heart rate sensor. */
    fun disconnect() {
        sensorManager.disconnect()
    }
}
