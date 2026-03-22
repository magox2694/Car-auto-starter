package com.example.carbeats.search

import com.example.carbeats.AudioCatalog

class DemoTrackSearchProvider : TrackSearchProvider {
    override fun search(query: String): List<TrackSearchResult> {
        return AudioCatalog.searchTracks(query).map {
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
    }
}
