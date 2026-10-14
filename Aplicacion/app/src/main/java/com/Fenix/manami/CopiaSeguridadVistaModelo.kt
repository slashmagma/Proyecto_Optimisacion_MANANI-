package com.Fenix.manami

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.api.AdjuntoCorreo
import com.Fenix.manami.api.CorreoApi
import com.Fenix.manami.inventario.data.InventarioInsertDto
import com.Fenix.manami.inventario.data.ModeloInventario
import com.Fenix.manami.inventario.data.inventarioRepositorio
import com.Fenix.manami.pedidos.data.PedidoInsertDto
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ===== Estructura del respaldo =====

@Serializable
data class RespaldoTrabajo(
    val tipo: String,
    val nombreCompleto: String,
    val medidas: Map<String, Float?> = emptyMap()
)

@Serializable
data class RespaldoPedido(
    val cliente: String,
    val materialUtilizado: String? = null,
    val detalle: String? = null,
    val monto: Int? = null,
    val estado: String? = null,
    val fecha: String? = null,
    val colegio: String? = null,
    val trabajos: List<RespaldoTrabajo> = emptyList()
)

@Serializable
data class Respaldo(
    val version: Int = 1,
    val fechaRespaldo: String = "",
    val pedidos: List<RespaldoPedido> = emptyList(),
    val inventario: List<ModeloInventario> = emptyList()
)

data class CopiaSeguridadUiState(
    val cargando: Boolean = false,
    val jsonExportado: String? = null,
    val resultadoImport: String? = null,
    val error: String? = null,
    val enviandoCorreo: Boolean = false,
    val correoEnviado: Boolean = false,
    val descargando: Boolean = false,
    val descargaRealizada: String? = null
)

class CopiaSeguridadVistaModelo(
    private val repositorioPedidos: PedidoRepositorio = PedidoRepositorio(),
    private val repositorioInventario: inventarioRepositorio = inventarioRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CopiaSeguridadUiState())
    val uiState: StateFlow<CopiaSeguridadUiState> = _uiState.asStateFlow()

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

    // ===== EXPORTAR =====
    fun exportar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null, jsonExportado = null) }
            try {
                _uiState.update {
                    it.copy(cargando = false, jsonExportado = GeneradorRespaldo.generar())
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.localizedMessage ?: "Error al exportar")
                }
            }
        }
    }

    // ===== DESCARGAR AL INSTANTE (archivo .json en Descargas) =====
    fun descargarAhora(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(descargando = true, error = null) }
            val resultado = RespaldoProgramado.respaldarAhora(context)
            resultado.fold(
                onSuccess = { ruta ->
                    _uiState.update {
                        it.copy(descargando = false, descargaRealizada = ruta)
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            descargando = false,
                            error = e.localizedMessage ?: "Error al descargar"
                        )
                    }
                }
            )
        }
    }

    // ===== IMPORTAR =====
    fun importar(textoJson: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null, resultadoImport = null) }

            if (textoJson.isBlank()) {
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo leer el archivo seleccionado")
                }
                return@launch
            }

            try {
                val respaldo = json.decodeFromString<Respaldo>(textoJson)

                // ===== PASO 1: ELIMINAR TODO LO EXISTENTE (reemplazo total) =====
                // Si el respaldo trae menos datos, los que sobren se borran;
                // si trae datos que habían sido eliminados, vuelven a insertarse.
                repositorioPedidos.eliminarTodosLosPedidos()
                repositorioPedidos.eliminarTodasLasMedidas()
                repositorioInventario.eliminarTodosLosMateriales()

                // ===== PASO 2: INSERTAR LO QUE TRAE EL RESPALDO =====
                var pedidosCreados = 0
                var trabajosCreados = 0
                var materialesCreados = 0

                respaldo.pedidos.forEach { rp ->
                    val nuevoId = repositorioPedidos.insertarPedido(
                        PedidoInsertDto(
                            nombreCliente = rp.cliente,
                            materialUtilizado = rp.materialUtilizado,
                            detalle = rp.detalle,
                            monto = rp.monto,
                            estado = rp.estado ?: "espera",
                            fecha = rp.fecha,
                            colegio = rp.colegio
                        )
                    )
                    if (nuevoId != 0) pedidosCreados++

                    rp.trabajos.forEach { trabajo ->
                        if (trabajo.tipo.isBlank()) return@forEach
                        val tipoId = repositorioPedidos.obtenerTipoPedidoId(trabajo.tipo) ?: return@forEach

                        var pantalonId: Int? = null
                        var poleraId: Int? = null
                        var birreteId: Int? = null

                        when (trabajo.tipo.lowercase()) {
                            "pantalon" -> pantalonId = repositorioPedidos.insertarPantalon(
                                ancho = trabajo.medidas["ancho"],
                                alto = trabajo.medidas["alto"],
                                altoTalle = trabajo.medidas["alto_talle"],
                                cintura = trabajo.medidas["cintura"],
                                cadera = trabajo.medidas["cadera"],
                                tiro = trabajo.medidas["tiro"]
                            )
                            "polera" -> poleraId = repositorioPedidos.insertarPolera(
                                espalda = trabajo.medidas["espalda"],
                                ancho = trabajo.medidas["ancho"],
                                alto = trabajo.medidas["alto"],
                                cisa = trabajo.medidas["cisa"],
                                altoTalle = trabajo.medidas["alto_talle"],
                                largoManga = trabajo.medidas["largo_manga"],
                                cintura = trabajo.medidas["cintura"],
                                cadera = trabajo.medidas["cadera"],
                                tiro = trabajo.medidas["tiro"]
                            )
                            "birrete+estola" -> birreteId = repositorioPedidos.insertarBirreteEstola(
                                birrete = trabajo.medidas["birrete"],
                                estola = trabajo.medidas["estola"]
                            )
                        }

                        repositorioPedidos.insertarDetallePedido(
                            pedidoId = nuevoId,
                            nombreCompleto = trabajo.nombreCompleto,
                            tipoPedidoId = tipoId,
                            pantalonId = pantalonId,
                            poleraId = poleraId,
                            birreteEstolaId = birreteId
                        )
                        trabajosCreados++
                    }
                }

                respaldo.inventario.forEach { mat ->
                    repositorioInventario.insertarMaterial(
                        InventarioInsertDto(
                            nombreMaterial = mat.nombreMaterial,
                            tipoM = mat.tipoM,
                            cantidadM = mat.cantidadM,
                            unidadMedida = mat.unidadMedida,
                            costoM = mat.costoM,
                            imagenM = mat.imagenM
                        )
                    )
                    materialesCreados++
                }

                _uiState.update {
                    it.copy(
                        cargando = false,
                        resultadoImport = "Copia importada (reemplazó los datos anteriores):\n" +
                                "$pedidosCreados pedidos\n" +
                                "$trabajosCreados trabajos\n" +
                                "$materialesCreados materiales"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = "No se pudo completar la importación: ${e.localizedMessage}. " +
                                "Si el archivo no era una copia válida, tus datos no se modificaron."
                    )
                }
            }
        }
    }

    fun limpiarResultados() {
        _uiState.update {
            it.copy(
                jsonExportado = null,
                resultadoImport = null,
                error = null,
                correoEnviado = false,
                descargaRealizada = null
            )
        }
    }

    // ===== ENVIAR COPIA POR CORREO (Brevo / Resend) =====
    fun enviarCopiaPorCorreo(correoDestino: String) {
        val textoJson = _uiState.value.jsonExportado
        if (textoJson.isNullOrBlank()) {
            _uiState.update { it.copy(error = "Primero genera la copia de seguridad") }
            return
        }
        if (correoDestino.isBlank() || !correoDestino.contains("@")) {
            _uiState.update { it.copy(error = "Escribe un correo válido") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(enviandoCorreo = true, error = null, correoEnviado = false) }

            // Resumen para el cuerpo del correo (si algo falla, se omite el detalle)
            val resumenHtml = try {
                val respaldo = json.decodeFromString<Respaldo>(textoJson)
                val trabajos = respaldo.pedidos.sumOf { it.trabajos.size }
                """
                    <h2 style="color:#901ACC;margin-bottom:4px">Copia de seguridad - App Fenix</h2>
                    <p style="margin-top:0;color:#666">Fecha: ${java.time.LocalDate.now()}</p>
                    <ul style="line-height:1.6">
                        <li><b>${respaldo.pedidos.size}</b> pedidos</li>
                        <li><b>$trabajos</b> trabajos</li>
                        <li><b>${respaldo.inventario.size}</b> materiales de inventario</li>
                    </ul>
                    <p>La copia completa va adjunta en el archivo
                        <b>copia_fenix_${java.time.LocalDate.now()}.json</b>.
                        Se restaura desde la app en <i>Copia de seguridad → Importar</i>.</p>
                """.trimIndent()
            } catch (e: Exception) {
                """
                    <h2 style="color:#901ACC">Copia de seguridad - App Fenix</h2>
                    <p>Fecha: ${java.time.LocalDate.now()}</p>
                    <p>La copia completa va adjunta en formato JSON.</p>
                """.trimIndent()
            }

            // Adjunto: el respaldo completo en base64
            val adjunto = AdjuntoCorreo(
                nombre = "copia_fenix_${java.time.LocalDate.now()}.json",
                contenidoBase64 = android.util.Base64.encodeToString(
                    textoJson.toByteArray(Charsets.UTF_8),
                    android.util.Base64.NO_WRAP
                ),
                tipoMime = "application/json"
            )

            val resultado = CorreoApi.enviarCorreo(
                para = correoDestino.trim(),
                asunto = "Copia de seguridad Fenix - ${java.time.LocalDate.now()}",
                html = resumenHtml,
                adjunto = adjunto
            )

            resultado.fold(
                onSuccess = {
                    _uiState.update { it.copy(enviandoCorreo = false, correoEnviado = true) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            enviandoCorreo = false,
                            error = "Error al enviar correo: ${e.localizedMessage}"
                        )
                    }
                }
            )
        }
    }
}
