package com.sportos.watch.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.sportos.watch.presentation.theme.CalopeiaCardDark
import com.sportos.watch.presentation.theme.CalopeiaCrimson
import com.sportos.watch.presentation.theme.CalopeiaCrimsonBright
import com.sportos.watch.presentation.theme.CalopeiaCyan
import com.sportos.watch.presentation.theme.CalopeiaNeonGreen
import com.sportos.watch.presentation.theme.CalopeiaPureBlack
import com.sportos.watch.presentation.theme.CalopeiaTextMuted
import com.sportos.watch.presentation.theme.CalopeiaTextWhite
import com.sportos.watch.presentation.theme.SportIcon
import com.sportos.watch.presentation.theme.SportIconType
import com.sportos.watch.sports.running.GpsLocationPoint
import kotlin.math.cos
import kotlin.math.sin

// Strava Signature Vibrant Orange for Route Polyline
val StravaOrange = Color(0xFFFC5200)
val StravaOrangeGlow = Color(0x66FC5200)

@Composable
fun GpsMapView(
    routePoints: List<GpsLocationPoint>,
    distanceKm: Double,
    paceString: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarPulse")
    val pulseRadiusFraction by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CalopeiaPureBlack),
        contentAlignment = Alignment.Center
    ) {
        // Full Canvas GPS Breadcrumb Trail & Radar Rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)

            // 1. Subtle Radar concentric circles (Tactical GPS Look)
            drawCircle(
                color = Color(0xFF1E1E24),
                radius = w * 0.44f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF1A1A20),
                radius = w * 0.30f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF16161C),
                radius = w * 0.16f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Crosshair lines
            drawLine(
                color = Color(0xFF1A1A20),
                start = Offset(center.x, h * 0.10f),
                end = Offset(center.x, h * 0.90f),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color(0xFF1A1A20),
                start = Offset(w * 0.10f, center.y),
                end = Offset(w * 0.90f, center.y),
                strokeWidth = 1.dp.toPx()
            )

            if (routePoints.size >= 2) {
                // Determine bounding box for auto-scaling
                var minLat = routePoints.first().latitude
                var maxLat = routePoints.first().latitude
                var minLng = routePoints.first().longitude
                var maxLng = routePoints.first().longitude

                for (p in routePoints) {
                    if (p.latitude < minLat) minLat = p.latitude
                    if (p.latitude > maxLat) maxLat = p.latitude
                    if (p.longitude < minLng) minLng = p.longitude
                    if (p.longitude > maxLng) maxLng = p.longitude
                }

                val latSpan = (maxLat - minLat).coerceAtLeast(0.001)
                val lngSpan = (maxLng - minLng).coerceAtLeast(0.001)

                // Margin inside circle
                val margin = w * 0.22f
                val drawW = w - (margin * 2f)
                val drawH = h - (margin * 2f)

                fun project(p: GpsLocationPoint): Offset {
                    val nx = ((p.longitude - minLng) / lngSpan).toFloat()
                    // Invert latitude because canvas y increases downwards
                    val ny = (1f - ((p.latitude - minLat) / latSpan).toFloat())
                    return Offset(
                        margin + (nx * drawW),
                        margin + (ny * drawH)
                    )
                }

                // 2. Draw Glow Track
                val glowPath = Path().apply {
                    val first = project(routePoints.first())
                    moveTo(first.x, first.y)
                    for (i in 1 until routePoints.size) {
                        val pt = project(routePoints[i])
                        lineTo(pt.x, pt.y)
                    }
                }
                drawPath(
                    path = glowPath,
                    color = StravaOrangeGlow,
                    style = Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 3. Draw Crisp Primary Polyline (Strava Orange)
                drawPath(
                    path = glowPath,
                    color = StravaOrange,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 4. Start Waypoint Pin (Green circle with white border)
                val startPt = project(routePoints.first())
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = startPt
                )
                drawCircle(
                    color = CalopeiaNeonGreen,
                    radius = 3.5.dp.toPx(),
                    center = startPt
                )

                // 5. Current Position Puck with Directional Bearing Triangle
                val lastPoint = routePoints.last()
                val currentPt = project(lastPoint)

                // Pulsing outer halo
                drawCircle(
                    color = CalopeiaCyan.copy(alpha = 0.25f),
                    radius = 9.dp.toPx() * pulseRadiusFraction,
                    center = currentPt
                )
                // White ring
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = currentPt
                )
                // Blue core puck
                drawCircle(
                    color = CalopeiaCyan,
                    radius = 4.dp.toPx(),
                    center = currentPt
                )

                // Directional Bearing Cone
                val rad = Math.toRadians((lastPoint.bearingDegrees - 90.0)).toFloat()
                val coneDist = 12.dp.toPx()
                val tip = Offset(
                    currentPt.x + cos(rad) * coneDist,
                    currentPt.y + sin(rad) * coneDist
                )
                val leftRad = Math.toRadians((lastPoint.bearingDegrees - 90.0 + 150.0)).toFloat()
                val rightRad = Math.toRadians((lastPoint.bearingDegrees - 90.0 - 150.0)).toFloat()
                val pLeft = Offset(
                    currentPt.x + cos(leftRad) * (coneDist * 0.6f),
                    currentPt.y + sin(leftRad) * (coneDist * 0.6f)
                )
                val pRight = Offset(
                    currentPt.x + cos(rightRad) * (coneDist * 0.6f),
                    currentPt.y + sin(rightRad) * (coneDist * 0.6f)
                )

                val arrow = Path().apply {
                    moveTo(tip.x, tip.y)
                    lineTo(pLeft.x, pLeft.y)
                    lineTo(pRight.x, pRight.y)
                    close()
                }
                drawPath(path = arrow, color = Color.White, style = Fill)

            } else {
                // Empty state radar pulsing
                drawCircle(
                    color = CalopeiaCyan.copy(alpha = 0.3f),
                    radius = 12.dp.toPx() * pulseRadiusFraction,
                    center = center
                )
                drawCircle(
                    color = CalopeiaCyan,
                    radius = 5.dp.toPx(),
                    center = center
                )
            }
        }

        // Top Status Badges: GPS Fix & Compass North
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(CalopeiaCardDark.copy(alpha = 0.85f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SportIcon(SportIconType.GPS_SATELLITE, size = 11.dp, tint = CalopeiaNeonGreen)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "LIVE GPS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = CalopeiaNeonGreen,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            SportIcon(SportIconType.COMPASS_NORTH, size = 14.dp, tint = CalopeiaCrimson)
        }

        // Bottom Translucent Floating HUD Pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .background(CalopeiaCardDark.copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF303038), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%.2f km", distanceKm),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = CalopeiaTextWhite
                )
                Text(
                    text = " • ",
                    fontSize = 10.sp,
                    color = CalopeiaTextMuted
                )
                Text(
                    text = "$paceString/km",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StravaOrange
                )
            }
        }
    }
}
