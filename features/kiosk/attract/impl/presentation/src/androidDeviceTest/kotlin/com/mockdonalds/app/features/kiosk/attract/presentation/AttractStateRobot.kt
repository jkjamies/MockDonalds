package com.mockdonalds.app.features.kiosk.attract.presentation

import com.mockdonalds.app.core.test.StateRobot
import com.mockdonalds.app.features.kiosk.attract.api.domain.Ad

class AttractStateRobot : StateRobot<AttractUiState, AttractEvent>() {

    override fun defaultState() = AttractUiState(
        ads = listOf(
            Ad(id = "1", imageUrl = "https://example.test/1.png", headline = "MIDNIGHT TRUFFLE", subheadline = "Order Here"),
            Ad(id = "2", imageUrl = "https://example.test/2.png", headline = "SAFFRON FRY FRIDAYS", subheadline = "Order Here"),
        ),
        currentIndex = 0,
        rotationSeconds = 8,
        eventSink = createEventSink(),
    )

    fun emptyAdsState() = AttractUiState(
        ads = emptyList(),
        currentIndex = 0,
        rotationSeconds = 0,
        eventSink = createEventSink(),
    )
}
