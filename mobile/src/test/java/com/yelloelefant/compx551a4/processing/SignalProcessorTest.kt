package com.yelloelefant.compx551a4.processing

import com.yelloelefant.compx551a4.sensor.AccSample
import com.yelloelefant.compx551a4.sensor.HrSample
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalProcessorTest {

    private val processor = SignalProcessor()

    @Test
    fun testProcessHrSample_calculatesMovingAverageAndZone() {
        val sample1 = HrSample(bpm = 70, rrMs = listOf(850), contact = true, timestampMs = 1000L)
        val sample2 = HrSample(bpm = 80, rrMs = listOf(800), contact = true, timestampMs = 2000L)

        val processed1 = processor.processHrSample(sample1)
        assertEquals(70, processed1.smoothedBpm)
        assertEquals(HrZone.WARMUP, processed1.zone)

        val processed2 = processor.processHrSample(sample2)
        assertEquals(75, processed2.smoothedBpm) // avg of 70 and 80
        assertEquals(HrZone.WARMUP, processed2.zone)
    }

    @Test
    fun testProcessAccSample_calculatesVectorMagnitudeInG() {
        // x=0, y=0, z=1000 milli-g -> 1.0g
        val sample = AccSample(x = 0, y = 0, z = 1000)
        val processed = processor.processAccSample(sample)

        assertEquals(1.0, processed.magnitudeG, 0.01)
        assertEquals(MotionCategory.STILL, processed.motionCategory)
    }

    @Test
    fun testProcessAccSample_detectsHighMotion() {
        // x=1500, y=1500, z=1000 milli-g -> ~2.34g
        val sample = AccSample(x = 1500, y = 1500, z = 1000)
        val processed = processor.processAccSample(sample)

        assertTrue(processed.magnitudeG > 2.0)
        assertEquals(MotionCategory.HIGH, processed.motionCategory)
    }
}
