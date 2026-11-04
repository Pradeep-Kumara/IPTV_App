package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String? = null,
    val channelCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)
