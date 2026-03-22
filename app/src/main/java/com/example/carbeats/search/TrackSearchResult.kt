package com.example.carbeats.search

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrackSearchResult(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val source: String,
    val playable: Boolean,
    val streamUrl: String? = null,
    val artworkUrl: String? = null
) : Parcelable
