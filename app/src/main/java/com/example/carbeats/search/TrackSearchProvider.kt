package com.example.carbeats.search

interface TrackSearchProvider {
    val providerName: String

    fun search(query: String): ProviderSearchResult
}
