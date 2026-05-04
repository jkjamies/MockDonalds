package com.mockdonalds.app.features.kiosk.identify.test

import com.mockdonalds.app.features.kiosk.identify.api.domain.GetIdentifyContent
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyContent
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@ContributesBinding(AppScope::class)
class FakeGetIdentifyContent(
    initial: IdentifyContent = DEFAULT,
) : GetIdentifyContent() {
    private val _content = MutableStateFlow(initial)

    override fun createObservable(params: Unit): Flow<IdentifyContent> = _content

    fun emit(content: IdentifyContent) {
        _content.value = content
    }

    companion object {
        val DEFAULT = IdentifyContent(
            skipEnabled = true,
            phoneEntryEnabled = true,
            qrScannerEnabled = true,
            countryDialCode = "+1",
        )
    }
}
