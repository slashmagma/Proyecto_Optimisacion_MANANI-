package com.Fenix.manami.pedidos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.pedidos.data.EstadoPedidoModelo
import com.Fenix.manami.pedidos.data.ModeloPedidos
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import com.Fenix.manami.pedidos.data.TipoTrabajoModelo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import com.Fenix.manami.pedidos.data.PedidoInsertDto
data class CrearPedidoUiState(
    val cliente: String = "",
    val descripcion: String = "",
    val materiales: String = "",
    val pago: String = "",
    val fechaTermino: String = "",
    val tipoTrabajoSeleccionado: TipoTrabajoModelo? = null,
    val estadoSeleccionado: EstadoPedidoModelo? = null,
    val listaTipos: List<TipoTrabajoModelo> = emptyList(),
    val listaEstados: List<EstadoPedidoModelo> = emptyList(),
    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null
)

class CrearPedidoVistaModelo(
    private val repositorio: PedidoRepositorio = PedidoRepositorio()
) : ViewModel() {


    private val _uiState = MutableStateFlow(CrearPedidoUiState())
    val uiState: StateFlow<CrearPedidoUiState> = _uiState.asStateFlow()

    init {
        cargarOpciones()
    }

    private fun cargarOpciones() {
        viewModelScope.launch {
            try {
                val tipos = repositorio.obtenerTiposTrabajo()
                val estados = repositorio.obtenerEstados()
                _uiState.value = _uiState.value.copy(
                    listaTipos = tipos,
                    listaEstados = estados,
                    tipoTrabajoSeleccionado = tipos.firstOrNull(),
                    estadoSeleccionado = estados.firstOrNull()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al cargar listas: ${e.message}")
            }
        }
    }

    fun onClienteChange(valor: String) { _uiState.value = _uiState.value.copy(cliente = valor) }
    fun onDescripcionChange(valor: String) { _uiState.value = _uiState.value.copy(descripcion = valor) }
    fun onMaterialesChange(valor: String) { _uiState.value = _uiState.value.copy(materiales = valor) }
    fun onPagoChange(valor: String) { _uiState.value = _uiState.value.copy(pago = valor) }
    fun onFechaTerminoChange(valor: String) { _uiState.value = _uiState.value.copy(fechaTermino = valor) }
    fun onTipoTrabajoChange(tipo: TipoTrabajoModelo) { _uiState.value = _uiState.value.copy(tipoTrabajoSeleccionado = tipo) }
    fun onEstadoChange(estado: EstadoPedidoModelo) { _uiState.value = _uiState.value.copy(estadoSeleccionado = estado) }

    fun guardarPedido() {
        val estadoActual = _uiState.value
        if (estadoActual.cliente.isBlank()) {
            _uiState.update { it.copy(error = "El nombre del cliente es obligatorio") }
            return
        }

        if (estadoActual.pago.isNotBlank() && estadoActual.pago.toIntOrNull() == null) {
            _uiState.update { it.copy(error = "El campo Pago debe ser un número entero maximo 9.999.999.999 ") }
            return
        }
        if (estadoActual.pago.isBlank() && estadoActual.pago.toIntOrNull() == null) {
            _uiState.update { it.copy(error = "El campo Pago no debe estar vacio") }
            return
        }

        if (estadoActual.tipoTrabajoSeleccionado == null) {
            _uiState.update { it.copy(error = "Debe seleccionar un tipo de trabajo") }
            return
        }

        if (estadoActual.estadoSeleccionado == null) {
            _uiState.update { it.copy(error = "Debe seleccionar un estado inicial") }
            return
        }
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(cargando = true, error = null) }

                val nuevoPedido = PedidoInsertDto(
                    cliente = _uiState.value.cliente,
                    pago = _uiState.value.pago.toIntOrNull(),
                    fechaTermino = _uiState.value.fechaTermino.ifEmpty { null },
                    descripcion = _uiState.value.descripcion.ifEmpty { null },
                    materiales = _uiState.value.materiales.ifEmpty { null },
                    idTipo = _uiState.value.tipoTrabajoSeleccionado?.id,
                    idEstado = _uiState.value.estadoSeleccionado?.id
                )

                repositorio.insertarPedido(nuevoPedido)
                _uiState.update { it.copy(cargando = false, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.localizedMessage) }
            }

        }
    }
}