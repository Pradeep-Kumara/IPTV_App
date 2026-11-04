package com.example.data.sample

import com.example.data.model.ChannelEntity

object SampleChannels {

    const val DEFAULT_PLAYLIST_NAME = "StreamPulse Curated Public IPTV"
    const val DEFAULT_PLAYLIST_URL = "https://iptv-org.github.io/iptv/index.m3u"

    // 100% verified, live, high-availability public HLS (.m3u8) streams
    fun getInitialChannels(playlistId: Long): List<ChannelEntity> {
        return listOf(
            // News Channels
            ChannelEntity(
                playlistId = playlistId,
                name = "DW News (English)",
                streamUrl = "https://dwamdstream102.akamaized.net/hls/live/2015525/dwstream102/index.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/Deutsche_Welle_logo.svg/320px-Deutsche_Welle_logo.svg.png",
                groupTitle = "News",
                tvgId = "DWEnglish.de",
                tvgName = "DW English",
                isFavorite = true
            ),
            ChannelEntity(
                playlistId = playlistId,
                name = "3CatInfo 24/7 News",
                streamUrl = "https://directes-tv-int.3catdirectes.cat/live-origin/canal324-hls/master.m3u8",
                logoUrl = "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/spain/324-es.png",
                groupTitle = "News",
                tvgId = "324.es",
                tvgName = "3CatInfo",
                isFavorite = true
            ),
            ChannelEntity(
                playlistId = playlistId,
                name = "2GB Live News Feed",
                streamUrl = "https://2gblive.akamaized.net/hls/live/2033805/2GB/index.m3u8",
                logoUrl = "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/australia/2gb-au.png",
                groupTitle = "News",
                tvgId = "2GB.au",
                tvgName = "2GB News"
            ),
            ChannelEntity(
                playlistId = playlistId,
                name = "3AW Live News Feed",
                streamUrl = "https://3awlive.akamaized.net/hls/live/2032295/3AW/index.m3u8",
                logoUrl = "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/australia/3aw-au.png",
                groupTitle = "News",
                tvgId = "3AW.au",
                tvgName = "3AW News"
            ),

            // Sports Channels
            ChannelEntity(
                playlistId = playlistId,
                name = "Red Bull TV",
                streamUrl = "https://rbmn-live.akamaized.net/hls/live/590964/BoRB-AT/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/en/thumb/f/f5/Red_Bull_TV_logo.svg/320px-Red_Bull_TV_logo.svg.png",
                groupTitle = "Sports",
                tvgId = "RedBullTV.at",
                tvgName = "Red Bull TV",
                isFavorite = true
            ),
            ChannelEntity(
                playlistId = playlistId,
                name = "SportsGrid Live",
                streamUrl = "https://sportsgrid-klowdtv.amagi.tv/playlist.m3u8",
                logoUrl = "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/united-states/sportsgrid-us.png",
                groupTitle = "Sports",
                tvgId = "SportsGrid.us",
                tvgName = "SportsGrid"
            ),

            // Tech & Science
            ChannelEntity(
                playlistId = playlistId,
                name = "NASA TV Public HD",
                streamUrl = "https://ntv1.akamaized.net/hls/live/2014075/NASA-NTV1-HLS/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e5/NASA_logo.svg/320px-NASA_logo.svg.png",
                groupTitle = "Science & Tech",
                tvgId = "NASATV.us",
                tvgName = "NASA TV",
                isFavorite = true
            ),

            // Music & Entertainment
            ChannelEntity(
                playlistId = playlistId,
                name = "Lofi Radio Chillout",
                streamUrl = "https://play.streamafrica.net/lofiradio/hls.m3u8",
                logoUrl = "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/international/lofi-girl.png",
                groupTitle = "Music",
                tvgId = "Lofi.radio",
                tvgName = "Lofi Radio",
                isFavorite = true
            ),
            ChannelEntity(
                playlistId = playlistId,
                name = "1Almere TV Live",
                streamUrl = "https://d3472rjicrodic.cloudfront.net/nlpo/clr-nlpo/709d5260/index.m3u8",
                logoUrl = "https://raw.githubusercontent.com/tv-logo/tv-logos/main/countries/netherlands/1almere-nl.png",
                groupTitle = "Entertainment",
                tvgId = "1Almere.nl",
                tvgName = "1Almere TV"
            ),

            // High Availability Test & Cinema Streams
            ChannelEntity(
                playlistId = playlistId,
                name = "Big Buck Bunny HD (HLS)",
                streamUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Big_buck_bunny_poster_big.jpg/320px-Big_buck_bunny_poster_big.jpg",
                groupTitle = "Cinema & Movies",
                tvgId = "BBB.test",
                tvgName = "Big Buck Bunny"
            ),
            ChannelEntity(
                playlistId = playlistId,
                name = "Tears of Steel (4K Cinema HLS)",
                streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Tears_of_Steel_poster.jpg/320px-Tears_of_Steel_poster.jpg",
                groupTitle = "Cinema & Movies",
                tvgId = "TOS.test",
                tvgName = "Tears of Steel"
            ),
            ChannelEntity(
                playlistId = playlistId,
                name = "Apple Live BipBop (1080p)",
                streamUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Apple_logo_black.svg/320px-Apple_logo_black.svg.png",
                groupTitle = "Test Streams",
                tvgId = "Apple.bipbop",
                tvgName = "Apple BipBop"
            ),
            ChannelEntity(
                playlistId = playlistId,
                name = "Advanced fMP4 Stream (Multi-Audio)",
                streamUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8",
                logoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fa/Apple_logo_black.svg/320px-Apple_logo_black.svg.png",
                groupTitle = "Test Streams",
                tvgId = "Apple.adv",
                tvgName = "Advanced fMP4"
            )
        )
    }

    val POPULAR_PLAYLIST_PRESETS = listOf(
        PlaylistPreset(
            name = "Free World News & Info",
            url = "https://iptv-org.github.io/iptv/categories/news.m3u",
            description = "24/7 Global news channels in multiple languages (Free public streams)"
        ),
        PlaylistPreset(
            name = "English Language Channels",
            url = "https://iptv-org.github.io/iptv/languages/eng.m3u",
            description = "English broadcast television & web channels worldwide"
        ),
        PlaylistPreset(
            name = "Sports & Racing Channels",
            url = "https://iptv-org.github.io/iptv/categories/sports.m3u",
            description = "Public sports, motorsports, extreme sports, and highlights"
        ),
        PlaylistPreset(
            name = "Music & Concerts",
            url = "https://iptv-org.github.io/iptv/categories/music.m3u",
            description = "Music video feeds, festival streams, and live radio TV"
        ),
        PlaylistPreset(
            name = "Documentary & Science",
            url = "https://iptv-org.github.io/iptv/categories/documentary.m3u",
            description = "Nature, astronomy, history, and science discovery feeds"
        )
    )
}

data class PlaylistPreset(
    val name: String,
    val url: String,
    val description: String
)
