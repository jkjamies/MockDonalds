package com.mockdonalds.app.features.order.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class SpoonacularMenuItemSearchResponseDto(
    val menuItems: List<SpoonacularMenuItemDto> = emptyList(),
    val number: Int = 0,
    val offset: Int = 0,
    val totalMenuItems: Int = 0,
)

@Serializable
data class SpoonacularMenuItemDto(
    val id: Int,
    val title: String,
    val restaurantChain: String,
    val image: String,
    val servingSize: String? = null,
)
