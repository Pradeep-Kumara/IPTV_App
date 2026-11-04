package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "channels",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("playlistId"),
        Index("groupTitle"),
        Index("isFavorite"),
        Index("lastWatched")
    ]
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playlistId: Long,
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String = "General",
    val tvgId: String? = null,
    val tvgName: String? = null,
    val isFavorite: Boolean = false,
    val lastWatched: Long = 0L
)
