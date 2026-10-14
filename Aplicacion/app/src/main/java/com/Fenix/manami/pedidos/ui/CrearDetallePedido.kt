package com.Fenix.manami.pedidos.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.HeaderFenix
import com.Fenix.manami.MedidasUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearDetallePedido(
    pedidoId: Int,
    onVolver: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onGuardado: () -> Unit = {},
    vistaModelo: CrearDetallePedidoVistaModelo = viewModel()
) {
    val morado = Color(0xFF901ACC)
    val state by vistaModelo.uiState.collectAsState()

    LaunchedEffect(pedidoId) {
        vistaModelo.cargarDatos(pedidoId)
    }

    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            onGuardado()
        }
    }

    var dropdownExpandido by remember { mutableStateOf(false) }
    val tiposTrabajo = listOf("pantalon", "polera", "birrete+estola")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        HeaderFenix(
            titulo = "Fenix",
            onNavigate = onNavigate,
            onCerrarSesion = onCerrarSesion
        )

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
                text = "Detalle_Pedido",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Nombre completo
            OutlinedTextField(
                value = state.nombreCompleto,
                onValueChange = { vistaModelo.onNombreCompletoChange(it) },
                placeholder = { Text("nombre completo", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )

            // Tipo pedido (solo lectura)
            OutlinedTextField(
                value = state.tipoPedido,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("tipo pedido", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )

            // Secciones expandibles para cada trabajo
            state.trabajos.forEachIndexed { index, trabajo ->
                SeccionTrabajo(
                    index = index,
                    trabajo = trabajo,
                    tiposTrabajo = tiposTrabajo,
                    onNombreCompletoChange = { vistaModelo.onTrabajoNombreCompletoChange(index, it) },
                    onTipoTrabajoChange = { tipoIndex, tipo -> vistaModelo.onTrabajoTipoTrabajoChange(index, tipoIndex, tipo) },
                    onCampoChange = { tipoIndex, campo, valor -> vistaModelo.onTrabajoCampoChange(index, tipoIndex, campo, valor) },
                    onEliminar = { vistaModelo.eliminarTrabajo(index) },
                    onAgregarTipoTrabajo = { vistaModelo.agregarTipoTrabajo(index) }
                )
            }

            // Botón "+" para añadir otro trabajo (al final de todas las secciones)
            Button(
                onClick = { vistaModelo.agregarTrabajo() },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = morado,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir trabajo",
                    modifier = Modifier.size(24.dp)
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { vistaModelo.guardarDetalle(pedidoId) },
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionTrabajo(
    index: Int,
    trabajo: TrabajoDetalle,
    tiposTrabajo: List<String>,
    onNombreCompletoChange: (String) -> Unit,
    onTipoTrabajoChange: (Int, String) -> Unit,
    onCampoChange: (Int, String, String) -> Unit,
    onEliminar: () -> Unit,
    onAgregarTipoTrabajo: () -> Unit
) {
    val morado = Color(0xFF901ACC)
    var expandido by remember { mutableStateOf(true) }

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
        ) {
            // Header expandible con nombre
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trabajo.nombreCompleto.ifEmpty { "Trabajo ${index + 1}" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEliminar) {
                        Text("✕", color = Color(0xFFE53935), fontSize = 18.sp)
                    }
                    Icon(
                        imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandido) "Contraer" else "Expandir",
                        tint = morado
                    )
                }
            }

            // Contenido expandible
            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Campo nombre completo dentro de la sección
                    OutlinedTextField(
                        value = trabajo.nombreCompleto,
                        onValueChange = onNombreCompletoChange,
                        placeholder = { Text("nombre completo", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black
                        )
                    )

                    // Múltiples tipos de trabajo con sus dropdowns
                    trabajo.tiposTrabajo.forEachIndexed { tipoIndex, tipoTrabajoDetalle ->
                        var dropdownExpandido by remember { mutableStateOf(false) }

                        // Dropdown tipo de trabajo
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpandido,
                            onExpandedChange = { dropdownExpandido = !dropdownExpandido }
                        ) {
                            OutlinedTextField(
                                value = tipoTrabajoDetalle.tipo,
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("tipo de trabajo", color = Color.Gray) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Seleccionar"
                                    )
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(4.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Gray,
                                    focusedBorderColor = Color.Black
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpandido,
                                onDismissRequest = { dropdownExpandido = false }
                            ) {
                                tiposTrabajo.forEach { tipo ->
                                    DropdownMenuItem(
                                        text = { Text(tipo) },
                                        onClick = {
                                            onTipoTrabajoChange(tipoIndex, tipo)
                                            dropdownExpandido = false
                                        }
                                    )
                                }
                            }
                        }

                        // Campos dinámicos según el tipo seleccionado
                        val campos = obtenerCamposPorTipo(tipoTrabajoDetalle.tipo)
                        campos.forEach { campo ->
                            OutlinedTextField(
                                value = tipoTrabajoDetalle.valoresCampos[campo] ?: "",
                                onValueChange = {
                                    // Deja pasar solo números y un separador decimal
                                    onCampoChange(tipoIndex, campo, MedidasUtils.filtrarDecimal(it))
                                },
                                placeholder = { Text(campo, color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(4.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Gray,
                                    focusedBorderColor = Color.Black
                                )
                            )
                        }

                        // Botón "+" solo debajo del último tipo de trabajo
                        if (tipoIndex == trabajo.tiposTrabajo.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(4.dp, RoundedCornerShape(24.dp))
                                    .background(Color.White, RoundedCornerShape(24.dp))
                                    .clickable { onAgregarTipoTrabajo() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir tipo de trabajo",
                                    modifier = Modifier.size(28.dp),
                                    tint = morado
                                )
                            }
                        }

                        // Espacio entre tipos de trabajo
                        if (tipoIndex < trabajo.tiposTrabajo.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Espacio al final de la sección
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

fun obtenerCamposPorTipo(tipoTrabajo: String): List<String> {
    return when (tipoTrabajo.lowercase()) {
        "pantalon" -> listOf( "Alto","Cintura", "Cadera", "Tiro")
        "polera" -> listOf("Espalda", "ancho", "Alto", "cisa", "alto_talle", "largo_manga")
        "birrete+estola" -> listOf("Birrete", "Estola")
        else -> emptyList()
    }
}
