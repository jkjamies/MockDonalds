package com.mockdonalds.app.features.nutrition.data

import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import com.mockdonalds.app.features.nutrition.api.domain.NutritionContent
import com.mockdonalds.app.features.nutrition.domain.NutritionRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class NutritionRepositoryImpl(
    private val appBuildConfig: AppBuildConfig,
) : NutritionRepository {
    override fun getNutrition(): Flow<NutritionContent> = flowOf(
        NutritionContent(url = appBuildConfig.nutritionUrl),
    )
}
