package com.mockdonalds.app.features.order.data.remote

interface MenuRemoteDataSource {
    suspend fun searchMenuItems(query: String): List<SpoonacularMenuItemDto>
}
