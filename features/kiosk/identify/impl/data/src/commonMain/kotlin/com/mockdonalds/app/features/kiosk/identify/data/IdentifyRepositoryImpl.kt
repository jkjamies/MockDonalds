package com.mockdonalds.app.features.kiosk.identify.data

import com.mockdonalds.app.core.buildconfig.AppBuildConfig
import com.mockdonalds.app.features.kiosk.identify.api.domain.IdentifyContent
import com.mockdonalds.app.features.kiosk.identify.domain.IdentifyRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
@Inject
class IdentifyRepositoryImpl(
    private val appBuildConfig: AppBuildConfig,
) : IdentifyRepository {

    override fun getIdentifyContent(): Flow<IdentifyContent> = flowOf(
        IdentifyContent(
            skipEnabled = true,
            phoneEntryEnabled = true,
            qrScannerEnabled = true,
            countryDialCode = appBuildConfig.phoneCountryDialCode,
        ),
    )
}
