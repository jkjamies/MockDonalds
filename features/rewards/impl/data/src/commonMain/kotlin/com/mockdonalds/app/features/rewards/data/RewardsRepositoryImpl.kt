@file:Suppress("MaxLineLength") // URLs in fake data

package com.mockdonalds.app.features.rewards.data

import com.mockdonalds.app.features.rewards.api.domain.HistoryEntry
import com.mockdonalds.app.features.rewards.api.domain.RewardsProgress
import com.mockdonalds.app.features.rewards.api.domain.VaultSpecial
import com.mockdonalds.app.features.rewards.domain.RewardsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@ContributesBinding(AppScope::class)
class RewardsRepositoryImpl : RewardsRepository {

    override fun getRewardsProgress(): Flow<RewardsProgress> = flowOf(
        RewardsProgress(
            currentPoints = 5432,
            nextRewardName = "Golden Burger",
            pointsToNextReward = 568,
            progressFraction = 0.88f,
        ),
    )

    override fun getVaultSpecials(): Flow<List<VaultSpecial>> = flowOf(
        listOf(
            VaultSpecial(
                id = "1",
                title = "The Midnight Wagyu",
                pointsCost = "2,500 PTS",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB5_cdcHUFE84dPqS6Myqe6DPjZLm7pZA-e1xL-BJHKW6FCi5icL1OaYz6O0QLr7dMgVSBGZTVSR3DW_x8R6vqU-1yGdcX4FitIvyYNz2CpwgdZY3RzxncTcPO2LXm58UMBTeT3MfGELg7SGehbrXvkUKdOhMUnPoHl4z5gxJMOzk8axC97CfHSaJWx-eSv0ZrGXjxJslTIoNQTYmcHWAYA7aknA-NTcH69D36Q3_7mthLoelcYqIPuYCloGsEcM3-a-aehWSYx23g",
                tag = "EXCLUSIVE",
                isFeatured = true,
            ),
            VaultSpecial(
                id = "2",
                title = "Truffle Penne",
                pointsCost = "1,200 PTS",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAtHYBNce4Wzgh78FbEg0YNoKbsdq-hHJUw2wrcV-wEYo3SNwtvHYwoqnnnOIGdLp43aAMfr8hYCP8COxfQVNjzEr9KOR0efa8_4WR8xQE-5h0zGVsaG0tc0NPiahRIFU5FXttF6_u6UrOdHCnmJgOhjeyNsgmLOv0rclMNNkWmxsfgLjH2UpmjWyzuir5SJ4y5uGhKA0Ffw4iBaWJqDdEvJVixU4liT-OqUP7F6dSYbT7IYKonLHCnzcDKGo0AZCKSsovifdDnlM",
                tag = null,
                isFeatured = false,
            ),
            VaultSpecial(
                id = "3",
                title = "Lava Soufflé",
                pointsCost = "850 PTS",
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCGwqtRYRBh2rJ9gv9F5Aj-NJIU0LV1aTQCuE-rAG-hc0Sp4HxZe68TmrfldrKtSWAyNhHps0VArNVduvzRYn7iju2ZUzmC0Ld1HgKNHCSgjcSPI6EiYYhlrRhTJiz9Lk5wmSFvZ9vjwTG6l6YLqr16HFuz9DHEoW5swuJDQYUGVMkxW-W8T_aJiKr8iM42PRBgnhBxVioMyJmqyIeZG0j4BgGaCLyK-v6mgNzlU5KAQmWDzhF4Vfr0JwBE4kOFH03cgiMyLgj8cLY",
                tag = null,
                isFeatured = false,
            ),
        ),
    )

    override fun getHistory(): Flow<List<HistoryEntry>> = flowOf(
        listOf(
            HistoryEntry(
                id = "1",
                title = "Late Night Diner Order",
                subtitle = "Oct 24 • Order #8821",
                points = "+125",
                isPositive = true,
                icon = "🍽️",
            ),
            HistoryEntry(
                id = "2",
                title = "The Midnight Wagyu",
                subtitle = "Oct 21 • Reward Redeemed",
                points = "-2,500",
                isPositive = false,
                icon = "🎁",
            ),
            HistoryEntry(
                id = "3",
                title = "Birthday Bonus",
                subtitle = "Oct 18 • Annual Gift",
                points = "+500",
                isPositive = true,
                icon = "🎉",
            ),
        ),
    )
}
