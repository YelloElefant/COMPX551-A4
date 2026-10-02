package com.yelloelefant.compx551a4.sensor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

// One heartbeat reading (the H10 sends about one per second)
data class HrSample(
    // For heart rate
    val bpm: Int,
    // time between beats in milliseconds,
    val rrMs: List<Int>,
    // is the strap touching skin?
    val contact: Boolean,
    // when it arrived
    val timestampMs: Long,
)

// one movement reading (50 per second), in milli-g
data class AccSample(val x: Int, val y: Int, val z: Int)

// For Noting whether the strap is connected or not
sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data class Connecting(val deviceId: String) : ConnectionState
    data class Connected(val deviceId: String) : ConnectionState
    data class Failed(val message: String) : ConnectionState
}

interface HeartSensorSource {
    val name: String
    val connectionState: StateFlow<ConnectionState>
    val accSampleRateHz: Int
    fun connect(deviceId: String)
    fun disconnect()
    // a never-ending stream of heartbeat note cards
    fun hrStream(): Flow<HrSample>
    // a never-ending stream of movement note cards
    fun accStream(): Flow<AccSample>
    fun shutdown()
}