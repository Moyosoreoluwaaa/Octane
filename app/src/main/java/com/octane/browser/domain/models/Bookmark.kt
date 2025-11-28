package com.octane.browser.domain.models

data class Bookmark(
    val id: String,
    val url: String,
    val title: String,
    val faviconUrl: String? = null,
    val folder: String? = null,
    val createdAt: Long
)
