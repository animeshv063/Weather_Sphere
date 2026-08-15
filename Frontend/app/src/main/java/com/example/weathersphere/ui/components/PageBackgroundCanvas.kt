package com.example.weathersphere.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun PageBackgroundCanvas(
    pageIndex: Int,
    weatherCondition: String? = null,
    isAnimationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (pageIndex) {
            0 -> HomeBackgroundCanvas(weatherCondition = weatherCondition, isAnimationEnabled = isAnimationEnabled)
            1 -> FavoritesBackgroundCanvas(isAnimationEnabled = isAnimationEnabled)
            2 -> SettingsBackgroundCanvas(isAnimationEnabled = isAnimationEnabled)
            3 -> AboutBackgroundCanvas(isAnimationEnabled = isAnimationEnabled)
            else -> HomeBackgroundCanvas(weatherCondition = weatherCondition, isAnimationEnabled = isAnimationEnabled)
        }
    }
}

// ----------------------------------------------------
// TAB 0: HOME PAGE - Dynamic Sky Atmospheric Canvas
// ----------------------------------------------------
@Composable
private fun HomeBackgroundCanvas(weatherCondition: String?, isAnimationEnabled: Boolean = true) {
    val isDark = isSystemInDarkTheme()
    val condition = weatherCondition?.lowercase() ?: ""

    val animationProgress = if (isAnimationEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "homeSkyAnimation")
        val anim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "skyProgress"
        )
        anim
    } else 0f

    val pulseProgress = if (isAnimationEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "homePulseAnimation")
        val pulse by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseProgress"
        )
        pulse
    } else 0.8f

    val gradientColors = remember(condition, isDark) {
        when {
            condition.contains("rain") || condition.contains("drizzle") || condition.contains("thunder") -> {
                if (isDark) listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
                else listOf(Color(0xFF475569), Color(0xFF64748B), Color(0xFF94A3B8))
            }
            condition.contains("cloud") || condition.contains("overcast") -> {
                if (isDark) listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF384152))
                else listOf(Color(0xFFBAE6FD), Color(0xFF7DD3FC), Color(0xFF38BDF8))
            }
            condition.contains("snow") || condition.contains("ice") -> {
                if (isDark) listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4338CA))
                else listOf(Color(0xFFE0F2FE), Color(0xFFBAE6FD), Color(0xFF7DD3FC))
            }
            condition.contains("night") -> {
                listOf(Color(0xFF030712), Color(0xFF0F172A), Color(0xFF1E1B4B))
            }
            else -> {
                if (isDark) listOf(Color(0xFF0B132B), Color(0xFF1C2541), Color(0xFF3A506B))
                else listOf(Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFF7DD3FC))
            }
        }
    }

    val particles = remember {
        List(30) {
            PointData(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 4f + 2f)
        }
    }

    val clouds = remember {
        List(4) {
            PointData(Random.nextFloat(), Random.nextFloat() * 0.4f, Random.nextFloat() * 120f + 80f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = gradientColors,
                startY = 0f,
                endY = height
            )
        )

        if (condition.contains("sun") || (condition.isEmpty() && !isDark)) {
            val sunCenter = Offset(width * 0.82f, height * 0.18f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFDE047).copy(alpha = 0.4f * pulseProgress),
                        Color(0xFFF97316).copy(alpha = 0.2f * pulseProgress),
                        Color.Transparent
                    ),
                    center = sunCenter,
                    radius = width * 0.5f
                ),
                center = sunCenter,
                radius = width * 0.5f
            )
        }

        clouds.forEach { cloud ->
            val shift = ((animationProgress + cloud.x) % 1.0f) * (width + 300f) - 150f
            val cloudY = cloud.y * height + 60f
            val radius = cloud.radius

            drawCircle(
                color = Color.White.copy(alpha = if (isDark) 0.08f else 0.22f),
                center = Offset(shift, cloudY),
                radius = radius
            )
            drawCircle(
                color = Color.White.copy(alpha = if (isDark) 0.06f else 0.18f),
                center = Offset(shift + radius * 0.6f, cloudY + 15f),
                radius = radius * 0.8f
            )
            drawCircle(
                color = Color.White.copy(alpha = if (isDark) 0.06f else 0.18f),
                center = Offset(shift - radius * 0.6f, cloudY + 10f),
                radius = radius * 0.7f
            )
        }

        if (condition.contains("rain") || condition.contains("drizzle")) {
            particles.forEach { p ->
                val px = p.x * width
                val py = ((p.y + animationProgress * 2.5f) % 1.0f) * height
                drawLine(
                    color = Color(0xFF93C5FD).copy(alpha = 0.6f),
                    start = Offset(px, py),
                    end = Offset(px - 10f, py + 35f),
                    strokeWidth = 3f
                )
            }
        } else if (condition.contains("snow")) {
            particles.forEach { p ->
                val sinVal = sin((animationProgress * 6.28318f + p.y * 10f).toDouble()).toFloat()
                val px = p.x * width + sinVal * 20f
                val py = ((p.y + animationProgress * 0.8f) % 1.0f) * height
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    center = Offset(px, py),
                    radius = p.radius
                )
            }
        } else if (isDark || condition.contains("night")) {
            particles.forEachIndexed { idx, p ->
                val px = p.x * width
                val py = p.y * height
                val sinVal = sin((animationProgress * 20f + idx).toDouble()).toFloat()
                val starAlpha = ((sinVal + 1f) / 2f * 0.8f + 0.1f).coerceIn(0f, 1f)
                drawCircle(
                    color = Color.White.copy(alpha = starAlpha),
                    center = Offset(px, py),
                    radius = p.radius
                )
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: FAVORITES PAGE - Constellation Star Mesh
// ----------------------------------------------------
@Composable
private fun FavoritesBackgroundCanvas(isAnimationEnabled: Boolean = true) {
    val animProgress = if (isAnimationEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "favoritesConstellation")
        val anim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28318f,
            animationSpec = infiniteRepeatable(
                animation = tween(16000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "constellationAngle"
        )
        anim
    } else 0f

    val nodes = remember {
        List(22) { index ->
            val baseX = (index % 5 + 0.5f) / 5.0f + (Random.nextFloat() * 0.12f - 0.06f)
            val baseY = (index / 5 + 0.5f) / 5.0f + (Random.nextFloat() * 0.12f - 0.06f)
            val speed = Random.nextFloat() * 0.5f + 0.5f
            val phase = Random.nextFloat() * 6.28318f
            val radius = Random.nextFloat() * 3f + 3f
            StarNode(baseX, baseY, speed, phase, radius)
        }
    }

    val cosmicDust = remember {
        List(35) {
            PointData(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 1.8f + 0.8f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Deep Galactic Space Void Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF01040D),
                    Color(0xFF051329),
                    Color(0xFF0A2244),
                    Color(0xFF041122),
                    Color(0xFF010308)
                )
            )
        )

        // Cosmic Nebula Glow Center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF0284C7).copy(alpha = 0.18f),
                    Color(0xFF0369A1).copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(width * 0.5f, height * 0.35f),
                radius = width * 0.8f
            ),
            center = Offset(width * 0.5f, height * 0.35f),
            radius = width * 0.8f
        )

        // Twinkling Galactic Micro Stars
        cosmicDust.forEachIndexed { idx, dust ->
            val dx = dust.x * width
            val dy = dust.y * height
            val twinkle = sin((animProgress * 3f + idx).toDouble()).toFloat()
            val alpha = ((twinkle + 1f) / 2f * 0.7f + 0.2f).coerceIn(0f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                center = Offset(dx, dy),
                radius = dust.radius
            )
        }

        val computedPoints = nodes.map { node ->
            val sinVal = sin((animProgress * node.speed + node.phase).toDouble()).toFloat()
            val cosVal = cos((animProgress * node.speed * 0.7f + node.phase).toDouble()).toFloat()
            val px = node.baseX * width + sinVal * 35f
            val py = node.baseY * height + cosVal * 35f
            PointData(px, py, node.radius)
        }

        val threshold = width * 0.35f
        for (i in computedPoints.indices) {
            for (j in i + 1 until computedPoints.size) {
                val p1 = computedPoints[i]
                val p2 = computedPoints[j]
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val dist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (dist < threshold) {
                    val lineAlpha = ((1f - (dist / threshold)) * 0.45f).coerceIn(0f, 1f)
                    val off1 = Offset(p1.x, p1.y)
                    val off2 = Offset(p2.x, p2.y)
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF38BDF8).copy(alpha = lineAlpha),
                                Color(0xFF67E8F9).copy(alpha = lineAlpha)
                            ),
                            start = off1,
                            end = off2
                        ),
                        start = off1,
                        end = off2,
                        strokeWidth = 1.8f
                    )
                }
            }
        }

        computedPoints.forEach { pt ->
            val center = Offset(pt.x, pt.y)
            drawCircle(
                color = Color(0xFF38BDF8).copy(alpha = 0.35f),
                center = center,
                radius = pt.radius * 3.5f
            )
            drawCircle(
                color = Color(0xFFE0F2FE),
                center = center,
                radius = pt.radius * 1.4f
            )
            drawCircle(
                color = Color.White,
                center = center,
                radius = pt.radius
            )
        }
    }
}

private data class StarNode(
    val baseX: Float,
    val baseY: Float,
    val speed: Float,
    val phase: Float,
    val radius: Float
)

private data class PointData(
    val x: Float,
    val y: Float,
    val radius: Float
)

// ----------------------------------------------------
// TAB 2: SETTINGS PAGE - Fluid Ambient Gradient Mesh
// ----------------------------------------------------
@Composable
private fun SettingsBackgroundCanvas(isAnimationEnabled: Boolean = true) {
    val waveOffset = if (isAnimationEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "settingsFluidWave")
        val wave by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28318f,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "waveOffset"
        )
        wave
    } else 0f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF020617),
                    Color(0xFF0F172A),
                    Color(0xFF1E293B)
                )
            )
        )

        val sin1 = sin(waveOffset.toDouble()).toFloat()
        val cos1 = cos((waveOffset * 0.8f).toDouble()).toFloat()
        val cos2 = cos((waveOffset * 1.2f).toDouble()).toFloat()
        val sin2 = sin(waveOffset.toDouble()).toFloat()
        val sin3 = sin((waveOffset * 0.6f).toDouble()).toFloat()
        val cos3 = cos((waveOffset * 1.4f).toDouble()).toFloat()

        val c1 = Offset(width * 0.2f + sin1 * 80f, height * 0.25f + cos1 * 60f)
        val c2 = Offset(width * 0.8f + cos2 * 90f, height * 0.65f + sin2 * 70f)
        val c3 = Offset(width * 0.4f + sin3 * 60f, height * 0.85f + cos3 * 80f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.35f), Color.Transparent),
                center = c1,
                radius = width * 0.65f
            ),
            center = c1,
            radius = width * 0.65f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.3f), Color.Transparent),
                center = c2,
                radius = width * 0.7f
            ),
            center = c2,
            radius = width * 0.7f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.25f), Color.Transparent),
                center = c3,
                radius = width * 0.6f
            ),
            center = c3,
            radius = width * 0.6f
        )

        val wavePath1 = Path().apply {
            moveTo(0f, height * 0.45f)
            var x = 0f
            while (x <= width + 50f) {
                val y = height * 0.45f + sin((x / width * 4f + waveOffset).toDouble()).toFloat() * 50f
                lineTo(x, y)
                x += 30f
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = wavePath1,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x2238BDF8), Color(0x050284C7))
            )
        )

        val wavePath2 = Path().apply {
            moveTo(0f, height * 0.68f)
            var x = 0f
            while (x <= width + 50f) {
                val y = height * 0.68f + cos((x / width * 3f - waveOffset * 1.2f).toDouble()).toFloat() * 40f
                lineTo(x, y)
                x += 30f
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = wavePath2,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x33A855F7), Color(0x056366F1))
            )
        )
    }
}

// ----------------------------------------------------
// TAB 3: ABOUT PAGE - Quantum Weather Sphere Vortex
// ----------------------------------------------------
@Composable
private fun AboutBackgroundCanvas(isAnimationEnabled: Boolean = true) {
    val rotationAngle = if (isAnimationEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "aboutVortex")
        val rot by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "vortexRotation"
        )
        rot
    } else 0f

    val pulseRadius = if (isAnimationEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "aboutPulse")
        val pulse by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radarPulse"
        )
        pulse
    } else 0.5f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height * 0.38f)

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1E1B4B),
                    Color(0xFF0F172A),
                    Color(0xFF030712)
                ),
                center = center,
                radius = height * 0.8f
            )
        )

        val maxPulseRadius = width * 0.75f
        val currentPulse = maxPulseRadius * pulseRadius
        val pulseAlpha = (1f - pulseRadius).coerceIn(0f, 1f) * 0.5f

        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = pulseAlpha),
            center = center,
            radius = currentPulse,
            style = Stroke(width = 2.5f)
        )

        val ringRadii = listOf(width * 0.25f, width * 0.42f, width * 0.6f)
        val ringColors = listOf(Color(0xFF38BDF8), Color(0xFFA855F7), Color(0xFFF43F5E))

        ringRadii.forEachIndexed { index, r ->
            drawCircle(
                color = ringColors[index].copy(alpha = 0.25f),
                center = center,
                radius = r,
                style = Stroke(width = 2f)
            )

            val particleCount = 4 + index * 2
            for (p in 0 until particleCount) {
                val angleDeg = (rotationAngle * (if (index % 2 == 0) 1 else -1) + (p * 360f / particleCount))
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val px = center.x + (r * cos(angleRad)).toFloat()
                val py = center.y + (r * sin(angleRad)).toFloat()
                val ptOffset = Offset(px, py)

                drawCircle(
                    color = ringColors[index],
                    center = ptOffset,
                    radius = 5f + index * 1.5f
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    center = ptOffset,
                    radius = 2.5f
                )
            }
        }
    }
}
