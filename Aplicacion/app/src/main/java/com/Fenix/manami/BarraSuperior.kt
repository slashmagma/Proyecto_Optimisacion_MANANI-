package com.Fenix.manami

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(titulo: String = "Manami",CerrarSesion: () -> Unit) {
    TopAppBar(
        title = { Text(titulo) },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        actions = {

            TextButton(onClick = CerrarSesion) {
                Text("Cerrar Sesión")
            }
        }
    )
}


