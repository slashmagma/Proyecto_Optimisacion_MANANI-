package com.Fenix.manami

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Logo de Fenix.
 *
 * Lo principal sale de Supabase Storage (guardado "en la base de datos"),
 * así que se ve igual en todos los dispositivos y se puede cambiar desde el
 * servidor sin actualizar la app.
 *
 * Mientras se descarga —o si algún día no hubiera internet— se muestra la
 * copia que viene dentro de la app (R.drawable.imagen_fenix), así el logo
 * nunca aparece vacío.
 *
 * Clave en Storage: materiales/logo_fenix.png
 * URL pública: https://<proyecto>.supabase.co/storage/v1/object/public/materiales/logo_fenix.png
 */
object LogoFenixDatos {
    const val URL =
        "https://nzbmwrvvqvxkqumbfzvh.supabase.co/storage/v1/object/public/materiales/logo_fenix.png"
    const val CLAVE_STORAGE = "materiales/logo_fenix.png"
}

@Composable
fun LogoFenix(
    modifier: Modifier = Modifier,
    tamano: Dp = 40.dp
) {
    val respaldo = painterResource(R.drawable.imagen_fenix)

    AsyncImage(
        model = LogoFenixDatos.URL,
        contentDescription = "Logo Fenix",
        placeholder = respaldo,
        error = respaldo,
        fallback = respaldo,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(tamano)
    )
}

/** El mismo logo, recortado en círculo (para el header y el login). */
@Composable
fun LogoFenixCircular(
    modifier: Modifier = Modifier,
    tamano: Dp = 40.dp
) {
    LogoFenix(
        modifier = modifier.clip(androidx.compose.foundation.shape.CircleShape),
        tamano = tamano
    )
}
