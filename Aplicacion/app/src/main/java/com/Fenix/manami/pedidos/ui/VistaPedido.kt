package com.Fenix.manami.pedidos.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.pedidos.data.ModeloPedidos
import androidx.compose.runtime.LaunchedEffect
@Composable
fun VistaPedido(
    vistaModelo: PedidosVistaModelo = viewModel(),
    onAgregarPedidoClick: () -> Unit = {}
) {

    val uiState by vistaModelo.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAgregarPedidoClick) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is PedidosUiState.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PedidosUiState.Exito -> {
                    if (state.listaPedidos.isEmpty()) {
                        Text(
                            text = "No hay pedidos registrados",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.listaPedidos) { pedido ->
                                TarjetaPedido(pedido = pedido)
                            }
                        }
                    }
                }
                is PedidosUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
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
    }
}

@Composable
fun TarjetaPedido(
    pedido: ModeloPedidos
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = pedido.cliente,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tipo: ${pedido.tipoTrabajo?.tipo ?: "No asignado"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Estado: ${pedido.estadoPedido?.estado ?: "No asignado"}",
                style = MaterialTheme.typography.bodySmall
            )
            if (!pedido.descripcion.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = pedido.descripcion,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}