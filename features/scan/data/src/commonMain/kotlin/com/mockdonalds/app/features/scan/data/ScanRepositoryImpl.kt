package com.mockdonalds.app.features.scan.data

import com.mockdonalds.app.features.scan.api.domain.MemberInfo
import com.mockdonalds.app.features.scan.api.domain.ScanRewardsProgress
import com.mockdonalds.app.features.scan.domain.ScanRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class ScanRepositoryImpl : ScanRepository {

    override fun getMemberInfo(): Flow<MemberInfo> = flowOf(
        MemberInfo(
            memberStatus = "Current Member",
            qrCodeUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC9Pojw6DdMsOOR6hCze-e8NXeAre3ygPVczci3TVq7UnAnPDxoxM_GJQysSal74SZsWTa2Eli6wrej9xa6D_JnOd9cFYjPNapwY2oPFt_4y1988l-6Smo9p3_7Tm1cpbycujNr-US0sB3HayQD2AbCIjUc93yNVTN8VNhZknndgmID66Z92VP8jVgZ_SLb4zLUb_TqSBcfwJX6CiG_OZpDr9dNsM-Av6tOdOBkuZixKo_kctR9aeyVVf9scLxlreCGNXUSrK3bdpw",
        )
    )

    override fun getRewardsProgress(): Flow<ScanRewardsProgress> = flowOf(
        ScanRewardsProgress(
            currentPoints = 750,
            pointsToNextReward = 250,
            progressFraction = 0.75f,
            message = "You're just 250 pts away from your next free treat!",
        )
    )
}
