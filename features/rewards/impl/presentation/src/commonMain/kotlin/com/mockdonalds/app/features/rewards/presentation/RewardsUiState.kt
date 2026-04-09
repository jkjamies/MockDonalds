package com.mockdonalds.app.features.rewards.presentation

import com.mockdonalds.app.features.rewards.api.domain.HistoryEntry
import com.mockdonalds.app.features.rewards.api.domain.RewardsProgress
import com.mockdonalds.app.features.rewards.api.domain.VaultSpecial
import com.slack.circuit.runtime.CircuitUiState

data class RewardsUiState(
    val progress: RewardsProgress? = null,
    val vaultSpecials: List<VaultSpecial> = emptyList(),
    val history: List<HistoryEntry> = emptyList(),
    val eventSink: (RewardsEvent) -> Unit,
) : CircuitUiState

sealed class RewardsEvent {
    data class VaultSpecialClicked(val id: String) : RewardsEvent()
    data object ViewAllClicked : RewardsEvent()
}
