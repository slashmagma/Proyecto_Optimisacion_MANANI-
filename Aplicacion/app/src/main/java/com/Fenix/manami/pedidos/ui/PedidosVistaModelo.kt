package com.Fenix.manami.pedidos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.pedidos.data.ModeloPedidos
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PedidosUiState {
    object Cargando : PedidosUiState
    data class Exito(
        val listaPedidos: List<ModeloPedidos>,
        val textoBusqueda: String = "", //Barra de busqueda
        val estadoSeleccionado: String = "" // Estados
    ) : PedidosUiState
    data class Error(val mensaje: String) : PedidosUiState
}

class PedidosVistaModelo(
    private val repositorio: PedidoRepositorio = PedidoRepositorio()
) : ViewModel() {

    private var listaCompleta: List<ModeloPedidos> = emptyList()
    private var textoBusquedaActual: String = ""
    private var estadoSeleccionadoActual: String = ""
    private val _uiState = MutableStateFlow<PedidosUiState>(PedidosUiState.Cargando)
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    init {
        cargarPedidos()
    }

    fun cargarPedidos() {
        viewModelScope.launch {
            _uiState.value = PedidosUiState.Cargando
            try {
                listaCompleta = repositorio.obtenerPedidos()
                aplicarFiltros()
            } catch (e: Exception) {
                _uiState.value = PedidosUiState.Error(
                    e.localizedMessage ?: "Error al cargar pedidos"
                )
            }
        }
    }

    fun onBusquedaChanged(nuevoTexto: String) {
        textoBusquedaActual = nuevoTexto
        aplicarFiltros()
    }


    fun onEstadoFiltroSelected(estado: String) {
        estadoSeleccionadoActual = if (estadoSeleccionadoActual == estado) "" else estado
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val listaFiltrada = listaCompleta.filter { pedido ->
            // 1. Filtro por Nombre del Cliente
            val coincideCliente = textoBusquedaActual.isBlank() ||
                    pedido.cliente.contains(textoBusquedaActual, ignoreCase = true)

            // 2. Filtro por Estado (accediendo a la propiedad .estado del objeto)
            val estadoPedidoTexto = pedido.estadoPedido?.estado ?: ""
            val coincideEstado = estadoSeleccionadoActual.isBlank() ||
                    estadoPedidoTexto.equals(estadoSeleccionadoActual, ignoreCase = true)

            coincideCliente && coincideEstado
        }

        _uiState.value = PedidosUiState.Exito(
            listaPedidos = listaFiltrada,
            textoBusqueda = textoBusquedaActual,
            estadoSeleccionado = estadoSeleccionadoActual
        )
    }
}
