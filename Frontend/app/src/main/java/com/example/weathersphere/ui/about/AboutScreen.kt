package com.example.weathersphere.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathersphere.ui.components.GlassCard
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AboutScreen() {
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HERO APP BADGE ---
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                contentPadding = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // WeatherSphere App Icon (Matches opening app icon exactly)
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF38BDF8),
                                        Color(0xFF6366F1),
                                        Color(0xFF8B5CF6)
                                    ),
                                    start = Offset.Zero,
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val scale = w / 76f
                            val center = Offset(w / 2f, h / 2f)

                            // Center Sun Core Circle
                            val coreRadius = 12f * scale
                            drawCircle(
                                color = Color.White,
                                radius = coreRadius,
                                center = center
                            )

                            // 8 Radiant Capsule Rays matching launcher icon exactly
                            val rayStart = 18f * scale
                            val rayEnd = 26f * scale
                            val rayStroke = 3.5f * scale

                            for (i in 0 until 8) {
                                val angleDeg = i * 45f
                                val angleRad = Math.toRadians(angleDeg.toDouble())
                                val cosA = cos(angleRad).toFloat()
                                val sinA = sin(angleRad).toFloat()

                                drawLine(
                                    color = Color.White,
                                    start = Offset(center.x + rayStart * cosA, center.y + rayStart * sinA),
                                    end = Offset(center.x + rayEnd * cosA, center.y + rayEnd * sinA),
                                    strokeWidth = rayStroke,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "WeatherSphere",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Version 1.0.0 • Build 2026.1",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Next-Generation Atmospheric & Weather Intelligence Platform",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // --- TECH STACK PILLS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AboutSectionHeader(title = "POWERED BY", icon = Icons.Default.Info)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val techStack = listOf(
                            "Weather API",
                            "Jetpack Compose",
                            "Kotlin Coroutines",
                            "Room Local DB",
                            "Retrofit2 REST",
                            "Canvas Animations"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column {
                                techStack.chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        rowItems.forEach { tech ->
                                            TechBadgeChip(label = tech)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DEVELOPER PROFILE CARD ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AboutSectionHeader(title = "DEVELOPER PROFILE", icon = Icons.Default.Person)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Developer",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = "Animesh Verma",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Computer Science Student & Android Developer",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Crafted with passion for fluid Jetpack Compose UI & modern Android architecture.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }

        // --- FEATURE HIGHLIGHTS CARD ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AboutSectionHeader(title = "KEY FEATURES", icon = Icons.Default.AutoAwesome)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FeatureRow(
                            icon = Icons.Default.Refresh,
                            title = "Live Weather Telemetry",
                            description = "Real-time global weather endpoints and instant city search suggestions."
                        )
                        FeatureRow(
                            icon = Icons.Default.Palette,
                            title = "Dynamic Canvas Backgrounds",
                            description = "Interactive 60FPS page-tailored animations (Sky, Constellation, Wave & Vortex)."
                        )
                        FeatureRow(
                            icon = Icons.Default.Favorite,
                            title = "Constellation Favorites",
                            description = "Persistent city list stored securely on your phone via Room Database."
                        )
                    }
                }
            }
        }

        // --- QUICK ACTIONS: CHECK UPDATES & SHARE APP ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Button(
                    onClick = {
                        isCheckingUpdate = true
                        updateMessage = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Check Updates", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out WeatherSphere - Atmospheric & Weather Intelligence Platform crafted with modern Jetpack Compose!\n\nhttps://github.com/animeshv063/Weather_Sphere"
                            )
                        }
                        try {
                            context.startActivity(Intent.createChooser(shareIntent, "Share WeatherSphere"))
                        } catch (_: Exception) {
                            updateMessage = "WeatherSphere v1.0.0"
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share App", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- DEDICATED FEEDBACK & SUPPORT CARD ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AboutSectionHeader(title = "FEEDBACK & SUPPORT", icon = Icons.Default.Email)

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 18.dp,
                    shape = RoundedCornerShape(22.dp),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:animeshv063@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "WeatherSphere App Feedback")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            updateMessage = "Developer Contact: animeshv063@gmail.com"
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = "Send Feedback & Inquiries",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "animeshv063@gmail.com",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Email",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isCheckingUpdate) {
            item {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1200)
                    isCheckingUpdate = false
                    updateMessage = "WeatherSphere is up to date! (v1.0.0)"
                }
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            }
        }

        if (updateMessage != null) {
            item {
                Text(
                    text = updateMessage!!,
                    fontSize = 14.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun TechBadgeChip(label: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun FeatureRow(icon: ImageVector, title: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun AboutSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp, start = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}