package com.jkjamies.sampleplatter.core.strata

import kotlin.time.Duration

public class StrataTimeoutException(
    public val duration: Duration,
    cause: Throwable,
) : StrataException("Execution timed out after $duration", cause)
