package com.mockdonalds.app.core.auth

import com.mockdonalds.app.core.test.FakeRefreshTokenSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class InMemoryAuthManagerTest : BehaviorSpec({

    val newTokens = AuthTokens(accessToken = "new-access", refreshToken = "new-refresh")

    Given("an authenticated InMemoryAuthManager") {

        When("multiple concurrent refresh() calls are made") {
            Then("the RefreshTokenSource is invoked exactly once and all callers receive the same tokens") {
                val source = FakeRefreshTokenSource(result = newTokens).apply {
                    holdNextCallsUntilReleased()
                }
                val manager = InMemoryAuthManager(source).apply { login() }

                val results = coroutineScope {
                    // UNDISPATCHED so each async body runs to its first suspension before
                    // release() is called — that puts one call through AuthManager's mutex
                    // as leader and the other 9 park on the leader's deferred. Without this,
                    // the dispatcher may not schedule any body until after release(), at
                    // which point the gate is null and every call becomes its own leader.
                    val deferreds = List(10) {
                        async(start = CoroutineStart.UNDISPATCHED) { manager.refresh() }
                    }
                    source.release()
                    deferreds.awaitAll()
                }

                source.callCount shouldBe 1
                results shouldHaveSize 10
                results.forEach { it shouldBe newTokens }
                manager.currentTokens() shouldBe newTokens
                manager.isAuthenticated shouldBe true
            }
        }

        When("the refresh source returns null") {
            Then("tokens are cleared and isAuthenticated flips to false") {
                val source = FakeRefreshTokenSource(result = null)
                val manager = InMemoryAuthManager(source).apply { login() }

                manager.refresh() shouldBe null
                manager.currentTokens() shouldBe null
                manager.isAuthenticated shouldBe false
            }
        }

        When("refresh succeeds") {
            Then("tokens and isAuthenticated are updated") {
                val source = FakeRefreshTokenSource(result = newTokens)
                val manager = InMemoryAuthManager(source).apply { login() }

                manager.refresh() shouldBe newTokens
                manager.currentTokens() shouldBe newTokens
                manager.isAuthenticated shouldBe true
                source.callCount shouldBe 1
            }
        }

        When("a second burst arrives after the first refresh has completed") {
            Then("a new refresh cycle runs — the second burst also collapses to one call") {
                val source = FakeRefreshTokenSource(result = newTokens)
                val manager = InMemoryAuthManager(source).apply { login() }

                manager.refresh()
                source.callCount shouldBe 1

                source.holdNextCallsUntilReleased()
                coroutineScope {
                    val second = List(5) {
                        async(start = CoroutineStart.UNDISPATCHED) { manager.refresh() }
                    }
                    source.release()
                    second.awaitAll()
                }

                source.callCount shouldBe 2
            }
        }
    }

    Given("an unauthenticated InMemoryAuthManager") {
        When("refresh() is called with no current tokens") {
            Then("it returns null without touching the refresh source") {
                val source = FakeRefreshTokenSource(result = newTokens)
                val manager = InMemoryAuthManager(source)

                manager.refresh() shouldBe null
                source.callCount shouldBe 0
                manager.isAuthenticated shouldBe false
            }
        }
    }
})
