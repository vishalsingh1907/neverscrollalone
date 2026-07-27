package com.example.service

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.db.DailyCount
import com.example.data.preferences.PetPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReelAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastScrollTime: Long = 0L
    private val scrollDebounceMs = 1200L // 1.2s optimal debounce per reel swipe

    private val targetPackages = setOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.facebook.katana",
        "com.snapchat.android",
        "com.twitter.android",
        "com.reddit.frontpage",
        "com.example"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: ""
        val eventType = event.eventType

        // Process scrolling events from short-form video apps or general scroll events
        if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            if (pkgName.isEmpty() || pkgName in targetPackages || pkgName.contains("reel") || pkgName.contains("video")) {
                val now = System.currentTimeMillis()
                if (now - lastScrollTime > scrollDebounceMs) {
                    lastScrollTime = now
                    processReelViewed(pkgName)
                }
            }
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    private fun processReelViewed(sourceApp: String) {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val prefs = PetPreferencesManager.getInstance(applicationContext)
            
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val existing = db.dailyCountDao().getDailyCount(todayStr)
            
            val limit = prefs.dailyLimit.value
            val currentCount = existing?.count ?: 0
            val newCount = currentCount + 1

            db.dailyCountDao().upsertDailyCount(
                DailyCount(date = todayStr, count = newCount, limitCount = limit)
            )

            // Debug Toast if enabled in Settings
            if (prefs.debugLoggingEnabled.value) {
                Handler(Looper.getMainLooper()).post {
                    val appLabel = when {
                        sourceApp.contains("instagram") -> "Instagram Reel"
                        sourceApp.contains("youtube") -> "YouTube Short"
                        sourceApp.contains("musically") || sourceApp.contains("trill") -> "TikTok"
                        else -> "Reel"
                    }
                    Toast.makeText(
                        applicationContext,
                        "🐾 $appLabel Counted! Today: $newCount/$limit",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            // Trigger notification if limit was just reached
            if (newCount == limit) {
                sendTiredNotification(prefs.petName.value, limit)
            }
        }
    }

    private fun sendTiredNotification(petName: String, limit: Int) {
        val channelId = "pet_wellness_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pet Screen-Time Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when daily reel viewing limit is reached"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Your $petName is getting tired 😴")
            .setContentText("You've hit your daily limit of $limit reels! Time to give your eyes a break.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
