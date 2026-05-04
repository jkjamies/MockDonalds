package com.mockdonalds.app.features.mobile.nutrition.data

import com.mockdonalds.app.core.buildconfig.test.FakeAppBuildConfig
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class NutritionRepositoryImplTest : BehaviorSpec({

    Given("a NutritionRepositoryImpl with a configured nutrition URL") {
        val sentinelUrl = "https://example.test/nutrition"
        val buildConfig = FakeAppBuildConfig().apply {
            nutritionUrl = sentinelUrl
        }
        val repository = NutritionRepositoryImpl(buildConfig)

        When("getNutrition is collected") {
            Then("it emits a single NutritionContent with the configured URL") {
                val result = repository.getNutrition().first()
                result.url shouldBe sentinelUrl
            }
        }
    }
})
