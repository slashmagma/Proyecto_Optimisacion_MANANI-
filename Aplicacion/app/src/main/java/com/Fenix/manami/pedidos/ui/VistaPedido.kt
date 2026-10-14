package com.Fenix.manami.pedidos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.FechaUtils
import com.Fenix.manami.HeaderFenix
import com.Fenix.manami.pedidos.data.PedidoModelo

@Composable
fun VistaPedido(
    vistaModelo: PedidosVistaModelo = viewModel(),
    onAgregarPedidoClick: () -> Unit = {},
    onPedidoClick: (Int) -> Unit = {},
    onEliminarPedido: (Int) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        vistaModelo.cargarPedidos()
    }
    val uiState by vistaModelo.uiState.collectAsState()

    val morado = Color(0xFF901ACC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            HeaderFenix(
                titulo = "Fenix",
                onNavigate = onNavigate,
                onCerrarSesion = onCerrarSesion
            )

            // Contenido
            when (val state = uiState) {
                is PedidosUiState.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is PedidosUiState.Exito -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // BARRA DE BÚSQUEDA
                        OutlinedTextField(
                            value = state.textoBusqueda,
                            onValueChange = { vistaModelo.onBusquedaChanged(it) },
                            label = { Text("Buscar cliente...") },
                            singleLine = true,
                            trailingIcon = {
                                if (state.textoBusqueda.isNotEmpty()) {
                                    IconButton(onClick = { vistaModelo.onBusquedaChanged("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpiar"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        // FILTROS POR ESTADO
                        val listaEstados = listOf("Espera", "Proceso", "Terminado")

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(listaEstados) { estado ->
                                val estaSeleccionado = state.estadoSeleccionado.equals(estado, ignoreCase = true)

                                FilterChip(
                                    selected = estaSeleccionado,
                                    onClick = { vistaModelo.onEstadoFiltroSelected(estado) },
                                    label = { Text(estado) }
                                )
                            }
                        }

                        // LISTA O MENSAJE VACÍO
                        if (state.listaPedidos.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (state.textoBusqueda.isBlank() && state.estadoSeleccionado.isBlank())
                                        "No hay pedidos registrados"
                                    else
                                        "No se encontraron pedidos para '${state.textoBusqueda}'"
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    FechaUtils.ordenarPorFechaCercana(state.listaPedidos)
                                ) { pedido ->
                                    TarjetaPedido(
                                        pedido = pedido,
                                        onClick = { onPedidoClick(pedido.id) },
                                        onEliminar = { vistaModelo.eliminarPedido(pedido.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                is PedidosUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.mensaje, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { vistaModelo.cargarPedidos() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }

        // Botón flotante "+" → directo a crear pedido
        FloatingActionButton(
            onClick = onAgregarPedidoClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = morado,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar pedido",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun TarjetaPedido(
    pedido: PedidoModelo,
    onClick: () -> Unit = {},
    onEliminar: () -> Unit = {}
) {
    val morado = Color(0xFF901ACC)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Card con fondo gris y texto morado centrado
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFEDEDED)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = morado,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = pedido.nombreCliente,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = morado,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tipo Pedido : ${pedido.detalle ?: "—"}",
                        fontSize = 14.sp,
                        color = morado
                    )
                    Text(
                        text = "Estado del pedido : ${pedido.estado ?: "—"}",
                        fontSize = 14.sp,
                        color = morado
                    )
                    Text(
                        text = "Costo : $${pedido.monto ?: 0}",
                        fontSize = 14.sp,
                        color = morado
                    )
                }

                // Fecha en la esquina superior derecha (dd/MM)
                val fechaCorta = formatearFechaCorta(pedido.fecha)
                if (fechaCorta.isNotEmpty()) {
                    Text(
                        text = fechaCorta,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = morado,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 12.dp)
                    )
                }
            }
        }

        // Basurero fuera de la card
        IconButton(onClick = onEliminar) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar pedido",
                tint = Color(0xFFE53935)
            )
        }
    }
}
