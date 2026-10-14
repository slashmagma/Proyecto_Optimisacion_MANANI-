package com.Fenix.manami

import com.Fenix.manami.inventario.data.inventarioRepositorio
import com.Fenix.manami.pedidos.data.DetallePedidoCompleto
import com.Fenix.manami.pedidos.data.PedidoRepositorio
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Genera el JSON de la copia de seguridad.
 * Separado del ViewModel para poder usarlo tanto desde la pantalla
 * (botón manual) como desde el respaldo automático programado.
 */
object GeneradorRespaldo {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Genera y devuelve el JSON con pedidos, trabajos e inventario. */
    suspend fun generar(): String {
        val repositorioPedidos = PedidoRepositorio()
        val repositorioInventario = inventarioRepositorio()

        val pedidos = repositorioPedidos.obtenerPedidos()
        val inventario = repositorioInventario.obtenerInventario()

        val respaldoPedidos = pedidos.map { pedido ->
            val detalles = repositorioPedidos.obtenerDetallesCompletos(pedido.id)
            RespaldoPedido(
                cliente = pedido.nombreCliente,
                materialUtilizado = pedido.materialUtilizado,
                detalle = pedido.detalle,
                monto = pedido.monto,
                estado = pedido.estado,
                fecha = pedido.fecha,
                colegio = pedido.colegio,
                trabajos = detalles.map { d ->
                    RespaldoTrabajo(
                        tipo = d.tipoPedido ?: "",
                        nombreCompleto = d.nombreCompleto ?: "",
                        medidas = medidasDe(d)
                    )
                }
            )
        }

        val respaldo = Respaldo(
            fechaRespaldo = java.time.LocalDate.now().toString(),
            pedidos = respaldoPedidos,
            inventario = inventario
        )

        return json.encodeToString(respaldo)
    }

    /** Extrae las medidas de un detalle según su tipo. */
    private fun medidasDe(d: DetallePedidoCompleto): Map<String, Float?> {
        return when (d.tipoPedido?.lowercase()) {
            "pantalon" -> d.pantalon?.let {
                mapOf(
                    "ancho" to it.ancho, "alto" to it.alto, "alto_talle" to it.altoTalle,
                    "cintura" to it.cintura, "cadera" to it.cadera, "tiro" to it.tiro
                )
            } ?: emptyMap()
            "polera" -> d.polera?.let {
                mapOf(
                    "espalda" to it.espalda, "ancho" to it.ancho, "alto" to it.alto,
                    "cisa" to it.cisa, "alto_talle" to it.altoTalle, "largo_manga" to it.largoManga,
                    "cintura" to it.cintura, "cadera" to it.cadera, "tiro" to it.tiro
                )
            } ?: emptyMap()
            "birrete+estola" -> d.birreteEstola?.let {
                mapOf("birrete" to it.birrete, "estola" to it.estola)
            } ?: emptyMap()
            else -> emptyMap()
        }
    }
}
