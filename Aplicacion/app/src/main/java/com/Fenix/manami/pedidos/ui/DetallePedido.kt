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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.HeaderFenix
import com.Fenix.manami.pedidos.data.DetallePedidoCompleto
import com.Fenix.manami.pedidos.data.MaterialPedidoEnMemoria

@Composable
fun DetallePedido(
    pedidoId: Int,
    onVolver: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onAgregarTrabajo: (Int) -> Unit = {},
    vistaModelo: DetallePedidoVistaModelo = viewModel()
) {
    val morado = Color(0xFF901ACC)
    val uiState by vistaModelo.uiState.collectAsState()

    LaunchedEffect(pedidoId) {
        vistaModelo.cargarDetalle(pedidoId)
    }

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

        // BotÃ³n volver + tÃ­tulo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = morado
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Detalle del pedido",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        // Contenido
        when {
            uiState.cargando -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { vistaModelo.cargarDetalle(pedidoId) }) {
                        Text("Reintentar")
                    }
                }
            }
            else -> {
                val pedido = uiState.pedido
                // Agrupar los trabajos por nombre completo
                val grupos = uiState.detalles.groupBy { it.nombreCompleto ?: "â€”" }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // ===== InformaciÃ³n del pedido (siempre visible arriba) =====
                    Text(
                        text = "Estado del pedido : ${pedido?.estado ?: "â€”"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = morado
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Costo: editable en modo ediciÃ³n, texto normal en modo vista
                    if (uiState.modoEdicion) {
                        OutlinedTextField(
                            value = uiState.costoTexto,
                            onValueChange = { vistaModelo.onCostoChange(it) },
                            label = { Text("costo", color = morado) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = morado,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                    } else {
                        Text(
                            text = "costo : $${pedido?.monto ?: 0}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = morado
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "nombre del cliente : ${pedido?.nombreCliente ?: "â€”"}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = morado
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "fecha : ${formatearFecha(pedido?.fecha)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = morado
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ===== Trabajos agrupados por nombre (comprimibles) =====
                    if (grupos.isEmpty()) {
                        Text(
                            text = "Este pedido no tiene trabajos registrados",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    grupos.forEach { (nombre, detallesGrupo) ->
                        GrupoTrabajos(
                            nombre = nombre,
                            detalles = detallesGrupo,
                            modoEdicion = uiState.modoEdicion,
                            onEliminarTrabajo = { idDetalle ->
                                vistaModelo.eliminarTrabajo(idDetalle, pedidoId)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // BotÃ³n aÃ±adir trabajo (solo en modo ediciÃ³n)
                    if (uiState.modoEdicion) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onAgregarTrabajo(pedidoId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = morado)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "AÃ±adir trabajo",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("aÃ±adir trabajo", color = morado)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ===== Materiales que usa el pedido =====
                    SeccionMaterialesPedido(
                        uiState = uiState,
                        onAgregar = { vistaModelo.agregarMaterial() },
                        onEliminar = { indice -> vistaModelo.eliminarMaterial(indice) },
                        onSeleccionar = { indice, material ->
                            vistaModelo.seleccionarMaterial(indice, material)
                        },
                        onCantidadChange = { indice, valor ->
                            vistaModelo.onCantidadMaterialChange(indice, valor)
                        },
                        onGuardar = { vistaModelo.guardarMateriales(pedidoId) }
                    )

                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
        } // fin Column contenido

        // ===== BotÃ³n flotante inferior derecha para actualizar =====
        FloatingActionButton(
            onClick = {
                if (uiState.modoEdicion) {
                    vistaModelo.guardarCosto(pedidoId)
                } else {
                    vistaModelo.alternarModoEdicion()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp),
            containerColor = morado,
            contentColor = Color.White
        ) {
            when {
                uiState.guardando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                }
                uiState.modoEdicion -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Guardar cambios"
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Actualizar datos"
                    )
                }
            }
        }
    } // fin Box
}

@Composable
fun GrupoTrabajos(
    nombre: String,
    detalles: List<DetallePedidoCompleto>,
    modoEdicion: Boolean = false,
    onEliminarTrabajo: (Int?) -> Unit = {}
) {
    val morado = Color(0xFF901ACC)
    var expandido by remember(modoEdicion) { mutableStateOf(modoEdicion) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header con el nombre (comprimible)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandido = !expandido }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = morado,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 40.dp)
                )
                Icon(
                    imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expandido) "Comprimir" else "Descomprimir",
                    tint = morado,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
            // LÃ­nea morada bajo el nombre
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(morado)
            )
        }

        // Contenido expandible
        AnimatedVisibility(
            visible = expandido,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                detalles.forEach { detalle ->
                    TrabajoDetalleCard(
                        detalle = detalle,
                        modoEdicion = modoEdicion,
                        onEliminar = { onEliminarTrabajo(detalle.idDetalle) }
                    )
                }
            }
        }
    }
}

@Composable
fun TrabajoDetalleCard(
    detalle: DetallePedidoCompleto,
    modoEdicion: Boolean = false,
    onEliminar: () -> Unit = {}
) {
    val morado = Color(0xFF901ACC)
    val medidas = obtenerMedidasFormateadas(detalle)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Tipo de pedido alineado a la derecha con triÃ¡ngulo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = detalle.tipoPedido ?: "â€”",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = morado
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = morado
            )
            // BotÃ³n borrar trabajo (solo en modo ediciÃ³n)
            if (modoEdicion) {
                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar trabajo",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        // LÃ­nea bajo el tipo de pedido
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(morado)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Medidas en dos columnas
        if (medidas.isEmpty()) {
            Text(
                text = "Sin medidas registradas",
                fontSize = 14.sp,
                color = Color.Gray
            )
        } else {
            val filas = medidas.chunked(2)
            filas.forEach { fila ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    fila.forEachIndexed { i, (etiqueta, valor) ->
                        Text(
                            text = "$etiqueta : $valor cm",
                            fontSize = 15.sp,
                            color = morado,
                            modifier = Modifier.weight(1f)
                        )
                        if (i == 0 && fila.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// Formatea la fecha yyyy-MM-dd a dd/MM/yyyy
fun formatearFecha(fecha: String?): String {
    if (fecha.isNullOrBlank()) return "â€”"
    return try {
        val partes = fecha.split("T")[0].split("-")
        if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else fecha
    } catch (e: Exception) {
        fecha
    }
}

// Formatea la fecha a dd/MM (sin aÃ±o) para la esquina de la card
fun formatearFechaCorta(fecha: String?): String {
    if (fecha.isNullOrBlank()) return ""
    return try {
        val partes = fecha.split("T")[0].split("-")
        if (partes.size == 3) "${partes[2]}/${partes[1]}" else ""
    } catch (e: Exception) {
        ""
    }
}

// Formatea las medidas del detalle segÃºn su tipo de pedido
fun obtenerMedidasFormateadas(detalle: DetallePedidoCompleto): List<Pair<String, String>> {
    fun fmt(v: Float?): String {
        if (v == null) return ""
        return if (v % 1f == 0f) v.toInt().toString() else v.toString()
    }

    return when (detalle.tipoPedido?.lowercase()) {
        "pantalon" -> {
            val p = detalle.pantalon ?: return emptyList()
            buildList {
                p.ancho?.let { add("Ancho" to fmt(it)) }
                p.alto?.let { add("Alto" to fmt(it)) }
                p.altoTalle?.let { add("Alto talle" to fmt(it)) }
                p.cintura?.let { add("Cintura" to fmt(it)) }
                p.cadera?.let { add("Cadera" to fmt(it)) }
                p.tiro?.let { add("Tiro" to fmt(it)) }
            }
        }
        "polera" -> {
            val p = detalle.polera ?: return emptyList()
            buildList {
                p.espalda?.let { add("Espalda" to fmt(it)) }
                p.ancho?.let { add("Ancho" to fmt(it)) }
                p.alto?.let { add("Alto" to fmt(it)) }
                p.cisa?.let { add("Cisa" to fmt(it)) }
                p.altoTalle?.let { add("Alto talle" to fmt(it)) }
                p.largoManga?.let { add("Largo manga" to fmt(it)) }
                p.cintura?.let { add("Cintura" to fmt(it)) }
                p.cadera?.let { add("Cadera" to fmt(it)) }
                p.tiro?.let { add("Tiro" to fmt(it)) }
            }
        }
        "birrete+estola" -> {
            val b = detalle.birreteEstola ?: return emptyList()
            buildList {
                b.birrete?.let { add("Birrete" to fmt(it)) }
                b.estola?.let { add("Estola" to fmt(it)) }
            }
        }
        else -> emptyList()
    }
}

// ===== Sección de materiales que usa el pedido =====

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionMaterialesPedido(
    uiState: DetallePedidoUiState,
    onAgregar: () -> Unit = {},
    onEliminar: (Int) -> Unit = {},
    onSeleccionar: (Int, com.Fenix.manami.inventario.data.ModeloInventario?) -> Unit = { _, _ -> },
    onCantidadChange: (Int, String) -> Unit = { _, _ -> },
    onGuardar: () -> Unit = {}
) {
    val morado = Color(0xFF901ACC)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Materiales del pedido",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = morado
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Al guardar se descuenta del inventario y se avisa por correo si queda poco stock.",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.materiales.isEmpty()) {
            Text(
                text = "Sin materiales cargados",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        uiState.materiales.forEachIndexed { index, material ->
            FilaMaterialPedido(
                index = index,
                material = material,
                inventario = uiState.materialesCargados,
                cargandoInventario = uiState.cargandoMateriales,
                onEliminar = { onEliminar(index) },
                onSeleccionar = { onSeleccionar(index, it) },
                onCantidadChange = { onCantidadChange(index, it) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Botón para agregar otra fila
        OutlinedButton(
            onClick = onAgregar,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = morado)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar material",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar material", color = morado)
        }

        // Aviso de lo que pasó al guardar
        uiState.avisoMateriales?.let { aviso ->
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (aviso.contains("falt")) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                )
            ) {
                Text(
                    text = aviso,
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = Color.Black
                )
            }
        }

        uiState.error?.let { error ->
            if (error.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }
        }

        if (uiState.materiales.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onGuardar,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.guardandoMateriales,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = morado)
            ) {
                if (uiState.guardandoMateriales) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar materiales", fontSize = 15.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilaMaterialPedido(
    index: Int,
    material: MaterialPedidoEnMemoria,
    inventario: List<com.Fenix.manami.inventario.data.ModeloInventario>,
    cargandoInventario: Boolean,
    onEliminar: () -> Unit,
    onSeleccionar: (com.Fenix.manami.inventario.data.ModeloInventario?) -> Unit,
    onCantidadChange: (String) -> Unit
) {
    val morado = Color(0xFF901ACC)
    var expandido by remember(index) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFDDDDDD), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown con los materiales del inventario
            ExposedDropdownMenuBox(
                expanded = expandido,
                onExpandedChange = { expandido = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = if (material.nombreMaterial.isNotBlank()) material.nombreMaterial
                    else "Elegir material",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Elegir material", color = Color.Gray, fontSize = 14.sp) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Gray,
                        focusedBorderColor = morado
                    )
                )

                ExposedDropdownMenu(
                    expanded = expandido,
                    onDismissRequest = { expandido = false }
                ) {
                    if (cargandoInventario) {
                        DropdownMenuItem(
                            text = { Text("Cargando...") },
                            onClick = { expandido = false }
                        )
                    } else {
                        inventario.forEach { mat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${mat.nombreMaterial}  " +
                                                "(${mat.cantidadM ?: 0} ${mat.unidadMedida ?: ""})"
                                    )
                                },
                                onClick = {
                                    onSeleccionar(mat)
                                    expandido = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón eliminar fila
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Quitar material",
                    tint = Color(0xFFD32F2F)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Cantidad + unidad de medida
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = material.cantidadTexto,
                onValueChange = onCantidadChange,
                placeholder = { Text("cantidad", color = Color.Gray, fontSize = 14.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = morado
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Unidad (solo lectura, viene del material elegido)
            Text(
                text = material.unidadMedida ?: "",
                fontSize = 14.sp,
                color = Color(0xFF555555),
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}
