package com.Fenix.manami.api

import com.Fenix.manami.BuildConfig
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// ===== Modelos de la API de Gemini =====

@Serializable
data class GeminiPart(val text: String)

@Serializable
data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

/**
 * Cliente para la API de Google Gemini (generateContent).
 * Usa la clave guardada en BuildConfig.GEMINI_API_KEY (viene de local.properties).
 */
object GeminiApi {

    private const val MODELO = "gemini-flash-latest"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Indica si hay una clave configurada. */
    fun estaConfigurada(): Boolean = BuildConfig.GEMINI_API_KEY.isNotBlank()

    /**
     * Envía un prompt a Gemini y devuelve la respuesta en texto.
     * @param instruccionSistema contexto inicial (rol del asistente).
     * @param prompt pregunta del usuario.
     */
    suspend fun preguntar(
        instruccionSistema: String,
        prompt: String
    ): Result<String> {
        if (!estaConfigurada()) {
            return Result.failure(
                IllegalStateException("Falta GEMINI_API_KEY en local.properties")
            )
        }

        return try {
            val urlDestino = "${BASE_URL}$MODELO:generateContent?key=${BuildConfig.GEMINI_API_KEY}"

            val peticion = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = prompt)))
                ),
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = instruccionSistema))
                )
            )

            val respuesta = ApiCliente.client.post {
                url(urlDestino)
                contentType(ContentType.Application.Json)
                setBody(peticion)
            }

            val cuerpo = respuesta.bodyAsText()
            val texto = extraerTexto(cuerpo)

            if (texto.isNullOrBlank()) {
                Result.failure(Exception("Gemini devolvió una respuesta vacía"))
            } else {
                Result.success(texto)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Extrae el texto del JSON de respuesta de Gemini. */
    private fun extraerTexto(cuerpo: String): String? {
        return try {
            val objeto = json.parseToJsonElement(cuerpo).jsonObject
            val candidatos = objeto["candidates"]?.jsonArray ?: return null
            val contenido = candidatos.firstOrNull()?.jsonObject?.get("content")?.jsonObject
            val partes = contenido?.get("parts")?.jsonArray ?: return null
            partes.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }
}
