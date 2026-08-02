package com.jkjamies.sampleplatter.features.order.data.remote

import com.jkjamies.sampleplatter.features.order.api.domain.MenuItem

fun SpoonacularMenuItemDto.toMenuItem(categoryId: String): MenuItem = MenuItem(
    id = id.toString(),
    title = title,
    restaurantChain = restaurantChain,
    imageUrl = image,
    servingSize = servingSize,
    categoryId = categoryId,
)
