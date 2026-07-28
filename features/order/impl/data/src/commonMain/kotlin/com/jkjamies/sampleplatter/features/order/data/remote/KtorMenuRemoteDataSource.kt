package com.jkjamies.sampleplatter.features.order.data.remote

import com.jkjamies.sampleplatter.features.order.data.wiring.SpoonacularHttpClient
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

@ContributesBinding(AppScope::class)
class KtorMenuRemoteDataSource(
    @SpoonacularHttpClient private val httpClient: HttpClient,
) : MenuRemoteDataSource {

    override suspend fun searchMenuItems(query: String): List<SpoonacularMenuItemDto> {
        val response: SpoonacularMenuItemSearchResponseDto = httpClient.get("menuItems/search") {
            parameter("query", query)
            parameter("number", PAGE_SIZE)
        }.body()
        return response.menuItems
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
