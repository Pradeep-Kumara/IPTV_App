package com.example.player

import com.example.data.model.ChannelEntity

enum class AspectRatioMode(val label: String) {
    FIT("Fit"),
    FILL("Fill"),
    RATIO_16_9("16:9"),
    RATIO_4_3("4:3")
}

data class PlayerUiState(
    val currentChannel: ChannelEntity? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val errorMessage: String? = null,
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FIT,
    val isMuted: Boolean = false,
    val volume: Float = 1.0f,
    val videoResolution: String? = null,
    val isFullscreen: Boolean = false
)
