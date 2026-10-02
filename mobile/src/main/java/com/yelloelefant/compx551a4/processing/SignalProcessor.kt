package com.yelloelefant.compx551a4.processing

import com.yelloelefant.compx551a4.sensor.AccSample
import com.yelloelefant.compx551a4.sensor.HrSample
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Advanced Signal Processor for Polar H10 Streams.
 *
 * Implements:
 * 1. Exponential Moving Average (EMA) / Moving Window Smoothing for Heart Rate.
 * 2. Root Mean Square of Successive Differences (RMSSD) calculation for Heart Rate Variability (HRV).
 * 3. 3-Axis Accelerometer Vector Magnitude calculation in milli-g/g force ($M = \sqrt{x^2+y^2+z^2}$).
 * 4. Physiological HR Zone classification & Motion Category detection.
 * 5. Event/Anomaly detection (High HR threshold, RR ectopic anomaly, Motion intensity spike).
 */
class SignalProcessor {

    private val bpmHistory = ArrayDeque<Int>()
    private val rrHistory = ArrayDeque<Int>()
    private val maxHistoryWindow = 10

    /**
     * Processes raw [HrSample] from Polar H10.
     */
    fun processHrSample(sample: HrSample): ProcessedHrSample {
        val rawBpm = sample.bpm.coerceIn(30, 240)

        // 1. Moving average smoothing
        bpmHistory.addLast(rawBpm)
        if (bpmHistory.size > maxHistoryWindow) bpmHistory.removeFirst()
        val smoothedBpm = (bpmHistory.sum().toDouble() / bpmHistory.size).toInt()

        // 2. RR intervals and RMSSD calculation
        var latestRr = sample.rrMs.firstOrNull() ?: (60000 / rawBpm)
        if (latestRr <= 0) latestRr = 60000 / rawBpm

        rrHistory.addLast(latestRr)
        if (rrHistory.size > 25) rrHistory.removeFirst()

        val rmssd = calculateRmssd(rrHistory.toList())

        // 3. Zone and event detection
        val zone = HrZone.fromBpm(smoothedBpm)
        val isHighHr = smoothedBpm > 165
        val isRrAnomaly = isRrAnomalyDetected(latestRr, rrHistory.toList())

        return ProcessedHrSample(
            bpm = rawBpm,
            smoothedBpm = smoothedBpm,
            rrMs = latestRr,
            rmssd = rmssd,
            zone = zone,
            contact = sample.contact,
            timestampMs = sample.timestampMs,
            isHighHrEvent = isHighHr,
            isRrAnomaly = isRrAnomaly
        )
    }

    /**
     * Processes raw [AccSample] from Polar H10.
     */
    fun processAccSample(sample: AccSample): ProcessedAccSample {
        // Calculate 3D Vector Magnitude in g-force (sample x, y, z are in milli-g)
        val magMilliG = sqrt(sample.x.toDouble().pow(2) + sample.y.toDouble().pow(2) + sample.z.toDouble().pow(2))
        val magG = magMilliG / 1000.0

        val category = MotionCategory.fromMagnitudeG(magG)

        return ProcessedAccSample(
            x = sample.x,
            y = sample.y,
            z = sample.z,
            magnitudeG = magG,
            motionCategory = category,
            timestampMs = System.currentTimeMillis()
        )
    }

    /**
     * Calculates RMSSD (Root Mean Square of Successive Differences) for HRV in milliseconds:
     * RMSSD = sqrt( 1/(N-1) * sum( (RR_{i+1} - RR_i)^2 ) )
     */
    private fun calculateRmssd(rrs: List<Int>): Double {
        if (rrs.size < 2) return 0.0
        var sumSquaredDiffs = 0.0
        for (i in 0 until rrs.size - 1) {
            val diff = (rrs[i + 1] - rrs[i]).toDouble()
            sumSquaredDiffs += diff * diff
        }
        val meanSquaredDiff = sumSquaredDiffs / (rrs.size - 1)
        return sqrt(meanSquaredDiff)
    }

    private fun isRrAnomalyDetected(currentRr: Int, history: List<Int>): Boolean {
        if (history.size < 5) return false
        val avgRr = history.average()
        val percentDiff = kotlin.math.abs(currentRr - avgRr) / avgRr
        return percentDiff > 0.35 // >35% sudden shift
    }

    /**
     * Recomputes overall session statistics from accumulated processed samples.
     */
    fun computeSessionStats(
        hrSamples: List<ProcessedHrSample>,
        accSamples: List<ProcessedAccSample>,
        durationSeconds: Long,
    ): LiveSessionStats {
        if (hrSamples.isEmpty()) {
            return LiveSessionStats(durationSeconds = durationSeconds)
        }

        val bpms = hrSamples.map { it.smoothedBpm }
        val rmssds = hrSamples.map { it.rmssd }.filter { it > 0 }
        val accMags = accSamples.map { it.magnitudeG }

        val avgBpm = bpms.average().toInt()
        val minBpm = bpms.minOrNull() ?: 0
        val maxBpm = bpms.maxOrNull() ?: 0
        val latestBpm = hrSamples.last().smoothedBpm

        val latestRmssd = hrSamples.last().rmssd
        val avgRmssd = if (rmssds.isNotEmpty()) rmssds.average() else 0.0

        val latestMotionG = accSamples.lastOrNull()?.magnitudeG ?: 0.0
        val maxMotionG = accMags.maxOrNull() ?: 0.0

        // HR Zone distribution
        val totalHrCount = hrSamples.size.toFloat()
        val zoneCounts = hrSamples.groupingBy { it.zone }.eachCount()
        val zonePercentages = HrZone.entries.associateWith { zone ->
            ((zoneCounts[zone] ?: 0) / totalHrCount) * 100f
        }

        // Motion Category distribution
        val totalAccCount = accSamples.size.coerceAtLeast(1).toFloat()
        val motionCounts = accSamples.groupingBy { it.motionCategory }.eachCount()
        val motionPercentages = MotionCategory.entries.associateWith { cat ->
            ((motionCounts[cat] ?: 0) / totalAccCount) * 100f
        }

        val eventCount = hrSamples.count { it.isHighHrEvent || it.isRrAnomaly }

        return LiveSessionStats(
            durationSeconds = durationSeconds,
            sampleCount = hrSamples.size,
            avgBpm = avgBpm,
            minBpm = minBpm,
            maxBpm = maxBpm,
            latestBpm = latestBpm,
            latestRmssd = latestRmssd,
            avgRmssd = avgRmssd,
            latestMotionG = latestMotionG,
            maxMotionG = maxMotionG,
            zonePercentages = zonePercentages,
            motionPercentages = motionPercentages,
            eventCount = eventCount
        )
    }
}
