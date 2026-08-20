package com.globaladhan.app.domain.qibla

import com.globaladhan.app.domain.model.GeoLocation
import org.junit.Assert.assertEquals
import org.junit.Test

class QiblaCalculatorTest {

    @Test
    fun `bearing from Makkah is 0 or near`() {
        val result = QiblaCalculator.calculate(
            GeoLocation(KaabaLat, KaabaLon, timeZoneId = "Asia/Riyadh")
        )
        assertEquals(0.0, result.bearingDegrees, 2.0)
    }

    @Test
    fun `bearing from London is approximately 118 degrees`() {
        // Known approximate Qibla bearing from London: ~118-119°
        val result = QiblaCalculator.calculate(
            GeoLocation(51.5074, -0.1278, timeZoneId = "Europe/London")
        )
        assertEquals(118.9, result.bearingDegrees, 2.0)
    }

    @Test
    fun `bearing from New York is approximately 58 degrees`() {
        val result = QiblaCalculator.calculate(
            GeoLocation(40.7128, -74.0060, timeZoneId = "America/New_York")
        )
        assertEquals(58.5, result.bearingDegrees, 2.0)
    }

    @Test
    fun `distance from London is about 4900 km`() {
        val result = QiblaCalculator.calculate(
            GeoLocation(51.5074, -0.1278, timeZoneId = "Europe/London")
        )
        assertEquals(4900.0, result.distanceKm, 150.0)
    }

    @Test
    fun `distance from Jakarta is about 7900 km`() {
        val result = QiblaCalculator.calculate(
            GeoLocation(-6.2088, 106.8456, timeZoneId = "Asia/Jakarta")
        )
        assertEquals(7900.0, result.distanceKm, 150.0)
    }

    @Test
    fun `distance from Makkah is near zero`() {
        val result = QiblaCalculator.calculate(
            GeoLocation(KaabaLat, KaabaLon, timeZoneId = "Asia/Riyadh")
        )
        assertEquals(0.0, result.distanceKm, 1.0)
    }

    companion object {
        private const val KaabaLat = 21.422487
        private const val KaabaLon = 39.826206
    }
}
