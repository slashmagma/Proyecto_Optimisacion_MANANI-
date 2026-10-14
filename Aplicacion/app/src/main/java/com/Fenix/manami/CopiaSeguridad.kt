package com.Fenix.manami

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.HeaderFenix
import com.Fenix.manami.api.CorreoApi

@Composable
fun CopiaSeguridad(
    onVolver: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    vistaModelo: CopiaSeguridadVistaModelo = viewModel()
) {
    val morado = Color(0xFF901ACC)
    val context = LocalContext.current
    val uiState by vistaModelo.uiState.collectAsState()
    var correoDestino by remember { mutableStateOf("") }

    // Estado del guardado automático (singleton que sobrevive a la pantalla)
    val autoActivo by RespaldoProgramado.activo.collectAsState()
    val ultimoRespaldo by RespaldoProgramado.ultimoResultado.collectAsState()
    val errorAuto by RespaldoProgramado.error.collectAsState()

    // Contenido del archivo elegido y control del diálogo de confirmación
    var contenidoSeleccionado by remember { mutableStateOf<String?>(null) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    // Selector de archivos del dispositivo (abrir copia de seguridad .json)
    val selectorArchivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val texto = try {
                context.contentResolver.openInputStream(uri)
                    ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            } catch (e: Exception) {
                null
            }
            if (texto.isNullOrBlank()) {
                // No se pudo leer: error sin borrar nada
                vistaModelo.importar("")
            } else {
                // Guarda el contenido y pide confirmación antes de borrar datos
                contenidoSeleccionado = texto
                mostrarConfirmacion = true
            }
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
            IconButton(onClick = onVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = morado
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Copia de seguridad",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== GUARDADO AUTOMÁTICO =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, morado, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Guardado automático",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = morado
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Descarga la copia de seguridad en la carpeta " +
                                "Download/Fenix ${RespaldoProgramado.textoIntervalo}, " +
                                "incluso con la aplicación cerrada.",
                        fontSize = 13.sp,
                        color = Color(0xFF555555)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Interruptor activar / desactivar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = autoActivo,
                            onCheckedChange = {
                                RespaldoProgramado.alternar(context, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = morado
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (autoActivo) "Guardado automático activado"
                            else "Guardado automático desactivado",
                            fontSize = 13.sp,
                            color = if (autoActivo) morado else Color(0xFF555555)
                        )
                    }

                    if (autoActivo && RespaldoProgramado.enModoPrueba) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠ Modo prueba: se descargará cada 15 minutos. " +
                                    "Para producción cambiar INTERVALO_ACTUAL_MS por INTERVALO_REAL_MS.",
                            fontSize = 11.sp,
                            color = Color(0xFFB26A00)
                        )
                    }

                    // Último respaldo realizado (automático o manual)
                    ultimoRespaldo?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Último respaldo: $it",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    errorAuto?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Error: $it",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ===== Botón: descargar al instante =====
                    Button(
                        onClick = { vistaModelo.descargarAhora(context) },
                        enabled = !uiState.descargando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = morado)
                    ) {
                        if (uiState.descargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Descargar ahora", color = Color.White)
                        }
                    }

                    uiState.descargaRealizada?.let { ruta ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Archivo descargado: $ruta",
                            fontSize = 12.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // ===== EXPORTAR =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, morado, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Exportar datos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = morado
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Guarda todos tus pedidos, trabajos y materiales en un archivo.",
                        fontSize = 13.sp,
                        color = Color(0xFF555555)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { vistaModelo.exportar() },
                        enabled = !uiState.cargando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = morado)
                    ) {
                        if (uiState.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Generar copia de seguridad", color = Color.White)
                        }
                    }
                }
            }

            // ===== IMPORTAR =====
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, morado, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Importar datos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = morado
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Restaura un archivo de copia de seguridad (.json). " +
                                "Reemplaza TODOS los datos actuales por los del archivo.",
                        fontSize = 13.sp,
                        color = Color(0xFF555555)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            selectorArchivo.launch(
                                arrayOf("application/json", "text/plain", "*/*")
                            )
                        },
                        enabled = !uiState.cargando,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, morado)
                    ) {
                        if (uiState.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = morado,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Seleccionar archivo y importar", color = morado)
                        }
                    }
                }
            }

            // ===== RESULTADO / MENSAJES =====
            uiState.jsonExportado?.let { json ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Copia generada correctamente",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = morado
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = json.take(500) + if (json.length > 500) "..." else "",
                            fontSize = 10.sp,
                            color = Color(0xFF555555),
                            maxLines = 12
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { compartirTexto(context, json) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = morado)
                            ) {
                                Text("Compartir archivo", color = Color.White, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { vistaModelo.limpiarResultados() },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Cerrar", color = morado, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFDDDDDD))
                        Spacer(modifier = Modifier.height(12.dp))

                        // ===== Enviar copia por correo (Brevo / Resend) =====
                        Text(
                            text = "Enviar copia por correo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = morado
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (!CorreoApi.estaConfigurada()) {
                            Text(
                                text = "Falta la clave de correo " +
                                        "(BREVO_API_KEY o RESEND_API_KEY en local.properties).",
                                fontSize = 11.sp,
                                color = Color(0xFFD32F2F)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        } else {
                            Text(
                                text = "El JSON completo llega adjunto al correo " +
                                        "(vía ${CorreoApi.proveedor}).",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = correoDestino,
                            onValueChange = { correoDestino = it },
                            placeholder = { Text("correo@ejemplo.com", color = Color.Gray, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 13.sp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = morado,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { vistaModelo.enviarCopiaPorCorreo(correoDestino) },
                            enabled = !uiState.enviandoCorreo && correoDestino.isNotBlank() && CorreoApi.estaConfigurada(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = morado)
                        ) {
                            if (uiState.enviandoCorreo) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Enviar correo", color = Color.White, fontSize = 13.sp)
                            }
                        }

                        if (uiState.correoEnviado) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "¡Correo enviado! Revisa tu bandeja (y spam).",
                                fontSize = 12.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }
            }

            uiState.resultadoImport?.let { resultado ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = resultado,
                            fontSize = 13.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = error,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // ===== Diálogo: confirmar reemplazo de datos =====
        if (mostrarConfirmacion) {
            AlertDialog(
                onDismissRequest = {
                    mostrarConfirmacion = false
                    contenidoSeleccionado = null
                },
                containerColor = Color.White,
                title = {
                    Text(
                        text = "¿Reemplazar todos los datos?",
                        color = morado,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Esta acción eliminará TODOS los pedidos, trabajos y " +
                                "materiales actuales y los reemplazará por los del archivo " +
                                "de copia de seguridad.\n\n" +
                                "• Los datos nuevos que no estén en el archivo se borrarán\n" +
                                "• Los datos eliminados volverán a aparecer\n\n" +
                                "Esta acción no se puede deshacer.",
                        fontSize = 13.sp,
                        color = Color(0xFF333333)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarConfirmacion = false
                            contenidoSeleccionado?.let { vistaModelo.importar(it) }
                            contenidoSeleccionado = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Sí, reemplazar", color = Color.White, fontSize = 13.sp)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            mostrarConfirmacion = false
                            contenidoSeleccionado = null
                        }
                    ) {
                        Text("Cancelar", color = morado, fontSize = 13.sp)
                    }
                }
            )
        }
    }
}

// Compartir el JSON como texto (el usuario puede guardarlo donde quiera)
fun compartirTexto(context: Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Guardar copia de seguridad"))
}
