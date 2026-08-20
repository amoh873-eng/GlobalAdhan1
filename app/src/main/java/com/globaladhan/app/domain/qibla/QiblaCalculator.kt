package com.globaladhan.app.domain.qibla

import com.globaladhan.app.domain.model.GeoLocation
import com.globaladhan.app.domain.model.Kaaba
import com.globaladhan.app.domain.model.QiblaResult
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Qibla direction calculation using the great-circle (haversine) formula.
 * Returns the initial bearing from the user's location toward the Kaaba,
 * plus the great-circle distance in kilometers.
 */
object QiblaCalculator {

    private const val EARTH_RADIUS_KM = 6371.0

    fun calculate(location: GeoLocation): QiblaResult {
        require(location.isValid) { "Invalid location: ${location.latitude}, ${location.longitude}" }

        val lat1 = Math.toRadians(location.latitude)
        val lon1 = Math.toRadians(location.longitude)
        val lat2 = Math.toRadians(Kaaba.LATITUDE)
        val lon2 = Math.toRadians(Kaaba.LONGITUDE)

        val dLon = lon2 - lon1

        val bearing = Math.toDegrees(
            atan2(
                sin(dLon) * cos(lat2),
                cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
            )
        )

        val normalizedBearing = (bearing + 360.0) % 360.0

        // Haversine distance
        val dLat = lat2 - lat1
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = EARTH_RADIUS_KM * c

        return QiblaResult(
            bearingDegrees = normalizedBearing,
            distanceKm = distance
        )
    }
}
