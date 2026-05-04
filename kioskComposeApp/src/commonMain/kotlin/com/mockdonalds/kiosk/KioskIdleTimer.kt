package com.mockdonalds.kiosk

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@Inject
class KioskIdleTimer {

    fun poke() {
        // Phase 5: emit on a buffered SharedFlow consumed by runFor.
    }
}
