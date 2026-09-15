package com.Fenix.manami.pedidos.data

import com.Fenix.manami.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PedidoRepositorio {

    private val tabla = SupabaseClient.client.from("Pedidos")

    suspend fun obtenerPedidos(): List<ModeloPedidos> = withContext(Dispatchers.IO) {
        tabla.select(
            Columns.raw(
                "*, TipoTrabajo_joined:TipoTrabajo(*), EstadosPedidos_joined:EstadosPedidos(*)"
            )
        ).decodeList<ModeloPedidos>()
    }

    suspend fun insertarPedido(pedido: PedidoInsertDto) {
        SupabaseClient.client.from("Pedidos").insert(pedido)    }

    suspend fun obtenerTiposTrabajo(): List<TipoTrabajoModelo> = withContext(Dispatchers.IO) {
        SupabaseClient.client.from("TipoTrabajo").select().decodeList<TipoTrabajoModelo>()
    }

    suspend fun obtenerEstados(): List<EstadoPedidoModelo> = withContext(Dispatchers.IO) {
        SupabaseClient.client.from("EstadosPedidos").select().decodeList<EstadoPedidoModelo>()
    }
}