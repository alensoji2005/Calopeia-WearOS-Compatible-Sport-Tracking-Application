package com.sportos.watch.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sportos.watch.presentation.theme.HrZones
import kotlin.math.cos
import kotlin.math.sin

/**
 * Curved Bezel Gauge Arc painted along the perimeter of circular Wear OS screens.
 * Accurately visualizes Heart Rate Zones (Zone 1 to 5) or Target Pace progress.
 */
@Composable
fun BezelHeartRateArc(
    heartRateBpm: Double,
    modifier: Modifier = Modifier,
    startAngle: Float = 135f,
    sweepAngle: Float = 270f
) {
    // Normal human workout HR range: 90 bpm to 195 bpm
    val normalizedFraction = ((heartRateBpm - 90.0) / (195.0 - 90.0)).coerceIn(0.0, 1.0).toFloat()
    
    val animatedProgress by animateFloatAsState(
        targetValue = normalizedFraction,
        animationSpec = tween(durationMillis = 600),
        label = "HrArcProgress"
    )

    val activeZoneColor = HrZones.getZoneColor(heartRateBpm)

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidthPx = 8.dp.toPx()
        val inset = strokeWidthPx / 2f + 2.dp.toPx()
        val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
        val arcTopLeft = Offset(inset, inset)

        // 1. Inactive dark background track
        drawArc(
            color = Color(0xFF202025),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
        )

        // 2. Active colored progress arc
        if (animatedProgress > 0.01f) {
            val activeSweep = sweepAngle * animatedProgress
            drawArc(
                color = activeZoneColor,
                startAngle = startAngle,
                sweepAngle = activeSweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // 3. Glowing Pip at the tip
            val tipAngleRad = Math.toRadians((startAngle + activeSweep).toDouble())
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val radius = (size.width - inset * 2) / 2f
            val pipX = centerX + (radius * cos(tipAngleRad)).toFloat()
            val pipY = centerY + (radius * sin(tipAngleRad)).toFloat()

            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(pipX, pipY)
            )
        }
    }
}
