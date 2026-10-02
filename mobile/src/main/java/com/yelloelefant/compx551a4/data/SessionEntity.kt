package com.yelloelefant.compx551a4.data

import com.yelloelefant.compx551a4.processing.HrZone
import com.yelloelefant.compx551a4.processing.MotionCategory
import java.util.UUID

data class HrDataPoint(
    val sec: Int,
    val bpm: Int,
    val rmssd: Double,
)

data class AccDataPoint(
    val sec: Int,
    val magG: Double,
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
    val avgRmssd: Double,
    val maxMotionG: Double,
    val eventCount: Int,
    val zoneDistribution: Map<HrZone, Float>,
    val motionDistribution: Map<MotionCategory, Float>,
    val notes: String = "",
    val hrSeries: List<HrDataPoint> = emptyList(),
    val accSeries: List<AccDataPoint> = emptyList(),
)

data class TrendStats(
    val totalSessions: Int = 0,
    val totalDurationSeconds: Long = 0,
    val overallAvgBpm: Int = 0,
    val peakBpm: Int = 0,
    val overallAvgRmssd: Double = 0.0,
    val totalEventsDetected: Int = 0,
)
