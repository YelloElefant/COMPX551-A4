package com.yelloelefant.compx551a4.wear.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.AccelerometerData
import com.example.shared.GyroscopeData
import com.yelloelefant.compx551a4.wear.sensor.AccelerometerSensorManager
import com.yelloelefant.compx551a4.wear.sensor.GyroscopeSensorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn

class SensorViewModel (application: Application) : AndroidViewModel(application) {
    private val accelerometerManager = AccelerometerSensorManager(application)
    private val gyroscopeManager = GyroscopeSensorManager(application) 

    val accelerometerData: StateFlow<AccelerometerData> = accelerometerManager
        .getAccelerometerData()
        .sample(250) // Update UI 4 times per second
        .conflate()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AccelerometerData()
        )

    val gyroscopeData: StateFlow<GyroscopeData> = gyroscopeManager
        .getGyroscopeData()
        .sample(250) // Update UI 4 times per second
        .conflate()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GyroscopeData()
        )
}