package com.mockdonalds.app.features.kiosk.identify.domain

import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyContent
import kotlinx.coroutines.flow.Flow

interface IdentifyRepository {
    fun getIdentifyContent(): Flow<IdentifyContent>
}
