package com.example.carbeats.search

import com.example.carbeats.AudioCatalog

class DemoTrackSearchProvider : TrackSearchProvider {
    override val providerName: String = "Demo"

    override fun search(query: String): ProviderSearchResult {
        val items = AudioCatalog.searchTracks(query).map {
            TrackSearchResult(
                id = it.mediaId,
                title = it.title,
                artist = it.artist,
                album = it.album,
                source = "demo",
                playable = true,
                streamUrl = it.url
            )
        }

        return ProviderSearchResult(
            items = items,
            status = SearchProviderStatus(
                providerName = providerName,
                state = if (items.isEmpty()) SearchProviderState.EMPTY else SearchProviderState.SUCCESS,
                resultCount = items.size
            )
        )
    }
}
