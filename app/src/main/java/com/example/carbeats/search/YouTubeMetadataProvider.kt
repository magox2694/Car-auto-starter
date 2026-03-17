package com.example.carbeats.search

import com.example.carbeats.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class YouTubeMetadataProvider : TrackSearchProvider {
    override fun search(query: String): List<TrackSearchResult> {
        val apiKey = BuildConfig.YOUTUBE_API_KEY
        if (apiKey.isBlank()) return emptyList()

        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        val endpoint =
            "https://www.googleapis.com/youtube/v3/search" +
                "?part=snippet&type=video&maxResults=5&q=$encodedQuery&key=$apiKey"

        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
        }

        return try {
            val code = connection.responseCode
            if (code !in 200..299) return emptyList()

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val items = json.optJSONArray("items") ?: return emptyList()
            val results = mutableListOf<TrackSearchResult>()

            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val idObject = item.optJSONObject("id") ?: continue
                val videoId = idObject.optString("videoId")
                if (videoId.isBlank()) continue

                val snippet = item.optJSONObject("snippet") ?: continue
                val title = snippet.optString("title", "Unknown")
                val channel = snippet.optString("channelTitle", "YouTube")

                results.add(
                    TrackSearchResult(
                        id = videoId,
                        title = title,
                        artist = channel,
                        album = "YouTube",
                        source = "youtube",
                        playable = false
                    )
                )
            }

            results
        } finally {
            connection.disconnect()
        }
    }
}
