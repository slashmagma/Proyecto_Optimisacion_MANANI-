package com.Fenix.manami.pedidos.data

import com.Fenix.manami.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PedidoRepositorio {

    private val tabla = SupabaseClient.client.from("pedidos")

    suspend fun obtenerPedidos(): List<PedidoModelo> = withContext(Dispatchers.IO) {
        tabla.select().decodeList<PedidoModelo>()
    }

    suspend fun obtenerPedidoPorId(id: Int): PedidoModelo? = withContext(Dispatchers.IO) {
        tabla.select() {
            filter {
                eq("id", id)
            }
        }.decodeSingleOrNull<PedidoModelo>()
    }

    suspend fun insertarPedido(pedido: PedidoInsertDto): Int = withContext(Dispatchers.IO) {
        val resultado = SupabaseClient.client.from("pedidos").insert(pedido) {
            select()
        }.decodeList<PedidoModelo>()
        resultado.firstOrNull()?.id ?: 0
    }

    suspend fun actualizarPedido(id: Int, pedido: PedidoInsertDto) = withContext(Dispatchers.IO) {
        tabla.update(pedido) {
            filter {
                eq("id", id)
            }
        }
    }

    suspend fun eliminarPedido(id: Int) = withContext(Dispatchers.IO) {
        tabla.delete {
            filter {
                eq("id", id)
            }
        }
        Unit
    }

    suspend fun actualizarMontoPedido(id: Int, monto: Int?) = withContext(Dispatchers.IO) {
        tabla.update(PedidoMontoDto(monto = monto)) {
            filter {
                eq("id", id)
            }
        }
        Unit
    }

    suspend fun eliminarTrabajoDetalle(idDetalle: Int) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from("detalle_pedidos").delete {
            filter {
                eq("id", idDetalle)
            }
        }
        Unit
    }

    // ============================================
    // BORRADO TOTAL (USADO AL IMPORTAR UNA COPIA)
    // ============================================

    /**
     * Elimina TODOS los pedidos y TODOS sus detalles.
     * Orden importante: primero los detalles (hijos) y luego los pedidos.
     */
    suspend fun eliminarTodosLosPedidos() = withContext(Dispatchers.IO) {
        SupabaseClient.client.from("detalle_pedidos").delete {
            filter {
                gte("id", 0)
            }
        }
        tabla.delete {
            filter {
                gte("id", 0)
            }
        }
        Unit
    }

    /**
     * Elimina todas las medidas (pantalon, polera, birrete_estola) y las
     * personas huérfanas. Se ignora un error en cada tabla: si alguna no
     * permite borrado, las filas que queden sin referencia no se muestran
     * en la interfaz y se reemplazarán solas al importar.
     */
    suspend fun eliminarTodasLasMedidas() = withContext(Dispatchers.IO) {
        listOf("detalle_pedidos", "pantalon", "polera", "birrete_estola", "personas")
            .forEach { nombreTabla ->
                try {
                    SupabaseClient.client.from(nombreTabla).delete {
                        filter {
                            gte("id", 0)
                        }
                    }
                } catch (e: Exception) {
                    // Tabla opcional: no interrumpe la importación
                }
            }
        Unit
    }

    suspend fun obtenerDetallesPedido(pedidoId: Int): List<DetallePedidoModelo> = withContext(Dispatchers.IO) {
        SupabaseClient.client.from("detalle_pedidos").select() {
            filter {
                eq("id_pedido", pedidoId)
            }
        }.decodeList<DetallePedidoModelo>()
    }

    // ============================================
    // NUEVOS MÉTODOS PARA TIPOS DE PEDIDO
    // ============================================

    suspend fun obtenerTipoPedidoId(nombreTipo: String): Int? = withContext(Dispatchers.IO) {
        SupabaseClient.client.from("tipo_pedido").select() {
            filter {
                eq("nombre", nombreTipo)
            }
        }.decodeSingleOrNull<TipoPedidoModelo>()?.id
    }

    suspend fun insertarPantalon(
        ancho: Float?,
        alto: Float?,
        altoTalle: Float?,
        cintura: Float?,
        cadera: Float?,
        tiro: Float?
    ): Int = withContext(Dispatchers.IO) {
        val resultado = SupabaseClient.client.from("pantalon").insert(
            PantalonInsertDto(
                ancho = ancho,
                alto = alto,
                altoTalle = altoTalle,
                cintura = cintura,
                cadera = cadera,
                tiro = tiro
            )
        ) {
            select()
        }.decodeList<PantalonModelo>()
        resultado.firstOrNull()?.id ?: 0
    }

    suspend fun insertarPolera(
        espalda: Float?,
        ancho: Float?,
        alto: Float?,
        cisa: Float?,
        altoTalle: Float?,
        largoManga: Float?,
        cintura: Float?,
        cadera: Float?,
        tiro: Float?
    ): Int = withContext(Dispatchers.IO) {
        val resultado = SupabaseClient.client.from("polera").insert(
            PoleraInsertDto(
                espalda = espalda,
                ancho = ancho,
                alto = alto,
                cisa = cisa,
                altoTalle = altoTalle,
                largoManga = largoManga,
                cintura = cintura,
                cadera = cadera,
                tiro = tiro
            )
        ) {
            select()
        }.decodeList<PoleraModelo>()
        resultado.firstOrNull()?.id ?: 0
    }

    suspend fun insertarBirreteEstola(
        birrete: Float?,
        estola: Float?
    ): Int = withContext(Dispatchers.IO) {
        val resultado = SupabaseClient.client.from("birrete_estola").insert(
            BirreteEstolaInsertDto(
                birrete = birrete,
                estola = estola
            )
        ) {
            select()
        }.decodeList<BirreteEstolaModelo>()
        resultado.firstOrNull()?.id ?: 0
    }

    suspend fun insertarDetallePedido(
        pedidoId: Int,
        nombreCompleto: String,
        tipoPedidoId: Int? = null,
        pantalonId: Int? = null,
        poleraId: Int? = null,
        birreteEstolaId: Int? = null
    ) = withContext(Dispatchers.IO) {
        SupabaseClient.client.from("detalle_pedidos").insert(
            DetallePedidoFilaDto(
                idPedido = pedidoId,
                nombreCompleto = nombreCompleto,
                tipoPedidoId = tipoPedidoId,
                pantalonId = pantalonId,
                poleraId = poleraId,
                birreteEstolaId = birreteEstolaId
            )
        )
    }

    // ============================================
    // OBTENER DATOS DE LAS TABLAS ESPECÍFICAS
    // ============================================

    suspend fun obtenerPantalon(id: Int): PantalonModelo? = withContext(Dispatchers.IO) {
        SupabaseClient.client.from("pantalon").select() {
            filter { eq("id", id) }
        }.decodeSingleOrNull<PantalonModelo>()
    }

    suspend fun obtenerPolera(id: Int): PoleraModelo? = withContext(Dispatchers.IO) {
        SupabaseClient.client.from("polera").select() {
            filter { eq("id", id) }
        }.decodeSingleOrNull<PoleraModelo>()
    }

    suspend fun obtenerBirreteEstola(id: Int): BirreteEstolaModelo? = withContext(Dispatchers.IO) {
        SupabaseClient.client.from("birrete_estola").select() {
            filter { eq("id", id) }
        }.decodeSingleOrNull<BirreteEstolaModelo>()
    }

    suspend fun obtenerDetallesCompletos(pedidoId: Int): List<DetallePedidoCompleto> = withContext(Dispatchers.IO) {
        val detalles = obtenerDetallesPedido(pedidoId)

        detalles.mapNotNull { detalle ->
            val tipoPedido = detalle.tipoPedidoId?.let { id ->
                SupabaseClient.client.from("tipo_pedido").select() {
                    filter { eq("id", id) }
                }.decodeSingleOrNull<TipoPedidoModelo>()
            }

            val pantalon = detalle.pantalonId?.let { obtenerPantalon(it) }
            val polera = detalle.poleraId?.let { obtenerPolera(it) }
            val birreteEstola = detalle.birreteEstolaId?.let { obtenerBirreteEstola(it) }

            DetallePedidoCompleto(
                idDetalle = detalle.id,
                idPedido = detalle.idPedido,
                idPersona = detalle.idPersona,
                nombreCompleto = detalle.nombreCompleto,
                tipoPedidoId = detalle.tipoPedidoId,
                pantalonId = detalle.pantalonId,
                poleraId = detalle.poleraId,
                birreteEstolaId = detalle.birreteEstolaId,
                tipoPedido = tipoPedido?.nombre,
                pantalon = pantalon,
                polera = polera,
                birreteEstola = birreteEstola
            )
        }
    }

    suspend fun obtenerDetalleCompleto(pedidoId: Int): DetallePedidoCompleto? = withContext(Dispatchers.IO) {
        obtenerDetallesCompletos(pedidoId).firstOrNull()
    }

    // ===== Materiales que usa un pedido =====

    private val tablaMaterialesPedido = SupabaseClient.client.from("materiales_pedido")

    /** Guarda la lista de materiales que usa un pedido. */
    suspend fun guardarMaterialesPedido(
        pedidoId: Int,
        materiales: List<MaterialPedidoInsertDto>
    ) = withContext(Dispatchers.IO) {
        materiales.forEach { material ->
            tablaMaterialesPedido.insert(material.copy(idPedido = pedidoId))
        }
        Unit
    }

    /** Materiales que usa un pedido. */
    suspend fun obtenerMaterialesPedido(pedidoId: Int): List<MaterialPedidoModelo> =
        withContext(Dispatchers.IO) {
            tablaMaterialesPedido.select {
                filter {
                    eq("id_pedido", pedidoId)
                }
            }.decodeList<MaterialPedidoModelo>()
        }

    /** Borra los materiales de un pedido (antes de volver a guardarlos). */
    suspend fun eliminarMaterialesPedido(pedidoId: Int) = withContext(Dispatchers.IO) {
        tablaMaterialesPedido.delete {
            filter {
                eq("id_pedido", pedidoId)
            }
        }
        Unit
    }
}

data class DetallePedidoCompleto(
    val idDetalle: Int? = null,
    val idPedido: Int?,
    val idPersona: Int?,
    val nombreCompleto: String?,
    val tipoPedidoId: Int?,
    val pantalonId: Int?,
    val poleraId: Int?,
    val birreteEstolaId: Int?,
    val tipoPedido: String?,
    val pantalon: PantalonModelo?,
    val polera: PoleraModelo?,
    val birreteEstola: BirreteEstolaModelo?
)
