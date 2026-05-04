package com.mockdonalds.app.features.kiosk.attract.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.kiosk.attract.api.domain.Ad
import com.mockdonalds.app.features.kiosk.attract.api.domain.AttractContent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GetAttractContentImplTest : BehaviorSpec({

    Given("a kiosk-attract use case backed by a fake repository") {
        val initial = AttractContent(
            ads = listOf(Ad(id = "1", imageUrl = "https://example.test/1.png", headline = "TEST", subheadline = null)),
            rotationSeconds = 5,
        )
        val source = MutableStateFlow(initial)
        val repository = object : AttractRepository {
            override fun getAttractContent(): Flow<AttractContent> = source
        }
        val interactor = GetAttractContentImpl(repository)

        When("the interactor is invoked and the flow is collected") {
            Then("it should forward the repository content unchanged") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem() shouldBe initial
                }
            }
        }

        When("the repository emits new content") {
            Then("the use case forwards the update") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem()
                    val updated = AttractContent(
                        ads = listOf(
                            Ad(id = "1", imageUrl = "https://example.test/1.png", headline = "FIRST", subheadline = null),
                            Ad(id = "2", imageUrl = "https://example.test/2.png", headline = "SECOND", subheadline = "Order Here"),
                        ),
                        rotationSeconds = 10,
                    )
                    source.value = updated
                    awaitItem() shouldBe updated
                }
            }
        }
    }
})
