package com.yelloelefant.compx551a4.sensor

import android.content.Context
import android.util.Log
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarSensorSetting
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow

// This class talks to the REAL Polar H10 strap.
class PolarH10Source(context: Context) : HeartSensorSource {

    override val name = "Polar H10"
    override val accSampleRateHz = 50          // movement readings per second
    override val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)

    private var deviceId = ""                  // the 8-character code on the strap
    private var heartRateReady = false         // Polar tells us when this is ready
    private var movementReady = false          // Polar tells us when this is ready


    // POLAR CONNECTION

    private val polar = PolarBleApiDefaultImpl.defaultImplementation(
        context.applicationContext,
        setOf(
            PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
            PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
        )
    )


    // LISTEN FOR MESSAGES FROM POLAR
    init {
        polar.setApiCallback(object : PolarBleApiCallback() {

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("H10", "Connected!")
                connectionState.value = ConnectionState.Connected(polarDeviceInfo.deviceId)
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d("H10", "Disconnected")
                heartRateReady = false
                movementReady = false
                connectionState.value = ConnectionState.Disconnected
            }

            override fun bleSdkFeatureReady(identifier: String, feature: PolarBleApi.PolarBleSdkFeature) {
                Log.d("H10", "Ready: $feature")
                if (feature == PolarBleApi.PolarBleSdkFeature.FEATURE_HR) heartRateReady = true
                if (feature == PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING) movementReady = true
            }
        })
    }


    // CONNECT TO THE STRAP
    override fun connect(deviceId: String) {
        this.deviceId = deviceId.uppercase()
        connectionState.value = ConnectionState.Connecting(this.deviceId)
        try {
            polar.connectToDevice(this.deviceId)
        } catch (e: Exception) {
            connectionState.value = ConnectionState.Failed("Could not connect: ${e.message}")
        }
    }

    override fun disconnect() {
        try {
            polar.disconnectFromDevice(deviceId)
        } catch (e: Exception) {
            Log.d("H10", "Disconnect failed")
        }
    }


    // HEART RATE READING: about 1 reading per second
    override fun hrStream(): Flow<HrSample> = flow {
        // Wait until Polar says heart rate is ready
        while (!heartRateReady) delay(500)

        // Start receiving, and turn each Polar reading into our own HrSample
        polar.startHrStreaming(deviceId).asFlow().collect { polarData ->
            for (reading in polarData.samples) {
                emit(
                    HrSample(
                        bpm = reading.hr,
                        rrMs = reading.rrsMs,
                        contact = reading.contactStatus || !reading.contactStatusSupported,
                        timestampMs = System.currentTimeMillis(),
                    )
                )
            }
        }
    }


    // MOVEMENT READING: 50 readings per second
    override fun accStream(): Flow<AccSample> = flow {
        // Wait until Polar says movement is ready
        while (!movementReady) delay(500)

        // Settings: 50 readings per second, range ±8 g
        val settings = PolarSensorSetting(
            mapOf(
                PolarSensorSetting.SettingType.SAMPLE_RATE to 50,
                PolarSensorSetting.SettingType.RESOLUTION to 16,
                PolarSensorSetting.SettingType.RANGE to 8,
            )
        )

        // Start receiving, and turn each Polar reading into our own AccSample
        polar.startAccStreaming(deviceId, settings).asFlow().collect { polarData ->
            for (reading in polarData.samples) {
                emit(AccSample(reading.x, reading.y, reading.z))
            }
        }
    }


    // SWITCH EVERYTHING OFF when the app closes
    override fun shutdown() {
        polar.shutDown()
    }
}