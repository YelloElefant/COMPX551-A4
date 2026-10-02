package com.yelloelefant.compx551a4.data

import java.util.UUID

data class HrDataPoint(
    val sec: Int,
    val bpm: Int
)

data class AccDataPoint(
    val sec: Int,
    val magG: Double
)

data class SessionEntity(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationSeconds: Long,
    val avgBpm: Int,
    val minBpm: Int,
    val maxBpm: Int,
    val sampleCount: Int,
    val notes: String = "",
    val hrSeries: List<HrDataPoint> = emptyList(),
    val accSeries: List<AccDataPoint> = emptyList()
)
