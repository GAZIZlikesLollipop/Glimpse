package org.app.glimpse.data.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object ApiService {
    val httpClient = HttpClient(CIO){
        install(ContentNegotiation){
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    serializersModule = kotlinx.serialization.modules.SerializersModule {
                        // Здесь можно добавить кастомные сериализаторы, если потребуется
                    }
                }
            )
        }
        install(Logging){
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d("HttpClient",message)
                }
            }
            level = LogLevel.ALL
        }
    }
}