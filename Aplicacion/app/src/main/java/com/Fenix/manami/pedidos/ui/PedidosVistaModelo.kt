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
    data class Exito(val listaPedidos: List<ModeloPedidos>) : PedidosUiState
    data class Error(val mensaje: String) : PedidosUiState
}

class PedidosVistaModelo(
    private val repositorio: PedidoRepositorio = PedidoRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PedidosUiState>(PedidosUiState.Cargando)
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    init {
        cargarPedidos()
    }

    fun cargarPedidos() {
        viewModelScope.launch {
            _uiState.value = PedidosUiState.Cargando
            try {
                val pedidos = repositorio.obtenerPedidos()
                _uiState.value = PedidosUiState.Exito(pedidos)
            } catch (e: Exception) {
                _uiState.value = PedidosUiState.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }

}