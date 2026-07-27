package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FactCheckStatus {
    ACCURATE,
    DISPUTED,
    UNVERIFIED,
    PENDING
}

@Entity(tableName = "saved_reels")
data class SavedReel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val caption: String,
    val shareUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val tags: String = "Watch Later", // Comma-separated tags
    val factCheckStatus: FactCheckStatus = FactCheckStatus.PENDING,
    val verdictShort: String = "Pending fact-check",
    val explanation: String = "",
    val sourcesNote: String = "AI-assisted claim evaluation"
)
