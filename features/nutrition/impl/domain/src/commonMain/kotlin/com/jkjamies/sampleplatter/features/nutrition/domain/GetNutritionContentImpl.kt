package com.jkjamies.sampleplatter.features.nutrition.domain

import com.jkjamies.sampleplatter.features.nutrition.api.domain.GetNutritionContent
import com.jkjamies.sampleplatter.features.nutrition.api.domain.NutritionContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
class GetNutritionContentImpl(
    private val repository: NutritionRepository,
) : GetNutritionContent() {
    override fun createObservable(params: Unit): Flow<NutritionContent> {
        return repository.getNutrition()
    }
}
