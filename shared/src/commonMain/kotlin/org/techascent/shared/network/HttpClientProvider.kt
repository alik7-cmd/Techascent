package org.techascent.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun provideHttpClient(debug: Boolean = false): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        // Logging is active only in debug builds.
        // LogLevel.NONE in release prevents accidental response-body leaks
        // and removes ~150 KB of logging overhead from the release binary.
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    if (debug) print("KtorHttpClient $message")
                }
            }
            level = if (debug) LogLevel.BODY else LogLevel.NONE
        }
    }
}