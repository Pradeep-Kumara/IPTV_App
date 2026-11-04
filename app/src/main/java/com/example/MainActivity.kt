package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.AddPlaylistSheet
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.IptvViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: IptvViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: IptvViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val channels by viewModel.filteredChannels.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylistId by viewModel.selectedPlaylistId.collectAsStateWithLifecycle()
    val favoriteChannels by viewModel.favoriteChannels.collectAsStateWithLifecycle()
    val recentChannels by viewModel.recentChannels.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isGridView by viewModel.isGridView.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var showAddSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // Handle system back navigation
    BackHandler(enabled = currentScreen != Screen.Channels) {
        when (currentScreen) {
            Screen.Player -> viewModel.navigateTo(Screen.Channels)
            Screen.Playlists -> viewModel.navigateTo(Screen.Channels)
            Screen.Favorites -> viewModel.navigateTo(Screen.Channels)
            Screen.Channels -> { /* exit activity handled by system */ }
        }
    }

    if (currentScreen == Screen.Player) {
        // Full screen video player
        VideoPlayerScreen(
            viewModel = viewModel,
            playerState = playerState,
            channelList = channels,
            onBack = { viewModel.navigateTo(Screen.Channels) }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Mini player bar when a channel is active
                    AnimatedVisibility(
                        visible = playerState.currentChannel != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        MiniPlayerBar(
                            playerState = playerState,
                            onExpand = { viewModel.navigateTo(Screen.Player) },
                            onTogglePlayPause = { viewModel.togglePlayPause() },
                            onClose = { viewModel.closePlayer() }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary,
                        tonalElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("main_bottom_nav")
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == Screen.Channels,
                            onClick = { viewModel.navigateTo(Screen.Channels) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.LiveTv,
                                    contentDescription = "Channels"
                                )
                            },
                            label = {
                                Text(
                                    text = "Channels",
                                    fontWeight = if (currentScreen == Screen.Channels) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = navigationItemColors(),
                            modifier = Modifier.testTag("nav_channels")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.Playlists,
                            onClick = { viewModel.navigateTo(Screen.Playlists) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.PlaylistPlay,
                                    contentDescription = "Playlists"
                                )
                            },
                            label = {
                                Text(
                                    text = "Playlists",
                                    fontWeight = if (currentScreen == Screen.Playlists) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = navigationItemColors(),
                            modifier = Modifier.testTag("nav_playlists")
                        )

                        NavigationBarItem(
                            selected = currentScreen == Screen.Favorites,
                            onClick = { viewModel.navigateTo(Screen.Favorites) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Favorites"
                                )
                            },
                            label = {
                                Text(
                                    text = "Favorites",
                                    fontWeight = if (currentScreen == Screen.Favorites) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = navigationItemColors(),
                            modifier = Modifier.testTag("nav_favorites")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    Screen.Channels -> {
                        ChannelsScreen(
                            viewModel = viewModel,
                            channels = channels,
                            playlists = playlists,
                            selectedPlaylistId = selectedPlaylistId,
                            recentChannels = recentChannels,
                            categories = categories,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            isGridView = isGridView,
                            currentPlayingId = playerState.currentChannel?.id,
                            onOpenAddSheet = { showAddSheet = true }
                        )
                    }

                    Screen.Playlists -> {
                        PlaylistsScreen(
                            viewModel = viewModel,
                            playlists = playlists,
                            selectedPlaylistId = selectedPlaylistId,
                            isLoading = isLoading,
                            onOpenAddSheet = { showAddSheet = true }
                        )
                    }

                    Screen.Favorites -> {
                        FavoritesScreen(
                            viewModel = viewModel,
                            favoriteChannels = favoriteChannels,
                            currentPlayingId = playerState.currentChannel?.id,
                            onNavigateToChannels = { viewModel.navigateTo(Screen.Channels) }
                        )
                    }

                    Screen.Player -> { /* Handled above */ }
                }

                // Global loading indicator overlay
                if (isLoading) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = DarkSurfaceElevated.copy(alpha = 0.95f),
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = ElectricCyan,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Loading IPTV Stream Data...",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Add Playlist / Stream Sheet
    if (showAddSheet) {
        AddPlaylistSheet(
            viewModel = viewModel,
            isLoading = isLoading,
            onDismiss = { showAddSheet = false }
        )
    }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = ElectricCyan,
    selectedTextColor = ElectricCyan,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
    indicatorColor = DarkSurfaceElevated
)
