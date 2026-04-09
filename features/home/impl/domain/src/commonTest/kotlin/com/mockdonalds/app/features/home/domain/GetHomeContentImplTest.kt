package com.mockdonalds.app.features.home.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.home.api.domain.Craving
import com.mockdonalds.app.features.home.api.domain.ExploreItem
import com.mockdonalds.app.features.home.api.domain.HeroPromotion
import com.mockdonalds.app.features.home.api.domain.HomeContent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GetHomeContentImplTest : BehaviorSpec({

    Given("a home content interactor with repository data") {
        val userName = MutableStateFlow("TestUser")
        val heroPromotion = MutableStateFlow(
            HeroPromotion(
                title = "Test Promo",
                description = "Desc",
                tag = "NEW",
                imageUrl = "https://example.com/img.png",
                ctaText = "Order Now",
            ),
        )
        val cravings = MutableStateFlow(
            listOf(Craving(id = "1", title = "Big Mac", subtitle = "Classic", imageUrl = "")),
        )
        val exploreItems = MutableStateFlow(
            listOf(ExploreItem(id = "1", icon = "star", title = "Deals", subtitle = "Save more")),
        )

        val repository = object : HomeRepository {
            override fun getUserName(): Flow<String> = userName
            override fun getHeroPromotion(): Flow<HeroPromotion> = heroPromotion
            override fun getRecentCravings(): Flow<List<Craving>> = cravings
            override fun getExploreItems(): Flow<List<ExploreItem>> = exploreItems
        }

        val interactor = GetHomeContentImpl(repository)

        When("the interactor is invoked and flow is collected") {
            Then("it should combine all repository data into HomeContent") {
                interactor(Unit)
                interactor.flow.test {
                    val content = awaitItem()
                    content shouldBe HomeContent(
                        userName = "TestUser",
                        heroPromotion = heroPromotion.value,
                        recentCravings = cravings.value,
                        exploreItems = exploreItems.value,
                    )
                }
            }
        }

        When("the repository userName updates") {
            Then("it should emit updated content") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem()
                    userName.value = "UpdatedUser"
                    val updated = awaitItem()
                    updated.userName shouldBe "UpdatedUser"
                }
            }
        }
    }
})
