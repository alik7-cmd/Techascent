package org.techascent.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun provideHttpClient(): HttpClient {
    return HttpClient {
        install(ContentNegotiation) {
            Json {
                ignoreUnknownKeys = true
            }.let { json -> json(json) }
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    print("KtorHttpClient $message")
                }
            }
            level = LogLevel.BODY
        }

        /*defaultRequest {
            url("https://api.aladhan.com")
            //header("Accept", "application/json")
        }*/
    }
}