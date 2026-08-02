package com.jkjamies.sampleplatter.core.strata

public abstract class StrataException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
