package com.mockdonalds.app.features.mobile.nutrition.domain

import com.mockdonalds.app.features.mobile.nutrition.api.domain.NutritionContent
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    fun getNutrition(): Flow<NutritionContent>
}
