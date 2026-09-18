package com.sportos.watch.core.util

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Ultra-low allocation formatters for Wear OS live telemetry HUD.
 * Uses direct integer arithmetic instead of heavyweight String.format() / regex parsers
 * to eliminate young-generation garbage collection during high-rate recompositions.
 */
object FormatUtils {

    /**
     * Formats duration in milliseconds into "MM:SS" or "H:MM:SS" if >= 1 hour.
     */
    fun fastFormatDuration(ms: Long): String {
        val totalSecs = ms / 1000
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60

        return if (hours > 0) {
            val h = hours.toString()
            val m = if (minutes < 10) "0$minutes" else minutes.toString()
            val s = if (seconds < 10) "0$seconds" else seconds.toString()
            "$h:$m:$s"
        } else {
            val m = if (minutes < 10) "0$minutes" else minutes.toString()
            val s = if (seconds < 10) "0$seconds" else seconds.toString()
            "$m:$s"
        }
    }

    /**
     * Formats pace (min/km) into "M'SS\"".
     */
    fun fastFormatPace(paceMinPerKm: Double): String {
        if (paceMinPerKm <= 0.1 || paceMinPerKm > 30.0) return "--'--\""
        var minutes = paceMinPerKm.toInt()
        var seconds = kotlin.math.round((paceMinPerKm - minutes) * 60).toInt()
        if (seconds >= 60) {
            minutes += 1
            seconds = 0
        }
        val s = if (seconds < 10) "0$seconds" else seconds.toString()
        return "$minutes'$s\""
    }

    /**
     * Fast 1-decimal-place formatter (e.g. 14.5 or 0.0) without regex.
     */
    fun fastFormat1Dec(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0.0"
        val rounded = (value * 10.0).roundToLong()
        val whole = rounded / 10
        val frac = abs(rounded % 10)
        return "$whole.$frac"
    }

    /**
     * Fast 2-decimal-place formatter (e.g. 5.42 or 0.00).
     */
    fun fastFormat2Dec(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "0.00"
        val rounded = (value * 100.0).roundToLong()
        val whole = rounded / 100
        val frac = abs(rounded % 100)
        val fracStr = if (frac < 10) "0$frac" else frac.toString()
        return "$whole.$fracStr"
    }
}
