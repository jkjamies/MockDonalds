package com.mockdonalds.app.features.order.data

import com.mockdonalds.app.features.order.api.domain.CartSummary
import com.mockdonalds.app.features.order.api.domain.FeaturedItem
import com.mockdonalds.app.features.order.api.domain.MenuCategory
import com.mockdonalds.app.features.order.domain.OrderRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class OrderRepositoryImpl : OrderRepository {

    override fun getMenuCategories(): Flow<List<MenuCategory>> = flowOf(
        listOf(
            MenuCategory(id = "1", name = "Burgers"),
            MenuCategory(id = "2", name = "Fries"),
            MenuCategory(id = "3", name = "Drinks"),
            MenuCategory(id = "4", name = "Desserts"),
        )
    )

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
        )
    )

    override fun getCartSummary(): Flow<CartSummary> = flowOf(
        CartSummary(itemCount = 2, total = "\$36.00")
    )
}
