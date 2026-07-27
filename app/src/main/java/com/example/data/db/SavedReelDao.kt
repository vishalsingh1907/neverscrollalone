package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedReelDao {
    @Query("SELECT * FROM saved_reels ORDER BY timestamp DESC")
    fun getAllSavedReels(): Flow<List<SavedReel>>

    @Query("SELECT * FROM saved_reels WHERE id = :id")
    suspend fun getReelById(id: Long): SavedReel?

    @Query("SELECT * FROM saved_reels WHERE caption LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchSavedReels(query: String): Flow<List<SavedReel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: SavedReel): Long

    @Update
    suspend fun updateReel(reel: SavedReel)

    @Delete
    suspend fun deleteReel(reel: SavedReel)

    @Query("DELETE FROM saved_reels WHERE id = :id")
    suspend fun deleteReelById(id: Long)

    @Query("DELETE FROM saved_reels")
    suspend fun deleteAllReels()
}
