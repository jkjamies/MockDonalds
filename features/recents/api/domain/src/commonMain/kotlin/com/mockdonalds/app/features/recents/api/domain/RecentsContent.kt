package com.mockdonalds.app.features.recents.api.domain

data class RecentsContent(
    val items: List<RecentItem>,
)

data class RecentItem(
    val id: String,
    val name: String,
    val description: String,
    val relativeTime: String,
    val imageUrl: String?,
)
