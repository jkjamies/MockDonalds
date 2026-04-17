package com.mockdonalds.app.features.recents.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.recents.api.domain.RecentItem
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetRecentsContentImplTest : BehaviorSpec({

    Given("a GetRecentsContentImpl") {
        val repository = object : RecentsRepository {
            override fun getRecentItems(): Flow<List<RecentItem>> = flowOf(
                listOf(
                    RecentItem("1", "A", "B", "C", null)
                )
            )
        }
        val impl = GetRecentsContentImpl(repository)

        When("observing content") {
            Then("it should combine repository flows correctly") {
                impl(Unit)
                impl.flow.test {
                    val result = awaitItem()
                    result.items.size shouldBe 1
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
