package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_counts")
data class DailyCount(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD
    val count: Int = 0,
    val limitCount: Int = 50
)
