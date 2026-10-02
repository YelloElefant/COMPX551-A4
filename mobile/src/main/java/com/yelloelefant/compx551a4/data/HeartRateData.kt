package com.yelloelefant.compx551a4.data

/**
 * Data class representing raw heart rate and RR interval measurements.
 *
 * @property timestamp Epoch timestamp in milliseconds when the sample was captured.
 * @property bpm Heart rate in beats per minute.
 * @property rrIntervals List of inter-beat (RR) intervals in milliseconds.
 */
data class HeartRateData(
    val timestamp: Long,
    val bpm: Int,
    val rrIntervals: List<Int> = emptyList()
)
