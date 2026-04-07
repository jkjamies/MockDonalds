package com.mockdonalds.app.core.centerpost

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public interface CenterPostDispatchers {
    public val default: CoroutineDispatcher
    public val io: CoroutineDispatcher
    public val main: CoroutineDispatcher
}

@ContributesBinding(AppScope::class)
public class DefaultCenterPostDispatchers : CenterPostDispatchers {
    override val default: CoroutineDispatcher = Dispatchers.Default
    // Dispatchers.IO is not available on all KMP targets (missing on native iOS, macOS, Linux, etc.)
    // Using Dispatchers.IO here works for Android/JVM; for full KMP support consider expect/actual
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main
}
