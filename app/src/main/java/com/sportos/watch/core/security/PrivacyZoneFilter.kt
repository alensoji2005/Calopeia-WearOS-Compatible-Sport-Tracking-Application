package com.sportos.watch.core.security

import android.location.Location

data class PrivacyZone(
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 500f // Default 500m privacy radius
)

/**
 * Filters GPS coordinates before saving them to the database or exporting/sharing,
 * hiding start/end points near home, office, etc.
 */
class PrivacyZoneFilter(private val privacyZones: List<PrivacyZone>) {

    /**
     * Returns true if the given coordinate is inside any of the user's privacy zones.
     */
    fun isInsidePrivacyZone(latitude: Double, longitude: Double): Boolean {
        if (privacyZones.isEmpty()) return false
        
        val pointLocation = Location("").apply {
            this.latitude = latitude
            this.longitude = longitude
        }

        for (zone in privacyZones) {
            val zoneLocation = Location("").apply {
                this.latitude = zone.latitude
                this.longitude = zone.longitude
            }
            if (pointLocation.distanceTo(zoneLocation) <= zone.radiusMeters) {
                return true
            }
        }
        return false
    }

    /**
     * Applies the privacy filter to a list of trackpoints.
     * Elements falling within privacy zones are omitted or masked.
     */
    fun filterTrackPoints(points: List<Pair<Double, Double>>): List<Pair<Double, Double>> {
        return points.filterNot { isInsidePrivacyZone(it.first, it.second) }
    }
}
