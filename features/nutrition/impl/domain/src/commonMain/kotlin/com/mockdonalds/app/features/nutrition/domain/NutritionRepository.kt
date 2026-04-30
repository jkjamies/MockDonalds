package com.mockdonalds.app.features.nutrition.domain

import com.mockdonalds.app.features.nutrition.api.domain.NutritionContent
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun getNutrition(): Flow<NutritionContent>
}
