package com.mockdonalds.app.features.profile.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.profile.api.domain.ProfileContent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GetProfileContentImplTest : BehaviorSpec({

    Given("a profile content interactor with repository data") {
        val profileContent = ProfileContent(
            name = "Night Owl",
            email = "gourmet@night.com",
            tier = "Gold",
            points = "4,280 pts",
            avatarUrl = "",
            memberSince = "Member since 2024",
        )

        val repository = object : ProfileRepository {
            override fun getProfile(): Flow<ProfileContent> = flowOf(profileContent)
        }

        val interactor = GetProfileContentImpl(repository)

        When("the interactor is invoked and flow is collected") {
            Then("it should emit the profile content from the repository") {
                interactor(Unit)
                interactor.flow.test {
                    val content = awaitItem()
                    content shouldBe profileContent
                }
            }
        }
    }
})
