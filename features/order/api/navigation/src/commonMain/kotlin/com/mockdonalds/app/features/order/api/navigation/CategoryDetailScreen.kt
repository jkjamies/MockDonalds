package com.mockdonalds.app.features.order.api.navigation

import com.mockdonalds.app.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen

@Parcelize
data class CategoryDetailScreen(
    val categoryId: String,
) : Screen
