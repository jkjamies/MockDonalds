package com.mockdonalds.app.features.more.api.domain

data class UserProfile(
    val name: String,
    val tier: String,
    val points: String,
    val avatarUrl: String,
)

data class MoreMenuItem(
    val id: String,
    val icon: String,
    val title: String,
)

data class MoreContent(
    val userProfile: UserProfile,
    val menuItems: List<MoreMenuItem>,
)
