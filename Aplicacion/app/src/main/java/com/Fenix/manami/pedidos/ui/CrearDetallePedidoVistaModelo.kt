package com.Fenix.manami.pedidos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.MedidasUtils
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TipoTrabajoDetalle(
    val tipo: String = "",
    val valoresCampos: Map<String, String> = emptyMap()
)

data class TrabajoDetalle(
    val id: Int = 0,
    val nombreCompleto: String = "",
    val tipoPedido: String = "",
    val tiposTrabajo: List<TipoTrabajoDetalle> = listOf(TipoTrabajoDetalle())
)

data class CrearDetallePedidoUiState(
    val nombreCompleto: String = "",
    val tipoPedido: String = "",
    val trabajos: List<TrabajoDetalle> = listOf(TrabajoDetalle()),
    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null
)

class CrearDetallePedidoVistaModelo(
    private val repositorio: PedidoRepositorio = PedidoRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearDetallePedidoUiState())
    val uiState: StateFlow<CrearDetallePedidoUiState> = _uiState.asStateFlow()

    fun cargarDatos(pedidoId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val pedido = repositorio.obtenerPedidoPorId(pedidoId)
                if (pedido != null) {
                    _uiState.update {
                        it.copy(
                            nombreCompleto = pedido.nombreCliente,
                            tipoPedido = pedido.detalle ?: "",
                            cargando = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(cargando = false, error = "Pedido no encontrado") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.localizedMessage) }
            }
        }
    }

    fun onNombreCompletoChange(valor: String) {
        _uiState.value = _uiState.value.copy(nombreCompleto = valor)
    }

    fun agregarTrabajo() {
        val nuevosTrabajos = _uiState.value.trabajos.toMutableList()
        nuevosTrabajos.add(TrabajoDetalle(id = nuevosTrabajos.size))
        _uiState.value = _uiState.value.copy(trabajos = nuevosTrabajos)
    }

    fun eliminarTrabajo(index: Int) {
        val nuevosTrabajos = _uiState.value.trabajos.toMutableList()
        if (nuevosTrabajos.size > 1) {
            nuevosTrabajos.removeAt(index)
            _uiState.value = _uiState.value.copy(trabajos = nuevosTrabajos)
        }
    }

    fun onTrabajoNombreCompletoChange(index: Int, valor: String) {
        val nuevosTrabajos = _uiState.value.trabajos.toMutableList()
        if (index < nuevosTrabajos.size) {
            nuevosTrabajos[index] = nuevosTrabajos[index].copy(nombreCompleto = valor)
            _uiState.value = _uiState.value.copy(trabajos = nuevosTrabajos)
        }
    }

    fun onTrabajoTipoTrabajoChange(trabajoIndex: Int, tipoIndex: Int, tipo: String) {
        val nuevosTrabajos = _uiState.value.trabajos.toMutableList()
        if (trabajoIndex < nuevosTrabajos.size) {
            val nuevosTipos = nuevosTrabajos[trabajoIndex].tiposTrabajo.toMutableList()
            if (tipoIndex < nuevosTipos.size) {
                nuevosTipos[tipoIndex] = nuevosTipos[tipoIndex].copy(
                    tipo = tipo,
                    valoresCampos = emptyMap()
                )
                nuevosTrabajos[trabajoIndex] = nuevosTrabajos[trabajoIndex].copy(tiposTrabajo = nuevosTipos)
                _uiState.value = _uiState.value.copy(trabajos = nuevosTrabajos)
            }
        }
    }

    fun onTrabajoCampoChange(trabajoIndex: Int, tipoIndex: Int, campo: String, valor: String) {
        val nuevosTrabajos = _uiState.value.trabajos.toMutableList()
        if (trabajoIndex < nuevosTrabajos.size) {
            val nuevosTipos = nuevosTrabajos[trabajoIndex].tiposTrabajo.toMutableList()
            if (tipoIndex < nuevosTipos.size) {
                val nuevosValores = nuevosTipos[tipoIndex].valoresCampos.toMutableMap()
                nuevosValores[campo] = valor
                nuevosTipos[tipoIndex] = nuevosTipos[tipoIndex].copy(valoresCampos = nuevosValores)
                nuevosTrabajos[trabajoIndex] = nuevosTrabajos[trabajoIndex].copy(tiposTrabajo = nuevosTipos)
                _uiState.value = _uiState.value.copy(trabajos = nuevosTrabajos)
            }
        }
    }

    fun agregarTipoTrabajo(trabajoIndex: Int) {
        val nuevosTrabajos = _uiState.value.trabajos.toMutableList()
        if (trabajoIndex < nuevosTrabajos.size) {
            val nuevosTipos = nuevosTrabajos[trabajoIndex].tiposTrabajo.toMutableList()
            nuevosTipos.add(TipoTrabajoDetalle())
            nuevosTrabajos[trabajoIndex] = nuevosTrabajos[trabajoIndex].copy(tiposTrabajo = nuevosTipos)
            _uiState.value = _uiState.value.copy(trabajos = nuevosTrabajos)
        }
    }

    fun guardarDetalle(pedidoId: Int) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(cargando = true, error = null) }

            try {
                for (trabajo in state.trabajos) {
                    for (tipoTrabajo in trabajo.tiposTrabajo) {
                        if (tipoTrabajo.tipo.isEmpty()) continue

                        val tipoPedidoId = repositorio.obtenerTipoPedidoId(tipoTrabajo.tipo) ?: continue

                        val detalleId = when (tipoTrabajo.tipo.lowercase()) {
                            "pantalon" -> {
                                repositorio.insertarPantalon(
                                    ancho = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["ancho"]),
                                    alto = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Alto"]),
                                    altoTalle = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["alto_talle"]),
                                    cintura = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Cintura"]),
                                    cadera = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Cadera"]),
                                    tiro = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Tiro"])
                                )
                            }
                            "polera" -> {
                                repositorio.insertarPolera(
                                    espalda = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Espalda"]),
                                    ancho = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["ancho"]),
                                    alto = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Alto"]),
                                    cisa = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["cisa"]),
                                    altoTalle = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["alto_talle"]),
                                    largoManga = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["largo_manga"]),
                                    cintura = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Cintura"]),
                                    cadera = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Cadera"]),
                                    tiro = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Tiro"])
                                )
                            }
                            "birrete+estola" -> {
                                repositorio.insertarBirreteEstola(
                                    birrete = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Birrete"]),
                                    estola = MedidasUtils.parseDecimal(tipoTrabajo.valoresCampos["Estola"])
                                )
                            }
                            else -> continue
                        }

                        repositorio.insertarDetallePedido(
                            pedidoId = pedidoId,
                            nombreCompleto = trabajo.nombreCompleto,
                            tipoPedidoId = tipoPedidoId,
                            pantalonId = if (tipoTrabajo.tipo.lowercase() == "pantalon") detalleId else null,
                            poleraId = if (tipoTrabajo.tipo.lowercase() == "polera") detalleId else null,
                            birreteEstolaId = if (tipoTrabajo.tipo.lowercase() == "birrete+estola") detalleId else null
                        )
                    }
                }

                _uiState.update { it.copy(cargando = false, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.localizedMessage ?: "Error al guardar") }
            }
        }
    }
}
