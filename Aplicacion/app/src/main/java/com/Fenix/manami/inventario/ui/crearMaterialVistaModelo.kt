package com.Fenix.manami.inventario.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.Fenix.manami.ImagenUtils
import com.Fenix.manami.inventario.data.InventarioInsertDto
import com.Fenix.manami.inventario.data.inventarioRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CrearMaterialUiState(
    val nombreMaterial: String = "",
    val tipoM: String = "",
    val cantidadM: String = "",
    val unidadMedida: String = "unidades",
    val costoM: String = "",
    val imagenUri: Uri? = null,
    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null
)

class CrearMaterialVistaModelo(
    private val repositorio: inventarioRepositorio = inventarioRepositorio()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrearMaterialUiState())
    val uiState: StateFlow<CrearMaterialUiState> = _uiState.asStateFlow()

    fun onNombreMaterialChange(valor: String) {
        _uiState.value = _uiState.value.copy(nombreMaterial = valor)
    }

    fun onTipoChange(valor: String) {
        _uiState.value = _uiState.value.copy(tipoM = valor)
    }

    fun onCantidadChange(valor: String) {
        _uiState.value = _uiState.value.copy(cantidadM = valor)
    }

    fun onUnidadMedidaChange(valor: String) {
        _uiState.value = _uiState.value.copy(unidadMedida = valor)
    }

    fun onCostoChange(valor: String) {
        _uiState.value = _uiState.value.copy(costoM = valor)
    }

    fun onImagenChange(uri: Uri?) {
        _uiState.value = _uiState.value.copy(imagenUri = uri)
    }

    /**
     * Guarda el material. Si hay imagen elegida, la sube a Supabase Storage
     * y guarda la URL permanente en la base de datos.
     */
    fun guardarMaterial(context: Context) {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.nombreMaterial.isBlank()) {
                _uiState.update { it.copy(error = "El nombre del material es obligatorio") }
                return@launch
            }

            _uiState.update { it.copy(cargando = true, error = null) }

            try {
                // ===== Subir imagen a Supabase (foto permanente) =====
                var imagenTexto = ""
                state.imagenUri?.let { uri ->
                    val bytes = ImagenUtils.uriABytes(context, uri)
                    if (bytes != null) {
                        val nombreArchivo = "material_${System.currentTimeMillis()}.jpg"
                        imagenTexto = repositorio.subirImagen(nombreArchivo, bytes)
                    }
                }

                val material = InventarioInsertDto(
                    nombreMaterial = state.nombreMaterial,
                    tipoM = state.tipoM.ifBlank { null },
                    cantidadM = state.cantidadM.toIntOrNull(),
                    unidadMedida = state.unidadMedida,
                    costoM = state.costoM.toLongOrNull(),
                    imagenM = imagenTexto
                )

                repositorio.insertarMaterial(material)
                _uiState.update { it.copy(cargando = false, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = e.localizedMessage ?: "Error al guardar") }
            }
        }
    }
}
