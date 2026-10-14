package com.Fenix.manami.inventario.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.HeaderFenix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearMaterial(
    onVolver: () -> Unit = {},
    onGuardar: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    viewModel: CrearMaterialVistaModelo = viewModel()
) {
    val morado = Color(0xFF901ACC)
    val state by viewModel.uiState.collectAsState()

    // Dropdown expandido
    var unidadExpandida by remember { mutableStateOf(false) }
    val opcionesUnidad = listOf("metros", "unidades", "centímetros")

    // Launcher para seleccionar imagen de la galería
    val context = LocalContext.current
    val imagenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImagenChange(it) }
    }

    // Navegar hacia atrás cuando se guarda exitosamente
    LaunchedEffect(state.guardadoExitoso) {
        if (state.guardadoExitoso) {
            onGuardar()
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
                text = "Nuevo material",
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
            // Nombre material
            OutlinedTextField(
                value = state.nombreMaterial,
                onValueChange = { viewModel.onNombreMaterialChange(it) },
                placeholder = { Text("Nombre material", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )

            // Tipo
            OutlinedTextField(
                value = state.tipoM,
                onValueChange = { viewModel.onTipoChange(it) },
                placeholder = { Text("tipo", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )

            // Cantidad y Unidad de medida (en una fila)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.cantidadM,
                    onValueChange = { viewModel.onCantidadChange(it) },
                    placeholder = { Text("cantidad", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Gray,
                        focusedBorderColor = Color.Black
                    )
                )

                // Dropdown de unidad de medida
                ExposedDropdownMenuBox(
                    expanded = unidadExpandida,
                    onExpandedChange = { unidadExpandida = !unidadExpandida },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = state.unidadMedida,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("unidad", color = Color.Gray) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Seleccionar unidad"
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
                        expanded = unidadExpandida,
                        onDismissRequest = { unidadExpandida = false }
                    ) {
                        opcionesUnidad.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    viewModel.onUnidadMedidaChange(opcion)
                                    unidadExpandida = false
                                }
                            )
                        }
                    }
                }
            }

            // Costo
            OutlinedTextField(
                value = state.costoM,
                onValueChange = { viewModel.onCostoChange(it) },
                placeholder = { Text("costo", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Black
                )
            )

            // Imagen del material - Botón para seleccionar de galería
            Text(
                text = "Imagen del material",
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )

            if (state.imagenUri != null) {
                // Mostrar imagen seleccionada (sin Coil)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Imagen seleccionada",
                        color = Color(0xFF555555),
                        fontSize = 14.sp
                    )
                }
            }

            // Botón para seleccionar imagen
            OutlinedButton(
                onClick = {
                    imagenLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (state.imagenUri != null) "Cambiar imagen" else "Seleccionar imagen de galería",
                    color = morado
                )
            }

            // Mostrar error si existe
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Guardar material
            Button(
                onClick = { viewModel.guardarMaterial(context) },
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
