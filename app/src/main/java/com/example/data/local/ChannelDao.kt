package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY name ASC")
    fun getChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE lastWatched > 0 ORDER BY lastWatched DESC LIMIT :limit")
    fun getRecentlyWatchedChannels(limit: Int = 10): Flow<List<ChannelEntity>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE (:playlistId IS NULL OR playlistId = :playlistId) AND groupTitle != '' ORDER BY groupTitle ASC")
    fun getDistinctGroups(playlistId: Long?): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: Long): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :channelId")
    suspend fun updateFavoriteStatus(channelId: Long, isFavorite: Boolean)

    @Query("UPDATE channels SET lastWatched = :timestamp WHERE id = :channelId")
    suspend fun updateLastWatched(channelId: Long, timestamp: Long)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteChannelsByPlaylist(playlistId: Long)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)

    @Query("SELECT COUNT(*) FROM channels WHERE playlistId = :playlistId")
    suspend fun getChannelCountForPlaylist(playlistId: Long): Int
}
