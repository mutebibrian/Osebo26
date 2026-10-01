package com.devbrian.osebo.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

const val OSEBO_BASE_URL = "https://prod-api.osebo.ai"

/**
 * Builds the shared Ktor client. The engine is resolved automatically per
 * platform (OkHttp on Android, Darwin on iOS) since each source set only has
 * one engine dependency on its classpath — no expect/actual needed for that.
 *
 * Not yet wired into Koin/NetworkModule: repositories still use the
 * Retrofit-based ApiService. This becomes the real client as repositories
 * migrate, one at a time.
 *
 * TODO(network-phase-2): port SessionAuthenticator's 401-refresh-retry logic
 * (see androidMain di/NetworkModule.kt) once a repository actually depends
 * on this client for authenticated calls.
 */
fun createOseboHttpClient(sessionProvider: OseboSessionProvider): HttpClient = HttpClient {
    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            }
        )
    }

    install(Logging) {
        level = LogLevel.INFO
    }

    defaultRequest {
        url(OSEBO_BASE_URL)
        contentType(ContentType.Application.Json)
        headers.append(HttpHeaders.Accept, "application/json")

        sessionProvider.authToken()?.takeIf { it.isNotBlank() }?.let { token ->
            headers.append(HttpHeaders.Authorization, "Bearer $token")
        }
        // X-Shop is deliberately not defaulted here: every shop-scoped
        // endpoint below takes shopId explicitly and sets it per-request,
        // matching the original ApiService.kt interface's explicit design
        // (a single global default would silently break multi-shop calls).
    }
}
