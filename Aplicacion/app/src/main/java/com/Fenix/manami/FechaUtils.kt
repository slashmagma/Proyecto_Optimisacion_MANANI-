package com.Fenix.manami

import com.Fenix.manami.pedidos.data.PedidoModelo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Utilidades para leer y ordenar las fechas de los pedidos.
 *
 * La fecha se guarda en la base como "yyyy-MM-dd". El orden pedido es por
 * fecha más cercana: primero las que faltan (de la más próxima a la más
 * lejana) y después las que ya pasaron (de la más reciente a la más vieja),
 * de modo que arriba siempre quedan las entregas que más importan.
 */
object FechaUtils {

    private val formatoIso = DateTimeFormatter.ISO_LOCAL_DATE

    /** Lee una fecha "yyyy-MM-dd" (o con hora "yyyy-MM-ddTHH:mm"). */
    fun parsearFecha(fecha: String?): LocalDate? {
        if (fecha.isNullOrBlank()) return null
        val texto = fecha.trim().split("T")[0]
        return try {
            LocalDate.parse(texto, formatoIso)
        } catch (e: Exception) {
            // Por si alguna fecha quedó guardada como dd/MM/yyyy
            try {
                val partes = texto.split("/")
                if (partes.size == 3) {
                    LocalDate.of(partes[2].toInt(), partes[1].toInt(), partes[0].toInt())
                } else null
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * Ordena los pedidos por fecha de entrega más cercana.
     * Los que no tienen fecha quedan al final.
     */
    fun ordenarPorFechaCercana(
        pedidos: List<PedidoModelo>,
        hoy: LocalDate = LocalDate.now()
    ): List<PedidoModelo> {
        val conFecha = pedidos.mapNotNull { pedido ->
            parsearFecha(pedido.fecha)?.let { fecha -> Triple(pedido, fecha, pedido.id) }
        }
        val sinFecha = pedidos.filter { parsearFecha(it.fecha) == null }

        // Las que faltan, de la más próxima a la más lejana
        val futuras = conFecha
            .filter { !it.second.isBefore(hoy) }
            .sortedWith(compareBy({ it.second }, { it.third }))

        // Las que ya pasaron, de la más reciente a la más vieja
        val pasadas = conFecha
            .filter { it.second.isBefore(hoy) }
            .sortedWith(compareByDescending { it.second })

        return (futuras + pasadas).map { it.first } + sinFecha
    }

    /** Las [cantidad] entregas con fecha más cercana (para el Home). */
    fun entregasCercanas(
        pedidos: List<PedidoModelo>,
        cantidad: Int = 3,
        hoy: LocalDate = LocalDate.now()
    ): List<PedidoModelo> = ordenarPorFechaCercana(pedidos, hoy).take(cantidad)

    /** Convierte "yyyy-MM-dd" a "dd/MM" para mostrar en las cards. */
    fun fechaCorta(fecha: String?): String {
        val f = parsearFecha(fecha) ?: return ""
        return "%02d/%02d".format(f.dayOfMonth, f.monthValue)
    }

    /** Convierte "yyyy-MM-dd" a "dd/MM/yyyy". */
    fun fechaLarga(fecha: String?): String {
        val f = parsearFecha(fecha) ?: return "—"
        return "%02d/%02d/%04d".format(f.dayOfMonth, f.monthValue, f.year)
    }
}
