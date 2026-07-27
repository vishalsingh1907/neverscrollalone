package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.model.OverlaySize
import com.example.model.PetSpecies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PetPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pet_companion_prefs", Context.MODE_PRIVATE)

    private val _activePet = MutableStateFlow(PetSpecies.fromId(prefs.getString(KEY_PET_SPECIES, PetSpecies.CAPYBARA.id) ?: PetSpecies.CAPYBARA.id))
    val activePet: StateFlow<PetSpecies> = _activePet.asStateFlow()

    private val _petName = MutableStateFlow(prefs.getString(KEY_PET_NAME, PetSpecies.CAPYBARA.defaultName) ?: PetSpecies.CAPYBARA.defaultName)
    val petName: StateFlow<String> = _petName.asStateFlow()

    private val _overlaySize = MutableStateFlow(OverlaySize.fromName(prefs.getString(KEY_OVERLAY_SIZE, OverlaySize.MEDIUM.name) ?: OverlaySize.MEDIUM.name))
    val overlaySize: StateFlow<OverlaySize> = _overlaySize.asStateFlow()

    private val _dailyLimit = MutableStateFlow(prefs.getInt(KEY_DAILY_LIMIT, 50))
    val dailyLimit: StateFlow<Int> = _dailyLimit.asStateFlow()

    private val _overlayEnabled = MutableStateFlow(prefs.getBoolean(KEY_OVERLAY_ENABLED, true))
    val overlayEnabled: StateFlow<Boolean> = _overlayEnabled.asStateFlow()

    private val _accessibilityEnabled = MutableStateFlow(prefs.getBoolean(KEY_ACCESSIBILITY_ENABLED, false))
    val accessibilityEnabled: StateFlow<Boolean> = _accessibilityEnabled.asStateFlow()

    private val _debugLoggingEnabled = MutableStateFlow(prefs.getBoolean(KEY_DEBUG_LOGGING, false))
    val debugLoggingEnabled: StateFlow<Boolean> = _debugLoggingEnabled.asStateFlow()

    private val _feedCount = MutableStateFlow(prefs.getInt(KEY_FEED_COUNT, 0))
    val feedCount: StateFlow<Int> = _feedCount.asStateFlow()

    fun setPetSpecies(species: PetSpecies) {
        prefs.edit().putString(KEY_PET_SPECIES, species.id).apply()
        _activePet.value = species
    }

    fun setPetName(name: String) {
        val trimmed = name.ifBlank { _activePet.value.defaultName }
        prefs.edit().putString(KEY_PET_NAME, trimmed).apply()
        _petName.value = trimmed
    }

    fun setOverlaySize(size: OverlaySize) {
        prefs.edit().putString(KEY_OVERLAY_SIZE, size.name).apply()
        _overlaySize.value = size
    }

    fun setDailyLimit(limit: Int) {
        val valClamped = limit.coerceIn(5, 500)
        prefs.edit().putInt(KEY_DAILY_LIMIT, valClamped).apply()
        _dailyLimit.value = valClamped
    }

    fun setOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
        _overlayEnabled.value = enabled
    }

    fun setAccessibilityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ACCESSIBILITY_ENABLED, enabled).apply()
        _accessibilityEnabled.value = enabled
    }

    fun setDebugLoggingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEBUG_LOGGING, enabled).apply()
        _debugLoggingEnabled.value = enabled
    }

    fun incrementFeedCount() {
        val newCount = _feedCount.value + 1
        prefs.edit().putInt(KEY_FEED_COUNT, newCount).apply()
        _feedCount.value = newCount
    }

    companion object {
        private const val KEY_PET_SPECIES = "key_pet_species"
        private const val KEY_PET_NAME = "key_pet_name"
        private const val KEY_OVERLAY_SIZE = "key_overlay_size"
        private const val KEY_DAILY_LIMIT = "key_daily_limit"
        private const val KEY_OVERLAY_ENABLED = "key_overlay_enabled"
        private const val KEY_ACCESSIBILITY_ENABLED = "key_accessibility_enabled"
        private const val KEY_DEBUG_LOGGING = "key_debug_logging"
        private const val KEY_FEED_COUNT = "key_feed_count"

        @Volatile
        private var INSTANCE: PetPreferencesManager? = null

        fun getInstance(context: Context): PetPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PetPreferencesManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
