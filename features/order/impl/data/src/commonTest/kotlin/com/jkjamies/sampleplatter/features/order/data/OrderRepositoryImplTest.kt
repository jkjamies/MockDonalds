package com.jkjamies.sampleplatter.features.order.data

import app.cash.turbine.test
import com.jkjamies.sampleplatter.core.test.TestStrataDispatchers
import com.jkjamies.sampleplatter.features.order.api.domain.MenuItem
import com.jkjamies.sampleplatter.features.order.data.local.MenuItemLocalDataSource
import com.jkjamies.sampleplatter.features.order.data.remote.MenuRemoteDataSource
import com.jkjamies.sampleplatter.features.order.data.remote.SpoonacularMenuItemDto
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

private class RepositoryHarness {
    val callsByQuery: MutableList<String> = mutableListOf()
    private val responses: MutableMap<String, Result<List<SpoonacularMenuItemDto>>> = mutableMapOf()
    private val store: MutableMap<String, MutableStateFlow<Pair<List<MenuItem>, Long>>> = mutableMapOf()

    val remote: MenuRemoteDataSource = object : MenuRemoteDataSource {
        override suspend fun searchMenuItems(query: String): List<SpoonacularMenuItemDto> {
            callsByQuery += query
            return responses[query]?.getOrThrow() ?: emptyList()
        }
    }

    val local: MenuItemLocalDataSource = object : MenuItemLocalDataSource {
        override fun observeByCategory(categoryId: String): Flow<List<MenuItem>> =
            entryFor(categoryId).map { it.first }

        override fun firstImageUrlByCategory(categoryId: String): String? =
            entryFor(categoryId).value.first.firstOrNull()?.imageUrl

        override fun countByCategory(categoryId: String): Int =
            entryFor(categoryId).value.first.size

        override fun oldestCachedAtByCategory(categoryId: String): Long? {
            val (items, cachedAt) = entryFor(categoryId).value
            return if (items.isEmpty()) null else cachedAt
        }

        override fun replaceCategory(categoryId: String, items: List<MenuItem>, cachedAt: Long) {
            entryFor(categoryId).value = items to cachedAt
        }
    }

    private val dispatchers = TestStrataDispatchers()

    val repository: OrderRepositoryImpl = OrderRepositoryImpl(
        remote = remote,
        local = local,
        dispatchers = dispatchers,
    )

    /**
     * Drains the test scheduler.
     *
     * `refreshIfStale` brackets its cache reads and writes in `withContext(dispatchers.io)`, so
     * nothing in the refresh path runs until the queue is advanced. Call this before awaiting
     * anything the refresh produces, and again before cancelling — a continuation left parked on
     * the scheduler makes cancellation itself unable to complete.
     */
    fun advanceUntilIdle() = dispatchers.advanceUntilIdle()

    fun seedCache(categoryId: String, items: List<MenuItem>, cachedAt: Long) {
        entryFor(categoryId).value = items to cachedAt
    }

    fun queueRemoteSuccess(query: String, items: List<SpoonacularMenuItemDto>) {
        responses[query] = Result.success(items)
    }

    fun queueRemoteFailure(query: String, error: Throwable = RuntimeException("network error")) {
        responses[query] = Result.failure(error)
    }

    private fun entryFor(categoryId: String) = store.getOrPut(categoryId) {
        MutableStateFlow(emptyList<MenuItem>() to 0L)
    }
}

class OrderRepositoryImplTest : BehaviorSpec({

    Given("a freshly cached burger category (cachedAt = now)") {
        val fixture = RepositoryHarness()
        val now = Clock.System.now().toEpochMilliseconds()
        fixture.seedCache(
            categoryId = "burgers",
            items = listOf(menuItem(id = "1", title = "Cached Burger", categoryId = "burgers")),
            cachedAt = now,
        )

        When("getMenuItemsByCategory is collected") {
            Then("it returns the cached item without calling remote") {
                fixture.repository.getMenuItemsByCategory("burgers").test {
                    fixture.advanceUntilIdle()
                    awaitItem() shouldContain menuItem(id = "1", title = "Cached Burger", categoryId = "burgers")
                    fixture.advanceUntilIdle()
                    cancel()
                }
                fixture.callsByQuery shouldBe emptyList()
            }
        }
    }

    Given("a stale burger cache (cachedAt = 25h ago)") {
        val fixture = RepositoryHarness()
        val twentyFiveHoursAgo = Clock.System.now().toEpochMilliseconds() - 25.hours.inWholeMilliseconds
        fixture.seedCache(
            categoryId = "burgers",
            items = listOf(menuItem(id = "old", title = "Old Cached Burger", categoryId = "burgers")),
            cachedAt = twentyFiveHoursAgo,
        )
        fixture.queueRemoteSuccess(
            query = "burger",
            items = listOf(
                SpoonacularMenuItemDto(
                    id = 99,
                    title = "Fresh Burger",
                    restaurantChain = "Sample Platter",
                    image = "https://img/fresh.jpg",
                    servingSize = null,
                ),
            ),
        )

        When("getMenuItemsByCategory is collected") {
            Then("it triggers a remote fetch and emits the refreshed items") {
                fixture.repository.getMenuItemsByCategory("burgers").test {
                    val initial = awaitItem()
                    initial.firstOrNull()?.title shouldBe "Old Cached Burger"
                    // The refresh is queued on the test scheduler; nothing above ran it.
                    fixture.advanceUntilIdle()
                    val refreshed = awaitItem()
                    refreshed.firstOrNull()?.title shouldBe "Fresh Burger"
                    fixture.advanceUntilIdle()
                    cancel()
                }
                fixture.callsByQuery shouldContain "burger"
            }
        }
    }

    Given("an empty cache and a successful remote response") {
        val fixture = RepositoryHarness()
        fixture.queueRemoteSuccess(
            query = "burger",
            items = listOf(
                SpoonacularMenuItemDto(
                    id = 1,
                    title = "Signature Stack",
                    restaurantChain = "Sample Platter",
                    image = "https://img/big-mac.jpg",
                    servingSize = "214g",
                ),
            ),
        )

        When("getMenuItemsByCategory is collected") {
            Then("it emits empty initially then the freshly fetched items") {
                fixture.repository.getMenuItemsByCategory("burgers").test {
                    awaitItem().shouldBeEmpty()
                    fixture.advanceUntilIdle()
                    val populated = awaitItem()
                    populated shouldHaveSize 1
                    populated.first().title shouldBe "Signature Stack"
                    fixture.advanceUntilIdle()
                    cancel()
                }
            }
        }
    }

    Given("an empty cache and a failing remote") {
        val fixture = RepositoryHarness()
        fixture.queueRemoteFailure(query = "burger")

        When("getMenuItemsByCategory is collected") {
            Then("it emits an empty list and does not propagate the error") {
                fixture.repository.getMenuItemsByCategory("burgers").test {
                    awaitItem().shouldBeEmpty()
                    // Lets the failing remote call run and be swallowed by runCatching.
                    fixture.advanceUntilIdle()
                    cancel()
                }
            }
        }
    }

    Given("a cache populated for some categories and not others") {
        val fixture = RepositoryHarness()
        val now = Clock.System.now().toEpochMilliseconds()
        fixture.seedCache(
            categoryId = "burgers",
            items = listOf(menuItem(id = "1", title = "Cached", categoryId = "burgers", imageUrl = "img-url")),
            cachedAt = now,
        )

        When("getCategoryPreviews is collected") {
            Then("it emits 6 previews — populated for cached categories, empty otherwise") {
                fixture.repository.getCategoryPreviews().test {
                    val previews = awaitItem()
                    previews shouldHaveSize 6
                    val burgers = previews.first { it.id == "burgers" }
                    burgers.itemCount shouldBe 1
                    burgers.firstItemImageUrl shouldBe "img-url"
                    val featured = previews.first { it.id == "featured" }
                    featured.itemCount shouldBe 0
                    featured.firstItemImageUrl shouldBe null
                    // Draining the scheduler runs the refresh, which repopulates five of the six
                    // categories and emits a fresh preview list. This test is about the state
                    // *before* that, so discard the rest — plain `cancel()` would leave those
                    // events unconsumed and Turbine fails the block on exit.
                    fixture.advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }
            }

            Then("the merged refresh side-effect fires remote.searchMenuItems for stale categories") {
                // burgers is fresh (seeded with cachedAt = now) so refreshIfStale early-returns;
                // the other 5 categories have empty caches → stale → trigger a remote call each.
                fixture.repository.getCategoryPreviews().test {
                    awaitItem()
                    // The whole refresh loop is queued on the test scheduler — without this the
                    // side effect never runs and every assertion below would see zero calls.
                    // The refresh then re-emits previews nobody consumes, so ignore the rest.
                    fixture.advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }
                fixture.callsByQuery shouldContain "popular"      // featured
                fixture.callsByQuery shouldContain "chicken sandwich"
                fixture.callsByQuery shouldContain "fries"
                fixture.callsByQuery shouldContain "soda"
                fixture.callsByQuery shouldContain "ice cream"
                (fixture.callsByQuery.contains("burger")) shouldBe false
            }
        }
    }

    Given("any repository") {
        val fixture = RepositoryHarness()

        When("categoryName is queried for a known id") {
            Then("it returns the display name") {
                fixture.repository.categoryName("burgers") shouldBe "Burgers"
                fixture.repository.categoryName("featured") shouldBe "Featured"
            }
        }

        When("categoryName is queried for an unknown id") {
            Then("it returns null") {
                fixture.repository.categoryName("nonsense") shouldBe null
            }
        }

        When("getCartSummary is collected") {
            Then("it emits a hardcoded summary") {
                fixture.repository.getCartSummary().test {
                    val cart = awaitItem()
                    cart.itemCount shouldBe 2
                    cart.total shouldNotBe ""
                    awaitComplete()
                }
            }
        }
    }
})

private fun menuItem(
    id: String,
    title: String,
    categoryId: String,
    imageUrl: String = "https://img/$id.jpg",
): MenuItem = MenuItem(
    id = id,
    title = title,
    restaurantChain = "TestChain",
    imageUrl = imageUrl,
    servingSize = null,
    categoryId = categoryId,
)
