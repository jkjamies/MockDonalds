package com.mockdonalds.app.features.recents.domain

import com.mockdonalds.app.features.recents.api.domain.GetRecentsContent
import com.mockdonalds.app.features.recents.api.domain.RecentsContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
class GetRecentsContentImpl(
    private val repository: RecentsRepository,
) : GetRecentsContent() {
    override fun createObservable(params: Unit): Flow<RecentsContent> {
        return repository.getRecentItems().map { RecentsContent(items = it) }
    }
}
