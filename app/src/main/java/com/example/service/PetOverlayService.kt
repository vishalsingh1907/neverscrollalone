package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.db.DailyCount
import com.example.data.preferences.PetPreferencesManager
import com.example.model.OverlaySize
import com.example.model.PetState
import com.example.ui.components.PetAnimationCanvas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PetOverlayService : Service(), SavedStateRegistryOwner, ViewModelStoreOwner {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    // Lifecycle requirements for ComposeView in a Service
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore = ViewModelStore()
    override val lifecycle = androidx.lifecycle.LifecycleRegistry(this)

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForegroundServiceNotification()
        setupOverlayView()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "pet_overlay_channel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.overlay_notification_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1002, notification)
    }

    private fun setupOverlayView() {
        val prefs = PetPreferencesManager.getInstance(applicationContext)
        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 180
        }
        layoutParams = params

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@PetOverlayService)
            setViewTreeViewModelStoreOwner(this@PetOverlayService)
            setViewTreeSavedStateRegistryOwner(this@PetOverlayService)

            setContent {
                PetOverlayComposable(
                    onDrag = { dx, dy ->
                        params.x += dx.toInt()
                        params.y += dy.toInt()
                        windowManager.updateViewLayout(this, params)
                    },
                    onOpenApp = {
                        val intent = Intent(this@PetOverlayService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                    },
                    onCloseService = {
                        prefs.setOverlayEnabled(false)
                        stopSelf()
                    }
                )
            }
        }

        overlayView = composeView
        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Composable
    private fun PetOverlayComposable(
        onDrag: (Float, Float) -> Unit,
        onOpenApp: () -> Unit,
        onCloseService: () -> Unit
    ) {
        val context = applicationContext
        val scope = rememberCoroutineScope()
        val prefs = remember { PetPreferencesManager.getInstance(context) }
        val activePet by prefs.activePet.collectAsState()
        val petName by prefs.petName.collectAsState()
        val overlaySize by prefs.overlaySize.collectAsState()
        val dailyLimit by prefs.dailyLimit.collectAsState()
        val feedCount by prefs.feedCount.collectAsState()

        val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
        val db = remember { AppDatabase.getDatabase(context) }
        val todayCountState by db.dailyCountDao().getDailyCountFlow(todayStr).collectAsState(initial = null)

        val currentCount = todayCountState?.count ?: 0
        val isTired = currentCount >= dailyLimit

        var isMenuOpen by remember { mutableStateOf(false) }
        var overridePetState by remember { mutableStateOf<PetState?>(null) }

        val computedState = when {
            overridePetState != null -> overridePetState!!
            isTired -> PetState.TIRED
            currentCount > 0 && currentCount % 10 == 0 -> PetState.HAPPY
            else -> PetState.IDLE
        }

        val sizeDp = overlaySize.dpSize.dp

        Surface(
            modifier = Modifier
                .pointerInput(Unit) {
                    var totalDragX = 0f
                    var totalDragY = 0f
                    detectDragGestures(
                        onDragStart = {
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragX += Math.abs(dragAmount.x)
                            totalDragY += Math.abs(dragAmount.y)
                            onDrag(dragAmount.x, dragAmount.y)
                        },
                        onDragEnd = {
                            // If user barely moved, treat as a Tap!
                            if (totalDragX < 15f && totalDragY < 15f) {
                                isMenuOpen = !isMenuOpen
                                // Play Trick animation on tap
                                scope.launch {
                                    overridePetState = PetState.REACTING
                                    delay(2200)
                                    overridePetState = null
                                }
                            }
                        }
                    )
                }
                .shadow(12.dp, shape = RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Transparent),
            color = Color(0xEE1E293B)
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pet Habitat Canvas
                Box(
                    modifier = Modifier
                        .size(sizeDp)
                        .clip(CircleShape)
                ) {
                    PetAnimationCanvas(
                        species = activePet,
                        petState = computedState,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isTired && overridePetState == null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color(0xFFE63946), CircleShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("😴 Tired", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Interactive Tap Popup Menu
                AnimatedVisibility(
                    visible = isMenuOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .width(sizeDp + 40.dp)
                            .padding(top = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pet Status Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            Column {
                                Text(
                                    text = "$petName ${activePet.emoji}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isTired) "Limits Reached ($currentCount/$dailyLimit)" else "$currentCount / $dailyLimit Reels Today",
                                    color = if (isTired) Color(0xFFFF6B6B) else Color(0xFFA8E6CF),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(
                                onClick = { isMenuOpen = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Menu Action 1: Feed Pet
                        OverlayMenuButton(
                            icon = Icons.Default.Restaurant,
                            label = "Feed $petName ($feedCount)",
                            containerColor = Color(0xFF2A9D8F),
                            onClick = {
                                prefs.incrementFeedCount()
                                scope.launch {
                                    overridePetState = PetState.EATING
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(context, "🍖 Yum! $petName ate food!", Toast.LENGTH_SHORT).show()
                                    }
                                    delay(3500)
                                    overridePetState = null
                                }
                            }
                        )

                        // Menu Action 2: Manual +1 Reel
                        OverlayMenuButton(
                            icon = Icons.Default.Add,
                            label = "+1 Reel Count",
                            containerColor = Color(0xFF3B82F6),
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val current = db.dailyCountDao().getDailyCount(todayStr)
                                    val countVal = (current?.count ?: 0) + 1
                                    db.dailyCountDao().upsertDailyCount(
                                        DailyCount(date = todayStr, count = countVal, limitCount = dailyLimit)
                                    )
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(context, "🐾 Reel Counted! ($countVal/$dailyLimit)", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )

                        // Menu Action 3: Fact-check Reel
                        OverlayMenuButton(
                            icon = Icons.Default.Search,
                            label = "Fact-Check Reel",
                            containerColor = Color(0xFF8B5CF6),
                            onClick = {
                                onOpenApp()
                            }
                        )

                        // Menu Action 4: Saved Reels
                        OverlayMenuButton(
                            icon = Icons.Default.Bookmark,
                            label = "Saved Reels",
                            containerColor = Color(0xFF10B981),
                            onClick = {
                                onOpenApp()
                            }
                        )

                        // Bottom Footer Row
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            IconButton(
                                onClick = onOpenApp,
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0xFF334155), CircleShape)
                            ) {
                                Icon(Icons.Default.OpenInFull, contentDescription = "Open App", tint = Color.White, modifier = Modifier.size(15.dp))
                            }

                            IconButton(
                                onClick = onCloseService,
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0xFF475569), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Overlay", tint = Color.White, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun OverlayMenuButton(
        icon: ImageVector,
        label: String,
        containerColor: Color,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            colors = ButtonDefaults.buttonColors(containerColor = containerColor),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        if (overlayView != null) {
            try {
                windowManager.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

