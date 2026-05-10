package com.mockdonalds.app.features.order.data

import app.cash.turbine.test
import com.mockdonalds.app.features.order.api.domain.MenuItem
import com.mockdonalds.app.features.order.data.local.MenuItemLocalDataSource
import com.mockdonalds.app.features.order.data.remote.MenuRemoteDataSource
import com.mockdonalds.app.features.order.data.remote.SpoonacularMenuItemDto
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

    val repository: OrderRepositoryImpl = OrderRepositoryImpl(remote = remote, local = local)

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
                    awaitItem() shouldContain menuItem(id = "1", title = "Cached Burger", categoryId = "burgers")
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
                    restaurantChain = "McDonald's",
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
                    val refreshed = awaitItem()
                    refreshed.firstOrNull()?.title shouldBe "Fresh Burger"
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
                    title = "Big Mac",
                    restaurantChain = "McDonald's",
                    image = "https://img/big-mac.jpg",
                    servingSize = "214g",
                ),
            ),
        )

        When("getMenuItemsByCategory is collected") {
            Then("it emits empty initially then the freshly fetched items") {
                fixture.repository.getMenuItemsByCategory("burgers").test {
                    awaitItem().shouldBeEmpty()
                    val populated = awaitItem()
                    populated shouldHaveSize 1
                    populated.first().title shouldBe "Big Mac"
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
                    cancel()
                }
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
