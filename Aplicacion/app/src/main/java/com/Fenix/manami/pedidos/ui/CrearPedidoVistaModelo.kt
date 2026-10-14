package com.Fenix.manami.pedidos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import com.Fenix.manami.pedidos.data.PedidoInsertDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update

data class CrearPedidoUiState(
    val nombreCliente: String = "",
    val detalle: String = "",
    val materialUtilizado: String = "",
    val monto: String = "",
    val fecha: String = "",
    val estado: String = "espera",
    val colegio: String = "",
    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val pedidoCreadoId: Int? = null,
    val error: String? = null
)

class CrearPedidoVistaModelo(
    private val repositorio: PedidoRepositorio = PedidoRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearPedidoUiState())
    val uiState: StateFlow<CrearPedidoUiState> = _uiState.asStateFlow()

    fun onNombreClienteChange(valor: String) { _uiState.value = _uiState.value.copy(nombreCliente = valor) }
    fun onDetalleChange(valor: String) { _uiState.value = _uiState.value.copy(detalle = valor) }
    fun onMaterialUtilizadoChange(valor: String) { _uiState.value = _uiState.value.copy(materialUtilizado = valor) }
    fun onMontoChange(valor: String) { _uiState.value = _uiState.value.copy(monto = valor) }
    fun onFechaChange(valor: String) { _uiState.value = _uiState.value.copy(fecha = valor) }
    fun onEstadoChange(valor: String) { _uiState.value = _uiState.value.copy(estado = valor) }
    fun onColegioChange(valor: String) { _uiState.value = _uiState.value.copy(colegio = valor) }

    fun guardarPedido() {
        val estadoActual = _uiState.value
        if (estadoActual.nombreCliente.isBlank()) {
            _uiState.update { it.copy(error = "El nombre del cliente es obligatorio") }
            return
        }

        if (estadoActual.monto.isNotBlank() && estadoActual.monto.toIntOrNull() == null) {
            _uiState.update { it.copy(error = "El campo Pago debe ser un número entero maximo 9.999.999.999 ") }
            return
        }
        if (estadoActual.monto.isBlank()) {
            _uiState.update { it.copy(error = "El campo Pago no debe estar vacio") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(cargando = true, error = null) }

                val s = _uiState.value

                val nuevoPedido = PedidoInsertDto(
                    nombreCliente = s.nombreCliente,
                    monto = s.monto.toIntOrNull(),
                    fecha = s.fecha.ifEmpty { null },
                    detalle = s.detalle.ifEmpty { null },
                    materialUtilizado = s.materialUtilizado.ifEmpty { null },
                    estado = "espera",  // Estado automático
                    colegio = s.colegio.ifEmpty { null }
                )

                val pedidoId = repositorio.insertarPedido(nuevoPedido)

                _uiState.update {
                    it.copy(cargando = false, guardadoExitoso = true, pedidoCreadoId = pedidoId)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.localizedMessage) }
            }
        }
    }
}
