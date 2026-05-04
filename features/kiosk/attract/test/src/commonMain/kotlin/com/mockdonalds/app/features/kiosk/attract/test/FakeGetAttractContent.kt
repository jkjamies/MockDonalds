package com.mockdonalds.app.features.kiosk.attract.test

import com.mockdonalds.app.features.kiosk.attract.api.domain.Ad
import com.mockdonalds.app.features.kiosk.attract.api.domain.AttractContent
import com.mockdonalds.app.features.kiosk.attract.api.domain.GetAttractContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
class FakeGetAttractContent(
    initial: AttractContent = DEFAULT,
) : GetAttractContent() {
    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<AttractContent> = _content

    fun emit(content: AttractContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = AttractContent(
            ads = listOf(
                Ad(
                    id = "1",
                    imageUrl = "https://example.test/ads/1.png",
                    headline = "TEST HEADLINE",
                    subheadline = "Order Here",
                ),
            ),
            rotationSeconds = 8,
        )
    }
}
