package com.Fenix.manami.pedidos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TipoTrabajoModelo(
    @SerialName("id") val id: Int? = null,
    @SerialName("Tipo") val tipo: String? = null
)

@Serializable
data class TipoPedidoModelo(
    @SerialName("id") val id: Int? = null,
    @SerialName("nombre") val nombre: String? = null,
    @SerialName("descripcion") val descripcion: String? = null
)

@Serializable
data class EstadoPedidoModelo(
    @SerialName("id") val id: Int? = null,
    @SerialName("Estado") val estado: String? = null
)

@Serializable
data class ModeloPedidos(
    @SerialName("id") val id: Int,
    @SerialName("Nombre_cliente") val cliente: String,
    @SerialName("Material_utilizado") val materiales: String? = null,
    @SerialName("Detalle") val descripcion: String? = null,
    @SerialName("Monto") val pago: Int? = null,
    @SerialName("Estado") val estado: String? = null,
    @SerialName("Fecha") val fechaTermino: String? = null,
    @SerialName("Colegio") val colegio: String? = null
)

@Serializable
data class DetallePedidoModelo(
    @SerialName("id") val id: Int? = null,
    @SerialName("id_pedido") val idPedido: Int? = null,
    @SerialName("id_persona") val idPersona: Int? = null,
    @SerialName("nombre_completo") val nombreCompleto: String? = null,
    @SerialName("tipo_pedido_id") val tipoPedidoId: Int? = null,
    @SerialName("pantalon_id") val pantalonId: Int? = null,
    @SerialName("polera_id") val poleraId: Int? = null,
    @SerialName("birrete_estola_id") val birreteEstolaId: Int? = null,
    @SerialName("Espalda") val espalda: Float? = null,
    @SerialName("ancho") val ancho: Float? = null,
    @SerialName("Alto") val alto: Float? = null,
    @SerialName("cisa") val cisa: Float? = null,
    @SerialName("alto_talle") val altoTalle: Float? = null,
    @SerialName("largo_manga") val largoManga: Float? = null,
    @SerialName("Cintura") val cintura: Float? = null,
    @SerialName("Cadera") val cadera: Float? = null,
    @SerialName("Tiro") val tiro: Float? = null,
    @SerialName("Ancho_pierna") val anchoPierna: Float? = null,
    @SerialName("Ancho_pierna_int") val anchoPiernaInt: Float? = null,
    @SerialName("Alto_falda") val altoFalda: Float? = null,
    @SerialName("Alto_pantalon") val altoPantalon: Float? = null,
    @SerialName("Birrete") val birrete: Float? = null
)

@Serializable
data class PedidoModelo(
    @SerialName("id") val id: Int,
    @SerialName("Nombre_cliente") val nombreCliente: String,
    @SerialName("Material_utilizado") val materialUtilizado: String? = null,
    @SerialName("Detalle") val detalle: String? = null,
    @SerialName("Monto") val monto: Int? = null,
    @SerialName("Estado") val estado: String? = null,
    @SerialName("Fecha") val fecha: String? = null,
    @SerialName("Colegio") val colegio: String? = null
)

@Serializable
data class PedidoInsertDto(
    @SerialName("Nombre_cliente") val nombreCliente: String,
    @SerialName("Material_utilizado") val materialUtilizado: String? = null,
    @SerialName("Detalle") val detalle: String? = null,
    @SerialName("Monto") val monto: Int? = null,
    @SerialName("Estado") val estado: String? = null,
    @SerialName("Fecha") val fecha: String? = null,
    @SerialName("Colegio") val colegio: String? = null
)

@Serializable
data class DetallePedidoInsertDto(
    @SerialName("id_pedido") val idPedido: Int,
    @SerialName("id_persona") val idPersona: Int? = null,
    @SerialName("nombre_completo") val nombreCompleto: String? = null,
    @SerialName("Tipo_pedido") val tipoPedido: String? = null,
    @SerialName("Espalda") val espalda: Float? = null,
    @SerialName("ancho") val ancho: Float? = null,
    @SerialName("Alto") val alto: Float? = null,
    @SerialName("cisa") val cisa: Float? = null,
    @SerialName("alto_talle") val altoTalle: Float? = null,
    @SerialName("largo_manga") val largoManga: Float? = null,
    @SerialName("Cintura") val cintura: Float? = null,
    @SerialName("Cadera") val cadera: Float? = null,
    @SerialName("Tiro") val tiro: Float? = null,
    @SerialName("Ancho_pierna") val anchoPierna: Float? = null,
    @SerialName("Ancho_pierna_int") val anchoPiernaInt: Float? = null,
    @SerialName("Alto_falda") val altoFalda: Float? = null,
    @SerialName("Alto_pantalon") val altoPantalon: Float? = null,
    @SerialName("Birrete") val birrete: Float? = null
)

@Serializable
data class PedidoMontoDto(
    @SerialName("Monto") val monto: Int? = null
)

@Serializable
data class PantalonInsertDto(
    @SerialName("ancho") val ancho: Float? = null,
    @SerialName("alto") val alto: Float? = null,
    @SerialName("alto_talle") val altoTalle: Float? = null,
    @SerialName("cintura") val cintura: Float? = null,
    @SerialName("cadera") val cadera: Float? = null,
    @SerialName("tiro") val tiro: Float? = null
)

@Serializable
data class PoleraInsertDto(
    @SerialName("espalda") val espalda: Float? = null,
    @SerialName("ancho") val ancho: Float? = null,
    @SerialName("alto") val alto: Float? = null,
    @SerialName("cisa") val cisa: Float? = null,
    @SerialName("alto_talle") val altoTalle: Float? = null,
    @SerialName("largo_manga") val largoManga: Float? = null,
    @SerialName("cintura") val cintura: Float? = null,
    @SerialName("cadera") val cadera: Float? = null,
    @SerialName("tiro") val tiro: Float? = null
)

@Serializable
data class BirreteEstolaInsertDto(
    @SerialName("birrete") val birrete: Float? = null,
    @SerialName("estola") val estola: Float? = null
)

@Serializable
data class DetallePedidoFilaDto(
    @SerialName("id_pedido") val idPedido: Int,
    @SerialName("nombre_completo") val nombreCompleto: String? = null,
    @SerialName("tipo_pedido_id") val tipoPedidoId: Int? = null,
    @SerialName("pantalon_id") val pantalonId: Int? = null,
    @SerialName("polera_id") val poleraId: Int? = null,
    @SerialName("birrete_estola_id") val birreteEstolaId: Int? = null
)

@Serializable
data class PantalonModelo(
    @SerialName("id") val id: Int? = null,
    @SerialName("ancho") val ancho: Float? = null,
    @SerialName("alto") val alto: Float? = null,
    @SerialName("alto_talle") val altoTalle: Float? = null,
    @SerialName("cintura") val cintura: Float? = null,
    @SerialName("cadera") val cadera: Float? = null,
    @SerialName("tiro") val tiro: Float? = null
)

@Serializable
data class PoleraModelo(
    @SerialName("id") val id: Int? = null,
    @SerialName("espalda") val espalda: Float? = null,
    @SerialName("ancho") val ancho: Float? = null,
    @SerialName("alto") val alto: Float? = null,
    @SerialName("cisa") val cisa: Float? = null,
    @SerialName("alto_talle") val altoTalle: Float? = null,
    @SerialName("largo_manga") val largoManga: Float? = null,
    @SerialName("cintura") val cintura: Float? = null,
    @SerialName("cadera") val cadera: Float? = null,
    @SerialName("tiro") val tiro: Float? = null
)

@Serializable
data class BirreteEstolaModelo(
    @SerialName("id") val id: Int? = null,
    @SerialName("birrete") val birrete: Float? = null,
    @SerialName("estola") val estola: Float? = null
)

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
