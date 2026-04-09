package com.mockdonalds.app.core.circuit

import com.slack.circuit.runtime.screen.Screen

interface TabScreen : Screen {
    val tag: String
}
