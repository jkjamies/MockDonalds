package com.mockdonalds.app.features.kiosk.attract.domain

import com.mockdonalds.app.features.kiosk.attract.api.domain.AttractContent
import com.mockdonalds.app.features.kiosk.attract.api.domain.GetAttractContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow

@ContributesBinding(AppScope::class)
class GetAttractContentImpl(
    private val repository: AttractRepository,
) : GetAttractContent() {
    override fun createObservable(params: Unit): Flow<AttractContent> = repository.getAttractContent()
}
