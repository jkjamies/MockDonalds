@file:Suppress("MaxLineLength") // sentinel URLs in fake data

package com.mockdonalds.app.features.kiosk.attract.data

import com.mockdonalds.app.features.kiosk.attract.api.domain.Ad
import com.mockdonalds.app.features.kiosk.attract.api.domain.AttractContent
import com.mockdonalds.app.features.kiosk.attract.domain.AttractRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
@Inject
class AttractRepositoryImpl : AttractRepository {

    override fun getAttractContent(): Flow<AttractContent> = flowOf(
        AttractContent(
            ads = STATIC_FALLBACK_ADS,
            rotationSeconds = DEFAULT_ROTATION_SECONDS,
        ),
    )

    private companion object {
        const val DEFAULT_ROTATION_SECONDS = 8

        val STATIC_FALLBACK_ADS = listOf(
            Ad(
                id = "1",
                imageUrl = "https://example.test/ads/1.png",
                headline = "MIDNIGHT TRUFFLE NIGHTS",
                subheadline = "Order Here",
            ),
            Ad(
                id = "2",
                imageUrl = "https://example.test/ads/2.png",
                headline = "SAFFRON FRY FRIDAYS",
                subheadline = "Order Here",
            ),
            Ad(
                id = "3",
                imageUrl = "https://example.test/ads/3.png",
                headline = "TASTE THE SIGNATURE LINE",
                subheadline = "Order Here",
            ),
        )
    }
}
