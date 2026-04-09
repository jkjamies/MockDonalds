package com.mockdonalds.app.core.metro

import com.mockdonalds.app.core.auth.AuthManager
import com.slack.circuit.foundation.Circuit

interface AppGraph {
    val circuit: Circuit
    val authManager: AuthManager
}
