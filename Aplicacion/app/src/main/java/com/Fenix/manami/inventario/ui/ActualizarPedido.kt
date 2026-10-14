package com.Fenix.manami.inventario.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualizarmaterialVista(
    pedidoId: Int,
    vistaModelo: ActualizarPedidoVistaModelo = viewModel(),
    onPedidoActualizado: () -> Unit = {}
) {
    val uiState by vistaModelo.uiState.collectAsState()

    LaunchedEffect(pedidoId) {
        if (pedidoId != 0) {
            vistaModelo.cargarDatosIniciales(pedidoId)
        }
    }

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) {
            onPedidoActualizado()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.cargando) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Editar Pedido #$pedidoId",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = uiState.nombreCliente,
                    onValueChange = { vistaModelo.onNombreClienteChanged(it) },
                    label = { Text("Nombre Cliente") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.materialUtilizado,
                    onValueChange = { vistaModelo.onMaterialUtilizadoChanged(it) },
                    label = { Text("Material Utilizado") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.detalle,
                    onValueChange = { vistaModelo.onDetalleChanged(it) },
                    label = { Text("Detalle") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.monto,
                    onValueChange = { vistaModelo.onMontoChanged(it) },
                    label = { Text("Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.fecha,
                    onValueChange = { vistaModelo.onFechaChanged(it) },
                    label = { Text("Fecha") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.estado,
                    onValueChange = { vistaModelo.onEstadoChanged(it) },
                    label = { Text("Estado") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.colegio,
                    onValueChange = { vistaModelo.onColegioChanged(it) },
                    label = { Text("Colegio") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { vistaModelo.actualizarPedido(pedidoId) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Cambios")
                }
                Button(
                    onClick = { vistaModelo.eliminarPedido(pedidoId) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Eliminar Pedido")
                }
            }
        }
    }
}
