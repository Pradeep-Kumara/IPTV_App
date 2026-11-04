package com.example.player

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.data.model.ChannelEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class IptvPlayerManager(
    private val context: Context
) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    @get:OptIn(UnstableApi::class)
    val exoPlayer: ExoPlayer by lazy {
        val defaultHeaders = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Accept" to "*/*",
            "Connection" to "keep-alive"
        )
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            .setDefaultRequestProperties(defaultHeaders)
            .setAllowCrossProtocolRedirects(true)
            .setKeepPostFor302Redirects(true)
            .setConnectTimeoutMs(20000)
            .setReadTimeoutMs(25000)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(httpDataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setSeekBackIncrementMs(10000)
            .setSeekForwardIncrementMs(10000)
            .build().apply {
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_OFF
                addListener(playerListener)
            }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    _uiState.update { it.copy(isBuffering = true, errorMessage = null) }
                }
                Player.STATE_READY -> {
                    _uiState.update {
                        it.copy(
                            isBuffering = false,
                            isPlaying = exoPlayer.isPlaying,
                            errorMessage = null
                        )
                    }
                }
                Player.STATE_ENDED -> {
                    _uiState.update { it.copy(isPlaying = false, isBuffering = false) }
                }
                Player.STATE_IDLE -> {
                    _uiState.update { it.copy(isBuffering = false) }
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
        }

        override fun onPlayerError(error: PlaybackException) {
            val cause = error.cause
            val userMsg = when {
                cause is HttpDataSource.InvalidResponseCodeException && cause.responseCode == 403 ->
                    "HTTP 403 Forbidden: Stream server denied access or geo-blocked. Tap Next Channel to continue."
                cause is HttpDataSource.InvalidResponseCodeException && cause.responseCode == 404 ->
                    "HTTP 404 Not Found: Live stream broadcast is currently offline."
                cause is HttpDataSource.InvalidResponseCodeException ->
                    "HTTP ${cause.responseCode} error: Server temporarily unavailable."
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
                error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                    "Network error: Unable to connect to stream server"
                error.errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ||
                error.errorCode == PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED ->
                    "Stream format not supported or manifest invalid"
                error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                    "Stream server returned HTTP error. Stream may be offline or geo-restricted."
                else -> error.localizedMessage ?: "Playback error occurred"
            }
            _uiState.update {
                it.copy(
                    isBuffering = false,
                    isPlaying = false,
                    errorMessage = userMsg
                )
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            if (videoSize.width > 0 && videoSize.height > 0) {
                val res = "${videoSize.width}x${videoSize.height}"
                _uiState.update { it.copy(videoResolution = res) }
            }
        }
    }

    fun playChannel(channel: ChannelEntity) {
        _uiState.update {
            it.copy(
                currentChannel = channel,
                isBuffering = true,
                errorMessage = null,
                videoResolution = null
            )
        }

        val mediaItem = MediaItem.Builder()
            .setUri(channel.streamUrl)
            .setMediaId(channel.id.toString())
            .build()

        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun retry() {
        val channel = _uiState.value.currentChannel
        if (channel != null) {
            playChannel(channel)
        }
    }

    fun setAspectRatioMode(mode: AspectRatioMode) {
        _uiState.update { it.copy(aspectRatioMode = mode) }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        exoPlayer.volume = clamped
        _uiState.update { it.copy(volume = clamped, isMuted = clamped == 0f) }
    }

    fun toggleMute() {
        val currentlyMuted = _uiState.value.isMuted
        if (currentlyMuted) {
            val restoreVolume = if (_uiState.value.volume > 0f) _uiState.value.volume else 1.0f
            exoPlayer.volume = restoreVolume
            _uiState.update { it.copy(isMuted = false, volume = restoreVolume) }
        } else {
            exoPlayer.volume = 0f
            _uiState.update { it.copy(isMuted = true) }
        }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = fullscreen) }
    }

    fun closePlayer() {
        exoPlayer.stop()
        _uiState.update {
            it.copy(
                currentChannel = null,
                isPlaying = false,
                isBuffering = false,
                errorMessage = null,
                isFullscreen = false
            )
        }
    }

    fun release() {
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }
}
