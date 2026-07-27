package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCountDao {
    @Query("SELECT * FROM daily_counts WHERE date = :date")
    fun getDailyCountFlow(date: String): Flow<DailyCount?>

    @Query("SELECT * FROM daily_counts WHERE date = :date")
    suspend fun getDailyCount(date: String): DailyCount?

    @Query("SELECT * FROM daily_counts ORDER BY date DESC LIMIT 7")
    fun getPastWeekCounts(): Flow<List<DailyCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyCount(dailyCount: DailyCount)

    @Query("UPDATE daily_counts SET count = count + 1 WHERE date = :date")
    suspend fun incrementCount(date: String): Int

    @Query("UPDATE daily_counts SET count = :count WHERE date = :date")
    suspend fun setCount(date: String, count: Int)

    @Query("UPDATE daily_counts SET limitCount = :limitVal WHERE date = :date")
    suspend fun setLimit(date: String, limitVal: Int)
}
