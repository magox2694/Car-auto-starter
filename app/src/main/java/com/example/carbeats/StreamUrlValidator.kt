package com.example.carbeats

import android.net.Uri

object StreamUrlValidator {
    fun normalizeHttpUrl(url: String?): String? {
        val rawUrl = url?.trim().orEmpty()
        if (rawUrl.isEmpty()) return null

        val uri = Uri.parse(rawUrl)
        val scheme = uri.scheme?.lowercase() ?: return null
        if (scheme != "http" && scheme != "https") return null
        if (uri.host.isNullOrBlank()) return null

        return uri.toString()
    }
}