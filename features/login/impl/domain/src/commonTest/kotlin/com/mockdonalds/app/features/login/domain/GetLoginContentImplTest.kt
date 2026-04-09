package com.mockdonalds.app.features.login.domain

import app.cash.turbine.test
import com.mockdonalds.app.features.login.api.domain.LoginContent
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GetLoginContentImplTest : BehaviorSpec({

    Given("a login content interactor with repository data") {
        val loginContent = MutableStateFlow(
            LoginContent(logoUrl = "https://example.com/logo.png"),
        )

        val repository = object : LoginRepository {
            override fun getLoginContent(): Flow<LoginContent> = loginContent
        }

        val interactor = GetLoginContentImpl(repository)

        When("the interactor is invoked and flow is collected") {
            Then("it should emit login content from the repository") {
                interactor(Unit)
                interactor.flow.test {
                    val content = awaitItem()
                    content shouldBe LoginContent(logoUrl = "https://example.com/logo.png")
                }
            }
        }

        When("the repository data updates") {
            Then("it should emit updated content") {
                interactor(Unit)
                interactor.flow.test {
                    awaitItem()
                    loginContent.value = LoginContent(logoUrl = "https://example.com/updated.png")
                    val updated = awaitItem()
                    updated.logoUrl shouldBe "https://example.com/updated.png"
                }
            }
        }
    }
})
