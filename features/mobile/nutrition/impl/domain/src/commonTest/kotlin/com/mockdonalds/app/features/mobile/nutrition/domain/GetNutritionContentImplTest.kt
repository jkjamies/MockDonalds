package com.mockdonalds.app.features.mobile.nutrition.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.mobile.nutrition.api.domain.NutritionContent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetNutritionContentImplTest : BehaviorSpec({

    Given("a GetNutritionContentImpl") {
        val sentinelUrl = "https://example.test/nutrition"
        val repository = object : NutritionRepository {
            override fun getNutrition(): Flow<NutritionContent> =
                flowOf(NutritionContent(url = sentinelUrl))
        }
        val impl = GetNutritionContentImpl(repository)

        When("observing content") {
            Then("it forwards the repository emission unchanged") {
                impl(Unit)
                impl.flow.test {
                    val result = awaitItem()
                    result.url shouldBe sentinelUrl
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
