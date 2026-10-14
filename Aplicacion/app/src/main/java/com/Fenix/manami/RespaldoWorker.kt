package com.Fenix.manami

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Trabajo periódico que genera la copia de seguridad.
 *
 * Lo programa WorkManager, así que se ejecuta cada 24 horas aunque la
 * aplicación esté cerrada (incluso si el usuario nunca la vuelve a abrir).
 *
 * Si falla (por ejemplo, sin internet) se pide reintentar; WorkManager lo
 * reintenta con espera progresiva hasta el próximo ciclo.
 */
class RespaldoWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val resultado = RespaldoProgramado.respaldarAhora(applicationContext)
        return if (resultado.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
