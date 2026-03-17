package com.example.carbeats.search

class SearchRepository(
    private val providers: List<TrackSearchProvider>
) {
    fun search(query: String): List<TrackSearchResult> {
        if (query.isBlank()) return emptyList()

        return providers
            .flatMap { provider ->
                try {
                    provider.search(query)
                } catch (_: Exception) {
                    emptyList()
                }
            }
            .distinctBy { "${it.source}:${it.id}" }
            .take(10)
    }

    companion object {
        fun default(): SearchRepository {
            return SearchRepository(
                providers = listOf(
                    DemoTrackSearchProvider(),
                    YouTubeMetadataProvider()
                )
            )
        }
    }
}
