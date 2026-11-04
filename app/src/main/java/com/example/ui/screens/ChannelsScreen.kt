package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChannelEntity
import com.example.data.model.PlaylistEntity
import com.example.ui.components.CategoryChipsRow
import com.example.ui.components.ChannelGridItem
import com.example.ui.components.ChannelListItem
import com.example.ui.components.RecentChannelsRow
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.IptvViewModel

@Composable
fun ChannelsScreen(
    viewModel: IptvViewModel,
    channels: List<ChannelEntity>,
    playlists: List<PlaylistEntity>,
    selectedPlaylistId: Long?,
    recentChannels: List<ChannelEntity>,
    categories: List<String>,
    selectedCategory: String,
    searchQuery: String,
    isGridView: Boolean,
    currentPlayingId: Long?,
    onOpenAddSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSearch by remember { mutableStateOf(searchQuery.isNotBlank()) }
    var playlistMenuExpanded by remember { mutableStateOf(false) }

    val activePlaylist = playlists.firstOrNull { it.id == selectedPlaylistId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("channels_screen")
    ) {
        // App Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding & Playlist Selector
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_iptv_logo),
                            contentDescription = "Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "StreamPulse",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(OnlineGreen)
                    )
                }

                // Active Playlist selector dropdown pill
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { playlistMenuExpanded = true }
                            .padding(top = 4.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlaylistPlay,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = activePlaylist?.name ?: "All Playlists",
                            color = ElectricCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    DropdownMenu(
                        expanded = playlistMenuExpanded,
                        onDismissRequest = { playlistMenuExpanded = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Playlists (${playlists.sumOf { it.channelCount }} channels)", color = TextPrimary) },
                            onClick = {
                                viewModel.selectPlaylist(null)
                                playlistMenuExpanded = false
                            }
                        )
                        playlists.forEach { pl ->
                            DropdownMenuItem(
                                text = { Text("${pl.name} (${pl.channelCount})", color = TextPrimary) },
                                onClick = {
                                    viewModel.selectPlaylist(pl.id)
                                    playlistMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Action icons: Search, Grid/List toggle, Add (+)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showSearch = !showSearch },
                    modifier = Modifier.testTag("toggle_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (showSearch) ElectricCyan else TextPrimary
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleGridView() },
                    modifier = Modifier.testTag("toggle_view_mode_button")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle View",
                        tint = TextPrimary
                    )
                }

                IconButton(
                    onClick = onOpenAddSheet,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan)
                        .testTag("add_content_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Playlist",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Search Input (Collapsible)
        if (showSearch) {
            Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search channel by name or category...", color = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("channel_search_input"),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = ElectricCyan,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Category Chips
        CategoryChipsRow(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.selectCategory(it) }
        )

        // Recently Watched (only when not searching)
        if (searchQuery.isBlank() && recentChannels.isNotEmpty()) {
            RecentChannelsRow(
                recentChannels = recentChannels,
                onChannelClick = { viewModel.playChannel(it) }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Channels List Header info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${channels.size} Channels",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            if (selectedCategory != "All") {
                Text(
                    text = "Category: $selectedCategory",
                    color = ElectricCyan,
                    fontSize = 11.sp
                )
            }
        }

        // Channel List or Grid
        if (channels.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No channels match '$searchQuery'" else "No Channels in this Playlist",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add an M3U playlist URL, paste playlist text, or import curated channels.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onOpenAddSheet,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Playlist / Channels", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 96.dp)
                ) {
                    items(channels, key = { it.id }) { channel ->
                        ChannelGridItem(
                            channel = channel,
                            isPlaying = channel.id == currentPlayingId,
                            onChannelClick = { viewModel.playChannel(channel) },
                            onToggleFavorite = { viewModel.toggleFavorite(channel) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(channels, key = { it.id }) { channel ->
                        ChannelListItem(
                            channel = channel,
                            isPlaying = channel.id == currentPlayingId,
                            onChannelClick = { viewModel.playChannel(channel) },
                            onToggleFavorite = { viewModel.toggleFavorite(channel) }
                        )
                    }
                }
            }
        }
    }
}
