package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OverlaySize
import com.example.service.PetOverlayService
import com.example.ui.MainViewModel
import com.example.ui.components.AccessibilityPermissionDialog
import com.example.ui.components.OverlayPermissionDialog
import com.example.utils.PermissionUtils

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val dailyLimit by viewModel.dailyLimit.collectAsState()
    val overlaySize by viewModel.overlaySize.collectAsState()
    val overlayEnabled by viewModel.overlayEnabled.collectAsState()
    val accessibilityEnabled by viewModel.accessibilityEnabled.collectAsState()
    val debugLoggingEnabled by viewModel.debugLoggingEnabled.collectAsState()
    val feedCount by viewModel.feedCount.collectAsState()
    val pastWeekCounts by viewModel.pastWeekCounts.collectAsState()

    var sliderLimitValue by remember(dailyLimit) { mutableFloatStateOf(dailyLimit.toFloat()) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var showOverlayDialog by remember { mutableStateOf(false) }

    val isOverlayGranted = remember(overlayEnabled) { PermissionUtils.isOverlayPermissionGranted(context) }
    val isAccessibilityGranted = remember(accessibilityEnabled) { PermissionUtils.isAccessibilityServiceEnabled(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Wellness Controls",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // Daily Reel Limit Slider Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Daily Reel Wellness Limit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "When you view ${sliderLimitValue.toInt()} reels in a day, your pet gets tired and alerts you to take a screen break.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Limit: ${sliderLimitValue.toInt()} Reels", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Button(
                        onClick = { viewModel.setDailyLimit(sliderLimitValue.toInt()) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_limit_btn")
                    ) {
                        Text("Save Limit")
                    }
                }

                Slider(
                    value = sliderLimitValue,
                    onValueChange = { sliderLimitValue = it },
                    valueRange = 10f..200f,
                    steps = 18,
                    modifier = Modifier.testTag("limit_slider")
                )
            }
        }

        // Floating Overlay Window Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Floating Overlay Window", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Switch(
                        checked = overlayEnabled && isOverlayGranted,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (PermissionUtils.isOverlayPermissionGranted(context)) {
                                    viewModel.setOverlayEnabled(true)
                                    val intent = Intent(context, PetOverlayService::class.java)
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        context.startForegroundService(intent)
                                    } else {
                                        context.startService(intent)
                                    }
                                } else {
                                    showOverlayDialog = true
                                }
                            } else {
                                viewModel.setOverlayEnabled(false)
                                context.stopService(Intent(context, PetOverlayService::class.java))
                            }
                        },
                        modifier = Modifier.testTag("overlay_settings_switch")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isOverlayGranted) "✅ Overlay permission granted" else "⚠️ Overlay permission required to float pet on screen",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isOverlayGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Overlay Size", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OverlaySize.entries.forEach { size ->
                        FilterChip(
                            selected = overlaySize == size,
                            onClick = { viewModel.setOverlaySize(size) },
                            label = { Text(size.label) }
                        )
                    }
                }
            }
        }

        // Accessibility Service Toggle & Disclosure Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Accessibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Reel Auto-Detection", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "Counts reel scrolls via Accessibility Service",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Switch(
                        checked = accessibilityEnabled && isAccessibilityGranted,
                        onCheckedChange = { checked ->
                            if (checked) {
                                if (PermissionUtils.isAccessibilityServiceEnabled(context)) {
                                    viewModel.setAccessibilityEnabled(true)
                                } else {
                                    showAccessibilityDialog = true
                                }
                            } else {
                                viewModel.setAccessibilityEnabled(false)
                            }
                        },
                        modifier = Modifier.testTag("accessibility_switch")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isAccessibilityGranted) "✅ Accessibility Service Active" else "⚠️ Manual fallback counter active (tap '+' on Home to count)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAccessibilityGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "🔒 Privacy Disclosure: The Accessibility Service is strictly used to detect scroll events in Instagram/Shorts/TikTok to count reels and trigger pet tiredness. No video content, text messages, or personal data are collected or transmitted.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Debug Counter Toasts", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "Shows a pop-up toast whenever a reel scroll is detected",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Switch(
                        checked = debugLoggingEnabled,
                        onCheckedChange = { viewModel.setDebugLoggingEnabled(it) },
                        modifier = Modifier.testTag("debug_toasts_switch")
                    )
                }
            }
        }

        // Past 7 Days Stats Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Recent Screen-Time Stats", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (pastWeekCounts.isEmpty()) {
                    Text(
                        text = "No history recorded yet. Start viewing reels to track stats!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pastWeekCounts.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item.date, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${item.count} / ${item.limitCount} Reels",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.count >= item.limitCount) Color(0xFFE63946) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { viewModel.resetTodayCount() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reset_today_count_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Today's Reel Counter")
                }
            }
        }
    }

    if (showAccessibilityDialog) {
        AccessibilityPermissionDialog(
            onGrantClicked = {
                showAccessibilityDialog = false
                viewModel.setAccessibilityEnabled(true)
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                context.startActivity(intent)
            },
            onDismiss = {
                showAccessibilityDialog = false
            }
        )
    }

    if (showOverlayDialog) {
        OverlayPermissionDialog(
            onGrantClicked = {
                showOverlayDialog = false
                viewModel.setOverlayEnabled(true)
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            },
            onDismiss = {
                showOverlayDialog = false
            }
        )
    }
}

