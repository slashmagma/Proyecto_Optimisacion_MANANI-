package com.Fenix.manami.pedidos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.pedidos.data.ActualizarUiState
import com.Fenix.manami.pedidos.data.PedidoInsertDto
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActualizarPedidoVistaModelo(
    private val repositorio: PedidoRepositorio = PedidoRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActualizarUiState())
    val uiState: StateFlow<ActualizarUiState> = _uiState.asStateFlow()

    // Carga los datos existentes del pedido y las listas para los desplegables
    fun cargarDatosIniciales(pedidoId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val tipos = repositorio.obtenerTiposTrabajo()
                val estados = repositorio.obtenerEstados()
                val pedido = repositorio.obtenerPedidoPorId(pedidoId)

                if (pedido != null) {
                    _uiState.update {
                        it.copy(
                            cliente = pedido.cliente,
                            descripcion = pedido.descripcion ?: "",
                            materiales = pedido.materiales ?: "",
                            pago = pedido.pago?.toString() ?: "",
                            fechaTermino = pedido.fechaTermino ?: "",
                            idTipoTrabajoSeleccionado = pedido.tipoTrabajo?.id,
                            idEstadoSeleccionado = pedido.estadoPedido?.id,
                            listaTipos = tipos,
                            listaEstados = estados,
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

    // Funciones para actualizar el estado con cada interacción del usuario
    fun onClienteChanged(nuevoCliente: String) {
        _uiState.update { it.copy(cliente = nuevoCliente) }
    }

    fun onDescripcionChanged(nuevaDescripcion: String) {
        _uiState.update { it.copy(descripcion = nuevaDescripcion) }
    }

    fun onMaterialesChanged(nuevosMateriales: String) {
        _uiState.update { it.copy(materiales = nuevosMateriales) }
    }

    fun onPagoChanged(nuevoPago: String) {
        _uiState.update { it.copy(pago = nuevoPago) }
    }

    fun onFechaTerminoChanged(nuevaFecha: String) {
        _uiState.update { it.copy(fechaTermino = nuevaFecha) }
    }

    fun onTipoTrabajoSeleccionado(idTipo: Int?) {
        _uiState.update { it.copy(idTipoTrabajoSeleccionado = idTipo) }
    }

    fun onEstadoSeleccionado(idEstado: Int?) {
        _uiState.update { it.copy(idEstadoSeleccionado = idEstado) }
    }

    // Ejecuta el UPDATE en Supabase
    fun actualizarPedido(pedidoId: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.update { it.copy(cargando = true, error = null) }

            try {
                val dto = PedidoInsertDto(
                    cliente = currentState.cliente,
                    pago = currentState.pago.toIntOrNull(),
                    fechaTermino = currentState.fechaTermino.ifBlank { null },
                    descripcion = currentState.descripcion.ifBlank { null },
                    materiales = currentState.materiales.ifBlank { null },
                    idTipo = currentState.idTipoTrabajoSeleccionado,
                    idEstado = currentState.idEstadoSeleccionado
                )

                repositorio.actualizarPedido(pedidoId, dto)
                _uiState.update { it.copy(cargando = false, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.localizedMessage ?: "Error al actualizar") }
            }
        }
    }
    // ElIMINAR PEDIDO
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