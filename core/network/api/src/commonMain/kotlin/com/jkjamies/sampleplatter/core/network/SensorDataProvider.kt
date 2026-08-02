package com.jkjamies.sampleplatter.core.network

/**
 * Supplies Akamai Bot Manager sensor data for outgoing requests.
 *
 * Real implementations delegate to the Akamai Mobile Bot Manager SDK, which generates
 * a short-lived token describing the device and runtime environment. The token is
 * added as a request header (see [HttpClientFactoryImpl]) and validated at the edge.
 *
 * The reference codebase ships a no-op stub; a real build wires the platform-specific
 * SDK behind this interface without touching client callers.
 */
interface SensorDataProvider {
    /** Sensor data token, or empty string when no sensor data is available. */
    suspend fun currentSensorData(): String
}
