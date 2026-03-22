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
                "?part=snippet&type=video" +
                "&videoEmbeddable=true" +
                "&videoSyndicated=true" +
                "&regionCode=IT" +
                "&maxResults=12" +
                "&q=$encodedQuery" +
                "&key=$apiKey"

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

            val candidateIds = mutableListOf<String>()
            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val idObject = item.optJSONObject("id") ?: continue
                val videoId = idObject.optString("videoId")
                if (videoId.isNotBlank()) {
                    candidateIds.add(videoId)
                }
            }

            val allowedIds = loadEmbeddableIds(candidateIds, apiKey)
            if (allowedIds.isEmpty()) return emptyList()

            val results = mutableListOf<TrackSearchResult>()

            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val idObject = item.optJSONObject("id") ?: continue
                val videoId = idObject.optString("videoId")
                if (videoId.isBlank()) continue
                if (!allowedIds.contains(videoId)) continue

                val snippet = item.optJSONObject("snippet") ?: continue
                val title = snippet.optString("title", "Unknown")
                val channel = snippet.optString("channelTitle", "YouTube")
                val thumbnails = snippet.optJSONObject("thumbnails")
                val artworkUrl =
                    thumbnails?.optJSONObject("medium")?.optString("url")
                        ?: thumbnails?.optJSONObject("default")?.optString("url")

                results.add(
                    TrackSearchResult(
                        id = videoId,
                        title = title,
                        artist = channel,
                        album = "YouTube",
                        source = "youtube",
                        playable = true,
                        artworkUrl = artworkUrl
                    )
                )
            }

            results
        } finally {
            connection.disconnect()
        }
    }

    private fun loadEmbeddableIds(videoIds: List<String>, apiKey: String): Set<String> {
        if (videoIds.isEmpty()) return emptySet()

        val ids = videoIds.joinToString(",")
        val endpoint =
            "https://www.googleapis.com/youtube/v3/videos" +
                "?part=status" +
                "&id=$ids" +
                "&key=$apiKey"

        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
        }

        return try {
            val code = connection.responseCode
            if (code !in 200..299) return emptySet()

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val items = json.optJSONArray("items") ?: return emptySet()
            val allowed = mutableSetOf<String>()

            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val id = item.optString("id")
                if (id.isBlank()) continue

                val status = item.optJSONObject("status") ?: continue
                if (!status.optBoolean("embeddable", false)) continue

                val regionRestriction = status.optJSONObject("regionRestriction")
                if (regionRestriction != null) {
                    val blocked = regionRestriction.optJSONArray("blocked")
                    if (blocked != null) {
                        var blockedInItaly = false
                        for (j in 0 until blocked.length()) {
                            if (blocked.optString(j).equals("IT", ignoreCase = true)) {
                                blockedInItaly = true
                                break
                            }
                        }
                        if (blockedInItaly) continue
                    }

                    val allowedRegions = regionRestriction.optJSONArray("allowed")
                    if (allowedRegions != null && allowedRegions.length() > 0) {
                        var italyAllowed = false
                        for (j in 0 until allowedRegions.length()) {
                            if (allowedRegions.optString(j).equals("IT", ignoreCase = true)) {
                                italyAllowed = true
                                break
                            }
                        }
                        if (!italyAllowed) continue
                    }
                }

                allowed.add(id)
            }

            allowed
        } finally {
            connection.disconnect()
        }
    }
}
