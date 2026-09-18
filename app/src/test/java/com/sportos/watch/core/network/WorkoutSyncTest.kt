package com.sportos.watch.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutSyncTest {

    @Test
    fun testCompactBeaconPacketRoundtrip() {
        val original = CompactBeaconPacket(
            sport = "Running",
            elapsedSec = 1450L,
            heartRate = 162,
            calories = 310,
            distanceMeters = 4850,
            speedKmh = 12.4f,
            primaryStat = "4'45\"",
            latitude = 37.77492,
            longitude = -122.41941
        )

        val compactString = original.toCompactString()
        assertNotNull(compactString)

        val parsed = CompactBeaconPacket.fromCompactString(compactString)
        assertNotNull(parsed)
        assertEquals(original.sport, parsed!!.sport)
        assertEquals(original.elapsedSec, parsed.elapsedSec)
        assertEquals(original.heartRate, parsed.heartRate)
        assertEquals(original.calories, parsed.calories)
        assertEquals(original.distanceMeters, parsed.distanceMeters)
        assertEquals(original.speedKmh, parsed.speedKmh, 0.15f)
        assertEquals(original.primaryStat, parsed.primaryStat)
        assertEquals(original.latitude!!, parsed.latitude!!, 0.0001)
        assertEquals(original.longitude!!, parsed.longitude!!, 0.0001)
    }

    @Test
    fun testIndoorSportWithoutGps() {
        val indoorPacket = CompactBeaconPacket(
            sport = "Basketball",
            elapsedSec = 900L,
            heartRate = 148,
            calories = 240,
            distanceMeters = 1200,
            speedKmh = 6.2f,
            primaryStat = "18 Jumps",
            latitude = null,
            longitude = null
        )

        val compactStr = indoorPacket.toCompactString()
        val parsed = CompactBeaconPacket.fromCompactString(compactStr)
        assertNotNull(parsed)
        assertEquals("Basketball", parsed!!.sport)
        assertNull(parsed.latitude)
        assertNull(parsed.longitude)
        assertEquals("18 Jumps", parsed.primaryStat)
    }

    @Test
    fun testPayloadCompactionEfficiency() {
        val packet = CompactBeaconPacket(
            sport = "Running",
            elapsedSec = 3600L,
            heartRate = 155,
            calories = 580,
            distanceMeters = 10000,
            speedKmh = 10.0f,
            primaryStat = "5'00\"",
            latitude = 51.50735,
            longitude = -0.12776
        )

        val compactPayload = packet.toCompactString()
        // Typical JSON equivalent
        val jsonPayload = """{"sport":"Running","elapsedSec":3600,"heartRate":155,"calories":580,"distanceMeters":10000,"speedKmh":10.0,"latitude":51.50735,"longitude":-0.12776,"primaryStat":"5'00\""}"""

        val compactBytes = compactPayload.toByteArray().size
        val jsonBytes = jsonPayload.toByteArray().size

        // Verify compact format achieves > 60% reduction in size
        val reductionRatio = 1.0 - (compactBytes.toDouble() / jsonBytes.toDouble())
        assertTrue("Expected >60% payload reduction, got ${(reductionRatio * 100).toInt()}%", reductionRatio > 0.60)
    }

    @Test
    fun testMalformedCompactStringReturnsNull() {
        val malformed = "Run|120|150"
        val result = CompactBeaconPacket.fromCompactString(malformed)
        assertNull(result)
    }

    @Test
    fun testExponentialBackoffProgression() {
        var delayMs = 5_000L
        val delays = mutableListOf<Long>()

        for (i in 1..6) {
            delays.add(delayMs)
            delayMs = (delayMs * 2).coerceAtMost(60_000L)
        }

        assertEquals(listOf(5000L, 10000L, 20000L, 40000L, 60000L, 60000L), delays)
    }
}
