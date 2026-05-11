package com.mockdonalds.app.features.order.test

import com.mockdonalds.app.features.order.api.domain.CategoryDetailContent
import com.mockdonalds.app.features.order.api.domain.GetCategoryDetailContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
class FakeGetCategoryDetailContent(
    initial: CategoryDetailContent = DEFAULT,
) : GetCategoryDetailContent() {

    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: String): Flow<CategoryDetailContent> = _content

    fun emit(content: CategoryDetailContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = CategoryDetailContent(
            categoryId = "burgers",
            categoryName = "Burgers",
            items = emptyList(),
        )
    }
}
