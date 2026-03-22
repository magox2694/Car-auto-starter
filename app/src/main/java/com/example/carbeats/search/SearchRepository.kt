package com.example.carbeats.search

class SearchRepository(
    private val providers: List<TrackSearchProvider>
) {
    fun searchDetails(query: String): SearchResponse {
        val normalizedQuery = query.trim().replace(Regex("\\s+"), " ")
        if (normalizedQuery.isBlank()) {
            return SearchResponse(emptyList(), emptyList(), fromCache = false)
        }

        readFromCache(normalizedQuery)?.let { cached ->
            return cached.copy(fromCache = true)
        }

        val providerResults = providers.map { provider ->
            try {
                provider.search(normalizedQuery)
            } catch (exception: Exception) {
                ProviderSearchResult(
                    items = emptyList(),
                    status = SearchProviderStatus(
                        providerName = provider.providerName,
                        state = SearchProviderState.ERROR,
                        resultCount = 0,
                        message = exception.message ?: "Provider non disponibile"
                    )
                )
            }
        }

        val merged = mergeProviderResults(providerResults.map { it.items })
            .distinctBy { "${it.source}:${it.id}" }
            .take(12)

        val response = SearchResponse(
            results = merged,
            providerStatuses = providerResults.map { it.status },
            fromCache = false
        )

        storeInCache(normalizedQuery, response)
        return response
    }

    fun search(query: String): List<TrackSearchResult> {
        return searchDetails(query).results
    }

    private fun mergeProviderResults(providerResults: List<List<TrackSearchResult>>): List<TrackSearchResult> {
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
    }

    private fun readFromCache(query: String): SearchResponse? {
        val now = System.currentTimeMillis()
        synchronized(cacheLock) {
            val entry = cache[query] ?: return null
            if (now - entry.timestampMs > cacheTtlMs) {
                cache.remove(query)
                return null
            }
            return entry.response
        }
    }

    private fun storeInCache(query: String, response: SearchResponse) {
        synchronized(cacheLock) {
            cache[query] = CacheEntry(response = response, timestampMs = System.currentTimeMillis())
        }
    }

    private data class CacheEntry(
        val response: SearchResponse,
        val timestampMs: Long
    )

    companion object {
        private const val cacheTtlMs = 5 * 60 * 1000L
        private val cacheLock = Any()
        private val cache = object : LinkedHashMap<String, CacheEntry>(24, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean {
                return size > 24
            }
        }

        @Volatile
        private var defaultInstance: SearchRepository? = null

        fun default(): SearchRepository {
            return defaultInstance ?: synchronized(this) {
                defaultInstance ?: SearchRepository(
                    providers = listOf(
                        ItunesPreviewSearchProvider(),
                        YouTubeMetadataProvider()
                    )
                )
                    .also { defaultInstance = it }
            }
        }
    }
}
