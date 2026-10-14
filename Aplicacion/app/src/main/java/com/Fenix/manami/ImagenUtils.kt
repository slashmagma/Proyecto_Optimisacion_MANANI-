package com.Fenix.manami

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream

/**
 * Convierte una imagen elegida en la galería en bytes listos para subir
 * a Supabase Storage.
 *
 * La imagen se guarda en JPEG y reducida, para que ocupe poco y el
 * inventario cargue rápido.
 */
object ImagenUtils {

    /** Lado más largo máximo de la imagen subida (en píxeles). */
    private const val LADO_MAXIMO = 1280

    /** Calidad del JPEG (0 a 100). */
    private const val CALIDAD = 85

    /** Lee la imagen de la galería y la devuelve como bytes JPEG. */
    fun uriABytes(context: Context, uri: Uri): ByteArray? {
        return try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.setTargetSize(
                        escalar(info.size.width, LADO_MAXIMO),
                        escalar(info.size.height, LADO_MAXIMO)
                    )
                }
            } else {
                val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, medidas)
                }

                val opciones = BitmapFactory.Options().apply {
                    inSampleSize = factorMuestreo(medidas.outWidth, medidas.outHeight, LADO_MAXIMO)
                }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opciones)
                }
            } ?: return null

            val salida = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, CALIDAD, salida)

            // Libera memoria si el bitmap ya no es el mismo objeto
            if (!bitmap.isRecycled) bitmap.recycle()

            salida.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    private fun escalar(lado: Int, maximo: Int): Int =
        if (lado <= maximo) lado else maximo

    /** Reduce la imagen dividiéndola por 2 hasta que entre en el tamaño máximo. */
    private fun factorMuestreo(ancho: Int, alto: Int, maximo: Int): Int {
        var factor = 1
        while (ancho / (factor * 2) >= maximo || alto / (factor * 2) >= maximo) {
            factor *= 2
        }
        return factor
    }
}
