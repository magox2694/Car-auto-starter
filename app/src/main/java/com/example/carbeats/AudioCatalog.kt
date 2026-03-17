package com.example.carbeats

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

object AudioCatalog {
    data class Track(
        val mediaId: String,
        val title: String,
        val artist: String,
        val album: String,
        val url: String
    )

    private val tracks = listOf(
        Track(
            mediaId = "demo-1",
            title = "CarBeats Demo",
            artist = "CarBeats",
            album = "Starter",
            url = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"
        ),
        Track(
            mediaId = "demo-2",
            title = "Acoustic Breeze",
            artist = "Demo Artist",
            album = "Road Sessions",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
        ),
        Track(
            mediaId = "demo-3",
            title = "Night Drive",
            artist = "Demo Artist",
            album = "Road Sessions",
            url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
        )
    )

    fun demoItem(): MediaItem {
        return toMediaItem(tracks.first())
    }

    fun demoPlaylist(): List<MediaItem> {
        return tracks.map(::toMediaItem)
    }

    fun allTracks(): List<Track> {
        return tracks
    }

    fun searchFirst(query: String): MediaItem? {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return null

        val match = tracks.firstOrNull {
            it.title.lowercase().contains(normalized) ||
                it.artist.lowercase().contains(normalized) ||
                it.album.lowercase().contains(normalized)
        }

        return match?.let(::toMediaItem)
    }

    fun searchTracks(query: String): List<Track> {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return emptyList()

        return tracks.filter {
            it.title.lowercase().contains(normalized) ||
                it.artist.lowercase().contains(normalized) ||
                it.album.lowercase().contains(normalized)
        }
    }

    fun findIndex(query: String): Int {
        val normalized = query.trim().lowercase()
        if (normalized.isEmpty()) return 0

        val index = tracks.indexOfFirst {
            it.title.lowercase().contains(normalized) ||
                it.artist.lowercase().contains(normalized) ||
                it.album.lowercase().contains(normalized)
        }

        return if (index >= 0) index else 0
    }

    fun toMediaItem(track: Track): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .build()

        return MediaItem.Builder()
            .setUri(track.url)
            .setMediaId(track.mediaId)
            .setMediaMetadata(metadata)
            .build()
    }
}
