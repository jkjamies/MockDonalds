package com.mockdonalds.app.features.order.data.remote

import com.mockdonalds.app.features.order.api.domain.MenuItem

fun SpoonacularMenuItemDto.toMenuItem(categoryId: String): MenuItem = MenuItem(
    id = id.toString(),
    title = title,
    restaurantChain = restaurantChain,
    imageUrl = image,
    servingSize = servingSize,
    categoryId = categoryId,
)
