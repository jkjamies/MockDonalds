package com.mockdonalds.app.core.network

/**
 * Base class for network-layer errors.
 * Features can match on subtypes to handle specific failure modes.
 */
sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** HTTP response with a non-2xx status code. */
    class HttpError(
        val statusCode: Int,
        val body: String?,
        message: String = "HTTP $statusCode",
    ) : NetworkException(message)

    /** Request timed out (connect, socket, or request timeout). */
    class Timeout(message: String = "Request timed out", cause: Throwable? = null) :
        NetworkException(message, cause)

    /** No network connectivity. */
    class NoConnectivity(message: String = "No network connection", cause: Throwable? = null) :
        NetworkException(message, cause)

    /** Serialization/deserialization failure. */
    class Serialization(message: String, cause: Throwable? = null) :
        NetworkException(message, cause)

    /** Unexpected error not covered by other subtypes. */
    class Unknown(message: String, cause: Throwable? = null) :
        NetworkException(message, cause)
}
