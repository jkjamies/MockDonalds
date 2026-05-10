package com.mockdonalds.app.features.order.domain

import com.mockdonalds.app.features.order.api.domain.CategoryDetailContent
import com.mockdonalds.app.features.order.api.domain.GetCategoryDetailContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
class GetCategoryDetailContentImpl(
    private val repository: OrderRepository,
) : GetCategoryDetailContent() {
    override fun createObservable(params: String): Flow<CategoryDetailContent> {
        val name = repository.categoryName(params).orEmpty()
        return repository.getMenuItemsByCategory(params).map { items ->
            CategoryDetailContent(
                categoryId = params,
                categoryName = name,
                items = items,
            )
        }
    }
}
