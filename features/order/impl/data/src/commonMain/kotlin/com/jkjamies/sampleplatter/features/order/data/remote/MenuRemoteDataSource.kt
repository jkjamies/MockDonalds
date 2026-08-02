package com.jkjamies.sampleplatter.features.order.data.remote

interface MenuRemoteDataSource {
    suspend fun searchMenuItems(query: String): List<SpoonacularMenuItemDto>
}
