package com.yelloelefant.compx551a4.data

import java.util.UUID

data class HrDataPoint(
    val sec: Int = 0,
    val bpm: Int = 0
)

data class AccDataPoint(
    val sec: Int = 0,
    val magG: Double = 0.0
)

data class HrEventPoint(
    val sec: Int = 0,
    val bpm: Int = 0,
    val label: String = "High HR Alert"
)

data class SessionEntity(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Workout Session",
    val startTimeMs: Long = 0L,
    val endTimeMs: Long = 0L,
    val durationSeconds: Long = 0L,
    val avgBpm: Int = 0,
    val minBpm: Int = 0,
    val maxBpm: Int = 0,
    val sampleCount: Int = 0,
    val notes: String = "",
    val hrSeries: List<HrDataPoint> = emptyList(),
    val accSeries: List<AccDataPoint> = emptyList(),
    val hrEvents: List<HrEventPoint> = emptyList()
)
