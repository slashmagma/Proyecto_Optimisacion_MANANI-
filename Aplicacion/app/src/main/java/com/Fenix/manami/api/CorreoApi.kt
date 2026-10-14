package com.Fenix.manami.api

import com.Fenix.manami.BuildConfig
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// ===== Adjunto =====

/** Adjunto de un correo (el contenido va en base64). */
@Serializable
data class AdjuntoCorreo(
    val nombre: String,
    val contenidoBase64: String,
    val tipoMime: String = "application/json"
)

// ===== Modelos de Brevo =====

@Serializable
data class BrevoRemitente(
    val name: String? = null,
    val email: String
)

@Serializable
data class BrevoDestino(
    val email: String,
    val name: String? = null
)

@Serializable
data class BrevoAdjunto(
    val content: String,
    val name: String
)

@Serializable
data class BrevoEmail(
    val sender: BrevoRemitente,
    val to: List<BrevoDestino>,
    val subject: String,
    val htmlContent: String,
    val attachment: List<BrevoAdjunto>? = null
)

// ===== Modelos de Resend (respaldo si no hay clave de Brevo) =====

@Serializable
data class ResendAdjunto(
    val filename: String,
    val content: String,
    val mime_type: String? = null
)

@Serializable
data class ResendEmail(
    val from: String,
    val to: List<String>,
    val subject: String,
    val html: String,
    val attachments: List<ResendAdjunto>? = null
)

/**
 * Envío de correos.
 *
 * Usa **Brevo** si hay una clave configurada (BREVO_API_KEY en
 * local.properties); Brevo permite enviar a cualquier destinatario sin
 * verificar un dominio: alcanza con verificar la casilla del remitente.
 *
 * Si no hay clave de Brevo, sigue usando **Resend**. Como Resend en modo
 * testing solo puede enviar al correo del dueño de la cuenta, se pueden
 * configurar dos cuentas (una de Pimentel y otra de Marcia) y cada una le
 * manda a su dueño: así los dos reciben sin verificar ningún dominio.
 *
 * Orden de intentos:
 *   1. Todos los destinatarios en un solo envío.
 *   2. Destino por destino.
 *   3. La siguiente cuenta configurada.
 */
object CorreoApi {

    private const val URL_BREVO = "https://api.brevo.com/v3/smtp/email"
    private const val URL_RESEND = "https://api.resend.com/emails"

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Cuenta de correo configurada: clave, remitente y proveedor. */
    private data class Servicio(
        val nombre: String,
        val clave: String,
        val desde: String,
        val esBrevo: Boolean
    )

    /**
     * Cuentas disponibles, en orden de uso.
     *
     * - **Brevo** si hay clave: puede enviar a cualquier destinatario.
     * - Cada cuenta de **Resend** (modo testing) solo puede enviar al correo
     *   de su dueño, por eso se pueden usar dos: una que manda a Pimentel y
     *   otra que manda a Marcia. Con eso los dos reciben sin verificar dominio.
     */
    private fun servicios(): List<Servicio> = buildList {
        if (BuildConfig.BREVO_API_KEY.isNotBlank()) {
            add(Servicio("Brevo", BuildConfig.BREVO_API_KEY, BuildConfig.CORREO_DESDE, true))
        }
        if (BuildConfig.RESEND_API_KEY.isNotBlank()) {
            add(Servicio("Resend", BuildConfig.RESEND_API_KEY, BuildConfig.RESEND_FROM, false))
        }
        if (BuildConfig.RESEND_API_KEY2.isNotBlank()) {
            add(Servicio("Resend (Marcia)", BuildConfig.RESEND_API_KEY2, BuildConfig.RESEND_FROM2, false))
        }
    }

    /** Indica si hay algún proveedor de correo configurado. */
    fun estaConfigurada(): Boolean = servicios().isNotEmpty()

    /** Proveedor que se va a usar hoy (para mostrarlo en la pantalla). */
    val proveedor: String
        get() = servicios().firstOrNull()?.nombre ?: "ninguno"

    /**
     * Envía un correo electrónico.
     *
     * @param para dirección destino principal.
     * @param extras otras direcciones que reciben el mismo correo.
     * @param asunto asunto del correo.
     * @param html contenido en HTML.
     * @param adjunto archivo adjunto opcional (contenido en base64).
     */
    suspend fun enviarCorreo(
        para: String,
        asunto: String,
        html: String,
        adjunto: AdjuntoCorreo? = null,
        extras: List<String> = emptyList()
    ): Result<String> {
        if (!estaConfigurada()) {
            return Result.failure(
                IllegalStateException("Falta la clave de correo en local.properties")
            )
        }

        val destinatarios = (listOf(para) + extras)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        if (destinatarios.isEmpty()) {
            return Result.failure(
                IllegalStateException("No hay correo destino configurado (CORREO_ALERTAS)")
            )
        }

        // A quién todavía le falta el correo. Cada cuenta le manda a los que
        // pueda alcanzar (Resend en modo testing solo acepta al dueño de la
        // cuenta) y seguimos con la siguiente hasta que todos reciban.
        val pendientes = destinatarios.toMutableList()
        var ultimoError: Result<String>? = null
        var ultimoExito: Result<String>? = null

        for (cuenta in servicios()) {
            if (pendientes.isEmpty()) break

            // 1) Todos los pendientes en un solo envío
            val juntos = enviarA(cuenta, pendientes.toList(), asunto, html, adjunto)
            if (juntos.isSuccess) {
                pendientes.clear()
                ultimoExito = juntos
                break
            }
            ultimoError = juntos

            // 2) De a uno (por si el proveedor acepta solo a algunos)
            val alcanzados = mutableListOf<String>()
            for (destino in pendientes.toList()) {
                val solo = enviarA(cuenta, listOf(destino), asunto, html, adjunto)
                if (solo.isSuccess) {
                    alcanzados += destino
                    ultimoExito = solo
                } else {
                    ultimoError = solo
                }
            }
            pendientes.removeAll(alcanzados.toSet())
        }

        // Éxito si todos recibieron (aunque haya habido intentos fallidos)
        if (pendientes.isEmpty()) {
            return ultimoExito ?: Result.success("ok")
        }

        // Si alguien quedó sin recibir, devolvemos el último error
        return ultimoError
            ?: Result.failure(IllegalStateException("No se pudo enviar el correo"))
    }

    // ===== Interno =====

    private suspend fun enviarA(
        cuenta: Servicio,
        destinatarios: List<String>,
        asunto: String,
        html: String,
        adjunto: AdjuntoCorreo?
    ): Result<String> = if (cuenta.esBrevo) {
        enviarPorBrevo(cuenta.clave, destinatarios, asunto, html, adjunto)
    } else {
        enviarPorResend(cuenta.clave, cuenta.desde, destinatarios, asunto, html, adjunto)
    }

    private suspend fun enviarPorBrevo(
        clave: String,
        destinatarios: List<String>,
        asunto: String,
        html: String,
        adjunto: AdjuntoCorreo?
    ): Result<String> {
        return try {
            val peticion = BrevoEmail(
                sender = BrevoRemitente(
                    name = BuildConfig.CORREO_DESDE_NOMBRE,
                    email = BuildConfig.CORREO_DESDE
                ),
                to = destinatarios.map { BrevoDestino(email = it) },
                subject = asunto,
                htmlContent = html,
                attachment = adjunto?.let {
                    listOf(BrevoAdjunto(content = it.contenidoBase64, name = it.nombre))
                }
            )

            val respuesta = ApiCliente.client.post {
                url(URL_BREVO)
                header("api-key", clave)
                contentType(ContentType.Application.Json)
                setBody(peticion)
            }

            val cuerpo = respuesta.bodyAsText()

            // Brevo responde 201 con {"messageId": "..."} si todo salió bien
            if (respuesta.status.value in 200..299) {
                val id = try {
                    json.parseToJsonElement(cuerpo).jsonObject["messageId"]
                        ?.jsonPrimitive?.content
                } catch (e: Exception) {
                    null
                }
                Result.success(id ?: "ok")
            } else {
                Result.failure(Exception("Brevo respondió ${respuesta.status.value}: $cuerpo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun enviarPorResend(
        clave: String,
        desde: String,
        destinatarios: List<String>,
        asunto: String,
        html: String,
        adjunto: AdjuntoCorreo?
    ): Result<String> {
        return try {
            val peticion = ResendEmail(
                from = desde,
                to = destinatarios,
                subject = asunto,
                html = html,
                attachments = adjunto?.let {
                    listOf(
                        ResendAdjunto(
                            filename = it.nombre,
                            content = it.contenidoBase64,
                            mime_type = it.tipoMime
                        )
                    )
                }
            )

            val respuesta = ApiCliente.client.post {
                url(URL_RESEND)
                header("Authorization", "Bearer $clave")
                contentType(ContentType.Application.Json)
                setBody(peticion)
            }

            val cuerpo = respuesta.bodyAsText()

            if (respuesta.status.value in 200..299) {
                val id = try {
                    json.parseToJsonElement(cuerpo).jsonObject["id"]?.jsonPrimitive?.content
                } catch (e: Exception) {
                    null
                }
                Result.success(id ?: "ok")
            } else {
                Result.failure(Exception("Resend respondió ${respuesta.status.value}: $cuerpo"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
