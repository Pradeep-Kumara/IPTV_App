package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sample.SampleChannels
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.IptvViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaylistSheet(
    viewModel: IptvViewModel,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Tab 0: URL
    var playlistName by remember { mutableStateOf("") }
    var playlistUrl by remember { mutableStateOf("") }

    // Tab 1: Presets
    // Tab 2: Raw Text
    var rawTextName by remember { mutableStateOf("") }
    var rawTextContent by remember { mutableStateOf("") }

    // Tab 3: Single Channel
    var singleName by remember { mutableStateOf("") }
    var singleUrl by remember { mutableStateOf("") }
    var singleGroup by remember { mutableStateOf("Custom") }
    var singleLogo by remember { mutableStateOf("") }

    val tabs = listOf("M3U URL", "Curated Feeds", "Paste M3U", "Single Stream")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
        tonalElevation = 12.dp,
        modifier = modifier.testTag("add_playlist_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddLink,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add IPTV Content",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurfaceVariant,
                contentColor = ElectricCyan,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (selectedTab) {
                0 -> {
                    // M3U URL Tab
                    Text(
                        text = "Import M3U or M3U8 Playlist URL",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Playlist Name (Optional)") },
                        placeholder = { Text("e.g. My Sports & News") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("playlist_name_input"),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = playlistUrl,
                        onValueChange = { playlistUrl = it },
                        label = { Text("M3U Playlist URL *") },
                        placeholder = { Text("https://example.com/playlist.m3u") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("playlist_url_input"),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.addPlaylistFromUrl(
                                name = playlistName.ifBlank { "Playlist" },
                                url = playlistUrl
                            )
                            onDismiss()
                        },
                        enabled = playlistUrl.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_playlist_url_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                1 -> {
                    // Curated Presets Tab
                    Text(
                        text = "Verified Public Free-to-Air Playlists",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SampleChannels.POPULAR_PLAYLIST_PRESETS.forEach { preset ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.addPlaylistFromUrl(preset.name, preset.url)
                                    onDismiss()
                                }
                                .testTag("preset_${preset.name}"),
                            color = DarkSurfaceElevated,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(NeonPurple.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleOutline,
                                        contentDescription = null,
                                        tint = NeonPurple
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = preset.name,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = preset.description,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Load",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Paste Raw M3U Content
                    Text(
                        text = "Paste raw M3U text content directly",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = rawTextName,
                        onValueChange = { rawTextName = it },
                        label = { Text("Playlist Name") },
                        placeholder = { Text("Custom Text Playlist") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("paste_playlist_name_input"),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = rawTextContent,
                        onValueChange = { rawTextContent = it },
                        label = { Text("M3U Content *") },
                        placeholder = { Text("#EXTM3U\n#EXTINF:-1,My Stream\nhttp://...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .testTag("paste_playlist_content_input"),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 8
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            viewModel.addPlaylistFromText(
                                name = rawTextName.ifBlank { "Custom Playlist" },
                                text = rawTextContent
                            )
                            onDismiss()
                        },
                        enabled = rawTextContent.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_paste_playlist_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add From Text", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                3 -> {
                    // Single Stream
                    Text(
                        text = "Add a single HLS/IPTV channel stream",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = singleName,
                        onValueChange = { singleName = it },
                        label = { Text("Channel Name *") },
                        placeholder = { Text("e.g. My Favorite News") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = singleUrl,
                        onValueChange = { singleUrl = it },
                        label = { Text("Stream URL (.m3u8, mp4, etc.) *") },
                        placeholder = { Text("https://example.com/live.m3u8") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = singleGroup,
                        onValueChange = { singleGroup = it },
                        label = { Text("Category / Group") },
                        placeholder = { Text("e.g. Sports, News, Movies") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = singleLogo,
                        onValueChange = { singleLogo = it },
                        label = { Text("Logo Image URL (Optional)") },
                        placeholder = { Text("https://example.com/logo.png") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val targetPlaylistId = viewModel.selectedPlaylistId.value
                                ?: viewModel.playlists.value.firstOrNull()?.id
                                ?: 1L
                            viewModel.addSingleChannel(
                                playlistId = targetPlaylistId,
                                name = singleName,
                                streamUrl = singleUrl,
                                groupTitle = singleGroup,
                                logoUrl = singleLogo.ifBlank { null }
                            )
                            onDismiss()
                        },
                        enabled = singleName.isNotBlank() && singleUrl.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LiveTv, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Channel Stream", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricCyan,
    unfocusedBorderColor = DarkBorder,
    focusedLabelColor = ElectricCyan,
    unfocusedLabelColor = TextSecondary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = ElectricCyan,
    focusedContainerColor = DarkBackground,
    unfocusedContainerColor = DarkBackground
)
