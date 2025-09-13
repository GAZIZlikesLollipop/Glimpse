package org.app.glimpse.data.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlin.time.ExperimentalTime

object ApiService {
    @OptIn(ExperimentalTime::class)
    val httpClient = HttpClient(CIO){
        install(WebSockets)
        install(ContentNegotiation){
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    serializersModule = SerializersModule {
                        contextual(InstantSerialize)
                        contextual(BitmapSerialize)
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