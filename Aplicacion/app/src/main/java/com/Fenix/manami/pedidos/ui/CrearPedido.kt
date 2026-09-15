package com.Fenix.manami.pedidos.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPedidoPantalla(
    viewModel: CrearPedidoVistaModelo = viewModel(),
    onPedidoCreado: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            onPedidoCreado()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nuevo Pedido") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.cliente,
                onValueChange = { viewModel.onClienteChange(it) },
                label = { Text("Cliente *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.pago,
                onValueChange = { viewModel.onPagoChange(it) },
                label = { Text("Pago") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Selector de fecha clickable en todo el campo
            OutlinedTextField(
                value = state.fechaTermino,
                onValueChange = {},
                readOnly = true,
                enabled = false, // Evita foco de teclado
                label = { Text("Fecha de Término") },
                trailingIcon = {
                    IconButton(onClick = { mostrarDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDatePicker = true }
            )

            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { viewModel.onDescripcionChange(it) },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.materiales,
                onValueChange = { viewModel.onMaterialesChange(it) },
                label = { Text("Materiales") },
                modifier = Modifier.fillMaxWidth()
            )

            // Selector Tipo de Trabajo
            SelectorDropdown(
                titulo = "Tipo de Trabajo",
                opciones = state.listaTipos,
                opcionSeleccionada = state.tipoTrabajoSeleccionado?.tipo ?: "",
                onOpcionSeleccionada = { viewModel.onTipoTrabajoChange(it) }
            )

            // Selector Estado Pedido
            SelectorDropdown(
                titulo = "Estado del Pedido",
                opciones = state.listaEstados,
                opcionSeleccionada = state.estadoSeleccionado?.estado ?: "",
                onOpcionSeleccionada = { viewModel.onEstadoChange(it) }
            )

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.guardarPedido() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.cargando
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar Pedido")
                }
            }
        }

        // El diálogo del calendario dentro de la función y del Scaffold
        if (mostrarDatePicker) {
            DatePickerDialog(
                onDismissRequest = { mostrarDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val fechaSeleccionadaMs = datePickerState.selectedDateMillis
                            if (fechaSeleccionadaMs != null) {
                                val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                formato.timeZone = TimeZone.getTimeZone("UTC")
                                val fechaTexto = formato.format(Date(fechaSeleccionadaMs))
                                viewModel.onFechaTerminoChange(fechaTexto)
                            }
                            mostrarDatePicker = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectorDropdown(
    titulo: String,
    opciones: List<T>,
    opcionSeleccionada: String,
    onOpcionSeleccionada: (T) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expandido,
        onExpandedChange = { expandido = !expandido }
    ) {
        OutlinedTextField(
            value = opcionSeleccionada,
            onValueChange = {},
            readOnly = true,
            label = { Text(titulo) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                val texto = when (opcion) {
                    is com.Fenix.manami.pedidos.data.TipoTrabajoModelo -> opcion.tipo ?: ""
                    is com.Fenix.manami.pedidos.data.EstadoPedidoModelo -> opcion.estado ?: ""
                    else -> ""
                }
                DropdownMenuItem(
                    text = { Text(texto) },
                    onClick = {
                        onOpcionSeleccionada(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}