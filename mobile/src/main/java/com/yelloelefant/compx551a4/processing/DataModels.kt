package com.yelloelefant.compx551a4.processing

import androidx.compose.ui.graphics.Color

enum class HrZone(
    val title: String,
    val description: String,
    val minBpm: Int,
    val maxBpm: Int,
    val colorHex: String,
) {
    REST("Resting", "Recovery & Rest", 0, 59, "#2196F3"),
    WARMUP("Warmup", "Light Activity", 60, 99, "#009688"),
    FAT_BURN("Fat Burn", "Weight Management", 100, 129, "#4CAF50"),
    AEROBIC("Aerobic", "Cardio Fitness", 130, 154, "#FF9800"),
    ANAEROBIC("Anaerobic", "Hard Training", 155, 174, "#FF5722"),
    PEAK("Peak", "Maximum Effort", 175, 250, "#F44336");

    val color: Color
        get() = Color(android.graphics.Color.parseColor(colorHex))

    companion object {
        fun fromBpm(bpm: Int): HrZone {
            return entries.find { bpm in it.minBpm..it.maxBpm } ?: REST
        }
    }
}

enum class MotionCategory(val label: String, val thresholdMinG: Double, val colorHex: String) {
    STILL("Still / Stationary", 0.0, "#9E9E9E"),
    LIGHT("Light Motion", 1.12, "#4FC3F7"),
    MODERATE("Moderate Motion", 1.35, "#FFB74D"),
    HIGH("High Motion / Impact", 1.80, "#E57373");

    val color: Color
        get() = Color(android.graphics.Color.parseColor(colorHex))

    companion object {
        fun fromMagnitudeG(g: Double): MotionCategory {
            return when {
                g >= HIGH.thresholdMinG -> HIGH
                g >= MODERATE.thresholdMinG -> MODERATE
                g >= LIGHT.thresholdMinG -> LIGHT
                else -> STILL
            }
        }
    }
}

data class ProcessedHrSample(
    val bpm: Int,
    val smoothedBpm: Int,
    val rrMs: Int,
    val rmssd: Double,
    val zone: HrZone,
    val contact: Boolean,
    val timestampMs: Long,
    val isHighHrEvent: Boolean = false,
    val isRrAnomaly: Boolean = false,
)

data class ProcessedAccSample(
    val x: Int,
    val y: Int,
    val z: Int,
    val magnitudeG: Double,
    val motionCategory: MotionCategory,
    val timestampMs: Long,
)

data class LiveSessionStats(
    val durationSeconds: Long = 0,
    val sampleCount: Int = 0,
    val avgBpm: Int = 0,
    val minBpm: Int = 0,
    val maxBpm: Int = 0,
    val latestBpm: Int = 0,
    val latestRmssd: Double = 0.0,
    val avgRmssd: Double = 0.0,
    val latestMotionG: Double = 0.0,
    val maxMotionG: Double = 0.0,
    val zonePercentages: Map<HrZone, Float> = HrZone.entries.associateWith { 0f },
    val motionPercentages: Map<MotionCategory, Float> = MotionCategory.entries.associateWith { 0f },
    val eventCount: Int = 0,
)
