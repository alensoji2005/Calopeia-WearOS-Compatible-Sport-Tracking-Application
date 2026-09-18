package com.sportos.watch.presentation.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SportIconType {
    RUNNING,
    BASKETBALL,
    FOOTBALL,
    CRICKET,
    TENNIS,
    GPS_SATELLITE,
    HEART_PULSE,
    TROPHY_PR,
    MAP_ROUTE,
    FLAME_STREAK,
    WHISTLE,
    COMPASS_NORTH,
    ELEVATION_GAIN,
    WATER_LOCK,
    SHARE_EXPORT
}

@Composable
fun SportIcon(
    type: SportIconType,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = CalopeiaCrimson
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        when (type) {
            SportIconType.RUNNING -> drawRunningVector(w, h, tint)
            SportIconType.BASKETBALL -> drawBasketballVector(w, h, tint)
            SportIconType.FOOTBALL -> drawFootballVector(w, h, tint)
            SportIconType.CRICKET -> drawCricketVector(w, h, tint)
            SportIconType.TENNIS -> drawTennisVector(w, h, tint)
            SportIconType.GPS_SATELLITE -> drawGpsVector(w, h, tint)
            SportIconType.HEART_PULSE -> drawHeartPulseVector(w, h, tint)
            SportIconType.TROPHY_PR -> drawTrophyVector(w, h, tint)
            SportIconType.MAP_ROUTE -> drawMapRouteVector(w, h, tint)
            SportIconType.FLAME_STREAK -> drawFlameVector(w, h, tint)
            SportIconType.WHISTLE -> drawWhistleVector(w, h, tint)
            SportIconType.COMPASS_NORTH -> drawCompassVector(w, h, tint)
            SportIconType.ELEVATION_GAIN -> drawElevationVector(w, h, tint)
            SportIconType.WATER_LOCK -> drawWaterLockVector(w, h, tint)
            SportIconType.SHARE_EXPORT -> drawShareVector(w, h, tint)
        }
    }
}

private fun DrawScope.drawRunningVector(w: Float, h: Float, tint: Color) {
    val strokeWidth = w * 0.11f
    // Head
    drawCircle(
        color = tint,
        radius = w * 0.13f,
        center = Offset(w * 0.62f, h * 0.18f)
    )
    // Torso & Legs path
    val path = Path().apply {
        // Torso forward lean
        moveTo(w * 0.55f, h * 0.32f)
        lineTo(w * 0.44f, h * 0.54f)
        // Back leg kicking up
        moveTo(w * 0.44f, h * 0.54f)
        lineTo(w * 0.22f, h * 0.58f)
        lineTo(w * 0.12f, h * 0.46f)
        // Front driving leg
        moveTo(w * 0.44f, h * 0.54f)
        lineTo(w * 0.58f, h * 0.70f)
        lineTo(w * 0.76f, h * 0.90f)
        // Forward driving arm
        moveTo(w * 0.52f, h * 0.36f)
        lineTo(w * 0.72f, h * 0.42f)
        lineTo(w * 0.84f, h * 0.32f)
        // Back arm
        moveTo(w * 0.52f, h * 0.36f)
        lineTo(w * 0.36f, h * 0.42f)
        lineTo(w * 0.28f, h * 0.34f)
    }
    drawPath(
        path = path,
        color = tint,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

private fun DrawScope.drawBasketballVector(w: Float, h: Float, tint: Color) {
    val radius = w * 0.42f
    val center = Offset(w * 0.5f, h * 0.5f)
    val stroke = Stroke(width = w * 0.09f)

    // Outer circle
    drawCircle(color = tint, radius = radius, center = center, style = stroke)
    // Horizontal equator
    drawLine(
        color = tint,
        start = Offset(center.x - radius, center.y),
        end = Offset(center.x + radius, center.y),
        strokeWidth = w * 0.08f,
        cap = StrokeCap.Round
    )
    // Vertical prime meridian
    drawLine(
        color = tint,
        start = Offset(center.x, center.y - radius),
        end = Offset(center.x, center.y + radius),
        strokeWidth = w * 0.08f,
        cap = StrokeCap.Round
    )
    // Curved rib arc left
    val leftArc = Path().apply {
        moveTo(center.x - radius * 0.7f, center.y - radius * 0.7f)
        quadraticBezierTo(center.x - radius * 0.2f, center.y, center.x - radius * 0.7f, center.y + radius * 0.7f)
    }
    drawPath(path = leftArc, color = tint, style = stroke)
    // Curved rib arc right
    val rightArc = Path().apply {
        moveTo(center.x + radius * 0.7f, center.y - radius * 0.7f)
        quadraticBezierTo(center.x + radius * 0.2f, center.y, center.x + radius * 0.7f, center.y + radius * 0.7f)
    }
    drawPath(path = rightArc, color = tint, style = stroke)
}

private fun DrawScope.drawFootballVector(w: Float, h: Float, tint: Color) {
    val radius = w * 0.42f
    val center = Offset(w * 0.5f, h * 0.5f)
    drawCircle(color = tint, radius = radius, center = center, style = Stroke(width = w * 0.09f))

    // Central pentagon
    val penta = Path().apply {
        val pr = radius * 0.42f
        for (i in 0 until 5) {
            val angle = Math.toRadians((i * 72.0) - 90.0)
            val px = (center.x + pr * Math.cos(angle)).toFloat()
            val py = (center.y + pr * Math.sin(angle)).toFloat()
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }
    drawPath(path = penta, color = tint, style = Fill)

    // Seams to boundary
    for (i in 0 until 5) {
        val angle = Math.toRadians((i * 72.0) - 90.0)
        val px1 = (center.x + radius * 0.42f * Math.cos(angle)).toFloat()
        val py1 = (center.y + radius * 0.42f * Math.sin(angle)).toFloat()
        val px2 = (center.x + radius * Math.cos(angle)).toFloat()
        val py2 = (center.y + radius * Math.sin(angle)).toFloat()
        drawLine(color = tint, start = Offset(px1, py1), end = Offset(px2, py2), strokeWidth = w * 0.07f)
    }
}

private fun DrawScope.drawCricketVector(w: Float, h: Float, tint: Color) {
    // Angled Bat
    val batPath = Path().apply {
        // Handle
        moveTo(w * 0.78f, h * 0.12f)
        lineTo(w * 0.65f, h * 0.25f)
        // Blade
        lineTo(w * 0.72f, h * 0.32f)
        lineTo(w * 0.32f, h * 0.72f)
        lineTo(w * 0.18f, h * 0.82f)
        lineTo(w * 0.12f, h * 0.78f)
        lineTo(w * 0.22f, h * 0.64f)
        lineTo(w * 0.58f, h * 0.28f)
        lineTo(w * 0.65f, h * 0.25f)
        close()
    }
    drawPath(path = batPath, color = tint, style = Fill)
    // Cricket ball
    drawCircle(
        color = CalopeiaCrimsonBright,
        radius = w * 0.16f,
        center = Offset(w * 0.75f, h * 0.68f)
    )
    // Seam on ball
    drawLine(
        color = Color.White,
        start = Offset(w * 0.64f, h * 0.68f),
        end = Offset(w * 0.86f, h * 0.68f),
        strokeWidth = w * 0.05f
    )
}

private fun DrawScope.drawTennisVector(w: Float, h: Float, tint: Color) {
    // Racket head ellipse
    drawOval(
        color = tint,
        topLeft = Offset(w * 0.18f, h * 0.08f),
        size = Size(w * 0.48f, h * 0.52f),
        style = Stroke(width = w * 0.09f)
    )
    // Cross strings
    drawLine(
        color = tint.copy(alpha = 0.6f),
        start = Offset(w * 0.42f, h * 0.12f),
        end = Offset(w * 0.42f, h * 0.56f),
        strokeWidth = w * 0.04f
    )
    drawLine(
        color = tint.copy(alpha = 0.6f),
        start = Offset(w * 0.22f, h * 0.34f),
        end = Offset(w * 0.62f, h * 0.34f),
        strokeWidth = w * 0.04f
    )
    // Throat & Shaft
    val shaft = Path().apply {
        moveTo(w * 0.38f, h * 0.60f)
        lineTo(w * 0.42f, h * 0.66f)
        lineTo(w * 0.76f, h * 0.94f)
    }
    drawPath(path = shaft, color = tint, style = Stroke(width = w * 0.09f, cap = StrokeCap.Round))
    // Tennis ball
    drawCircle(
        color = Color(0xFFCDDC39),
        radius = w * 0.13f,
        center = Offset(w * 0.76f, h * 0.32f)
    )
}

private fun DrawScope.drawGpsVector(w: Float, h: Float, tint: Color) {
    val center = Offset(w * 0.5f, h * 0.5f)
    drawCircle(color = tint, radius = w * 0.12f, center = center, style = Fill)
    drawCircle(color = tint, radius = w * 0.32f, center = center, style = Stroke(width = w * 0.08f))
    // Crosshair ticks
    drawLine(color = tint, start = Offset(center.x, h * 0.05f), end = Offset(center.x, h * 0.22f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
    drawLine(color = tint, start = Offset(center.x, h * 0.78f), end = Offset(center.x, h * 0.95f), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
    drawLine(color = tint, start = Offset(w * 0.05f, center.y), end = Offset(w * 0.22f, center.y), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
    drawLine(color = tint, start = Offset(w * 0.78f, center.y), end = Offset(w * 0.95f, center.y), strokeWidth = w * 0.08f, cap = StrokeCap.Round)
}

private fun DrawScope.drawHeartPulseVector(w: Float, h: Float, tint: Color) {
    val path = Path().apply {
        moveTo(w * 0.08f, h * 0.55f)
        lineTo(w * 0.32f, h * 0.55f)
        lineTo(w * 0.42f, h * 0.20f)
        lineTo(w * 0.55f, h * 0.85f)
        lineTo(w * 0.68f, h * 0.40f)
        lineTo(w * 0.76f, h * 0.55f)
        lineTo(w * 0.92f, h * 0.55f)
    }
    drawPath(path = path, color = tint, style = Stroke(width = w * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawTrophyVector(w: Float, h: Float, tint: Color) {
    // Cup
    val cup = Path().apply {
        moveTo(w * 0.24f, h * 0.18f)
        lineTo(w * 0.76f, h * 0.18f)
        lineTo(w * 0.70f, h * 0.54f)
        quadraticBezierTo(w * 0.50f, h * 0.68f, w * 0.30f, h * 0.54f)
        close()
    }
    drawPath(path = cup, color = tint, style = Fill)
    // Handles
    val handles = Path().apply {
        moveTo(w * 0.24f, h * 0.26f)
        quadraticBezierTo(w * 0.08f, h * 0.36f, w * 0.28f, h * 0.48f)
        moveTo(w * 0.76f, h * 0.26f)
        quadraticBezierTo(w * 0.92f, h * 0.36f, w * 0.72f, h * 0.48f)
    }
    drawPath(path = handles, color = tint, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
    // Stem and base
    drawLine(color = tint, start = Offset(w * 0.50f, h * 0.64f), end = Offset(w * 0.50f, h * 0.80f), strokeWidth = w * 0.10f)
    drawLine(color = tint, start = Offset(w * 0.32f, h * 0.85f), end = Offset(w * 0.68f, h * 0.85f), strokeWidth = w * 0.11f, cap = StrokeCap.Round)
}

private fun DrawScope.drawMapRouteVector(w: Float, h: Float, tint: Color) {
    // Folded Map
    val map = Path().apply {
        moveTo(w * 0.15f, h * 0.25f)
        lineTo(w * 0.40f, h * 0.15f)
        lineTo(w * 0.65f, h * 0.25f)
        lineTo(w * 0.85f, h * 0.15f)
        lineTo(w * 0.85f, h * 0.75f)
        lineTo(w * 0.65f, h * 0.85f)
        lineTo(w * 0.40f, h * 0.75f)
        lineTo(w * 0.15f, h * 0.85f)
        close()
    }
    drawPath(path = map, color = tint.copy(alpha = 0.3f), style = Fill)
    drawPath(path = map, color = tint, style = Stroke(width = w * 0.07f, join = StrokeJoin.Round))

    // Pin Point
    val pin = Path().apply {
        moveTo(w * 0.52f, h * 0.30f)
        lineTo(w * 0.62f, h * 0.44f)
        lineTo(w * 0.52f, h * 0.60f)
        lineTo(w * 0.42f, h * 0.44f)
        close()
    }
    drawPath(path = pin, color = CalopeiaCrimsonBright, style = Fill)
}

private fun DrawScope.drawFlameVector(w: Float, h: Float, tint: Color) {
    val flame = Path().apply {
        moveTo(w * 0.50f, h * 0.10f)
        cubicTo(w * 0.62f, h * 0.28f, w * 0.82f, h * 0.42f, w * 0.80f, h * 0.65f)
        cubicTo(w * 0.78f, h * 0.84f, w * 0.64f, h * 0.92f, w * 0.50f, h * 0.92f)
        cubicTo(w * 0.36f, h * 0.92f, w * 0.22f, h * 0.84f, w * 0.20f, h * 0.65f)
        cubicTo(w * 0.18f, h * 0.46f, w * 0.38f, h * 0.32f, w * 0.45f, h * 0.35f)
        cubicTo(w * 0.42f, h * 0.45f, w * 0.46f, h * 0.50f, w * 0.50f, h * 0.50f)
        cubicTo(w * 0.54f, h * 0.50f, w * 0.55f, h * 0.38f, w * 0.50f, h * 0.10f)
        close()
    }
    drawPath(path = flame, color = CalopeiaAmber, style = Fill)
}

private fun DrawScope.drawWhistleVector(w: Float, h: Float, tint: Color) {
    // Body chamber
    drawCircle(color = tint, radius = w * 0.24f, center = Offset(w * 0.36f, h * 0.56f))
    // Tube mouthpiece
    val tube = Path().apply {
        moveTo(w * 0.36f, h * 0.36f)
        lineTo(w * 0.86f, h * 0.36f)
        lineTo(w * 0.86f, h * 0.54f)
        lineTo(w * 0.52f, h * 0.54f)
    }
    drawPath(path = tube, color = tint, style = Fill)
    // Sound vent slit
    drawLine(color = Color.Black, start = Offset(w * 0.48f, h * 0.32f), end = Offset(w * 0.48f, h * 0.44f), strokeWidth = w * 0.08f)
}

private fun DrawScope.drawCompassVector(w: Float, h: Float, tint: Color) {
    val center = Offset(w * 0.5f, h * 0.5f)
    drawCircle(color = tint.copy(alpha = 0.3f), radius = w * 0.44f, center = center, style = Stroke(width = w * 0.06f))
    // North Arrow (Red)
    val north = Path().apply {
        moveTo(center.x, h * 0.14f)
        lineTo(center.x + w * 0.16f, center.y)
        lineTo(center.x, center.y - h * 0.04f)
        close()
    }
    drawPath(path = north, color = CalopeiaCrimsonBright, style = Fill)
    // South Arrow (Muted)
    val south = Path().apply {
        moveTo(center.x, h * 0.86f)
        lineTo(center.x - w * 0.16f, center.y)
        lineTo(center.x, center.y + h * 0.04f)
        close()
    }
    drawPath(path = south, color = CalopeiaTextMuted, style = Fill)
}

private fun DrawScope.drawElevationVector(w: Float, h: Float, tint: Color) {
    val mountain = Path().apply {
        moveTo(w * 0.10f, h * 0.82f)
        lineTo(w * 0.44f, h * 0.32f)
        lineTo(w * 0.62f, h * 0.56f)
        lineTo(w * 0.74f, h * 0.42f)
        lineTo(w * 0.92f, h * 0.82f)
        close()
    }
    drawPath(path = mountain, color = tint.copy(alpha = 0.25f), style = Fill)
    drawPath(path = mountain, color = tint, style = Stroke(width = w * 0.08f, join = StrokeJoin.Round))
    // Upward arrow
    val arrow = Path().apply {
        moveTo(w * 0.44f, h * 0.12f)
        lineTo(w * 0.34f, h * 0.24f)
        moveTo(w * 0.44f, h * 0.12f)
        lineTo(w * 0.54f, h * 0.24f)
        moveTo(w * 0.44f, h * 0.12f)
        lineTo(w * 0.44f, h * 0.30f)
    }
    drawPath(path = arrow, color = CalopeiaNeonGreen, style = Stroke(width = w * 0.08f, cap = StrokeCap.Round))
}

private fun DrawScope.drawWaterLockVector(w: Float, h: Float, tint: Color) {
    // Water drop
    val drop = Path().apply {
        moveTo(w * 0.5f, h * 0.15f)
        cubicTo(w * 0.75f, h * 0.48f, w * 0.82f, h * 0.65f, w * 0.72f, h * 0.82f)
        cubicTo(w * 0.62f, h * 0.94f, w * 0.38f, h * 0.94f, w * 0.28f, h * 0.82f)
        cubicTo(w * 0.18f, h * 0.65f, w * 0.25f, h * 0.48f, w * 0.5f, h * 0.15f)
        close()
    }
    drawPath(path = drop, color = CalopeiaCyan, style = Fill)
}

private fun DrawScope.drawShareVector(w: Float, h: Float, tint: Color) {
    // Top right node
    val topX = w * 0.76f
    val topY = h * 0.26f
    // Bottom right node
    val botX = w * 0.76f
    val botY = h * 0.74f
    // Left origin node
    val leftX = w * 0.26f
    val leftY = h * 0.50f
    val nodeRadius = w * 0.12f

    // Connecting lines
    drawLine(
        color = tint,
        start = Offset(leftX, leftY),
        end = Offset(topX, topY),
        strokeWidth = w * 0.09f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = tint,
        start = Offset(leftX, leftY),
        end = Offset(botX, botY),
        strokeWidth = w * 0.09f,
        cap = StrokeCap.Round
    )

    // Node circles
    drawCircle(color = tint, radius = nodeRadius, center = Offset(leftX, leftY))
    drawCircle(color = tint, radius = nodeRadius, center = Offset(topX, topY))
    drawCircle(color = tint, radius = nodeRadius, center = Offset(botX, botY))
}
