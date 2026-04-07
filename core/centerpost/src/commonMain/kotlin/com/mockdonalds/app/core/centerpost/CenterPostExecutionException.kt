package com.mockdonalds.app.core.centerpost

public class CenterPostExecutionException(
    cause: Throwable,
) : CenterPostException(cause.message ?: "Execution failed", cause)
