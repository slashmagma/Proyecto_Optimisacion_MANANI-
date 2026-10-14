package com.Fenix.manami.inventario.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Registro del historial de compras de un material.
 *
 * Cada vez que se agrega más material a un inventario queda guardado aquí:
 * nombre, fecha, cantidad con su unidad de medida y costo.
 * Por ahora solo se guarda "por detrás"; más adelante se usará para
 * calcular costos y gastos.
 */
@Serializable
data class ModeloHistorial(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("id_material")
    val idMaterial: Int? = null,

    @SerialName("nombre_material")
    val nombreMaterial: String,

    @SerialName("fecha")
    val fecha: String? = null,

    @SerialName("cantidad")
    val cantidad: Int = 0,

    @SerialName("unidad_medida")
    val unidadMedida: String? = null,

    @SerialName("costo")
    val costo: Long? = null
)

@Serializable
data class HistorialInsertDto(
    @SerialName("id_material")
    val idMaterial: Int? = null,

    @SerialName("nombre_material")
    val nombreMaterial: String,

    @SerialName("fecha")
    val fecha: String? = null,

    @SerialName("cantidad")
    val cantidad: Int = 0,

    @SerialName("unidad_medida")
    val unidadMedida: String? = null,

    @SerialName("costo")
    val costo: Long? = null
)
