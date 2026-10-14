package com.Fenix.manami.pedidos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.HeaderFenix
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPedidoPantalla(
    viewModel: CrearPedidoVistaModelo = viewModel(),
    onPedidoCreado: (Int) -> Unit,
    onVolver: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var mostrarDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            onPedidoCreado(state.pedidoCreadoId ?: 0)
        }
    }

    val morado = Color(0xFF901ACC)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        HeaderFenix(
            titulo = "Fenix",
            onNavigate = onNavigate,
            onCerrarSesion = onCerrarSesion
        )

        // Botón volver + título
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onVolver() },
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onVolver) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = morado
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Nuevo pedido",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        // Formulario
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nombre cliente
            OutlinedTextField(
                value = state.nombreCliente,
                onValueChange = { viewModel.onNombreClienteChange(it) },
                placeholder = { Text("Nombre cliente *", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )

            // Material utilizado
            OutlinedTextField(
                value = state.materialUtilizado,
                onValueChange = { viewModel.onMaterialUtilizadoChange(it) },
                placeholder = { Text("Material utilizado", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )

            // Detalle
            OutlinedTextField(
                value = state.detalle,
                onValueChange = { viewModel.onDetalleChange(it) },
                placeholder = { Text("Detalle", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )

            // Monto
            OutlinedTextField(
                value = state.monto,
                onValueChange = { viewModel.onMontoChange(it) },
                placeholder = { Text("Monto", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )

            // Fecha
            OutlinedTextField(
                value = state.fecha,
                onValueChange = {},
                readOnly = true,
                enabled = false,
                placeholder = { Text("Fecha", color = Color.Gray) },
                trailingIcon = {
                    IconButton(onClick = { mostrarDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Gray,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    disabledTrailingIconColor = Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDatePicker = true }
            )

            // Colegio
            OutlinedTextField(
                value = state.colegio,
                onValueChange = { viewModel.onColegioChange(it) },
                placeholder = { Text("Colegio", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Guardar Pedido
            Button(
                onClick = { viewModel.guardarPedido() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !state.cargando,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = morado,
                    contentColor = Color.White
                )
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Guardar Pedido",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // DatePicker
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
                                viewModel.onFechaChange(fechaTexto)
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
