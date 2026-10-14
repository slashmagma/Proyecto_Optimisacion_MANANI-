package com.Fenix.manami

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.Fenix.manami.HeaderFenix
import kotlinx.coroutines.launch

@Composable
fun Chatbot(
    onVolver: () -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    vistaModelo: ChatbotVistaModelo = viewModel()
) {
    val morado = Color(0xFF901ACC)
    val uiState by vistaModelo.uiState.collectAsState()
    val listaState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll al último mensaje
    LaunchedEffect(uiState.mensajes.size) {
        if (uiState.mensajes.isNotEmpty()) {
            listaState.animateScrollToItem(uiState.mensajes.size - 1)
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
                text = "Asistente",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        // Lista de mensajes
        LazyColumn(
            state = listaState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(uiState.mensajes) { mensaje ->
                BurbujaMensaje(mensaje = mensaje, morado = morado)
            }

            if (uiState.cargando) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = morado,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }

        // Campo de entrada
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = uiState.input,
                onValueChange = { vistaModelo.onInputChange(it) },
                placeholder = { Text("Escribe tu pregunta...", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = morado,
                    unfocusedBorderColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { vistaModelo.enviarMensaje() },
                enabled = uiState.input.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (uiState.input.isNotBlank()) morado else Color.Gray
                )
            }
        }
    }
}

@Composable
fun BurbujaMensaje(mensaje: MensajeChat, morado: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mensaje.esUsuario) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (mensaje.esUsuario) morado else Color(0xFFF0E6F6),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text(
                text = mensaje.texto,
                fontSize = 14.sp,
                color = if (mensaje.esUsuario) Color.White else Color.Black,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
