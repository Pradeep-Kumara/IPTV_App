package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ChannelEntity
import com.example.data.model.PlaylistEntity
import com.example.data.repository.IptvRepository
import com.example.player.AspectRatioMode
import com.example.player.IptvPlayerManager
import com.example.player.PlayerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Channels : Screen()
    data object Playlists : Screen()
    data object Favorites : Screen()
    data object Player : Screen()
}

class IptvViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = IptvRepository(database.playlistDao(), database.channelDao())
    val playerManager = IptvPlayerManager(application)

    val playerState: StateFlow<PlayerUiState> = playerManager.uiState

    val playlists: StateFlow<List<PlaylistEntity>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteChannels: StateFlow<List<ChannelEntity>> = repository.favoriteChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentChannels: StateFlow<List<ChannelEntity>> = repository.recentlyWatchedChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Channels)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Raw channels stream based on selected playlist
    private val rawChannels = combine(
        repository.allChannels,
        _selectedPlaylistId
    ) { all, playlistId ->
        if (playlistId == null) {
            all
        } else {
            all.filter { it.playlistId == playlistId }
        }
    }

    // Dynamic distinct categories extracted from currently selected playlist/all
    val categories: StateFlow<List<String>> = rawChannels.combine(_selectedPlaylistId) { channelsList, _ ->
        val groups = channelsList.map { it.groupTitle }
            .filter { it.isNotBlank() && !it.equals("General", ignoreCase = true) }
            .distinct()

        val priorityCategories = listOf(
            "News", "Sports", "Movies", "Music", "Entertainment",
            "Animation", "Documentary", "Kids", "Series", "Science & Tech", "Classic"
        )
        val orderedPriority = priorityCategories.filter { cat ->
            groups.any { it.equals(cat, ignoreCase = true) }
        }
        val rest = groups.filterNot { g ->
            priorityCategories.any { it.equals(g, ignoreCase = true) }
        }.sorted()

        listOf("All") + orderedPriority + rest
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // Filtered channels combining search query and category
    val filteredChannels: StateFlow<List<ChannelEntity>> = combine(
        rawChannels,
        _selectedCategory,
        _searchQuery
    ) { channels, category, query ->
        channels.filter { channel ->
            val matchesCategory = (category == "All" || channel.groupTitle.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                    channel.name.contains(query, ignoreCase = true) ||
                    channel.groupTitle.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initializeDefaultPlaylistIfEmpty()
        }
    }

    fun selectPlaylist(id: Long?) {
        _selectedPlaylistId.value = id
        _selectedCategory.value = "All"
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun playChannel(channel: ChannelEntity, openFullScreen: Boolean = true) {
        viewModelScope.launch {
            repository.markChannelWatched(channel.id)
            playerManager.playChannel(channel)
            if (openFullScreen) {
                _currentScreen.value = Screen.Player
            }
        }
    }

    fun playNextChannel() {
        val currentList = filteredChannels.value
        val currentChannel = playerState.value.currentChannel ?: return
        val currentIndex = currentList.indexOfFirst { it.id == currentChannel.id }
        if (currentIndex != -1 && currentList.isNotEmpty()) {
            val nextIndex = (currentIndex + 1) % currentList.size
            playChannel(currentList[nextIndex], openFullScreen = false)
        }
    }

    fun playPreviousChannel() {
        val currentList = filteredChannels.value
        val currentChannel = playerState.value.currentChannel ?: return
        val currentIndex = currentList.indexOfFirst { it.id == currentChannel.id }
        if (currentIndex != -1 && currentList.isNotEmpty()) {
            val prevIndex = if (currentIndex - 1 < 0) currentList.size - 1 else currentIndex - 1
            playChannel(currentList[prevIndex], openFullScreen = false)
        }
    }

    fun toggleFavorite(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(channel)
        }
    }

    fun addPlaylistFromUrl(name: String, url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Downloading & parsing playlist..."
            val result = repository.addPlaylistFromUrl(name, url)
            _isLoading.value = false
            result.onSuccess {
                _statusMessage.value = "Added '${it.name}' with ${it.channelCount} channels!"
                _selectedPlaylistId.value = it.id
                _currentScreen.value = Screen.Channels
            }.onFailure {
                _statusMessage.value = "Failed: ${it.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun addPlaylistFromText(name: String, text: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Parsing playlist content..."
            val result = repository.addPlaylistFromText(name, text)
            _isLoading.value = false
            result.onSuccess {
                _statusMessage.value = "Added '${it.name}' with ${it.channelCount} channels!"
                _selectedPlaylistId.value = it.id
                _currentScreen.value = Screen.Channels
            }.onFailure {
                _statusMessage.value = "Failed: ${it.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun refreshPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Refreshing playlist streams..."
            val result = repository.refreshPlaylist(playlistId)
            _isLoading.value = false
            result.onSuccess { count ->
                _statusMessage.value = "Playlist updated! Loaded $count channels."
            }.onFailure {
                _statusMessage.value = "Refresh error: ${it.localizedMessage ?: "Failed"}"
            }
        }
    }

    fun deletePlaylist(playlist: PlaylistEntity) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
            if (_selectedPlaylistId.value == playlist.id) {
                _selectedPlaylistId.value = null
            }
            if (playerState.value.currentChannel?.playlistId == playlist.id) {
                playerManager.closePlayer()
            }
            _statusMessage.value = "Deleted playlist '${playlist.name}'"
        }
    }

    fun addSingleChannel(playlistId: Long, name: String, streamUrl: String, groupTitle: String, logoUrl: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.addSingleChannel(playlistId, name, streamUrl, groupTitle, logoUrl)
            _isLoading.value = false
            result.onSuccess {
                _statusMessage.value = "Channel '$name' added successfully!"
            }.onFailure {
                _statusMessage.value = "Failed: ${it.localizedMessage ?: "Error"}"
            }
        }
    }

    fun deleteChannel(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.deleteChannel(channel)
            if (playerState.value.currentChannel?.id == channel.id) {
                playerManager.closePlayer()
            }
            _statusMessage.value = "Deleted '${channel.name}'"
        }
    }

    fun togglePlayPause() = playerManager.togglePlayPause()
    fun retryPlayback() = playerManager.retry()
    fun setAspectRatio(mode: AspectRatioMode) = playerManager.setAspectRatioMode(mode)
    fun toggleMute() = playerManager.toggleMute()
    fun setVolume(volume: Float) = playerManager.setVolume(volume)
    fun closePlayer() = playerManager.closePlayer()

    fun importIptvOrgGlobal() {
        addPlaylistFromUrl(
            name = "IPTV-org Global (11,000+ Channels)",
            url = "https://iptv-org.github.io/iptv/index.m3u"
        )
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
