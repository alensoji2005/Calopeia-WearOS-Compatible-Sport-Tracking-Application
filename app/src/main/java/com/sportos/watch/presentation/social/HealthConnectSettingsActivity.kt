package com.sportos.watch.presentation.social

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.AppScaffold
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.ScreenScaffold
import com.google.android.horologist.compose.layout.rememberResponsiveColumnState
import com.sportos.watch.presentation.theme.CalopeiaCardDark
import com.sportos.watch.presentation.theme.CalopeiaCrimson
import com.sportos.watch.presentation.theme.CalopeiaNeonGreen
import com.sportos.watch.presentation.theme.CalopeiaPureBlack
import com.sportos.watch.presentation.theme.CalopeiaTextMuted
import com.sportos.watch.presentation.theme.CalopeiaTextWhite
import com.sportos.watch.presentation.theme.SportIcon
import com.sportos.watch.presentation.theme.SportIconType
import com.sportos.watch.presentation.theme.SportOSTheme

/**
 * Rationale & Settings screen for Android Health Connect permissions.
 * Fulfills Android Health Connect guideline requiring an activity handling
 * ACTION_SHOW_PERMISSIONS_RATIONALE declared in AndroidManifest.xml.
 */
class HealthConnectSettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SportOSTheme {
                AppScaffold {
                    HealthConnectRationaleScreen(
                        onOpenSettings = {
                            try {
                                val intent = Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS")
                                startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback if Health Connect standalone settings app is not installed
                            }
                        },
                        onDismiss = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun HealthConnectRationaleScreen(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val columnState = rememberResponsiveColumnState(
        contentPadding = ScalingLazyColumnDefaults.padding(
            first = ScalingLazyColumnDefaults.ItemType.Text,
            last = ScalingLazyColumnDefaults.ItemType.Chip
        )
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(
            columnState = columnState,
            modifier = Modifier
                .fillMaxSize()
                .background(CalopeiaPureBlack)
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CalopeiaNeonGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "HEALTH CONNECT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CalopeiaNeonGreen,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "DATA SYNC",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CalopeiaTextWhite
                    )
                }
            }

            // Explanatory Rationale Card
            item {
                Text(
                    text = "Calopeia integrates with Health Connect to sync real-time heart rate, GPS routes, pace, and exercise sessions securely with your connected health ecosystem.",
                    fontSize = 10.sp,
                    color = CalopeiaTextMuted,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            // Permissions list overview
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    PermissionRow(title = "Heart Rate", desc = "Live BPM zones")
                    PermissionRow(title = "Distance & Steps", desc = "Splits & cadence")
                    PermissionRow(title = "Exercise Route", desc = "GPS track & elevation")
                }
            }

            // Open Health Connect Settings Button
            item {
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalopeiaCrimson,
                        contentColor = CalopeiaTextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "OPEN SETTINGS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Done / Back Button
            item {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CalopeiaCardDark,
                        contentColor = CalopeiaTextMuted
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("Done", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "• $title", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CalopeiaTextWhite)
        Text(text = desc, fontSize = 8.sp, color = CalopeiaTextMuted)
    }
}
