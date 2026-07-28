package com.jkjamies.sampleplatter.features.home.domain

import com.jkjamies.sampleplatter.features.home.api.domain.Craving
import com.jkjamies.sampleplatter.features.home.api.domain.ExploreItem
import com.jkjamies.sampleplatter.features.home.api.domain.HeroPromotion
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getUserName(): Flow<String>
    fun getHeroPromotion(): Flow<HeroPromotion>
    fun getRecentCravings(): Flow<List<Craving>>
    fun getExploreItems(): Flow<List<ExploreItem>>
}
