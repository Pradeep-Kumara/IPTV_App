package com.example

import com.example.data.parser.M3uParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testM3uParserExtractsChannelInfo() {
    val sampleM3u = """
      #EXTM3U
      #EXTINF:-1 tvg-id="France24.fr" tvg-name="France 24" tvg-logo="https://example.com/logo.png" group-title="News",France 24 English
      https://static.france24.com/live/live.m3u8
      #EXTINF:-1 group-title="Sports",Red Bull TV
      https://rbmn-live.akamaized.net/master.m3u8
    """.trimIndent()

    val channels = M3uParser.parse(sampleM3u, playlistId = 1L)
    assertEquals(2, channels.size)

    val first = channels[0]
    assertEquals("France 24 English", first.name)
    assertEquals("News", first.groupTitle)
    assertEquals("https://static.france24.com/live/live.m3u8", first.streamUrl)
    assertEquals("https://example.com/logo.png", first.logoUrl)
    assertEquals("France24.fr", first.tvgId)

    val second = channels[1]
    assertEquals("Red Bull TV", second.name)
    assertEquals("Sports", second.groupTitle)
  }

  @Test
  fun testM3uParserHandlesUnquotedAttributes() {
    val sampleM3u = """
      #EXTM3U
      #EXTINF:-1 group-title=Entertainment tvg-logo=https://example.com/art.jpg,Retro TV
      http://stream.example.com/live.m3u8
    """.trimIndent()

    val channels = M3uParser.parse(sampleM3u, playlistId = 42L)
    assertEquals(1, channels.size)
    assertEquals("Retro TV", channels[0].name)
    assertEquals("Entertainment", channels[0].groupTitle)
    assertEquals("https://example.com/art.jpg", channels[0].logoUrl)
  }

  @Test
  fun testInitialChannelsNotEmpty() {
    val channels = com.example.data.sample.SampleChannels.getInitialChannels(1L)
    assertTrue(channels.isNotEmpty())
    channels.forEach { ch ->
      assertTrue(ch.streamUrl.startsWith("http"))
      assertTrue(ch.name.isNotBlank())
    }
  }
}
