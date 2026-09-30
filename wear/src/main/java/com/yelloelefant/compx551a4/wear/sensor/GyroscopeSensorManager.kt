package com.yelloelefant.compx551a4.wear.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.example.shared.GyroscopeData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class GyroscopeSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    fun getGyroscopeData(): Flow<GyroscopeData> = callbackFlow {
        if (gyroscope == null) {
            Log.e("SensorManager", "Gyroscope not available on this device")
            close()
            return@callbackFlow
        }

        // Create and start a dedicated background thread for handling sensor events
        // to prevent blocking the main (UI) thread.
        val handlerThread = HandlerThread("GyroscopeSensorThread").apply { start() }
        val handler = Handler(handlerThread.looper)

        // Define the listener to capture sensor changes
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                        // Emit accelerometer values (X, Y, Z axes and timestamp) into the flow
                        trySend(
                            GyroscopeData(
                                x = it.values[0],
                                y = it.values[1],
                                z = it.values[2],
                                timestamp = it.timestamp
                            )
                        )
                    }
                }
            }

            // Required interface callback; left empty as accuracy tracking is not needed for raw accelerometer streaming
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // Register the sensor listener with normal sample rate and background handler
        sensorManager.registerListener(
            listener,
            gyroscope,
            SensorManager.SENSOR_DELAY_NORMAL,
            handler
        )

        awaitClose {
            Log.d("SensorManager", "Unregistering gyroscope listener and stopping thread")
            sensorManager.unregisterListener(listener)
            handlerThread.quitSafely()
        }
    }
}