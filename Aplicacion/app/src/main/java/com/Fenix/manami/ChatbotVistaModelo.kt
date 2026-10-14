package com.Fenix.manami

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.api.GeminiApi
import com.Fenix.manami.api.GroqApi
import com.Fenix.manami.inventario.data.ModeloInventario
import com.Fenix.manami.inventario.data.inventarioRepositorio
import com.Fenix.manami.pedidos.data.PedidoModelo
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MensajeChat(
    val texto: String,
    val esUsuario: Boolean
)

data class ChatbotUiState(
    val mensajes: List<MensajeChat> = listOf(
        MensajeChat(
            texto = "¡Hola! Soy el asistente de Fenix. Puedes preguntarme sobre tus pedidos o materiales. " +
                    "Ejemplos:\n" +
                    "• ¿cuántos pedidos tengo?\n" +
                    "• ¿qué pedidos están en espera?\n" +
                    "• ¿cuál es el costo total?\n" +
                    "• ¿qué materiales tengo?",
            esUsuario = false
        )
    ),
    val input: String = "",
    val cargando: Boolean = false
)

class ChatbotVistaModelo(
    private val repositorioPedidos: PedidoRepositorio = PedidoRepositorio(),
    private val repositorioInventario: inventarioRepositorio = inventarioRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    private var pedidos: List<PedidoModelo> = emptyList()
    private var materiales: List<ModeloInventario> = emptyList()
    private var datosCargados = false

    fun onInputChange(valor: String) {
        _uiState.update { it.copy(input = valor) }
    }

    fun enviarMensaje() {
        val texto = _uiState.value.input.trim()
        if (texto.isEmpty()) return

        _uiState.update {
            it.copy(
                input = "",
                mensajes = it.mensajes + MensajeChat(texto, esUsuario = true),
                cargando = true
            )
        }

        viewModelScope.launch {
            if (!datosCargados) {
                try {
                    pedidos = repositorioPedidos.obtenerPedidos()
                    materiales = repositorioInventario.obtenerInventario()
                    datosCargados = true
                } catch (e: Exception) {
                    _uiState.update { it.copy(cargando = false) }
                }
            }

            // Orden: Groq (gratis) → Gemini → asistente de reglas
            val respuesta = when {
                GroqApi.estaConfigurada() -> preguntarConIA(texto)
                GeminiApi.estaConfigurada() -> preguntarAGemini(texto)
                else -> responder(texto)
            }

            _uiState.update {
                it.copy(
                    cargando = false,
                    mensajes = it.mensajes + MensajeChat(respuesta, esUsuario = false)
                )
            }
        }
    }

    // ===== Pregunta a Groq (IA gratis) con contexto de los datos reales =====
    private suspend fun preguntarConIA(texto: String): String {
        val instruccion = construirInstruccion()
        val resultado = GroqApi.preguntar(instruccionSistema = instruccion, pregunta = texto)
        return resultado.fold(
            onSuccess = { it },
            onFailure = { e ->
                // Si Groq falla, intentamos Gemini; si no, asistente de reglas
                if (GeminiApi.estaConfigurada()) {
                    preguntarAGemini(texto)
                } else {
                    "No pude conectar con la IA (${e.localizedMessage}). " + responder(texto)
                }
            }
        )
    }

    // ===== Pregunta a Gemini con contexto de los datos reales =====
    private suspend fun preguntarAGemini(texto: String): String {
        val instruccion = construirInstruccion()

        val resultado = GeminiApi.preguntar(instruccionSistema = instruccion, prompt = texto)
        return resultado.getOrElse { fallback ->
            // Si Gemini falla, usamos el asistente de reglas
            val mensajeRed = fallback.message ?: ""
            if (mensajeRed.contains("GEMINI_API_KEY")) {
                "No configuré mi clave de Gemini. " + responder(texto)
            } else {
                "No pude procesar tu pregunta con IA. " + responder(texto)
            }
        }
    }

    // Instrucción común para la IA, con los datos reales dentro
    private fun construirInstruccion(): String {
        val contexto = construirContexto()
        return "Eres el asistente de la app Fenix, una aplicación para gestionar pedidos de " +
                "sastrería (pantalones, poleras, birretes y estolas) e inventario de materiales. " +
                "Responde en español, de forma corta y útil (máximo 3 líneas). " +
                "Usa ÚNICAMENTE los datos que te doy abajo; si la respuesta no está en los datos, " +
                "dilo amablemente.\n\nDATOS ACTUALES:\n$contexto"
    }

    // Construye un resumen de los datos reales para dárselo a Gemini
    private fun construirContexto(): String {
        val sb = StringBuilder()

        sb.appendLine("PEDIDOS (${pedidos.size}):")
        pedidos.forEach { p ->
            sb.appendLine(
                "- Cliente: ${p.nombreCliente} | Estado: ${p.estado ?: "sin estado"} | " +
                        "Costo: $${p.monto ?: 0} | Fecha: ${p.fecha ?: "sin fecha"} | " +
                        "Colegio: ${p.colegio ?: "-"} | Detalle: ${p.detalle ?: "-"}"
            )
        }

        sb.appendLine()
        sb.appendLine("MATERIALES (${materiales.size}):")
        materiales.forEach { m ->
            sb.appendLine(
                "- ${m.nombreMaterial}: ${m.cantidadM ?: 0} ${m.unidadMedida ?: ""} " +
                        "(tipo: ${m.tipoM ?: "-"}, costo: $${m.costoM ?: 0})"
            )
        }

        return sb.toString()
    }

    fun recargarDatos() {
        datosCargados = false
    }

    // ===== Lógica del asistente =====
    private fun responder(texto: String): String {
        val t = texto.lowercase()

        return when {
            t.contains("hola") || t.contains("buenos días") || t.contains("buenas") ->
                "¡Hola! ¿En qué puedo ayudarte hoy?"

            t.contains("cuántos") && t.contains("pedido") || t.contains("cuantos") && t.contains("pedido") ->
                "Tienes ${pedidos.size} pedido(s) registrados."

            t.contains("espera") || t.contains("pendiente") -> {
                val enEspera = pedidos.filter { it.estado?.lowercase() == "espera" }
                if (enEspera.isEmpty()) "No tienes pedidos en espera."
                else "Pedidos en espera:\n" + enEspera.joinToString("\n") { "• ${it.nombreCliente} - $${it.monto ?: 0}" }
            }

            t.contains("terminado") || t.contains("entregado") || t.contains("completado") -> {
                val terminados = pedidos.filter {
                    it.estado?.lowercase() == "terminado" || it.estado?.lowercase() == "entregado"
                }
                if (terminados.isEmpty()) "No tienes pedidos terminados."
                else "Pedidos terminados:\n" + terminados.joinToString("\n") { "• ${it.nombreCliente}" }
            }

            t.contains("costo") || t.contains("total") || t.contains("monto") -> {
                val total = pedidos.sumOf { it.monto ?: 0 }
                "El costo total de tus ${pedidos.size} pedido(s) es: $$total"
            }

            t.contains("material") || t.contains("inventario") -> {
                if (materiales.isEmpty()) "No tienes materiales registrados."
                else "Materiales en inventario:\n" + materiales.joinToString("\n") {
                    "• ${it.nombreMaterial}: ${it.cantidadM ?: 0} ${it.unidadMedida ?: ""}"
                }
            }

            t.contains("poco") || t.contains("bajo") || t.contains("stock") -> {
                val bajos = materiales.filter { (it.cantidadM ?: 0) <= 5 }
                if (bajos.isEmpty()) "No tienes materiales con stock bajo."
                else "Materiales con poco stock:\n" + bajos.joinToString("\n") {
                    "• ${it.nombreMaterial}: ${it.cantidadM ?: 0} ${it.unidadMedida ?: ""}"
                }
            }

            t.contains("ayuda") || t.contains("help") || t.contains("puedes") ->
                "Puedo ayudarte con:\n" +
                        "• Cuántos pedidos tienes\n" +
                        "• Pedidos en espera o terminados\n" +
                        "• Costo total\n" +
                        "• Materiales y stock bajo"

            else ->
                "No entendí tu pregunta. Escribe 'ayuda' para ver lo que puedo hacer."
        }
    }
}
