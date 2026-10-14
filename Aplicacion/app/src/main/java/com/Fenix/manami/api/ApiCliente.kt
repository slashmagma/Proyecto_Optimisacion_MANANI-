package com.Fenix.manami.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Cliente HTTP compartido para llamar a APIs externas (Gemini / Resend).
 * Se usa el motor Android de Ktor (ya incluido en el proyecto).
 */
object ApiCliente {

    val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    encodeDefaults = true
                    isLenient = true
                }
            )
        }
    }

    // ContentType reutilizable para los bodies JSON
    val jsonContentType: ContentType = ContentType.Application.Json
}

// Utilidad para poner el ContentType en las peticiones
fun io.ktor.client.request.HttpRequestBuilder.jsonBody() {
    contentType(ContentType.Application.Json)
}
