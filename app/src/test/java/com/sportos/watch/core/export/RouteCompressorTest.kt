package com.sportos.watch.core.export

import com.sportos.watch.core.util.FormatUtils
import com.sportos.watch.sports.running.GpsLocationPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class RouteCompressorTest {

    @Test
    fun testEmptyRoute() {
        val encoded = RouteCompressor.encode(emptyList())
        assertEquals("", encoded)
        val decoded = RouteCompressor.decode("")
        assertTrue(decoded.isEmpty())
    }

    @Test
    fun testRoundtripFidelityAndCompression() {
        val originalPoints = listOf(
            GpsLocationPoint(37.77490, -122.41940, 42.0, 1000L),
            GpsLocationPoint(37.77495, -122.41935, 42.5, 2000L),
            GpsLocationPoint(37.77510, -122.41920, 43.0, 3000L),
            GpsLocationPoint(37.77530, -122.41900, 44.2, 4000L),
            GpsLocationPoint(37.77555, -122.41870, 45.1, 5000L)
        )

        val encoded = RouteCompressor.encode(originalPoints)
        assertTrue(encoded.isNotEmpty())

        val decoded = RouteCompressor.decode(encoded)
        assertEquals(originalPoints.size, decoded.size)

        for (i in originalPoints.indices) {
            val orig = originalPoints[i]
            val dec = decoded[i]
            // Precision ~ 1e-5 degrees (approx 1 meter)
            assertEquals(orig.latitude, dec.latitude, 0.00002)
            assertEquals(orig.longitude, dec.longitude, 0.00002)
            assertEquals(orig.altitudeMeters, dec.altitudeMeters, 0.15)
            assertEquals(orig.timestampMs, dec.timestampMs)
        }
    }

    @Test
    fun testFormatUtils() {
        assertEquals("00:00", FormatUtils.fastFormatDuration(0))
        assertEquals("05:22", FormatUtils.fastFormatDuration(322_000))
        assertEquals("1:05:22", FormatUtils.fastFormatDuration(3922_000))

        assertEquals("5'00\"", FormatUtils.fastFormatPace(5.0))
        assertEquals("4'48\"", FormatUtils.fastFormatPace(4.80))
        assertEquals("--'--\"", FormatUtils.fastFormatPace(0.0))

        assertEquals("14.5", FormatUtils.fastFormat1Dec(14.52))
        assertEquals("5.42", FormatUtils.fastFormat2Dec(5.424))
        assertEquals("5.00", FormatUtils.fastFormat2Dec(5.0))
    }
}
