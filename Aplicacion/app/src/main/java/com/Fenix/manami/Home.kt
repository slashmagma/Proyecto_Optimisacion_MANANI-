package com.Fenix.manami

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.HeaderFenix
import com.Fenix.manami.pedidos.data.PedidoModelo

@Composable
fun Home(
    onNavegarAPedidos: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    vistaModelo: HomeVistaModelo = viewModel()
) {
    val morado = Color(0xFF901ACC)
    val uiState by vistaModelo.uiState.collectAsState()

    LaunchedEffect(Unit) {
        vistaModelo.cargarPedidos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header: menú hamburguesa con navegación a todas las vistas
        HeaderFenix(
            titulo = "Fenix",
            onNavigate = { ruta ->
                if (ruta == "pedidos") {
                    onNavegarAPedidos()
                } else {
                    onNavigate(ruta)
                }
            },
            onCerrarSesion = onCerrarSesion
        )

        // Contenido con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bienvenida
            Text(
                text = "bienvenido usuario",
                color = morado,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Calendario con pedidos
            Calendario(pedidos = uiState.pedidos)

            Spacer(modifier = Modifier.height(24.dp))

            // Título Entregas cercanas
            Text(
                text = "Entregas cercanas",
                color = morado,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Cards de entregas: las 3 fechas más cercanas de entrega
            val cercanas = FechaUtils.entregasCercanas(uiState.pedidos, cantidad = 3)

            if (cercanas.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = morado,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay pedidos registrados",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cercanas.forEach { pedido ->
                        CardEntrega(
                            pedido = pedido,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CardEntrega(pedido: PedidoModelo, modifier: Modifier = Modifier) {
    val morado = Color(0xFF901ACC)
    val hoy = java.time.LocalDate.now()
    val fechaPedido = com.Fenix.manami.FechaUtils.parsearFecha(pedido.fecha)
    val pasoLaFecha = fechaPedido != null && fechaPedido.isBefore(hoy)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (pasoLaFecha) Color(0xFFD32F2F) else morado,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = pedido.nombreCliente.ifBlank { "Sin nombre" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "fecha : ${FechaUtils.fechaCorta(pedido.fecha).ifEmpty { "—" }}",
                fontSize = 11.sp,
                color = if (pasoLaFecha) Color(0xFFD32F2F) else Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "estado : ${pedido.estado ?: "—"}",
                fontSize = 11.sp,
                color = Color(0xFF555555)
            )
            Text(
                text = pedido.colegio ?: "—",
                fontSize = 11.sp,
                color = Color(0xFF555555),
                maxLines = 2
            )
        }
    }
}
