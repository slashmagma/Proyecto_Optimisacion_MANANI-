package com.Fenix.manami.inventario.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.HeaderFenix
import com.Fenix.manami.inventario.data.ModeloInventario

@Composable
fun VistaMateriales(
    vistaModelo: inventarioVistaModelo = viewModel(),
    onAgregarMaterialClick: () -> Unit = {},
    onMaterialClick: (Int) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        vistaModelo.cargarInventario()
    }
    val uiState by vistaModelo.uiState.collectAsState()

    // Material que se quiere eliminar (muestra el diálogo de confirmación)
    var materialAEliminar by remember { mutableStateOf<ModeloInventario?>(null) }

    val morado = Color(0xFF901ACC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            HeaderFenix(
                titulo = "Fenix",
                onNavigate = onNavigate,
                onCerrarSesion = onCerrarSesion
            )

            // Título Inventario
            Text(
                text = "Inventario",
                color = morado,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            // Contenido
            when (val state = uiState) {
                is inventarioUiState.Cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is inventarioUiState.Exito -> {
                    if (state.listaInventario.isEmpty()) {
                        Text(
                            text = "No hay materiales registrados",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(state.listaInventario) { material ->
                                CardMaterial(
                                    material = material,
                                    onClick = {
                                        material.id?.let { id ->
                                            onMaterialClick(id)
                                        }
                                    },
                                    onDelete = { materialAEliminar = material },
                                    onRestar = {
                                        material.id?.let { id ->
                                            vistaModelo.ajustarCantidad(id, -1)
                                        }
                                    },
                                    onSumar = {
                                        material.id?.let { id ->
                                            vistaModelo.ajustarCantidad(id, +1)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                is inventarioUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.mensaje, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { vistaModelo.cargarInventario() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
        }

        // Botón flotante "+"
        FloatingActionButton(
            onClick = onAgregarMaterialClick,
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
                contentDescription = "Agregar material",
                modifier = Modifier.size(28.dp)
            )
        }

        // Confirmación antes de eliminar
        materialAEliminar?.let { material ->
            AlertDialog(
                onDismissRequest = { materialAEliminar = null },
                title = { Text("¿Eliminar material?") },
                text = {
                    Text("Se va a borrar \"${material.nombreMaterial}\" del inventario. " +
                            "Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            material.id?.let { vistaModelo.eliminarMaterial(it) }
                            materialAEliminar = null
                        }
                    ) {
                        Text("Eliminar", color = Color(0xFFD32F2F))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { materialAEliminar = null }) {
                        Text("Cancelar", color = morado)
                    }
                }
            )
        }
    }
}

@Composable
fun CardMaterial(
    material: ModeloInventario,
    onClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onRestar: () -> Unit = {},
    onSumar: () -> Unit = {}
) {
    val morado = Color(0xFF901ACC)
    val cantidad = material.cantidadM ?: 0
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color(0xFF901ACC),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen del material
            if (material.imagenM.isNotEmpty()) {
                ImagenMaterial(
                    url = material.imagenM,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            } else {
                // Placeholder si no hay imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📷",
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre
            Text(
                text = material.nombreMaterial,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            // Cantidad
            Text(
                text = "cantidad : $cantidad ${material.unidadMedida ?: ""}",
                fontSize = 11.sp,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botones - y + para quitar o poner de a 1 unidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BotonMasMenos(
                    texto = "−",
                    color = morado,
                    habilitado = cantidad > 0,
                    onClick = onRestar
                )

                Spacer(modifier = Modifier.width(24.dp))

                BotonMasMenos(
                    texto = "+",
                    color = morado,
                    onClick = onSumar
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Botón eliminar
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Eliminar",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Botón redondo con "−" o "+" para ajustar la cantidad de a 1 unidad.
 * El "−" queda deshabilitado cuando la cantidad llega a 0,
 * así nunca queda en negativo.
 */
@Composable
private fun BotonMasMenos(
    texto: String,
    color: Color,
    habilitado: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier.size(36.dp),
        contentPadding = PaddingValues(0.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, if (habilitado) color else Color(0xFFDDDDDD)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color,
            disabledContentColor = Color(0xFFBBBBBB)
        )
    ) {
        Text(
            text = texto,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp
        )
    }
}
