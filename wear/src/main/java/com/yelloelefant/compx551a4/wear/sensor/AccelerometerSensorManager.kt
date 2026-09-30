package com.yelloelefant.compx551a4.wear.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.example.shared.AccelerometerData
import kotlinx.coroutines.channels.awaitClose

/**
 * Manages accelerometer sensor readings on Wear OS / Android devices,
 * exposing sensor data as a Kotlin Coroutines [Flow].
 */
class AccelerometerSensorManager(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    /**
     * Returns a [Flow] emitting [AccelerometerData] updates.
     * Automatically handles background thread execution, sensor registration,
     * and cleanup when the flow collection is cancelled.
     */
    fun getAccelerometerData(): Flow<AccelerometerData> = callbackFlow {
        // Check if the accelerometer sensor is available on the device
        if (accelerometer == null) {
            Log.e("SensorManager", "Accelerometer not available on this device")
            close()
            return@callbackFlow
        }

        // Create and start a dedicated background thread for handling sensor events
        // to prevent blocking the main (UI) thread.
        val handlerThread = HandlerThread("AccelerometerSensorThread").apply { start() }
        val handler = Handler(handlerThread.looper)

        // Define the listener to capture sensor changes
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        // Emit accelerometer values (X, Y, Z axes and timestamp) into the flow
                        trySend(
                            AccelerometerData(
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
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL,
            handler
        )

        // Clean up resources (unregister listener and stop thread) when the flow collector is cancelled or closed
        awaitClose {
            Log.d("SensorManager", "Unregistering accelerometer listener and stopping thread")
            sensorManager.unregisterListener(listener)
            handlerThread.quitSafely()
        }
    }
}
