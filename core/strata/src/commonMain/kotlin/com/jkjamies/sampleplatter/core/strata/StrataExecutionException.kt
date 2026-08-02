package com.jkjamies.sampleplatter.core.strata

public class StrataExecutionException(
    cause: Throwable,
) : StrataException(cause.message ?: "Execution failed", cause)
