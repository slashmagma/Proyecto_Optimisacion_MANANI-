package com.Fenix.manami.pedidos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import com.Fenix.manami.pedidos.data.PedidoInsertDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActualizarUiState(
    val nombreCliente: String = "",
    val detalle: String = "",
    val materialUtilizado: String = "",
    val monto: String = "",
    val fecha: String = "",
    val estado: String = "",
    val colegio: String = "",
    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null
)

class ActualizarPedidoVistaModelo(
    private val repositorio: PedidoRepositorio = PedidoRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActualizarUiState())
    val uiState: StateFlow<ActualizarUiState> = _uiState.asStateFlow()

    fun cargarDatosIniciales(pedidoId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val pedido = repositorio.obtenerPedidoPorId(pedidoId)

                if (pedido != null) {
                    _uiState.update {
                        it.copy(
                            nombreCliente = pedido.nombreCliente,
                            detalle = pedido.detalle ?: "",
                            materialUtilizado = pedido.materialUtilizado ?: "",
                            monto = pedido.monto?.toString() ?: "",
                            fecha = pedido.fecha ?: "",
                            estado = pedido.estado ?: "",
                            colegio = pedido.colegio ?: "",
                            cargando = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(cargando = false, error = "Pedido no encontrado") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.localizedMessage ?: "Error al cargar datos") }
            }
        }
    }

    fun onNombreClienteChanged(nuevo: String) { _uiState.update { it.copy(nombreCliente = nuevo) } }
    fun onDetalleChanged(nuevo: String) { _uiState.update { it.copy(detalle = nuevo) } }
    fun onMaterialUtilizadoChanged(nuevo: String) { _uiState.update { it.copy(materialUtilizado = nuevo) } }
    fun onMontoChanged(nuevo: String) { _uiState.update { it.copy(monto = nuevo) } }
    fun onFechaChanged(nuevo: String) { _uiState.update { it.copy(fecha = nuevo) } }
    fun onEstadoChanged(nuevo: String) { _uiState.update { it.copy(estado = nuevo) } }
    fun onColegioChanged(nuevo: String) { _uiState.update { it.copy(colegio = nuevo) } }

    fun actualizarPedido(pedidoId: Int) {

        val estadoActual = _uiState.value
        if (estadoActual.nombreCliente.isBlank()) {
            _uiState.update { it.copy(error = "El nombre del cliente es obligatorio") }
            return
        }

        if (estadoActual.monto.isNotBlank() && estadoActual.monto.toIntOrNull() == null) {
            _uiState.update { it.copy(error = "El campo Pago debe ser un número entero maximo 9.999.999.999") }
            return
        }
        if (estadoActual.monto.isBlank()) {
            _uiState.update { it.copy(error = "El campo Pago no debe estar vacio") }
            return
        }

        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.update { it.copy(cargando = true, error = null) }

            try {
                val dto = PedidoInsertDto(
                    nombreCliente = currentState.nombreCliente,
                    monto = currentState.monto.toIntOrNull(),
                    fecha = currentState.fecha.ifBlank { null },
                    detalle = currentState.detalle.ifBlank { null },
                    materialUtilizado = currentState.materialUtilizado.ifBlank { null },
                    estado = currentState.estado.ifBlank { null },
                    colegio = currentState.colegio.ifBlank { null }
                )

                repositorio.actualizarPedido(pedidoId, dto)
                _uiState.update { it.copy(cargando = false, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.localizedMessage ?: "Error al actualizar") }
            }
        }
    }

    fun eliminarPedido(pedidoId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                repositorio.eliminarPedido(pedidoId)
                _uiState.update { it.copy(cargando = false, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.localizedMessage ?: "Error al eliminar") }
            }
        }
    }
}
