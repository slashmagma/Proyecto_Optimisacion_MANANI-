package com.Fenix.manami.inventario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.inventario.data.ModeloInventario
import com.Fenix.manami.HeaderFenix

@Composable
fun VistaInventario(
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.listaInventario) { material ->
                                TarjetaInventario(
                                    material = material,
                                    onClick = {
                                        material.id?.let { id ->
                                            onMaterialClick(id)
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
                        Text(
                            text = state.mensaje,
                            color = MaterialTheme.colorScheme.error
                        )
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
    }
}

@Composable
fun TarjetaInventario(
    material: ModeloInventario,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color(0xFF901ACC),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = material.nombreMaterial,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (!material.tipoM.isNullOrBlank()) {
                Text(
                    text = "Tipo: ${material.tipoM}",
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )
            }
            Text(
                text = "Cantidad: ${material.cantidadM ?: 0} ${material.unidadMedida ?: ""}",
                fontSize = 14.sp,
                color = Color(0xFF555555)
            )
            if (material.costoM != null) {
                Text(
                    text = "Costo: $${material.costoM}",
                    fontSize = 14.sp,
                    color = Color(0xFF555555)
                )
            }
        }
    }
}
