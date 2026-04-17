package com.mockdonalds.app.features.recents.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first

class RecentsRepositoryImplTest : BehaviorSpec({

    Given("a RecentsRepositoryImpl") {
        val repository = RecentsRepositoryImpl()

        When("fetching data") {
            Then("it should return expected values") {
                val result = repository.getRecentItems().first()
                result.size shouldBe 5
            }
        }
    }
})