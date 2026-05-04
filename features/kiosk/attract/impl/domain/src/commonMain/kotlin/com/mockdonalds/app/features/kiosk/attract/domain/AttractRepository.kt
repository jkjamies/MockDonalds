package com.mockdonalds.app.features.kiosk.attract.domain

import com.mockdonalds.app.features.kiosk.attract.api.domain.AttractContent
import kotlinx.coroutines.flow.Flow

interface AttractRepository {
    fun getAttractContent(): Flow<AttractContent>
}
