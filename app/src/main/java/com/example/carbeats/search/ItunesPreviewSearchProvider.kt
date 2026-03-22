package com.example.carbeats.search

import com.example.carbeats.StreamUrlValidator
import org.json.JSONObject
import java.net.URLEncoder

class ItunesPreviewSearchProvider : TrackSearchProvider {
    override val providerName: String = "iTunes"

    override fun search(query: String): ProviderSearchResult {
        if (query.isBlank()) {
            return ProviderSearchResult(
                items = emptyList(),
                status = SearchProviderStatus(providerName, SearchProviderState.EMPTY, 0)
            )
        }

        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        val endpoint =
            "https://itunes.apple.com/search" +
                "?term=$encodedQuery&entity=song&limit=10"

        val body = SearchHttpClient.get(endpoint)
        val json = JSONObject(body)
        val items = json.optJSONArray("results")
        val results = mutableListOf<TrackSearchResult>()

        if (items != null) {
            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val previewUrl = StreamUrlValidator.normalizeHttpUrl(item.optString("previewUrl"))
                    ?: continue

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
        }

        return ProviderSearchResult(
            items = results,
            status = SearchProviderStatus(
                providerName = providerName,
                state = if (results.isEmpty()) SearchProviderState.EMPTY else SearchProviderState.SUCCESS,
                resultCount = results.size
            )
        )
    }
}
