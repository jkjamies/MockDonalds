package com.jkjamies.sampleplatter.features.order.api.navigation

import com.jkjamies.sampleplatter.core.circuit.Parcelize
import com.slack.circuit.runtime.screen.Screen

@Parcelize
data class CategoryDetailScreen(
    val categoryId: String,
) : Screen
