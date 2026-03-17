package com.example.carbeats.search

data class TrackSearchResult(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val source: String,
    val playable: Boolean
)
