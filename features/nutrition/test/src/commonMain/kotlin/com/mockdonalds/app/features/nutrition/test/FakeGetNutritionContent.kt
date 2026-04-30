package com.mockdonalds.app.features.nutrition.test

import com.mockdonalds.app.features.nutrition.api.domain.GetNutritionContent
import com.mockdonalds.app.features.nutrition.api.domain.NutritionContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
class FakeGetNutritionContent(
    initial: NutritionContent = DEFAULT,
) : GetNutritionContent() {
    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<NutritionContent> = _content

    fun emit(content: NutritionContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = NutritionContent(url = "https://example.test/nutrition")
    }
}
