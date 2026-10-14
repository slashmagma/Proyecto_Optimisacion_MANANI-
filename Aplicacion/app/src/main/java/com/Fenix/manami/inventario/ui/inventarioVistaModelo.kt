package com.Fenix.manami.inventario.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.AlertaStock
import com.Fenix.manami.inventario.data.inventarioRepositorio
import com.Fenix.manami.inventario.data.ModeloInventario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface inventarioUiState {

    data object Cargando : inventarioUiState

    data class Exito(
        val listaInventario: List<ModeloInventario>
    ) : inventarioUiState

    data class Error(
        val mensaje: String
    ) : inventarioUiState
}


class inventarioVistaModelo(
    private val repositorio: inventarioRepositorio = inventarioRepositorio()
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<inventarioUiState>(
            inventarioUiState.Cargando
        )

    val uiState: StateFlow<inventarioUiState> =
        _uiState.asStateFlow()


    init {
        cargarInventario()
    }


    fun cargarInventario() {

        viewModelScope.launch {

            _uiState.value = inventarioUiState.Cargando

            try {

                val inventario =
                    repositorio.obtenerInventario()

                _uiState.value =
                    inventarioUiState.Exito(inventario)

            } catch (e: Exception) {

                _uiState.value =
                    inventarioUiState.Error(
                        e.localizedMessage
                            ?: "Error al cargar el inventario"
                    )
            }
        }
    }

    /** Elimina un material y recarga la lista. */
    fun eliminarMaterial(id: Int) {

        viewModelScope.launch {

            try {
                repositorio.eliminarMaterial(id)
            } catch (e: Exception) {
                _uiState.value =
                    inventarioUiState.Error(
                        e.localizedMessage
                            ?: "No se pudo eliminar el material"
                    )
                return@launch
            }

            cargarInventario()
        }
    }

    /**
     * Suma (delta = 1) o resta (delta = -1) una unidad a la cantidad
     * de un material del inventario.
     *
     * - La pantalla se actualiza al toque (sin esperar a la red).
     * - El cambio queda en la base y anotado en el historial de movimientos.
     * - Si al bajar queda en el umbral de alerta (5 o menos) o en 0,
     *   se manda un correo de aviso.
     * - Nunca deja la cantidad en negativo: si está en 0 y se resta,
     *   no hace nada.
     */
    fun ajustarCantidad(id: Int, delta: Int) {

        val estado = _uiState.value
        if (estado !is inventarioUiState.Exito) return

        val material = estado.listaInventario.firstOrNull { it.id == id } ?: return
        val cantidadActual = material.cantidadM ?: 0
        val cantidadNueva = (cantidadActual + delta).coerceAtLeast(0)

        // Ya está en 0 y quieren restar → no cambia nada
        if (cantidadNueva == cantidadActual) return

        // Actualización optimista: la UI responde de inmediato
        _uiState.value = inventarioUiState.Exito(
            estado.listaInventario.map {
                if (it.id == id) it.copy(cantidadM = cantidadNueva) else it
            }
        )

        viewModelScope.launch {
            try {
                // Guarda el stock y anota el movimiento en el historial
                repositorio.ajustarCantidadConHistorial(material, delta)
            } catch (e: Exception) {
                // Si no se pudo guardar, volvemos a lo que hay en la base
                cargarInventario()
                return@launch
            }

            // Si quedó con poco stock (o se agotó), avisa por correo.
            // Nunca corta el flujo si el correo falla.
            if (AlertaStock.debeAvisarAjuste(
                    idMaterial = id,
                    cantidadAnterior = cantidadActual,
                    cantidadNueva = cantidadNueva,
                    delta = delta
                )
            ) {
                try {
                    AlertaStock.enviarAlertaAjusteInventario(
                        nombreMaterial = material.nombreMaterial,
                        cantidadAnterior = cantidadActual,
                        cantidadNueva = cantidadNueva,
                        unidad = material.unidadMedida
                    )
                } catch (e: Exception) {
                    // El aviso por correo es secundario: se ignora el error
                }
            }
        }
    }
}