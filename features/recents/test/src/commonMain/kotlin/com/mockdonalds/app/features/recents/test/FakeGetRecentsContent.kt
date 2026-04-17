package com.mockdonalds.app.features.recents.test

import com.mockdonalds.app.features.recents.api.domain.GetRecentsContent
import com.mockdonalds.app.features.recents.api.domain.RecentItem
import com.mockdonalds.app.features.recents.api.domain.RecentsContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
class FakeGetRecentsContent(
    initial: RecentsContent = DEFAULT,
) : GetRecentsContent() {
    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<RecentsContent> = _content

    fun emit(content: RecentsContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = RecentsContent(
            items = listOf(
                RecentItem("1", "Big Mac Combo", "Combo Meal", "2 days ago", null),
                RecentItem("2", "McFlurry Oreo", "Dessert", "Last week", null),
                RecentItem("3", "10 pc. Chicken McNuggets", "Chicken", "2 weeks ago", null),
                RecentItem("4", "Medium Fries", "Sides", "1 month ago", null),
                RecentItem("5", "Large Iced Coffee", "Beverages", "1 month ago", null),
            ),
        )
    }
}
