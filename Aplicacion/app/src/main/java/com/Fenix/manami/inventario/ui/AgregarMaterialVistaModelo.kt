package com.Fenix.manami.inventario.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.inventario.data.ModeloInventario
import com.Fenix.manami.inventario.data.inventarioRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AgregarMaterialUiState(
    val material: ModeloInventario? = null,
    val cantidadAAgregar: String = "",
    val costo: String = "",
    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val stockNuevo: Int? = null,
    val error: String? = null
)

class AgregarMaterialVistaModelo(
    private val repositorio: inventarioRepositorio = inventarioRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgregarMaterialUiState())
    val uiState: StateFlow<AgregarMaterialUiState> = _uiState.asStateFlow()

    /** Carga el material que se va a actualizar. */
    fun cargarMaterial(id: Int) {
        // Ya estaba cargado: no vuelve a pedirlo a la base de datos
        if (_uiState.value.material?.id == id) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val material = repositorio.obtenerMaterialPorId(id)
                if (material == null) {
                    _uiState.update {
                        it.copy(cargando = false, error = "No se encontró el material")
                    }
                } else {
                    _uiState.update {
                        it.copy(cargando = false, material = material, costo = material.costoM?.toString() ?: "")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.localizedMessage ?: "Error al cargar")
                }
            }
        }
    }

    fun onCantidadAgregarChange(valor: String) {
        _uiState.update { it.copy(cantidadAAgregar = valor.filter { c -> c.isDigit() }) }
    }

    fun onCostoChange(valor: String) {
        _uiState.update { it.copy(costo = valor.filter { c -> c.isDigit() }) }
    }

    /**
     * Suma la cantidad al inventario y guarda el registro en el historial
     * (nombre, fecha, cantidad con unidad y costo).
     */
    fun agregarMaterial() {
        viewModelScope.launch {
            val state = _uiState.value
            val material = state.material ?: return@launch

            val cantidad = state.cantidadAAgregar.toIntOrNull() ?: 0
            if (cantidad <= 0) {
                _uiState.update { it.copy(error = "Escribe cuánto material agregas") }
                return@launch
            }

            _uiState.update { it.copy(cargando = true, error = null) }

            try {
                val stockNuevo = repositorio.agregarMaterial(
                    material = material,
                    cantidadAAgregar = cantidad,
                    costo = state.costo.toLongOrNull()
                )

                _uiState.update {
                    it.copy(
                        cargando = false,
                        guardadoExitoso = true,
                        stockNuevo = stockNuevo,
                        material = material.copy(cantidadM = stockNuevo)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.localizedMessage ?: "Error al guardar")
                }
            }
        }
    }

    /** Resetea el resultado para poder agregar otro lote. */
    fun limpiarResultado() {
        _uiState.update {
            it.copy(guardadoExitoso = false, stockNuevo = null, cantidadAAgregar = "", error = null)
        }
    }
}
