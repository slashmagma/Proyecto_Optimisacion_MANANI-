package com.Fenix.manami

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Copia de seguridad automática.
 *
 * El trabajo lo ejecuta WorkManager, así que corre cada 24 horas aunque la
 * aplicación esté cerrada (no hace falta tenerla abierta ni en primer plano).
 * WorkManager persiste la orden, así que sobrevive a cerrar el celular y a
 * reiniciar el dispositivo.
 *
 * El estado (activado/desactivado) se persiste en SharedPreferences,
 * así que también sobrevive a cerrar y abrir la aplicación.
 */
object RespaldoProgramado {

    // ===== Intervalos =====
    /** Modo testeo: cada 15 minutos (mínimo que permite Android). */
    const val INTERVALO_PRUEBA_MS = 15L * 60L * 1000L
    /** Producción: cada 24 horas. */
    const val INTERVALO_REAL_MS = 24L * 60L * 60L * 1000L

    // >>> Para probar más seguido, cambiar esta línea a INTERVALO_PRUEBA_MS <<<
    const val INTERVALO_ACTUAL_MS = INTERVALO_REAL_MS

    /** true mientras el intervalo esté en modo testeo (15 min). */
    val enModoPrueba: Boolean get() = INTERVALO_ACTUAL_MS < INTERVALO_REAL_MS

    /** Texto del intervalo para mostrar en la interfaz. */
    val textoIntervalo: String
        get() = if (enModoPrueba) "cada 15 minutos (modo prueba)"
        else "cada 24 horas"

    // ===== Preferencias =====
    private const val PREFS = "copia_seguridad"
    private const val CLAVE_ACTIVO = "guardado_automatico"

    /** Nombre único del trabajo en WorkManager. */
    private const val TRABAJO = "respaldo_fenix_automatico"

    // ===== Estado observable por la interfaz =====
    private val _activo = MutableStateFlow(false)
    val activo: StateFlow<Boolean> = _activo.asStateFlow()

    private val _ultimoResultado = MutableStateFlow<String?>(null)
    val ultimoResultado: StateFlow<String?> = _ultimoResultado.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Llamar al iniciar la app (MainActivity.onCreate).
     * Restaura la preferencia y vuelve a programar el trabajo si estaba activado.
     */
    fun iniciar(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val activado = prefs.getBoolean(CLAVE_ACTIVO, false)
        _activo.value = activado
        if (activado) programar(context.applicationContext)
    }

    /** Activa o desactiva el guardado automático (y lo persiste). */
    fun alternar(context: Context, activar: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(CLAVE_ACTIVO, activar).apply()
        _activo.value = activar

        val app = context.applicationContext
        if (activar) {
            programar(app)
        } else {
            cancelar(app)
        }
    }

    /**
     * Genera y descarga la copia ahora mismo (usado por el botón manual
     * y por el trabajo programado).
     */
    suspend fun respaldarAhora(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            val contenido = GeneradorRespaldo.generar()
            val ruta = guardarEnDispositivo(context.applicationContext, contenido)
            _ultimoResultado.value = "${fechaHora()}  •  $ruta"
            _error.value = null
            Result.success(ruta)
        } catch (e: Exception) {
            _error.value = e.localizedMessage ?: "Error al generar la copia"
            Result.failure(e)
        }
    }

    // ===== Programación (WorkManager) =====

    /**
     * Registra el trabajo periódico en WorkManager.
     * Se ejecuta cada INTERVALO_ACTUAL_MS aunque la app esté cerrada.
     */
    private fun programar(context: Context) {
        val red = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // WorkManager espera entre ejecuciones; el margen es un cuarto del intervalo.
        val margen = (INTERVALO_ACTUAL_MS / 4).coerceIn(60_000L, INTERVALO_ACTUAL_MS)

        val peticion = PeriodicWorkRequestBuilder<RespaldoWorker>(
            INTERVALO_ACTUAL_MS, TimeUnit.MILLISECONDS,
            margen, TimeUnit.MILLISECONDS
        )
            .setConstraints(red)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TRABAJO,
            // UPDATE: si cambia el intervalo se actualiza el trabajo ya agendado
            ExistingPeriodicWorkPolicy.UPDATE,
            peticion
        )
    }

    private fun cancelar(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(TRABAJO)
    }

    /**
     * Guarda el JSON como archivo .json.
     * Android 10+ (API 29): carpeta Download/Fenix vía MediaStore, sin permisos.
     * Antes de Android 10: carpeta pública Download/Fenix si hay permiso,
     * si no, carpeta privada de la app (Download/Fenix dentro de ella).
     */
    private fun guardarEnDispositivo(context: Context, contenido: String): String {
        val nombre = "respaldo_fenix_${archivoHora()}.json"
        val datos = contenido.toByteArray(Charsets.UTF_8)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nombre)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Fenix")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            ) ?: throw IllegalStateException("No se pudo crear el archivo")

            context.contentResolver.openOutputStream(uri)?.use { it.write(datos) }
                ?: throw IllegalStateException("No se pudo escribir el archivo")

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)

            "Download/Fenix/$nombre"
        } else {
            val conPermiso = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            val carpeta = if (conPermiso) {
                File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    ), "Fenix"
                )
            } else {
                File(context.getExternalFilesDir(null), "Download/Fenix")
            }

            carpeta.mkdirs()
            val archivo = File(carpeta, nombre)
            archivo.writeText(contenido, Charsets.UTF_8)
            "${carpeta.name}/$nombre"
        }
    }

    private fun fechaHora(): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

    private fun archivoHora(): String =
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
}
