package com.Fenix.manami.pedidos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.AlertaStock
import com.Fenix.manami.MedidasUtils
import com.Fenix.manami.inventario.data.ModeloInventario
import com.Fenix.manami.inventario.data.inventarioRepositorio
import com.Fenix.manami.pedidos.data.DetallePedidoCompleto
import com.Fenix.manami.pedidos.data.MaterialPedidoEnMemoria
import com.Fenix.manami.pedidos.data.MaterialPedidoInsertDto
import com.Fenix.manami.pedidos.data.PedidoModelo
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetallePedidoUiState(
    val pedido: PedidoModelo? = null,
    val detalles: List<DetallePedidoCompleto> = emptyList(),
    val cargando: Boolean = false,
    val modoEdicion: Boolean = false,
    val costoTexto: String = "",
    val guardando: Boolean = false,
    val error: String? = null,

    // ===== Materiales que usa el pedido =====
    val materiales: List<MaterialPedidoEnMemoria> = emptyList(),
    val materialesCargados: List<ModeloInventario> = emptyList(),
    val cargandoMateriales: Boolean = false,
    val guardandoMateriales: Boolean = false,
    val avisoMateriales: String? = null
)

class DetallePedidoVistaModelo(
    private val repositorio: PedidoRepositorio = PedidoRepositorio(),
    private val repositorioInventario: inventarioRepositorio = inventarioRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetallePedidoUiState())
    val uiState: StateFlow<DetallePedidoUiState> = _uiState.asStateFlow()

    fun cargarDetalle(pedidoId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            try {
                val pedido = repositorio.obtenerPedidoPorId(pedidoId)
                val detalles = repositorio.obtenerDetallesCompletos(pedidoId)
                val materialesGuardados = repositorio.obtenerMaterialesPedido(pedidoId)

                _uiState.value = DetallePedidoUiState(
                    pedido = pedido,
                    detalles = detalles,
                    costoTexto = pedido?.monto?.toString() ?: "",
                    materiales = materialesGuardados.map {
                        MaterialPedidoEnMemoria(
                            idMaterial = it.idMaterial,
                            nombreMaterial = it.nombreMaterial,
                            cantidadTexto = it.cantidad?.let { c ->
                                if (c % 1f == 0f) c.toInt().toString() else "$c"
                            } ?: "",
                            unidadMedida = it.unidadMedida
                        )
                    }
                )

                cargarMaterialesInventario()
            } catch (e: Exception) {
                _uiState.value = DetallePedidoUiState(error = e.localizedMessage ?: "Error al cargar")
            }
        }
    }

    // ===== Materiales del inventario (para el dropdown) =====

    private fun cargarMaterialesInventario() {
        if (_uiState.value.materialesCargados.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargandoMateriales = true) }
            try {
                val lista = repositorioInventario.obtenerInventario()
                _uiState.update { it.copy(cargandoMateriales = false, materialesCargados = lista) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargandoMateriales = false) }
            }
        }
    }

    /** Agrega una fila vacía a la lista de materiales. */
    fun agregarMaterial() {
        _uiState.update {
            it.copy(materiales = it.materiales + MaterialPedidoEnMemoria())
        }
    }

    fun eliminarMaterial(index: Int) {
        _uiState.update { estado ->
            if (index in estado.materiales.indices) {
                estado.copy(materiales = estado.materiales.filterIndexed { i, _ -> i != index })
            } else estado
        }
    }

    /** Elige un material del inventario en la fila "index". */
    fun seleccionarMaterial(index: Int, material: ModeloInventario?) {
        _uiState.update { estado ->
            if (index !in estado.materiales.indices) {
                estado
            } else {
                val nuevos = estado.materiales.toMutableList()
                nuevos[index] = MaterialPedidoEnMemoria(
                    idMaterial = material?.id,
                    nombreMaterial = material?.nombreMaterial ?: "",
                    cantidadTexto = nuevos[index].cantidadTexto,
                    unidadMedida = material?.unidadMedida
                )
                estado.copy(materiales = nuevos)
            }
        }
    }

    fun onCantidadMaterialChange(index: Int, valor: String) {
        _uiState.update { estado ->
            if (index !in estado.materiales.indices) {
                estado
            } else {
                val nuevos = estado.materiales.toMutableList()
                val limpio = MedidasUtils.filtrarDecimal(valor)
                nuevos[index] = nuevos[index].copy(cantidadTexto = limpio)
                estado.copy(materiales = nuevos)
            }
        }
    }

    /**
     * Guarda los materiales del pedido: descuenta del inventario y manda
     * el correo de alerta si algo quedó con poco stock o faltó.
     */
    fun guardarMateriales(pedidoId: Int) {
        viewModelScope.launch {
            val estado = _uiState.value

            val conMaterial = estado.materiales.filter { it.idMaterial != null }
            if (conMaterial.isEmpty()) {
                _uiState.update { it.copy(error = "Agrega al menos un material del inventario") }
                return@launch
            }

            val sinCantidad = conMaterial.any { MedidasUtils.parseDecimal(it.cantidadTexto) == null }
            if (sinCantidad) {
                _uiState.update { it.copy(error = "Escribe la cantidad de cada material") }
                return@launch
            }

            _uiState.update { it.copy(guardandoMateriales = true, error = null, avisoMateriales = null) }

            try {
                // ===== Guarda la lista en la base de datos =====
                val hoy = java.time.LocalDate.now().toString()

                repositorio.eliminarMaterialesPedido(pedidoId)
                repositorio.guardarMaterialesPedido(
                    pedidoId = pedidoId,
                    materiales = conMaterial.map { m ->
                        MaterialPedidoInsertDto(
                            idMaterial = m.idMaterial,
                            nombreMaterial = m.nombreMaterial,
                            cantidad = MedidasUtils.parseDecimal(m.cantidadTexto),
                            unidadMedida = m.unidadMedida,
                            fecha = hoy
                        )
                    }
                )

                // ===== Descuenta del inventario y revisa el stock =====
                val revisados = conMaterial.map { m ->
                    val resultado = AlertaStock.descontarMaterial(
                        repositorio = repositorioInventario,
                        materialId = m.idMaterial,
                        cantidadPedida = MedidasUtils.parseDecimal(m.cantidadTexto) ?: 0f,
                        unidadPedido = m.unidadMedida
                    )
                    AlertaStock.MaterialRevisado(
                        nombre = m.nombreMaterial,
                        unidad = m.unidadMedida,
                        cantidadPedida = MedidasUtils.parseDecimal(m.cantidadTexto) ?: 0f,
                        resultado = resultado
                    )
                }

                // ===== Correo con todos los materiales =====
                AlertaStock.enviarAlertaStockBajo(revisados = revisados)

                val faltaron = revisados.filter { it.resultado?.alcanzaba == false }

                _uiState.update {
                    it.copy(
                        guardandoMateriales = false,
                        avisoMateriales = if (faltaron.isEmpty()) {
                            "✓ Materiales guardados y stock descontado."
                        } else {
                            "⚠️ A ${faltaron.size} material(es) le faltó stock: " +
                                    faltaron.joinToString(", ") { f -> f.nombre } +
                                    ". Se envió un correo con el detalle."
                        }
                    )
                }

                // Recarga el inventario para que muestre los nuevos valores
                cargarMaterialesInventario()

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardandoMateriales = false, error = e.localizedMessage ?: "Error al guardar")
                }
            }
        }
    }

    fun limpiarAvisoMateriales() {
        _uiState.update { it.copy(avisoMateriales = null, error = null) }
    }

    fun alternarModoEdicion() {
        _uiState.update { estado ->
            estado.copy(
                modoEdicion = !estado.modoEdicion,
                costoTexto = estado.pedido?.monto?.toString() ?: ""
            )
        }
    }

    fun onCostoChange(valor: String) {
        _uiState.update { it.copy(costoTexto = valor) }
    }

    fun guardarCosto(pedidoId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                val monto = _uiState.value.costoTexto.toIntOrNull()
                repositorio.actualizarMontoPedido(pedidoId, monto)
                val pedidoActualizado = repositorio.obtenerPedidoPorId(pedidoId)
                _uiState.update {
                    it.copy(
                        pedido = pedidoActualizado,
                        modoEdicion = false,
                        guardando = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(guardando = false, error = e.localizedMessage ?: "Error al guardar")
                }
            }
        }
    }

    fun eliminarTrabajo(idDetalle: Int?, pedidoId: Int) {
        if (idDetalle == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                repositorio.eliminarTrabajoDetalle(idDetalle)
                cargarDetalle(pedidoId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(cargando = false, error = e.localizedMessage ?: "Error al eliminar")
                }
            }
        }
    }
}
