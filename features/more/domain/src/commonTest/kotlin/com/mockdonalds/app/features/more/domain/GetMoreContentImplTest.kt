package com.mockdonalds.app.features.more.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.more.api.domain.MoreContent
import com.mockdonalds.app.features.more.api.domain.MoreMenuItem
import com.mockdonalds.app.features.more.api.domain.UserProfile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GetMoreContentImplTest : BehaviorSpec({

    Given("a more content interactor with repository data") {
        val userProfile = MutableStateFlow(
            UserProfile(
                name = "Test User",
                tier = "Gold Member",
                points = "1,000 pts",
                avatarUrl = "",
            ),
        )
        val menuItems = MutableStateFlow(
            listOf(
                MoreMenuItem(id = "1", icon = "🕒", title = "Recents"),
            ),
        )

        val repository = object : MoreRepository {
            override fun getUserProfile(): Flow<UserProfile> = userProfile
            override fun getMenuItems(): Flow<List<MoreMenuItem>> = menuItems
        }

        val interactor = GetMoreContentImpl(repository)

        When("the interactor is invoked and flow is collected") {
            Then("it should combine all repository data into MoreContent") {
                interactor(Unit)
                interactor.flow.test {
                    val content = awaitItem()
                    content shouldBe MoreContent(
                        userProfile = userProfile.value,
                        menuItems = menuItems.value,
                    )
                }
            }
        }

        When("the repository user profile updates") {
            Then("it should emit updated content") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem()
                    userProfile.value = userProfile.value.copy(name = "Updated User")
                    val updated = awaitItem()
                    updated.userProfile.name shouldBe "Updated User"
                }
            }
        }
    }
})
