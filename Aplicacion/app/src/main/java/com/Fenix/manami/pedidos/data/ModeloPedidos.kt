package com.Fenix.manami.pedidos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.Long as Long1

@Serializable
data class TipoTrabajoModelo(
    @SerialName("id") val id: Int? = null,
    @SerialName("Tipo") val tipo: String? = null
)

@Serializable
data class EstadoPedidoModelo(
    @SerialName("id") val id: Int? = null,
    @SerialName("Estado") val estado: String? = null
)

@Serializable
data class ModeloPedidos(
    @SerialName("id")
    val id: Long1? = null,

    @SerialName("Fecha_termino")
    val fechaTermino: String? = null,

    @SerialName("Cliente")
    val cliente: String,

    @SerialName("Materiales")
    val materiales: String? = null,

    @SerialName("descripcion")
    val descripcion: String? = null,

    @SerialName("Pago")
    val pago: Int,


    @SerialName("TipoTrabajo_joined")
    val tipoTrabajo: TipoTrabajoModelo? = null,

    @SerialName("EstadosPedidos_joined")
    val estadoPedido: EstadoPedidoModelo? = null
)

@Serializable
data class PedidoInsertDto(
    @SerialName("Cliente") val cliente: String,
    @SerialName("Pago") val pago: Int?,
    @SerialName("Fecha_termino") val fechaTermino: String?,
    @SerialName("descripcion") val descripcion: String?,
    @SerialName("Materiales") val materiales: String?,
    @SerialName("TipoTrabajo") val idTipo: Int?,
    @SerialName("EstadosPedidos") val idEstado: Int?
)