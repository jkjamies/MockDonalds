package com.jkjamies.sampleplatter.core.centerpost

public abstract class CenterPostException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
