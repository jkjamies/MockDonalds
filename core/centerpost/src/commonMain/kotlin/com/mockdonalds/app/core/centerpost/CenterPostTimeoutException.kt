package com.mockdonalds.app.core.centerpost

import kotlin.time.Duration

public class CenterPostTimeoutException(
    public val duration: Duration,
    cause: Throwable,
) : CenterPostException("Execution timed out after $duration", cause)
