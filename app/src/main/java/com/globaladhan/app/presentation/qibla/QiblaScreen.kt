package com.globaladhan.app.presentation.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.globaladhan.app.R
import com.globaladhan.app.domain.model.Kaaba
import com.globaladhan.app.domain.model.QiblaResult
import com.globaladhan.app.presentation.theme.IslamicGold
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun QiblaScreen(
    viewModel: QiblaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val location = uiState
    val qibla = remember(location) {
        if (location?.hasLocation == true) {
            QiblaCalculatorUi.compute(
                latitude = location.latitude,
                longitude = location.longitude
            )
        } else null
    }

    // Device heading from sensors
    val context = LocalContext.current
    var heading by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(true) }
    var interference by remember { mutableStateOf(false) }
    var sensorAccuracy by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor == null) {
            sensorAvailable = false
            return@DisposableEffect onDispose {}
        }
        val listener = object : SensorEventListener {
            val rotationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val degrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                heading = (degrees + 360f) % 360f
                interference = false
                sensorAccuracy = when (event.accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High accuracy"
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium accuracy"
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low accuracy"
                    else -> "Unreliable"
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                interference = accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
                sensorAccuracy = when (accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High accuracy"
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium accuracy"
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low accuracy"
                    else -> "Unreliable"
                }
            }
        }
        sensorManager.registerListener(
            listener,
            rotationSensor,
            SensorManager.SENSOR_DELAY_UI
        )
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (qibla != null) {
            QiblaCompass(
                qiblaBearing = qibla.bearingDegrees,
                deviceHeading = heading
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.qibla_direction),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${qibla.bearingDegrees.roundToInt()}°",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = IslamicGold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.distance_to_makkah) + ": " +
                            formatDistance(qibla.distanceKm),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = Kaaba.NAME,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Alignment guidance (spec §25)
            val diff = ((qibla.bearingDegrees - heading + 540.0) % 360.0) - 180.0
            val aligned = kotlin.math.abs(diff) <= 3.0
            if (aligned) {
                Text(
                    text = "✓ ${stringResource(R.string.facing_qibla)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                val turn = if (diff > 0) {
                    stringResource(R.string.turn_right, kotlin.math.abs(diff).roundToInt())
                } else {
                    stringResource(R.string.turn_left, kotlin.math.abs(diff).roundToInt())
                }
                Text(
                    text = turn,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Calibration hint when sensors are unreliable (spec §26)
            if (interference || sensorAccuracy == "Low accuracy" || sensorAccuracy == "Unreliable") {
                Text(
                    text = stringResource(R.string.calibrate_compass),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (sensorAccuracy.isNotEmpty()) {
                Text(
                    text = sensorAccuracy,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (interference) {
                Text(
                    text = stringResource(R.string.compass_interference),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (!sensorAvailable) {
                Text(
                    text = stringResource(R.string.sensors_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                text = stringResource(R.string.waiting_for_location),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun QiblaCompass(qiblaBearing: Double, deviceHeading: Float) {
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(260.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 12f

            // Outer ring
            drawCircle(
                color = surfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = 4f)
            )

            // Tick marks every 30 degrees
            for (i in 0 until 12) {
                val angle = Math.toRadians(i * 30.0)
                val inner = radius - 8f
                val outer = if (i % 3 == 0) radius - 20f else radius - 14f
                val start = Offset(
                    center.x + (inner * cos(angle)).toFloat(),
                    center.y + (inner * sin(angle)).toFloat()
                )
                val end = Offset(
                    center.x + (outer * cos(angle)).toFloat(),
                    center.y + (outer * sin(angle)).toFloat()
                )
                drawLine(
                    color = onSurfaceVariant,
                    start = start,
                    end = end,
                    strokeWidth = if (i % 3 == 0) 3f else 1f
                )
            }

            // Rotate the Qibla marker by the bearing relative to device heading
            rotate(degrees = -(deviceHeading) + qiblaBearing.toFloat()) {
                drawCircle(
                    color = IslamicGold,
                    radius = 10f,
                    center = Offset(center.x, center.y - (radius - 30f))
                )
            }
        }

        // Static center dot
        Canvas(modifier = Modifier.size(16.dp)) {
            drawCircle(color = primary)
        }
    }
}

private fun formatDistance(km: Double): String {
    return if (km >= 1000) {
        "%.1f km".format(km)
    } else {
        "%.0f km".format(km)
    }
}

/** Lightweight Qibla computation usable from the UI. */
object QiblaCalculatorUi {
    fun compute(latitude: Double, longitude: Double): QiblaResult {
        val lat1 = Math.toRadians(latitude)
        val lon1 = Math.toRadians(longitude)
        val lat2 = Math.toRadians(Kaaba.LATITUDE)
        val lon2 = Math.toRadians(Kaaba.LONGITUDE)
        val dLon = lon2 - lon1

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360.0) % 360.0

        // Haversine
        val dLat = lat2 - lat1
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = 6371.0 * c

        return QiblaResult(bearingDegrees = bearing, distanceKm = distance)
    }
}
