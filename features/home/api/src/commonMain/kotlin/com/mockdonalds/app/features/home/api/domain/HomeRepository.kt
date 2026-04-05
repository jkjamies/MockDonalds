package com.mockdonalds.app.features.home.api.domain

import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getPromotions(): Flow<List<Promotion>>
}

data class Promotion(
    val id: String,
    val title: String,
    val imageUrl: String,
)
