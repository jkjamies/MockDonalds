package com.jkjamies.sampleplatter.features.nutrition.domain

import com.jkjamies.sampleplatter.features.nutrition.api.domain.NutritionContent
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun getNutrition(): Flow<NutritionContent>
}
