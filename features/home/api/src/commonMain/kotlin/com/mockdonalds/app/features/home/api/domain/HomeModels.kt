package com.mockdonalds.app.features.home.api.domain

data class Promotion(
    val id: String,
    val title: String,
    val imageUrl: String,
)

data class HeroPromotion(
    val title: String,
    val description: String,
    val tag: String,
    val imageUrl: String,
    val ctaText: String,
)

data class Craving(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
)

data class ExploreItem(
    val id: String,
    val icon: String,
    val title: String,
    val subtitle: String,
)

data class HomeContent(
    val userName: String,
    val heroPromotion: HeroPromotion,
    val recentCravings: List<Craving>,
    val exploreItems: List<ExploreItem>,
)
