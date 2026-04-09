package com.mockdonalds.app.features.profile.api.domain

data class ProfileContent(
    val name: String,
    val email: String,
    val tier: String,
    val points: String,
    val avatarUrl: String,
    val memberSince: String,
)
