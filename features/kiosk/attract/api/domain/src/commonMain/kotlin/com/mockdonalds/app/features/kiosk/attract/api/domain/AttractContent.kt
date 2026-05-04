package com.mockdonalds.app.features.kiosk.attract.api.domain

data class AttractContent(
    val ads: List<Ad>,
    val rotationSeconds: Int,
)

data class Ad(
    val id: String,
    val imageUrl: String,
    val headline: String,
    val subheadline: String?,
)
