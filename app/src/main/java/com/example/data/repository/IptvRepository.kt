package com.example.data.repository

import com.example.data.local.ChannelDao
import com.example.data.local.PlaylistDao
import com.example.data.model.ChannelEntity
import com.example.data.model.PlaylistEntity
import com.example.data.parser.M3uParser
import com.example.data.remote.PlaylistFetcher
import com.example.data.sample.SampleChannels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class IptvRepository(
    private val playlistDao: PlaylistDao,
    private val channelDao: ChannelDao,
    private val playlistFetcher: PlaylistFetcher = PlaylistFetcher()
) {

    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()
    val allChannels: Flow<List<ChannelEntity>> = channelDao.getAllChannels()
    val favoriteChannels: Flow<List<ChannelEntity>> = channelDao.getFavoriteChannels()
    val recentlyWatchedChannels: Flow<List<ChannelEntity>> = channelDao.getRecentlyWatchedChannels(15)

    fun getChannelsByPlaylist(playlistId: Long): Flow<List<ChannelEntity>> {
        return channelDao.getChannelsByPlaylist(playlistId)
    }

    fun getDistinctGroups(playlistId: Long?): Flow<List<String>> {
        return channelDao.getDistinctGroups(playlistId)
    }

    suspend fun initializeDefaultPlaylistIfEmpty() = withContext(Dispatchers.IO) {
        val count = playlistDao.getPlaylistCount()
        if (count == 0) {
            val starterPlaylist = PlaylistEntity(
                name = SampleChannels.DEFAULT_PLAYLIST_NAME,
                url = null,
                channelCount = 0,
                isDefault = true
            )
            val starterId = playlistDao.insertPlaylist(starterPlaylist)
            val initialChannels = SampleChannels.getInitialChannels(starterId)
            channelDao.insertChannelsChunked(initialChannels)
            playlistDao.updatePlaylist(starterPlaylist.copy(id = starterId, channelCount = initialChannels.size))
        }

        // Ensure iptv-org index playlist is imported
        val allPlaylists = playlistDao.getAllPlaylists().firstOrNull() ?: emptyList()
        val hasIptvOrg = allPlaylists.any { it.url == "https://iptv-org.github.io/iptv/index.m3u" }
        if (!hasIptvOrg) {
            addPlaylistFromUrl(
                name = "IPTV-org Global (11,000+ Channels)",
                url = "https://iptv-org.github.io/iptv/index.m3u"
            )
        }
    }

    suspend fun addPlaylistFromUrl(name: String, url: String): Result<PlaylistEntity> = withContext(Dispatchers.IO) {
        val fetchResult = playlistFetcher.fetchPlaylist(url)
        if (fetchResult.isFailure) {
            return@withContext Result.failure(fetchResult.exceptionOrNull() ?: Exception("Failed to load playlist"))
        }

        val content = fetchResult.getOrThrow()
        val playlist = PlaylistEntity(
            name = name.trim().ifEmpty { "My Playlist" },
            url = url.trim(),
            channelCount = 0
        )
        val playlistId = playlistDao.insertPlaylist(playlist)

        val parsedChannels = M3uParser.parse(content, playlistId)
        if (parsedChannels.isEmpty()) {
            playlistDao.deletePlaylistById(playlistId)
            return@withContext Result.failure(Exception("No valid stream channels found in this playlist"))
        }

        channelDao.insertChannelsChunked(parsedChannels)
        val updatedPlaylist = playlist.copy(id = playlistId, channelCount = parsedChannels.size)
        playlistDao.updatePlaylist(updatedPlaylist)
        Result.success(updatedPlaylist)
    }

    suspend fun addPlaylistFromText(name: String, rawContent: String): Result<PlaylistEntity> = withContext(Dispatchers.IO) {
        if (rawContent.isBlank()) {
            return@withContext Result.failure(Exception("Playlist content cannot be empty"))
        }

        val playlist = PlaylistEntity(
            name = name.trim().ifEmpty { "Custom Playlist" },
            url = null,
            channelCount = 0
        )
        val playlistId = playlistDao.insertPlaylist(playlist)
        val parsedChannels = M3uParser.parse(rawContent, playlistId)

        if (parsedChannels.isEmpty()) {
            playlistDao.deletePlaylistById(playlistId)
            return@withContext Result.failure(Exception("No valid stream channels parsed from content"))
        }

        channelDao.insertChannelsChunked(parsedChannels)
        val updatedPlaylist = playlist.copy(id = playlistId, channelCount = parsedChannels.size)
        playlistDao.updatePlaylist(updatedPlaylist)
        Result.success(updatedPlaylist)
    }

    suspend fun refreshPlaylist(playlistId: Long): Result<Int> = withContext(Dispatchers.IO) {
        val playlist = playlistDao.getPlaylistByIdOnce(playlistId)
            ?: return@withContext Result.failure(Exception("Playlist not found"))

        val url = playlist.url
        if (url.isNullOrBlank()) {
            return@withContext Result.failure(Exception("Cannot refresh playlist without URL"))
        }

        val fetchResult = playlistFetcher.fetchPlaylist(url)
        if (fetchResult.isFailure) {
            return@withContext Result.failure(fetchResult.exceptionOrNull() ?: Exception("Refresh failed"))
        }

        val content = fetchResult.getOrThrow()
        val parsedChannels = M3uParser.parse(content, playlistId)
        if (parsedChannels.isNotEmpty()) {
            channelDao.deleteChannelsByPlaylist(playlistId)
            channelDao.insertChannelsChunked(parsedChannels)
            playlistDao.updatePlaylist(playlist.copy(channelCount = parsedChannels.size))
            Result.success(parsedChannels.size)
        } else {
            Result.failure(Exception("Playlist refreshed but contained no channels"))
        }
    }

    suspend fun addSingleChannel(
        playlistId: Long,
        name: String,
        streamUrl: String,
        groupTitle: String,
        logoUrl: String?
    ): Result<Long> = withContext(Dispatchers.IO) {
        if (streamUrl.isBlank()) {
            return@withContext Result.failure(Exception("Stream URL is required"))
        }

        val channel = ChannelEntity(
            playlistId = playlistId,
            name = name.trim().ifEmpty { "Custom Stream" },
            streamUrl = streamUrl.trim(),
            groupTitle = groupTitle.trim().ifEmpty { "Custom" },
            logoUrl = logoUrl?.trim()?.ifEmpty { null }
        )
        val channelId = channelDao.insertChannel(channel)
        val count = channelDao.getChannelCountForPlaylist(playlistId)
        val playlist = playlistDao.getPlaylistByIdOnce(playlistId)
        if (playlist != null) {
            playlistDao.updatePlaylist(playlist.copy(channelCount = count))
        }
        Result.success(channelId)
    }

    suspend fun toggleFavorite(channel: ChannelEntity) = withContext(Dispatchers.IO) {
        channelDao.updateFavoriteStatus(channel.id, !channel.isFavorite)
    }

    suspend fun markChannelWatched(channelId: Long) = withContext(Dispatchers.IO) {
        channelDao.updateLastWatched(channelId, System.currentTimeMillis())
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) = withContext(Dispatchers.IO) {
        channelDao.deleteChannelsByPlaylist(playlist.id)
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun deleteChannel(channel: ChannelEntity) = withContext(Dispatchers.IO) {
        channelDao.deleteChannel(channel)
        val count = channelDao.getChannelCountForPlaylist(channel.playlistId)
        val playlist = playlistDao.getPlaylistByIdOnce(channel.playlistId)
        if (playlist != null) {
            playlistDao.updatePlaylist(playlist.copy(channelCount = count))
        }
    }
}
