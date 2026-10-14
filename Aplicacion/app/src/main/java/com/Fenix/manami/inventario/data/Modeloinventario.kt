package com.Fenix.manami.inventario.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ModeloInventario(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("nombre_material")
    val nombreMaterial: String,

    @SerialName("Tipo_m")
    val tipoM: String? = null,

    @SerialName("Cantidad_m")
    val cantidadM: Int? = null,

    @SerialName("Unidad_medida")
    val unidadMedida: String? = null,

    @SerialName("costo_m")
    val costoM: Long? = null,

    @SerialName("imagen_m")
    val imagenM: String
)

@Serializable
data class InventarioInsertDto(
    @SerialName("nombre_material")
    val nombreMaterial: String,

    @SerialName("Tipo_m")
    val tipoM: String? = null,

    @SerialName("Cantidad_m")
    val cantidadM: Int? = null,

    @SerialName("Unidad_medida")
    val unidadMedida: String? = null,

    @SerialName("costo_m")
    val costoM: Long? = null,

    @SerialName("imagen_m")
    val imagenM: String
)

data class InventarioUiState(
    val nombreMaterial: String = "",
    val tipoM: String = "",
    val cantidadM: String = "",
    val unidadMedida: String = "mts",
    val costoM: String = "",
    val imagenM: String = "",
    val listaMateriales: List<ModeloInventario> = emptyList(),
    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null
)