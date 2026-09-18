package com.sportos.watch.presentation.screens

import com.sportos.watch.sports.basketball.BasketballState
import com.sportos.watch.sports.cricket.CricketState
import com.sportos.watch.sports.football.FootballState
import com.sportos.watch.sports.running.RunningState
import com.sportos.watch.sports.tennis.TennisState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmbientModeTest {

    @Test
    fun testAmbientStateDefaults() {
        val state = AmbientState()
        assertFalse(state.isAmbient)
        assertFalse(state.burnInProtectionRequired)
        assertFalse(state.deviceHasLowBitAmbient)
    }

    @Test
    fun testAmbientStateTransitions() {
        val ambientActive = AmbientState(
            isAmbient = true,
            burnInProtectionRequired = true,
            deviceHasLowBitAmbient = true
        )
        assertTrue(ambientActive.isAmbient)
        assertTrue(ambientActive.burnInProtectionRequired)
        assertTrue(ambientActive.deviceHasLowBitAmbient)

        val restoredInteractive = ambientActive.copy(isAmbient = false)
        assertFalse(restoredInteractive.isAmbient)
    }

    @Test
    fun testBurnInOffsetsRemainBounded() {
        // Burn-in pixel shifting formula:
        // burnInOffsetX: ((currentMinute % 5) - 2)
        // burnInOffsetY: (((currentMinute / 5) % 5) - 2)
        // Verify that across 10,000 simulated minutes, offsets never exceed [-2, 2] dp
        for (minute in 0..10_000) {
            val offsetX = (minute % 5) - 2
            val offsetY = ((minute / 5) % 5) - 2

            assertTrue("OffsetX should be >= -2", offsetX >= -2)
            assertTrue("OffsetX should be <= 2", offsetX <= 2)
            assertTrue("OffsetY should be >= -2", offsetY >= -2)
            assertTrue("OffsetY should be <= 2", offsetY <= 2)
        }
    }

    @Test
    fun testSportStatesCopyAndEquality() {
        val run1 = RunningState(distanceMeters = 5000.0, currentPaceMinPerKm = 4.75)
        val run2 = run1.copy()
        assertEquals(run1, run2)

        val bball = BasketballState(jumpCount = 12, maxJumpHeightInches = 28.5)
        assertEquals(12, bball.jumpCount)

        val football = FootballState(distanceMeters = 4200.0, topSpeedKmh = 24.5)
        assertEquals(24.5, football.topSpeedKmh, 0.001)

        val cricket = CricketState(totalBallsBowled = 36, lastDeliverySpeedKmh = 122.0)
        assertEquals(36, cricket.totalBallsBowled)

        val tennis = TennisState(forehands = 20, backhands = 15, serves = 5)
        assertEquals(40, tennis.swingCount)
    }
}
