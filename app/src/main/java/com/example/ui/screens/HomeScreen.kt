package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.FactCheckStatus
import com.example.model.PetSpecies
import com.example.model.PetState
import com.example.service.PetOverlayService
import com.example.ui.MainViewModel
import com.example.ui.components.OverlayPermissionDialog
import com.example.ui.components.PetAnimationCanvas
import com.example.utils.PermissionUtils

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val activePet by viewModel.activePet.collectAsState()
    val petName by viewModel.petName.collectAsState()
    val dailyLimit by viewModel.dailyLimit.collectAsState()
    val todayCountObj by viewModel.todayCount.collectAsState()
    val isFactChecking by viewModel.isFactChecking.collectAsState()
    val lastFactCheck by viewModel.lastFactCheckResult.collectAsState()
    val overlayEnabled by viewModel.overlayEnabled.collectAsState()

    val currentCount = todayCountObj?.count ?: 0
    val progress = (currentCount.toFloat() / dailyLimit.toFloat()).coerceIn(0f, 1f)
    val isTired = currentCount >= dailyLimit

    val petState = when {
        isFactChecking -> PetState.FACT_CHECKING
        isTired -> PetState.TIRED
        else -> PetState.IDLE
    }

    var captionInput by remember { mutableStateOf("") }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Sanctuary Sanctuary Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$petName's Sanctuary",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = activePet.habitatName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(activePet.primaryColor.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = activePet.emoji,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Animated Pet Canvas Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    PetAnimationCanvas(
                        species = activePet,
                        petState = petState,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isTired) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color(0xFFE63946), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "😴 Pet is Tired!",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.feedPet() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feed_pet_home_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Pets, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Feed $petName 🍖")
                }
            }
        }

        // Daily Reel Wellness Count Progress
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isTired) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Screen time icon",
                            tint = if (isTired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Daily Reel Budget",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$currentCount / $dailyLimit Reels",
                            fontWeight = FontWeight.Bold,
                            color = if (isTired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { viewModel.incrementTodayCount() },
                            modifier = Modifier.testTag("increment_count_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Manual Increment")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = if (isTired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isTired)
                        "⚠️ $petName is yawning and tired! You've reached your daily limit of $dailyLimit reels."
                    else
                        "✨ $petName is happy doing idle animations while you browse reels within your goal.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        // Floating Overlay Toggle Control
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Overlay icon",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Floating Pet Companion",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Floats over Instagram/Reels apps",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Switch(
                    checked = overlayEnabled && PermissionUtils.isOverlayPermissionGranted(context),
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
                                showOverlayPermissionDialog = true
                            }
                        } else {
                            viewModel.setOverlayEnabled(false)
                            context.stopService(Intent(context, PetOverlayService::class.java))
                        }
                    },
                    modifier = Modifier.testTag("overlay_switch"),
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }
        }

        // Quick Fact-Checker Input Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Fact Check",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Check Reel Caption",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Paste a reel description or text claim. $petName will analyze claims against reliable sources.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = captionInput,
                    onValueChange = { captionInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fact_check_input"),
                    placeholder = { Text("e.g., 'Drinking hot lemon water cures 100% of flu symptoms instantly'") },
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (captionInput.isNotBlank()) {
                            viewModel.checkAndSaveReel(captionInput)
                            captionInput = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fact_check_btn"),
                    enabled = captionInput.isNotBlank() && !isFactChecking,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isFactChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$petName is analyzing...")
                    } else {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Fact-Check with $petName")
                    }
                }

                // Fact Check Result Card
                AnimatedVisibility(visible = lastFactCheck != null) {
                    lastFactCheck?.let { reel ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when (reel.factCheckStatus) {
                                    FactCheckStatus.ACCURATE -> Color(0xFFE8F5E9)
                                    FactCheckStatus.DISPUTED -> Color(0xFFFFEBEE)
                                    else -> Color(0xFFFFF8E1)
                                }
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (reel.factCheckStatus) {
                                                FactCheckStatus.ACCURATE -> Icons.Default.CheckCircle
                                                FactCheckStatus.DISPUTED -> Icons.Default.Warning
                                                else -> Icons.Default.Info
                                            },
                                            contentDescription = null,
                                            tint = when (reel.factCheckStatus) {
                                                FactCheckStatus.ACCURATE -> Color(0xFF2E7D32)
                                                FactCheckStatus.DISPUTED -> Color(0xFFC62828)
                                                else -> Color(0xFFF57F17)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = reel.verdictShort,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.clearLastFactCheckResult() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Dismiss")
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = reel.explanation,
                                    fontSize = 12.sp,
                                    color = Color.Black.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "💡 Sources: ${reel.sourcesNote} (AI-generated result)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOverlayPermissionDialog) {
        OverlayPermissionDialog(
            onGrantClicked = {
                showOverlayPermissionDialog = false
                viewModel.setOverlayEnabled(true)
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            },
            onDismiss = {
                showOverlayPermissionDialog = false
            }
        )
    }
}
