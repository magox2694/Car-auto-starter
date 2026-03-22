package com.example.carbeats.search

class SearchRepository(
    private val providers: List<TrackSearchProvider>
) {
    fun search(query: String): List<TrackSearchResult> {
        if (query.isBlank()) return emptyList()

        val providerResults = providers.map { provider ->
            try {
                provider.search(query)
            } catch (_: Exception) {
                emptyList()
            }
        }

        val merged = mutableListOf<TrackSearchResult>()
        var round = 0
        while (merged.size < 12) {
            var addedInRound = false
            providerResults.forEach { results ->
                if (round < results.size) {
                    merged.add(results[round])
                    addedInRound = true
                }
            }
            if (!addedInRound) break
            round++
        }

        return merged
            .distinctBy { "${it.source}:${it.id}" }
            .take(12)
    }

    companion object {
        fun default(): SearchRepository {
            return SearchRepository(
                providers = listOf(
                    ItunesPreviewSearchProvider(),
                    YouTubeMetadataProvider()
                )
            )
        }
    }
}
