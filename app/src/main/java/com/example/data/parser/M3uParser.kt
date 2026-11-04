package com.example.data.parser

import com.example.data.model.ChannelEntity
import java.io.BufferedReader
import java.io.StringReader
import java.util.regex.Pattern

object M3uParser {

    private val GROUP_TITLE_PATTERN = Pattern.compile("group-title=\"([^\"]*)\"|group-title=([^\\s,]+)", Pattern.CASE_INSENSITIVE)
    private val TVG_LOGO_PATTERN = Pattern.compile("tvg-logo=\"([^\"]*)\"|tvg-logo=([^\\s,]+)", Pattern.CASE_INSENSITIVE)
    private val TVG_ID_PATTERN = Pattern.compile("tvg-id=\"([^\"]*)\"|tvg-id=([^\\s,]+)", Pattern.CASE_INSENSITIVE)
    private val TVG_NAME_PATTERN = Pattern.compile("tvg-name=\"([^\"]*)\"|tvg-name=([^\\s,]+)", Pattern.CASE_INSENSITIVE)

    fun parse(content: String, playlistId: Long): List<ChannelEntity> {
        val channels = mutableListOf<ChannelEntity>()
        val reader = BufferedReader(StringReader(content))

        var currentName = ""
        var currentGroup = "General"
        var currentLogo: String? = null
        var currentTvgId: String? = null
        var currentTvgName: String? = null
        var hasInf = false

        reader.forEachLine { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachLine

            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                hasInf = true
                val infContent = line.substring(8)

                // Extract group-title
                val groupMatcher = GROUP_TITLE_PATTERN.matcher(infContent)
                currentGroup = if (groupMatcher.find()) {
                    val rawG = (groupMatcher.group(1) ?: groupMatcher.group(2) ?: "General").trim()
                    rawG.split(';').firstOrNull()?.trim()?.ifEmpty { "General" } ?: "General"
                } else {
                    "General"
                }

                // Extract logo
                val logoMatcher = TVG_LOGO_PATTERN.matcher(infContent)
                currentLogo = if (logoMatcher.find()) {
                    (logoMatcher.group(1) ?: logoMatcher.group(2))?.trim()?.ifEmpty { null }
                } else null

                // Extract tvg-id
                val idMatcher = TVG_ID_PATTERN.matcher(infContent)
                currentTvgId = if (idMatcher.find()) {
                    (idMatcher.group(1) ?: idMatcher.group(2))?.trim()?.ifEmpty { null }
                } else null

                // Extract tvg-name
                val nameMatcher = TVG_NAME_PATTERN.matcher(infContent)
                currentTvgName = if (nameMatcher.find()) {
                    (nameMatcher.group(1) ?: nameMatcher.group(2))?.trim()?.ifEmpty { null }
                } else null

                // Extract display name after the last comma
                val commaIndex = line.lastIndexOf(',')
                currentName = if (commaIndex != -1 && commaIndex < line.length - 1) {
                    line.substring(commaIndex + 1).trim()
                } else {
                    currentTvgName ?: "Live Channel"
                }

                if (currentName.isEmpty()) {
                    currentName = currentTvgName ?: "Channel ${channels.size + 1}"
                }
            } else if (!line.startsWith("#")) {
                // This is a stream URL
                if (line.startsWith("http://", ignoreCase = true) ||
                    line.startsWith("https://", ignoreCase = true) ||
                    line.startsWith("rtmp://", ignoreCase = true) ||
                    line.startsWith("rtsp://", ignoreCase = true) ||
                    line.startsWith("mms://", ignoreCase = true) ||
                    hasInf
                ) {
                    val streamName = if (currentName.isNotEmpty()) {
                        currentName
                    } else {
                        // Extract name from URL if no EXTINF was provided
                        val pathSegments = line.split('/')
                        pathSegments.lastOrNull { it.isNotEmpty() }?.substringBefore('.') ?: "Channel ${channels.size + 1}"
                    }

                    channels.add(
                        ChannelEntity(
                            playlistId = playlistId,
                            name = streamName,
                            streamUrl = line,
                            logoUrl = currentLogo,
                            groupTitle = currentGroup,
                            tvgId = currentTvgId,
                            tvgName = currentTvgName,
                            isFavorite = false,
                            lastWatched = 0L
                        )
                    )
                }

                // Reset for next channel
                currentName = ""
                currentGroup = "General"
                currentLogo = null
                currentTvgId = null
                currentTvgName = null
                hasInf = false
            }
        }

        return channels
    }
}
