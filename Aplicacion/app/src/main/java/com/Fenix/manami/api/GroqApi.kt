package com.Fenix.manami.api

import com.Fenix.manami.BuildConfig
import io.ktor.client.request.header
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

// ===== Modelos de la API de Groq (compatible con OpenAI) =====

@Serializable
data class GroqMensaje(
    val role: String,
    val content: String
)

@Serializable
data class GroqRequest(
    val model: String,
    val messages: List<GroqMensaje>,
    val temperature: Double = 0.4,
    val max_tokens: Int = 600
)

/**
 * Cliente para la API de Groq (https://groq.com).
 * Gratis y sin tarjeta: la clave se obtiene en console.groq.com.
 * Usa la clave guardada en BuildConfig.GROQ_API_KEY (viene de local.properties).
 */
object GroqApi {

    private const val URL = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODELO = "qwen/qwen3.8-27b"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Indica si hay una clave configurada. */
    fun estaConfigurada(): Boolean = BuildConfig.GROQ_API_KEY.isNotBlank()

    /**
     * Envía el historial de mensajes al modelo y devuelve la respuesta en texto.
     * @param instruccionSistema rol del asistente (se envía como "system").
     * @param pregunta última pregunta del usuario.
     */
    suspend fun preguntar(
        instruccionSistema: String,
        pregunta: String
    ): Result<String> {
        if (!estaConfigurada()) {
            return Result.failure(
                IllegalStateException("Falta GROQ_API_KEY en local.properties")
            )
        }

        return try {
            val peticion = GroqRequest(
                model = MODELO,
                messages = listOf(
                    GroqMensaje(role = "system", content = instruccionSistema),
                    GroqMensaje(role = "user", content = pregunta)
                )
            )

            val respuesta = ApiCliente.client.post {
                url(URL)
                header("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
                contentType(ContentType.Application.Json)
                setBody(peticion)
            }

            val cuerpo = respuesta.bodyAsText()

            if (respuesta.status.value in 200..299) {
                val texto = extraerTexto(cuerpo)
                if (texto.isNullOrBlank()) {
                    Result.failure(Exception("Groq devolvió una respuesta vacía"))
                } else {
                    Result.success(texto)
                }
            } else {
                Result.failure(Exception("Groq respondió ${respuesta.status.value}: $cuerpo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Extrae choices[0].message.content del JSON de respuesta. */
    private fun extraerTexto(cuerpo: String): String? {
        return try {
            val objeto = json.parseToJsonElement(cuerpo).jsonObject
            objeto["choices"]?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }
}
