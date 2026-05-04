@file:Suppress("MaxLineLength") // URLs in fake data

package com.mockdonalds.app.features.order.data

import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.FeaturedItem
import com.mockdonalds.app.features.order.api.domain.MenuCategory
import com.mockdonalds.app.features.order.api.domain.MenuItem
import com.mockdonalds.app.features.order.domain.OrderRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class OrderRepositoryImpl : OrderRepository {

    override fun getMenuCategories(): Flow<List<MenuCategory>> = flowOf(CATEGORIES)

    override fun getFeaturedItems(): Flow<List<FeaturedItem>> = flowOf(
        listOf(
            FeaturedItem(
                id = "1",
                title = "Midnight Truffle",
                price = "\$24",
                description = "Double wagyu beef, black truffle aioli, aged gruyère, and caramelized balsamic onions on a charcoal brioche bun.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCIgoaLEiJ_bs2e8Me_lZ4aFmkrX6vIJZNq8pxZvTHgTWm1Tf3owVjnv0TB10EdFBml3pZnGq5zSKGEA7e-jieP5DA8Z6TPrl-bZubc97xrDi06vNYqb2tQQ4lyimnkB7D0ea0DQWBUuDI399M7wip6bz1Sx03HyAqp4FKPE8QDJezB45YFf3lOWquJQm0PordZaY7vResoMshyeZI6C2VR-oryDi51W3sAThDRaUZdHSPcZOxs_DxnsPssQmc8SzuMxeh3BbD_iDQ",
                tag = "SIGNATURE",
                isPrimary = true,
            ),
            FeaturedItem(
                id = "2",
                title = "Saffron Fries",
                price = "\$12",
                description = "Triple-cooked hand-cut batons dusted with Kashmiri saffron and served with a roasted garlic confit dip.",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuANtMk8nUlKAA2ReyocZY_KslUkl91nRJwy1_LJJXjCWfbl8XW6LpFS4Ho6KvqoTJEpVs7O0Bp0W7VBmi16AOTa73CdSIi4EjuqgG3X1_nE-JOy1KeQwEk1CpHrpPA5cz5u2JvkOQrhHnc8CGSwWgaSXmNn3bSXD0KdBca78UZzHk1p9PAWGuOfzJALFy8yKPj3JvcBz9CCMPcZgmTWVtipd8bRea_17N4VetRnVtPjz5Nx13eA2qweBqYtgQqzgRVnSrx-1r24gMM",
                tag = "TRENDING",
                isPrimary = false,
            ),
        ),
    )

    override fun getItemsByCategory(): Flow<Map<String, List<MenuItem>>> = flowOf(ITEMS_BY_CATEGORY)

    override fun getCartSummary(): Flow<CartSummary> = flowOf(
        CartSummary(itemCount = 2, total = "\$36.00"),
    )

    private companion object {
        val CATEGORIES: List<MenuCategory> = listOf(
            MenuCategory(id = "burgers", name = "Burgers"),
            MenuCategory(id = "happy-meals", name = "Happy Meals"),
            MenuCategory(id = "sandwiches", name = "Sandwiches & Meals"),
            MenuCategory(id = "main-menu", name = "Main Menu"),
            MenuCategory(id = "all-day-breakfast", name = "All Day Breakfast"),
            MenuCategory(id = "fries-sides", name = "Fries & Sides"),
            MenuCategory(id = "sweets-treats", name = "Sweets & Treats"),
            MenuCategory(id = "beverages", name = "Beverages"),
        )

        // Per-category fake items. Image URLs use the example.test sentinel domain
        // (RFC2606 reserved) — placeholder dev hygiene; real menu data comes from
        // the backend in a follow-up.
        val ITEMS_BY_CATEGORY: Map<String, List<MenuItem>> = mapOf(
            "burgers" to listOf(
                MenuItem("burger-bigmac", "burgers", "Big Mac", "$4.59", 540, "https://example.test/menu/big-mac.png"),
                MenuItem("burger-hamburger", "burgers", "Hamburger", "$1.50", 250, "https://example.test/menu/hamburger.png"),
                MenuItem("burger-mcdouble", "burgers", "McDouble", "$1.99", 380, "https://example.test/menu/mcdouble.png"),
                MenuItem("burger-qpc", "burgers", "Quarter Pounder With Cheese", "$4.59", 530, "https://example.test/menu/qpc.png"),
                MenuItem("burger-cheeseburger", "burgers", "Cheeseburger", "$1.79", 300, "https://example.test/menu/cheeseburger.png"),
                MenuItem("burger-bacon-mcdouble", "burgers", "Bacon McDouble", "$2.39", 430, "https://example.test/menu/bacon-mcdouble.png", tags = listOf("DELUXE")),
                MenuItem("burger-qpc-deluxe", "burgers", "Quarter Pounder Cheese Deluxe", "$4.79", 590, "https://example.test/menu/qpc-deluxe.png"),
                MenuItem("burger-double-cheese", "burgers", "Double Cheeseburger", "$2.49", 430, "https://example.test/menu/double-cheese.png"),
                MenuItem("burger-double-qpc", "burgers", "Double Quarter With Cheese", "$5.69", 770, "https://example.test/menu/double-qpc.png"),
                MenuItem("burger-triple-cheese", "burgers", "Triple Cheeseburger", "$3.00", 520, "https://example.test/menu/triple-cheese.png"),
            ),
            "happy-meals" to listOf(
                MenuItem("hm-nuggets", "happy-meals", "4 pc McNuggets Happy Meal", "$5.99", 410, "https://example.test/menu/hm-nuggets.png"),
                MenuItem("hm-burger", "happy-meals", "Hamburger Happy Meal", "$5.49", 480, "https://example.test/menu/hm-burger.png"),
                MenuItem("hm-cheese", "happy-meals", "Cheeseburger Happy Meal", "$5.69", 530, "https://example.test/menu/hm-cheese.png"),
            ),
            "sandwiches" to listOf(
                MenuItem("sand-mccrispy", "sandwiches", "McCrispy", "$4.99", 470, "https://example.test/menu/mccrispy.png", tags = listOf("DELUXE")),
                MenuItem("sand-mcchicken", "sandwiches", "McChicken", "$1.99", 400, "https://example.test/menu/mcchicken.png"),
                MenuItem("sand-fish", "sandwiches", "Filet-O-Fish", "$4.79", 390, "https://example.test/menu/filet.png"),
            ),
            "main-menu" to listOf(
                MenuItem("main-bigmac-meal", "main-menu", "Big Mac Meal", "$8.99", 1080, "https://example.test/menu/bigmac-meal.png"),
                MenuItem("main-qpc-meal", "main-menu", "Quarter Pounder Meal", "$8.99", 1100, "https://example.test/menu/qpc-meal.png"),
                MenuItem("main-mccrispy-meal", "main-menu", "McCrispy Meal", "$9.49", 1010, "https://example.test/menu/mccrispy-meal.png", tags = listOf("NEW")),
            ),
            "all-day-breakfast" to listOf(
                MenuItem("bf-eggmcmuffin", "all-day-breakfast", "Egg McMuffin", "$3.99", 310, "https://example.test/menu/egg-mcmuffin.png"),
                MenuItem("bf-hashbrown", "all-day-breakfast", "Hash Brown", "$1.79", 150, "https://example.test/menu/hashbrown.png"),
                MenuItem("bf-pancakes", "all-day-breakfast", "Hotcakes", "$3.79", 580, "https://example.test/menu/hotcakes.png"),
            ),
            "fries-sides" to listOf(
                MenuItem("side-fries-md", "fries-sides", "Medium Fries", "$2.79", 320, "https://example.test/menu/fries-md.png"),
                MenuItem("side-fries-lg", "fries-sides", "Large Fries", "$3.49", 480, "https://example.test/menu/fries-lg.png"),
                MenuItem("side-apple-slices", "fries-sides", "Apple Slices", "$1.49", 15, "https://example.test/menu/apple-slices.png"),
            ),
            "sweets-treats" to listOf(
                MenuItem("treat-mcflurry-oreo", "sweets-treats", "McFlurry Oreo", "$3.99", 510, "https://example.test/menu/mcflurry-oreo.png"),
                MenuItem("treat-applepie", "sweets-treats", "Apple Pie", "$1.49", 230, "https://example.test/menu/apple-pie.png"),
                MenuItem("treat-icedcoffee", "sweets-treats", "Iced Coffee", "$2.49", 140, "https://example.test/menu/iced-coffee.png"),
            ),
            "beverages" to listOf(
                MenuItem("bev-coke-md", "beverages", "Medium Coca-Cola", "$1.99", 200, "https://example.test/menu/coke-md.png"),
                MenuItem("bev-sprite-md", "beverages", "Medium Sprite", "$1.99", 200, "https://example.test/menu/sprite-md.png"),
                MenuItem("bev-lemonade", "beverages", "Frozen Strawberry Lemonade", "$2.99", 240, "https://example.test/menu/lemonade.png", tags = listOf("NEW")),
            ),
        )
    }
}
