package com.mockdonalds.app.features.home.domain

import com.mockdonalds.app.features.home.api.domain.Craving
import com.mockdonalds.app.features.home.api.domain.ExploreItem
import com.mockdonalds.app.features.home.api.domain.HeroPromotion
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getUserName(): Flow<String>
    fun getHeroPromotion(): Flow<HeroPromotion>
    fun getRecentCravings(): Flow<List<Craving>>
    fun getExploreItems(): Flow<List<ExploreItem>>
}
