package com.Fenix.manami.inventario.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Muestra la imagen de un material.
 *
 * - Si es una URL de Supabase (https://...) la carga con Coil.
 * - Si es un enlace viejo de la galería (content://...) la carga desde el dispositivo.
 * - Si no hay imagen o no se puede cargar, muestra el placeholder 📷.
 */
@Composable
fun ImagenMaterial(
    url: String,
    modifier: Modifier = Modifier
) {
    val placeholderMod = modifier
        .background(Color(0xFFF5F5F5))

    if (url.isBlank()) {
        PlaceholderImagen(modifier = placeholderMod)
        return
    }

    val esUrlNube = url.startsWith("http://") || url.startsWith("https://")

    if (esUrlNube) {
        AsyncImage(
            model = url,
            contentDescription = "Imagen del material",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // Enlace local de la galería (materiales guardados antes de subir a la nube)
        val contexto = LocalContext.current

        val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, url) {
            value = withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(url)
                    contexto.contentResolver
                        .openInputStream(uri)
                        ?.use { BitmapFactory.decodeStream(it) }
                } catch (e: Exception) {
                    null
                }
            }
        }

        val resultado = bitmap
        if (resultado != null) {
            Image(
                bitmap = resultado.asImageBitmap(),
                contentDescription = "Imagen del material",
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        } else {
            PlaceholderImagen(modifier = placeholderMod)
        }
    }
}

/** Recuadro gris con 📷 cuando no hay imagen. */
@Composable
private fun PlaceholderImagen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "📷", fontSize = 32.sp)
    }
}

/** Carga una imagen local (content://) como bitmap. Usada por las pantallas. */
@Composable
fun loadBitmapFromUri(uriString: String): android.graphics.Bitmap? {
    val context = LocalContext.current
    return remember(uriString) {
        try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            null
        }
    }
}
