package com.example.carbeats.search

interface TrackSearchProvider {
    fun search(query: String): List<TrackSearchResult>
}
