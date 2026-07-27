package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiFactChecker
import com.example.data.db.AppDatabase
import com.example.data.db.DailyCount
import com.example.data.db.SavedReel
import com.example.data.preferences.PetPreferencesManager
import com.example.model.OverlaySize
import com.example.model.PetSpecies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val prefs = PetPreferencesManager.getInstance(application)

    val activePet: StateFlow<PetSpecies> = prefs.activePet
    val petName: StateFlow<String> = prefs.petName
    val overlaySize: StateFlow<OverlaySize> = prefs.overlaySize
    val dailyLimit: StateFlow<Int> = prefs.dailyLimit
    val overlayEnabled: StateFlow<Boolean> = prefs.overlayEnabled
    val accessibilityEnabled: StateFlow<Boolean> = prefs.accessibilityEnabled
    val debugLoggingEnabled: StateFlow<Boolean> = prefs.debugLoggingEnabled
    val feedCount: StateFlow<Int> = prefs.feedCount

    val savedReels: StateFlow<List<SavedReel>> = db.savedReelDao().getAllSavedReels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDateStr: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todayCount: StateFlow<DailyCount?> = db.dailyCountDao().getDailyCountFlow(todayDateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pastWeekCounts: StateFlow<List<DailyCount>> = db.dailyCountDao().getPastWeekCounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isFactChecking = MutableStateFlow(false)
    val isFactChecking: StateFlow<Boolean> = _isFactChecking.asStateFlow()

    private val _lastFactCheckResult = MutableStateFlow<SavedReel?>(null)
    val lastFactCheckResult: StateFlow<SavedReel?> = _lastFactCheckResult.asStateFlow()

    init {
        // Ensure today's counter entry exists
        viewModelScope.launch {
            val existing = db.dailyCountDao().getDailyCount(todayDateStr)
            if (existing == null) {
                db.dailyCountDao().upsertDailyCount(
                    DailyCount(date = todayDateStr, count = 0, limitCount = prefs.dailyLimit.value)
                )
            }
        }
    }

    fun setPetSelection(species: PetSpecies, name: String) {
        prefs.setPetSpecies(species)
        prefs.setPetName(name)
    }

    fun setOverlaySize(size: OverlaySize) {
        prefs.setOverlaySize(size)
    }

    fun setDailyLimit(limit: Int) {
        prefs.setDailyLimit(limit)
        viewModelScope.launch {
            db.dailyCountDao().setLimit(todayDateStr, limit)
        }
    }

    fun setOverlayEnabled(enabled: Boolean) {
        prefs.setOverlayEnabled(enabled)
    }

    fun setAccessibilityEnabled(enabled: Boolean) {
        prefs.setAccessibilityEnabled(enabled)
    }

    fun setDebugLoggingEnabled(enabled: Boolean) {
        prefs.setDebugLoggingEnabled(enabled)
    }

    fun feedPet() {
        prefs.incrementFeedCount()
    }

    fun checkAndSaveReel(captionText: String, shareUrl: String = "", customTags: String = "Watch Later") {
        if (captionText.isBlank()) return

        viewModelScope.launch {
            _isFactChecking.value = true

            val eval = GeminiFactChecker.analyzeCaption(captionText)

            val newReel = SavedReel(
                caption = captionText.trim(),
                shareUrl = shareUrl.trim(),
                timestamp = System.currentTimeMillis(),
                tags = if (customTags.isBlank()) "Watch Later" else customTags.trim(),
                factCheckStatus = eval.status,
                verdictShort = eval.verdictShort,
                explanation = eval.explanation,
                sourcesNote = eval.sourcesNote
            )

            val newId = db.savedReelDao().insertReel(newReel)
            val savedWithId = newReel.copy(id = newId)

            _lastFactCheckResult.value = savedWithId
            _isFactChecking.value = false
        }
    }

    fun updateReelTags(reel: SavedReel, newTags: String) {
        viewModelScope.launch {
            db.savedReelDao().updateReel(reel.copy(tags = newTags))
        }
    }

    fun deleteReel(reel: SavedReel) {
        viewModelScope.launch {
            db.savedReelDao().deleteReel(reel)
        }
    }

    fun clearLastFactCheckResult() {
        _lastFactCheckResult.value = null
    }

    fun incrementTodayCount() {
        viewModelScope.launch {
            val current = db.dailyCountDao().getDailyCount(todayDateStr)
            val countVal = (current?.count ?: 0) + 1
            db.dailyCountDao().upsertDailyCount(
                DailyCount(date = todayDateStr, count = countVal, limitCount = prefs.dailyLimit.value)
            )
        }
    }

    fun resetTodayCount() {
        viewModelScope.launch {
            db.dailyCountDao().setCount(todayDateStr, 0)
        }
    }
}
