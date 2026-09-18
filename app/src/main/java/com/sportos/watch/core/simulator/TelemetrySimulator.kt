package com.sportos.watch.core.simulator

import com.sportos.watch.sports.SportEngine
import com.sportos.watch.sports.running.RunningEngine
import com.sportos.watch.sports.basketball.BasketballEngine
import com.sportos.watch.sports.football.FootballEngine
import com.sportos.watch.sports.cricket.CricketEngine
import com.sportos.watch.sports.tennis.TennisEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * Intelligent Telemetry Simulator for emulator testing and cold sensor warmups.
 * Generates physiologically and kinematically plausible live data flows
 * so all dials, arcs, graphs, and ghost pacers animate realistically.
 */
class TelemetrySimulator(
    private val engine: SportEngine,
    private val sportType: String
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    // Simulation states
    private var simulatedDistanceMeters = 0.0
    private var simulatedHeartRate = 128.0
    private var simulatedPlayerLoad = 0.0
    private var tickCount = 0

    // Basketball simulation
    private var simulatedJumps = 0
    private var maxJump = 0.0

    // Football simulation
    private var simulatedSprints = 0
    private var simulatedHird = 0.0
    private var topSpeed = 0.0

    // Cricket simulation
    private var simulatedOvers = 0
    private var simulatedBalls = 0
    private var simulatedWicketSprints = 0

    // Tennis simulation
    private var simulatedForehands = 0
    private var simulatedBackhands = 0
    private var simulatedServes = 0

    fun start() {
        job?.cancel()
        job = scope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive) {
                delay(1000L) // 1Hz live update loop
                tickCount++

                val elapsed = System.currentTimeMillis() - startTime
                
                // Common HR simulation: Smooth sinusoidal climb from 125 to 168 bpm
                val hrWave = (sin(tickCount / 15.0) * 18.0) + (Random.nextDouble(-1.5, 1.5))
                simulatedHeartRate = (142.0 + hrWave).coerceIn(115.0, 182.0)

                when (val e = engine) {
                    is RunningEngine -> {
                        // Pace around 4:48 to 4:56 min/km -> ~3.45 m/s
                        val currentSpeedMps = 3.42 + (sin(tickCount / 8.0) * 0.25)
                        val deltaDist = currentSpeedMps * 1.0
                        simulatedDistanceMeters += deltaDist

                        val paceMinPerKm = 1000.0 / (currentSpeedMps * 60.0)
                        val cadence = 168.0 + (sin(tickCount / 5.0) * 4.0) + Random.nextInt(0, 3)
                        val calories = (simulatedDistanceMeters / 1000.0) * 72.0

                        // Ghost Runner Target (5:00 min/km = 3.33 m/s)
                        val expectedDist = (elapsed / 1000.0) * 3.33
                        val distDelta = simulatedDistanceMeters - expectedDist
                        val deltaMs = (distDelta / 3.33 * 1000.0).toLong()

                        // Simulate GPS Loop coordinates & elevation
                        val angle = (tickCount * 0.04)
                        val latOffset = (Math.cos(angle) * 0.0025)
                        val lngOffset = (Math.sin(angle) * 0.0025)
                        val bearing = ((angle * 180.0 / Math.PI + 90.0) % 360.0).toFloat()
                        val elevation = 42.0 + (sin(tickCount / 10.0) * 6.0)
                        val elevationGain = (tickCount * 0.35)

                        val gpsPoint = com.sportos.watch.sports.running.GpsLocationPoint(
                            latitude = 37.7749 + latOffset,
                            longitude = -122.4194 + lngOffset,
                            altitudeMeters = elevation,
                            timestampMs = System.currentTimeMillis(),
                            speedMps = currentSpeedMps.toFloat(),
                            bearingDegrees = bearing
                        )

                        e.updateLiveMetrics(
                            elapsedMs = elapsed,
                            heartRate = simulatedHeartRate,
                            distanceM = simulatedDistanceMeters,
                            paceMinKm = paceMinPerKm,
                            cadence = cadence,
                            deltaMs = deltaMs,
                            deltaDistM = distDelta,
                            calories = calories,
                            elevationGainM = elevationGain,
                            gpsPoint = gpsPoint
                        )
                    }

                    is BasketballEngine -> {
                        simulatedPlayerLoad += Random.nextDouble(0.8, 2.2)
                        
                        // Every 12-16 seconds, simulate a jump
                        var lastJumpHeight = 0.0
                        if (tickCount % 14 == 0) {
                            simulatedJumps++
                            lastJumpHeight = Random.nextDouble(20.5, 27.8)
                            if (lastJumpHeight > maxJump) maxJump = lastJumpHeight
                        }

                        val calories = simulatedPlayerLoad * 1.7 + (elapsed / 1000.0 / 60.0) * 9.0

                        e.updateLiveSimulation(
                            elapsedMs = elapsed,
                            heartRate = simulatedHeartRate,
                            jumps = simulatedJumps,
                            lastHeight = lastJumpHeight,
                            maxHeight = maxJump,
                            pLoad = simulatedPlayerLoad,
                            calories = calories
                        )
                    }

                    is FootballEngine -> {
                        // Alternating jog (11 km/h) and sprint (22 km/h)
                        val isSprintPhase = (tickCount % 22) in 18..21
                        val speedKmh = if (isSprintPhase) {
                            Random.nextDouble(20.5, 23.8)
                        } else {
                            Random.nextDouble(8.5, 12.8)
                        }

                        if (speedKmh > topSpeed) topSpeed = speedKmh
                        val speedMps = speedKmh / 3.6
                        val distDelta = speedMps * 1.0
                        simulatedDistanceMeters += distDelta

                        if (speedKmh > 15.0) simulatedHird += distDelta
                        if (isSprintPhase && tickCount % 22 == 18) simulatedSprints++

                        val calories = (simulatedDistanceMeters / 1000.0) * 65.0 + (simulatedSprints * 4.0)

                        e.updateLiveSimulation(
                            elapsedMs = elapsed,
                            heartRate = simulatedHeartRate,
                            distanceM = simulatedDistanceMeters,
                            speedKmh = speedKmh,
                            topSpeed = topSpeed,
                            sprints = simulatedSprints,
                            hirdM = simulatedHird,
                            calories = calories
                        )
                    }

                    is CricketEngine -> {
                        // Every 8 ticks, log a ball delivery
                        var lastSpeed = 0.0
                        if (tickCount % 8 == 0) {
                            simulatedBalls++
                            if (simulatedBalls >= 6) {
                                simulatedBalls = 0
                                simulatedOvers++
                            }
                            lastSpeed = Random.nextDouble(108.0, 128.0)
                        }

                        // Every 18 ticks, a wicket sprint burst
                        if (tickCount % 18 == 0) {
                            simulatedWicketSprints++
                        }

                        val calories = (elapsed / 1000.0 / 60.0) * 6.5 + (simulatedWicketSprints * 3.0)

                        e.updateLiveSimulation(
                            elapsedMs = elapsed,
                            heartRate = simulatedHeartRate,
                            overs = simulatedOvers,
                            balls = simulatedBalls,
                            lastSpeed = lastSpeed,
                            sprints = simulatedWicketSprints,
                            calories = calories
                        )
                    }

                    is TennisEngine -> {
                        val strokeRoll = tickCount % 6
                        var racketSpeed = 0.0
                        if (strokeRoll == 0) {
                            simulatedForehands++
                            racketSpeed = Random.nextDouble(88.0, 112.0)
                        } else if (strokeRoll == 2) {
                            simulatedBackhands++
                            racketSpeed = Random.nextDouble(82.0, 104.0)
                        } else if (strokeRoll == 5) {
                            simulatedServes++
                            racketSpeed = Random.nextDouble(115.0, 138.0)
                        }

                        val rallySeconds = (tickCount % 15).coerceAtLeast(1) * 1000L
                        val calories = (elapsed / 1000.0 / 60.0) * 7.5

                        e.updateLiveSimulation(
                            elapsedMs = elapsed,
                            heartRate = simulatedHeartRate,
                            distanceM = (tickCount * 1.6),
                            fore = simulatedForehands,
                            back = simulatedBackhands,
                            serv = simulatedServes,
                            racketSpeed = racketSpeed,
                            rallyMs = rallySeconds,
                            calories = calories
                        )
                    }
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
