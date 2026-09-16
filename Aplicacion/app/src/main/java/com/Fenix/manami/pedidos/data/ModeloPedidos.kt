package com.Fenix.manami.pedidos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    val id: Int ,

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

//Actualizar datos
data class ActualizarUiState(
    val cliente: String = "",
    val descripcion: String = "",
    val materiales: String = "",
    val pago: String = "",
    val fechaTermino: String = "",
    val idTipoTrabajoSeleccionado: Int? = null,
    val idEstadoSeleccionado: Int? = null,
    val listaTipos: List<TipoTrabajoModelo> = emptyList(),
    val listaEstados: List<EstadoPedidoModelo> = emptyList(),
    val cargando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val error: String? = null
)