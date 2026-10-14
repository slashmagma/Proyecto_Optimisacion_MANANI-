package com.Fenix.manami

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.Fenix.manami.pedidos.data.PedidoModelo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Paleta de colores para diferenciar pedidos
val coloresPedidos = listOf(
    Color(0xFF4CAF50), // verde
    Color(0xFF2196F3), // azul
    Color(0xFFFF9800), // naranja
    Color(0xFFE91E63), // rosa
    Color(0xFF9C27B0), // morado
    Color(0xFF00BCD4), // celeste
    Color(0xFFFF5722), // naranja rojo
    Color(0xFF8BC34A)  // verde claro
)

@Composable
fun Calendario(pedidos: List<PedidoModelo> = emptyList()) {
    val morado = Color(0xFF901ACC)
    val grisBorde = Color(0xFFD5D5D5)
    val grisPasado = Color(0xFF777777)

    val hoy = LocalDate.now()
    // Mes/año visibles (navegables con flechas)
    var mesVisible by remember { mutableStateOf(hoy.withDayOfMonth(1)) }

    val anioActual = mesVisible.year
    val mesActual = mesVisible.monthValue

    // Fecha actual formateada para comparar (yyyy-MM-dd)
    val fechaHoyStr = hoy.format(DateTimeFormatter.ISO_LOCAL_DATE)

    // Pedidos agrupados por fecha (dd-MM-yyyy o yyyy-MM-dd)
    val pedidosPorFecha: Map<String, List<PedidoModelo>> = pedidos
        .filter { !it.fecha.isNullOrBlank() }
        .groupBy { normalizarFecha(it.fecha!!) }

    val nombresMeses = listOf(
        "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO",
        "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"
    )

    val nombreMes = nombresMeses[mesActual - 1]
    val primerDiaMes = LocalDate.of(anioActual, mesActual, 1)
    val diasEnMes = primerDiaMes.lengthOfMonth()

    // Calcular en qué día de la semana cae el 1 (LUNES=0 ... DOMINGO=6)
    val offsetDias = primerDiaMes.dayOfWeek.value - 1

    // Generar celdas del calendario (incluye días vacíos al inicio)
    val celdas = mutableListOf<String>()
    repeat(offsetDias) { celdas.add("") }
    for (dia in 1..diasEnMes) {
        celdas.add(dia.toString())
    }
    while (celdas.size % 7 != 0) {
        celdas.add("")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = morado)
            .background(Color.White)
    ) {
        // ===== CABECERA CON FLECHAS DE NAVEGACIÓN =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flecha ← mes anterior
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        mesVisible = mesVisible.minusMonths(1)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Mes anterior",
                    tint = morado
                )
            }

            // Mes y año (sin número de día)
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = nombreMes,
                    fontSize = 22.sp,
                    color = Color(0xFF555555),
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = anioActual.toString(),
                    fontSize = 22.sp,
                    color = Color(0xFF555555),
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Flecha → mes siguiente (pegada a la esquina derecha)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        mesVisible = mesVisible.plusMonths(1)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Mes siguiente",
                    tint = morado
                )
            }
        }

        // ===== DÍAS DE LA SEMANA =====
        FilaDiasSemana(
            listOf("LU", "MA", "MI", "JU", "VI", "SA", "DOM"),
            grisBorde
        )

        // ===== SEMANAS =====
        celdas.chunked(7).forEach { semana ->
            FilaCalendario(
                dias = semana,
                grisBorde = grisBorde,
                grisPasado = grisPasado,
                morado = morado,
                fechaHoy = fechaHoyStr,
                anio = anioActual,
                mes = mesActual,
                pedidosPorFecha = pedidosPorFecha
            )
        }
    }
}

// Normaliza la fecha a yyyy-MM-dd para comparar
fun normalizarFecha(fecha: String): String {
    return try {
        // Intentar yyyy-MM-dd (Postgres)
        LocalDate.parse(fecha).format(DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        try {
            // Intentar dd/MM/yyyy
            val partes = fecha.split("/")
            if (partes.size == 3) {
                val dia = partes[0].padStart(2, '0')
                val mes = partes[1].padStart(2, '0')
                val anio = partes[2]
                "$anio-$mes-$dia"
            } else {
                fecha
            }
        } catch (e2: Exception) {
            fecha
        }
    }
}

// =================================================
// CABECERA
// =================================================

@Composable
fun CeldaCabecera(
    texto: String,
    ancho: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(ancho / 4f)
            .height(42.dp)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            fontSize = 22.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
    }
}

// =================================================
// DÍAS DE LA SEMANA
// =================================================

@Composable
fun FilaDiasSemana(
    dias: List<String>,
    grisBorde: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
    ) {
        dias.forEach { dia ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .border(width = 0.5.dp, color = grisBorde)
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dia,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF444444),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// =================================================
// FILA DEL CALENDARIO
// =================================================

@Composable
fun FilaCalendario(
    dias: List<String>,
    grisBorde: Color,
    grisPasado: Color,
    morado: Color,
    fechaHoy: String,
    anio: Int,
    mes: Int,
    pedidosPorFecha: Map<String, List<PedidoModelo>>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
    ) {
        dias.forEach { dia ->
            val numeroDia = dia.toIntOrNull()

            // Fecha de esta celda
            val fechaCelda = if (numeroDia != null) {
                String.format("%04d-%02d-%02d", anio, mes, numeroDia)
            } else {
                ""
            }

            val esHoy = fechaCelda == fechaHoy
            val esPasado = numeroDia != null && fechaCelda < fechaHoy

            // Colores de los pedidos de este día
            val pedidosDelDia = pedidosPorFecha[fechaCelda] ?: emptyList()
            val coloresDelDia = pedidosDelDia.map { pedido ->
                // Asignar color por índice del pedido en la lista general
                val indice = (pedido.id % coloresPedidos.size)
                coloresPedidos[indice]
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .background(
                        color = if (esPasado || dia.isEmpty()) Color(0xFFD5D5D5) else Color.White
                    )
                    .border(
                        width = if (esHoy) 2.dp else 0.5.dp,
                        color = if (esHoy) morado else grisBorde,
                        shape = if (esHoy) RoundedCornerShape(4.dp) else RectangleShape
                    )
                    .padding(3.dp)
            ) {
                if (dia.isNotEmpty()) {
                    Text(
                        text = dia,
                        modifier = Modifier.align(Alignment.TopEnd),
                        fontSize = 8.sp,
                        color = if (esPasado) grisPasado else Color(0xFF555555)
                    )
                }

                // Puntos de pedidos (uno por cada pedido del día)
                if (coloresDelDia.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        coloresDelDia.take(3).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(color = color, shape = CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
}
