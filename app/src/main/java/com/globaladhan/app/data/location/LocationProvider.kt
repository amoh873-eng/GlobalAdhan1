package com.globaladhan.app.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.globaladhan.app.domain.model.GeoLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Acquires the device location once (one-shot), preferring fine accuracy.
 * Falls back gracefully when permissions are missing so the app never crashes.
 *
 * The one-shot request is wrapped in a timeout so it can never hang, and it
 * prefers the last-known location first for instant results, then requests a
 * fresh fix. Battery-friendly: no continuous polling.
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {

    private val hasFineLocation: Boolean
        get() = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private val hasCoarseLocation: Boolean
        get() = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    val hasLocationPermission: Boolean
        get() = hasFineLocation || hasCoarseLocation

    fun applicationContext(): Context = context

    /** One-shot location request. Returns null if unavailable or permission missing. */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): GeoLocation? {
        if (!hasLocationPermission) return null

        return withContext(Dispatchers.IO) {
            try {
                withTimeout(LOCATION_TIMEOUT_MS) {
                    // Try Play Services (last-known + fresh fix) and the
                    // platform LocationManager fallback in PARALLEL; the first
                    // valid result wins. This fixes the sequential-hang bug.
                    val fused = async {
                        fusedLocation()
                    }
                    val platform = async {
                        platformLocation()
                    }
                    val result = fused.await() ?: platform.await()
                    result?.let { loc ->
                        GeoLocation(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            country = null,
                            city = null,
                            timeZoneId = java.util.TimeZone.getDefault().id
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }
    }

    /** Play Services FusedLocationProvider: last-known then fresh fix. */
    @SuppressLint("MissingPermission")
    private suspend fun fusedLocation(): android.location.Location? {
        // Last-known first (instant, zero battery).
        val lastKnown = runCatching {
            suspendCancellableCoroutine<android.location.Location?> { cont ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { loc -> cont.resume(loc) }
                    .addOnFailureListener { cont.resume(null) }
            }
        }.getOrNull()

        // Fresh fix with a short inner timeout so a silent provider can't hang.
        val fresh = runCatching {
            withTimeout(8_000) {
                suspendCancellableCoroutine<android.location.Location?> { cont ->
                    fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        null
                    ).addOnSuccessListener { loc -> cont.resume(loc) }
                        .addOnFailureListener { cont.resume(null) }
                }
            }
        }.getOrNull()

        return fresh ?: lastKnown
    }

    /**
     * Platform LocationManager fallback for devices without Google Play
     * Services: GPS provider then network provider.
     */
    @SuppressLint("MissingPermission")
    private suspend fun platformLocation(): android.location.Location? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        val providers = runCatching { lm.getProviders(true) }.getOrDefault(emptyList())
        val preferred = when {
            "gps" in providers -> "gps"
            "network" in providers -> "network"
            else -> providers.firstOrNull()
        } ?: return null

        return runCatching {
            withTimeout(8_000) {
                suspendCancellableCoroutine<android.location.Location?> { cont ->
                    val listener = object : android.location.LocationListener {
                        override fun onLocationChanged(loc: android.location.Location) {
                            if (!cont.isCancelled) {
                                lm.removeUpdates(this)
                                cont.resume(loc)
                            }
                        }
                        @Deprecated("Deprecated by platform")
                        override fun onStatusChanged(
                            provider: String?, status: Int, extras: android.os.Bundle?
                        ) = Unit
                        override fun onProviderEnabled(provider: String) = Unit
                        override fun onProviderDisabled(provider: String) = Unit
                    }
                    lm.requestSingleUpdate(preferred, listener, null)
                    cont.invokeOnCancellation {
                        lm.removeUpdates(listener)
                    }
                }
            }
        }.getOrNull()
    }

    /** Reverse-geocode a location to get country, region, and city names. */
    suspend fun reverseGeocode(location: GeoLocation): GeoLocation {
        return withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (results.isNullOrEmpty()) {
                    location
                } else {
                    val address = results.first()
                    location.copy(
                        country = address.countryName ?: location.country,
                        region = address.adminArea ?: location.region,
                        city = address.locality ?: address.subAdminArea ?: location.city
                    )
                }
            }.getOrDefault(location)
        }
    }

    companion object {
        const val LOCATION_TIMEOUT_MS = 12_000L
    }
}
