package com.Fenix.manami

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.pedidos.data.PedidoModelo
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val pedidos: List<PedidoModelo> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class HomeVistaModelo(
    private val repositorio: PedidoRepositorio = PedidoRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun cargarPedidos() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(cargando = true)
            try {
                val pedidos = repositorio.obtenerPedidos()
                _uiState.value = HomeUiState(pedidos = pedidos)
            } catch (e: Exception) {
                _uiState.value = HomeUiState(error = e.localizedMessage ?: "Error al cargar")
            }
        }
    }
}
