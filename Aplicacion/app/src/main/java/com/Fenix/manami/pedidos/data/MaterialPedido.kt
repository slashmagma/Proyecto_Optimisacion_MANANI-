package com.Fenix.manami.pedidos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Material que se usa en un pedido.
 *
 * Cada pedido puede tener varios materiales (lista). De cada uno se guarda
 * cuánto se usa y en qué unidad, para poder descontar del inventario y
 * avisar por correo si algo queda con poco stock.
 */
@Serializable
data class MaterialPedidoModelo(
    @SerialName("id") val id: Int? = null,
    @SerialName("id_pedido") val idPedido: Int? = null,
    @SerialName("id_material") val idMaterial: Int? = null,
    @SerialName("nombre_material") val nombreMaterial: String = "",
    @SerialName("cantidad") val cantidad: Float? = null,
    @SerialName("unidad_medida") val unidadMedida: String? = null,
    @SerialName("fecha") val fecha: String? = null
)

@Serializable
data class MaterialPedidoInsertDto(
    @SerialName("id_pedido") val idPedido: Int? = null,
    @SerialName("id_material") val idMaterial: Int? = null,
    @SerialName("nombre_material") val nombreMaterial: String = "",
    @SerialName("cantidad") val cantidad: Float? = null,
    @SerialName("unidad_medida") val unidadMedida: String? = null,
    @SerialName("fecha") val fecha: String? = null
)

/**
 * Material elegido en la pantalla antes de guardarlo.
 * @param cantidadTexto lo que escribió el usuario (puede llevar coma o punto).
 */
data class MaterialPedidoEnMemoria(
    val idMaterial: Int? = null,
    val nombreMaterial: String = "",
    val cantidadTexto: String = "",
    val unidadMedida: String? = null
)
