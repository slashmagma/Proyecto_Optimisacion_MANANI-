package com.Fenix.manami.inventario.ui

import androidx.compose.foundation.Image
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.HeaderFenix

/**
 * Agregar material a un material ya existente.
 *
 * El nombre, el tipo, la imagen y la unidad de medida quedan bloqueados
 * (no se pueden cambiar); solo se puede sumar cantidad y poner el costo.
 * Cada suma queda guardada en el historial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarMaterial(
    materialId: Int,
    onVolver: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    viewModel: AgregarMaterialVistaModelo = viewModel()
) {
    val morado = Color(0xFF901ACC)
    val state by viewModel.uiState.collectAsState()

    // Carga el material la primera vez que se entra
    LaunchedEffect(materialId) {
        viewModel.cargarMaterial(materialId)
    }

    // Vuelve al inventario cuando se guardó
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            onVolver()
        }
    }

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
                    .clickable(onClick = onVolver),
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

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Agregar material",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        when {
            state.cargando && state.material == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            state.material == null -> {
                Text(
                    text = state.error ?: "No se encontró el material",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                val material = state.material!!
                val unidad = material.unidadMedida ?: "unidades"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ===== Nombre (bloqueado) =====
                    CampoBloqueado(
                        etiqueta = "Nombre material",
                        valor = material.nombreMaterial
                    )

                    // ===== Tipo (bloqueado) =====
                    CampoBloqueado(
                        etiqueta = "tipo",
                        valor = material.tipoM ?: "-"
                    )

                    // ===== Cantidad que ya tiene + unidad (bloqueada) =====
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CampoBloqueado(
                            etiqueta = "Stock actual",
                            valor = "${material.cantidadM ?: 0}",
                            modifier = Modifier.weight(1f)
                        )

                        CampoBloqueado(
                            etiqueta = "unidad",
                            valor = unidad,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // ===== Cantidad a agregar (editable) =====
                    OutlinedTextField(
                        value = state.cantidadAAgregar,
                        onValueChange = { viewModel.onCantidadAgregarChange(it) },
                        placeholder = { Text("cantidad a agregar", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black
                        )
                    )

                    // ===== Costo (editable) =====
                    OutlinedTextField(
                        value = state.costo,
                        onValueChange = { viewModel.onCostoChange(it) },
                        placeholder = { Text("costo", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = Color.Black
                        )
                    )

                    // ===== Imagen (solo lectura) =====
                    Text(
                        text = "Imagen del material",
                        fontSize = 14.sp,
                        color = Color(0xFF555555)
                    )

                    ImagenMaterial(
                        url = material.imagenM,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    )

                    // Aviso de lo que va a pasar
                    val cantidad = state.cantidadAAgregar.toIntOrNull() ?: 0
                    if (cantidad > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                        ) {
                            Text(
                                text = "Al guardar, ${material.nombreMaterial} quedará con " +
                                        "${(material.cantidadM ?: 0) + cantidad} $unidad. " +
                                        "El registro queda en el historial.",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }
                    }

                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ===== Botón guardar =====
                    Button(
                        onClick = { viewModel.agregarMaterial() },
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
                                text = "Guardar material",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/** Campo que solo muestra el dato, sin poder editarlo. */
@Composable
private fun CampoBloqueado(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = {},
        enabled = false,
        label = { Text(etiqueta, fontSize = 13.sp) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledBorderColor = Color.Gray,
            disabledLabelColor = Color.Gray,
            disabledContainerColor = Color.White
        )
    )
}
