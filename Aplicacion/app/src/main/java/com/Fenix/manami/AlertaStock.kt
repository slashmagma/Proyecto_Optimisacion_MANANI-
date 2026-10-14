package com.Fenix.manami

import com.Fenix.manami.api.CorreoApi
import com.Fenix.manami.inventario.data.ModeloInventario
import com.Fenix.manami.inventario.data.inventarioRepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Control de stock del inventario.
 *
 * Cuando se crea un pedido se descuenta el material usado y, si algo queda
 * por debajo del umbral, se manda un correo con la lista de todo lo que está
 * por quedarse sin material (nombre + cantidad restante + cuánto falta).
 */
object AlertaStock {

    /** Un material se considera "poco" cuando su cantidad llega a este número o menos. */
    const val UMBRAL_STOCK_BAJO = 5

    /** Materiales que están en el umbral bajo o por debajo. */
    fun pocoStock(materiales: List<ModeloInventario>): List<ModeloInventario> =
        materiales
            .filter { (it.cantidadM ?: 0) <= UMBRAL_STOCK_BAJO }
            .sortedBy { it.cantidadM ?: 0 }

    /**
     * Resultado de descontar material de un pedido.
     * @param alcanzaba si había suficiente material para cubrir todo el pedido.
     * @param faltante cuántas unidades faltaron para cubrirlo (0 si alcanzaba).
     */
    class ResultadoDescuento(
        val alcanzaba: Boolean,
        val faltante: Float
    )

    /**
     * Convierte una cantidad a la unidad del material del inventario.
     *
     * Si el material está en metros y el pedido lo pidió en centímetros,
     * se pasan los centímetros a metros (30 cm → 0.3 m).
     * Si las unidades no se pueden convertir (metros contra unidades),
     * se devuelve la cantidad tal cual.
     */
    fun convertirAUnidadDelMaterial(
        cantidad: Float,
        unidadPedido: String?,
        unidadMaterial: String?
    ): Float {
        val desde = (unidadPedido ?: "").lowercase().trim()
        val hacia = (unidadMaterial ?: "").lowercase().trim()
        if (desde.isEmpty() || hacia.isEmpty() || desde == hacia) return cantidad

        val esMetro = { u: String -> u.contains("metro") }
        val esCentimetro = { u: String -> u.contains("cent") }

        return when {
            esCentimetro(desde) && esMetro(hacia) -> cantidad / 100f
            esMetro(desde) && esCentimetro(hacia) -> cantidad * 100f
            else -> cantidad
        }
    }

    /**
     * Descuenta el material del inventario después de crear un pedido.
     *
     * @param materialId id del material en la tabla inventario (null si el pedido no usa material del inventario).
     * @param cantidadPedida unidades que consume el pedido.
     * @param unidadPedido unidad en la que el usuario cargó la cantidad (puede diferir de la del material).
     * @return resultado del descuento, o null si no había material que descontar.
     */
    suspend fun descontarMaterial(
        repositorio: inventarioRepositorio = inventarioRepositorio(),
        materialId: Int?,
        cantidadPedida: Float,
        unidadPedido: String? = null
    ): ResultadoDescuento? = withContext(Dispatchers.IO) {
        if (materialId == null || cantidadPedida <= 0f) return@withContext null

        val material = repositorio.obtenerMaterialPorId(materialId) ?: return@withContext null
        val stockActual = (material.cantidadM ?: 0).toFloat()

        // Pasa la cantidad a la unidad en que está guardado el material
        val pedidaEnUnidadMaterial = convertirAUnidadDelMaterial(
            cantidad = cantidadPedida,
            unidadPedido = unidadPedido,
            unidadMaterial = material.unidadMedida
        )

        val faltante = (pedidaEnUnidadMaterial - stockActual).coerceAtLeast(0f)
        val restante = (stockActual - pedidaEnUnidadMaterial).coerceAtLeast(0f)

        // Nunca deja el stock negativo: si no alcanzó, queda en 0.
        repositorio.actualizarCantidad(materialId, restante.toInt())

        ResultadoDescuento(alcanzaba = faltante <= 0f, faltante = faltante)
    }

    /**
     * Un material del pedido y lo que pasó con su stock.
     */
    class MaterialRevisado(
        val nombre: String,
        val unidad: String?,
        val cantidadPedida: Float,
        val resultado: ResultadoDescuento?
    )

    /**
     * Manda el correo de alerta con TODOS los materiales que están por quedarse sin stock.
     *
     * @param revisados materiales del pedido y qué pasó con cada uno (null si no se pudo descontar).
     */
    suspend fun enviarAlertaStockBajo(
        revisados: List<MaterialRevisado>
    ): Result<String> {
        val repositorio = inventarioRepositorio()
        val inventario = repositorio.obtenerInventario()

        val bajos = pocoStock(inventario)

        val faltoAlgo = revisados.any { it.resultado?.alcanzaba == false }
        val huboAlerta = faltoAlgo || bajos.isNotEmpty()

        // Nada que avisar: ni falta de material ni stock bajo.
        if (!huboAlerta) return Result.success("Sin alertas")

        val ahora = java.time.LocalDate.now()

        val html = buildString {
            appendLine("<h2 style='color:#901ACC;margin-bottom:4px'>Alerta de stock - App Fenix</h2>")
            appendLine("<p style='margin-top:0;color:#666'>Fecha: $ahora</p>")

            // ===== Parte 1: los materiales del pedido =====
            if (revisados.isNotEmpty()) {
                appendLine("<h3 style='margin-bottom:8px'>Materiales del pedido</h3>")
                appendLine("<table cellpadding='6' cellspacing='0' " +
                        "style='border-collapse:collapse;width:100%;max-width:560px'>")
                appendLine("<tr style='background:#901ACC;color:#fff'>" +
                        "<th align='left'>Material</th>" +
                        "<th align='right'>Pedido</th>" +
                        "<th align='right'>Faltante</th>" +
                        "</tr>")

                revisados.forEachIndexed { indice, r ->
                    val fondo = if (indice % 2 == 0) "#F6ECFA" else "#FFFFFF"
                    val faltante = r.resultado?.faltante ?: 0f
                    val unidad = r.unidad ?: ""

                    val textoPedida = if (r.cantidadPedida % 1f == 0f)
                        r.cantidadPedida.toInt().toString() else "${r.cantidadPedida}"

                    val textoFaltante = when {
                        r.resultado == null -> "—"
                        faltante <= 0f -> "✓ ok"
                        faltante % 1f == 0f -> faltante.toInt().toString()
                        else -> "$faltante"
                    }

                    val colorFaltante = if (faltante > 0f) "color:#D32F2F;font-weight:bold" else "color:#4CAF50"

                    appendLine("<tr style='background:$fondo'>" +
                            "<td>${r.nombre}</td>" +
                            "<td align='right'>$textoPedida $unidad</td>" +
                            "<td align='right' style='$colorFaltante'>$textoFaltante</td>" +
                            "</tr>")
                }
                appendLine("</table>")

                if (faltoAlgo) {
                    appendLine("<p style='color:#D32F2F;margin-top:10px'>⚠️ <b>A algún material le faltó stock.</b> " +
                            "El stock quedó en 0 para ese material. Hay que comprar más.</p>")
                }
            }

            // ===== Parte 2: todos los materiales con poco stock =====
            if (bajos.isNotEmpty()) {
                appendLine("<h3 style='margin-bottom:8px'>Materiales con poco stock " +
                        "(${bajos.size})</h3>")
                appendLine("<table cellpadding='6' cellspacing='0' " +
                        "style='border-collapse:collapse;width:100%;max-width:560px'>")
                appendLine("<tr style='background:#901ACC;color:#fff'>" +
                        "<th align='left'>Material</th>" +
                        "<th align='right'>Cantidad</th>" +
                        "<th align='right'>Unidad</th>" +
                        "</tr>")

                bajos.forEachIndexed { indice, m ->
                    val fondo = if (indice % 2 == 0) "#F6ECFA" else "#FFFFFF"
                    val agotado = (m.cantidadM ?: 0) == 0
                    val etiqueta = if (agotado) "  ❌ AGOTADO" else ""
                    appendLine("<tr style='background:$fondo'>" +
                            "<td>${m.nombreMaterial}$etiqueta</td>" +
                            "<td align='right'>${m.cantidadM ?: 0}</td>" +
                            "<td align='right'>${m.unidadMedida ?: "-"}</td>" +
                            "</tr>")
                }
                appendLine("</table>")

                appendLine("<p style='color:#666;margin-top:12px'>Umbral de alerta: " +
                        "$UMBRAL_STOCK_BAJO unidades o menos.</p>")
            }

            append("<p style='color:#999;font-size:12px'>Enviado automáticamente por App Fenix.</p>")
        }

        return enviarAlerta(
            asunto = "Stock bajo en Fenix - $ahora",
            html = html
        )
    }

    // ===== Destinatarios de las alertas automáticas =====

    /**
     * Correos que reciben las alertas (stock y ajustes).
     * El principal viene de local.properties (CORREO_ALERTAS) y el resto
     * son los extras (CORREO_ALERTAS2), si están configurados.
     */
    private val destinatariosAlerta: List<String>
        get() = listOf(BuildConfig.CORREO_ALERTAS, BuildConfig.CORREO_ALERTAS2)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    /** Envía un correo de alerta a todos los destinatarios configurados. */
    private suspend fun enviarAlerta(asunto: String, html: String): Result<String> {
        val lista = destinatariosAlerta
        val principal = lista.firstOrNull()
            ?: return Result.failure(
                IllegalStateException("Falta CORREO_ALERTAS en local.properties")
            )

        return CorreoApi.enviarCorreo(
            para = principal,
            asunto = asunto,
            html = html,
            extras = lista.drop(1)
        )
    }

    // ===== Alerta por ajuste manual (+ / -) del inventario =====

    /**
     * Ventana mínima entre correos de un mismo material.
     * Evita mandar un correo por cada toque del botón "−".
     */
    private const val ESPACIO_ENTRE_ALERTAS_MS = 30L * 60L * 1000L

    /** Última vez que se avisó por cada material (id → timestamp). */
    private val ultimasAlertas =
        java.util.concurrent.ConcurrentHashMap<Int, Long>()

    /**
     * Decide si un ajuste manual merece mandar correo.
     *
     * Se avisa cuando el ajuste:
     *  - es una resta y termina en el umbral o por debajo (<= 5), y además
     *  - cruzó el umbral (venía de más de 5), o dejó el material en 0,
     *    o ya pasó el tiempo mínimo desde el último correo de ese material.
     *
     * @param delta negativo si se quitó material, positivo si se agregó.
     */
    fun debeAvisarAjuste(
        idMaterial: Int,
        cantidadAnterior: Int,
        cantidadNueva: Int,
        delta: Int
    ): Boolean {
        // Solo importa cuando se quita material
        if (delta >= 0) return false

        // No quedó en zona de alerta
        if (cantidadNueva > UMBRAL_STOCK_BAJO) return false

        val cruzoElUmbral = cantidadAnterior > UMBRAL_STOCK_BAJO
        val seAgoTo = cantidadAnterior > 0 && cantidadNueva == 0

        val ahora = System.currentTimeMillis()
        val ultima = ultimasAlertas[idMaterial] ?: 0L
        val pasoElTiempo = (ahora - ultima) >= ESPACIO_ENTRE_ALERTAS_MS

        val avisar = cruzoElUmbral || seAgoTo || pasoElTiempo
        if (avisar) ultimasAlertas[idMaterial] = ahora
        return avisar
    }

    /**
     * Correo de alerta por un ajuste manual del inventario.
     * Muestra de qué material se trató (antes → ahora) y la lista de todo
     * lo que está con poco stock.
     */
    suspend fun enviarAlertaAjusteInventario(
        nombreMaterial: String,
        cantidadAnterior: Int,
        cantidadNueva: Int,
        unidad: String?
    ): Result<String> = withContext(Dispatchers.IO) {

        val inventario = inventarioRepositorio().obtenerInventario()
        val bajos = pocoStock(inventario)
        val ahora = java.time.LocalDate.now()
        val unidadTexto = unidad ?: ""

        val html = buildString {
            appendLine("<h2 style='color:#901ACC;margin-bottom:4px'>Alerta de stock - App Fenix</h2>")
            appendLine("<p style='margin-top:0;color:#666'>Fecha: $ahora</p>")

            // ===== Parte 1: el ajuste que dispara la alerta =====
            appendLine("<h3 style='margin-bottom:8px'>Ajuste en el inventario</h3>")
            appendLine("<table cellpadding='6' cellspacing='0' " +
                    "style='border-collapse:collapse;width:100%;max-width:560px'>")
            appendLine("<tr style='background:#901ACC;color:#fff'>" +
                    "<th align='left'>Material</th>" +
                    "<th align='right'>Antes</th>" +
                    "<th align='right'>Ahora</th>" +
                    "</tr>")
            appendLine("<tr style='background:#F6ECFA'>" +
                    "<td>$nombreMaterial</td>" +
                    "<td align='right'>$cantidadAnterior $unidadTexto</td>" +
                    "<td align='right'><b>$cantidadNueva $unidadTexto</b></td>" +
                    "</tr>")
            appendLine("</table>")

            when {
                cantidadNueva == 0 -> appendLine(
                    "<p style='color:#D32F2F;margin-top:10px'>❌ <b>AGOTADO.</b> " +
                            "Se terminó el material. Hay que comprar más.</p>"
                )
                else -> appendLine(
                    "<p style='color:#B26A00;margin-top:10px'>⚠️ <b>Quedó con poco stock.</b> " +
                            "Llegó a $cantidadNueva $unidadTexto, que está en el umbral de alerta " +
                            "($UMBRAL_STOCK_BAJO o menos).</p>"
                )
            }

            // ===== Parte 2: todos los materiales con poco stock =====
            if (bajos.isNotEmpty()) {
                appendLine("<h3 style='margin-bottom:8px'>Materiales con poco stock " +
                        "(${bajos.size})</h3>")
                appendLine("<table cellpadding='6' cellspacing='0' " +
                        "style='border-collapse:collapse;width:100%;max-width:560px'>")
                appendLine("<tr style='background:#901ACC;color:#fff'>" +
                        "<th align='left'>Material</th>" +
                        "<th align='right'>Cantidad</th>" +
                        "<th align='right'>Unidad</th>" +
                        "</tr>")

                bajos.forEachIndexed { indice, m ->
                    val fondo = if (indice % 2 == 0) "#F6ECFA" else "#FFFFFF"
                    val agotado = (m.cantidadM ?: 0) == 0
                    val etiqueta = if (agotado) "  ❌ AGOTADO" else ""
                    appendLine("<tr style='background:$fondo'>" +
                            "<td>${m.nombreMaterial}$etiqueta</td>" +
                            "<td align='right'>${m.cantidadM ?: 0}</td>" +
                            "<td align='right'>${m.unidadMedida ?: "-"}</td>" +
                            "</tr>")
                }
                appendLine("</table>")

                appendLine("<p style='color:#666;margin-top:12px'>Umbral de alerta: " +
                        "$UMBRAL_STOCK_BAJO unidades o menos.</p>")
            }

            append("<p style='color:#999;font-size:12px'>Enviado automáticamente por App Fenix.</p>")
        }

        enviarAlerta(
            asunto = "Stock bajo en Fenix - $ahora",
            html = html
        )
    }
}
