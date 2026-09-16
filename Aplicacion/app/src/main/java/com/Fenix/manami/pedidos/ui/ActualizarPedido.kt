package com.Fenix.manami.pedidos.ui

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
fun ActualizarPedidoVista(
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
                    value = uiState.cliente,
                    onValueChange = { vistaModelo.onClienteChanged(it) },
                    label = { Text("Cliente") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.pago,
                    onValueChange = { vistaModelo.onPagoChanged(it) },
                    label = { Text("Pago") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.fechaTermino,
                    onValueChange = { vistaModelo.onFechaTerminoChanged(it) },
                    label = { Text("Fecha Término") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.descripcion,
                    onValueChange = { vistaModelo.onDescripcionChanged(it) },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.materiales,
                    onValueChange = { vistaModelo.onMaterialesChanged(it) },
                    label = { Text("Materiales") },
                    modifier = Modifier.fillMaxWidth()
                )

                DesplegableSeleccion(
                    etiqueta = "Tipo de Trabajo",
                    opciones = uiState.listaTipos.map { Pair(it.id ?: 0, it.tipo ?: "") },
                    seleccionadoId = uiState.idTipoTrabajoSeleccionado,
                    onSeleccionado = { vistaModelo.onTipoTrabajoSeleccionado(it) }
                )

                DesplegableSeleccion(
                    etiqueta = "Estado del Pedido",
                    opciones = uiState.listaEstados.map { Pair(it.id ?: 0, it.estado ?: "") },
                    seleccionadoId = uiState.idEstadoSeleccionado,
                    onSeleccionado = { vistaModelo.onEstadoSeleccionado(it) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DesplegableSeleccion(
    etiqueta: String,
    opciones: List<Pair<Int, String>>,
    seleccionadoId: Int?,
    onSeleccionado: (Int) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    val textoSeleccionado = opciones.find { it.first == seleccionadoId }?.second ?: "Seleccionar..."

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido }
    ) {
        OutlinedTextField(
            value = textoSeleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { (id, nombre) ->
                DropdownMenuItem(
                    text = { Text(nombre) },
                    onClick = {
                        onSeleccionado(id)
                        expandido = false
                    }
                )
            }
        }
    }
}