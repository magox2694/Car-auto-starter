package com.example.carbeats.search

data class SearchResponse(
    val results: List<TrackSearchResult>,
    val providerStatuses: List<SearchProviderStatus>,
    val fromCache: Boolean
) {
    val hasProviderErrors: Boolean
        get() = providerStatuses.any { it.state == SearchProviderState.ERROR }

    val hasDisabledProviders: Boolean
        get() = providerStatuses.any { it.state == SearchProviderState.DISABLED }

    val unavailableProviderCount: Int
        get() = providerStatuses.count {
            it.state == SearchProviderState.ERROR || it.state == SearchProviderState.DISABLED
        }
}

data class SearchProviderStatus(
    val providerName: String,
    val state: SearchProviderState,
    val resultCount: Int,
    val message: String? = null
)

enum class SearchProviderState {
    SUCCESS,
    EMPTY,
    DISABLED,
    ERROR
}

data class ProviderSearchResult(
    val items: List<TrackSearchResult>,
    val status: SearchProviderStatus
)