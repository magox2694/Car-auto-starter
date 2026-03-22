package com.example.carbeats.search

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ItunesPreviewSearchProvider : TrackSearchProvider {
    override fun search(query: String): List<TrackSearchResult> {
        if (query.isBlank()) return emptyList()

        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        val endpoint =
            "https://itunes.apple.com/search" +
                "?term=$encodedQuery&entity=song&limit=10"

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
            val items = json.optJSONArray("results") ?: return emptyList()
            val results = mutableListOf<TrackSearchResult>()

            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val previewUrl = item.optString("previewUrl")
                if (previewUrl.isBlank()) continue

                val trackId = item.optLong("trackId")
                val title = item.optString("trackName", "Unknown")
                val artist = item.optString("artistName", "Unknown")
                val album = item.optString("collectionName", "iTunes")
                val artwork = item.optString("artworkUrl100")

                results.add(
                    TrackSearchResult(
                        id = "itunes-$trackId",
                        title = title,
                        artist = artist,
                        album = album,
                        source = "itunes",
                        playable = true,
                        streamUrl = previewUrl,
                        artworkUrl = artwork.ifBlank { null }
                    )
                )
            }

            results
        } finally {
            connection.disconnect()
        }
    }
}
