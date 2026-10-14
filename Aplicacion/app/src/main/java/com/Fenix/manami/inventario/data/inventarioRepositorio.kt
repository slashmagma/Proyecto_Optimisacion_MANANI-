package com.Fenix.manami.inventario.data

import com.Fenix.manami.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Solo la cantidad, para descontar material sin tocar el resto de la fila. */
@Serializable
data class InventarioCantidadDto(
    @SerialName("Cantidad_m")
    val cantidadM: Int?
)

class inventarioRepositorio {

    private val tabla = SupabaseClient.client.from("inventario")

    suspend fun obtenerInventario(): List<ModeloInventario> =
        withContext(Dispatchers.IO) {
            tabla
                .select()
                .decodeList<ModeloInventario>()
        }

    suspend fun obtenerMaterialPorId(id: Int): ModeloInventario? =
        withContext(Dispatchers.IO) {
            tabla
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<ModeloInventario>()
        }

    suspend fun insertarMaterial(material: InventarioInsertDto) =
        withContext(Dispatchers.IO) {
            tabla.insert(material)
        }

    suspend fun actualizarMaterial(
        id: Int,
        material: InventarioInsertDto
    ) =
        withContext(Dispatchers.IO) {
            tabla.update(material) {
                filter {
                    eq("id", id)
                }
            }
        }

    suspend fun eliminarMaterial(id: Int) =
        withContext(Dispatchers.IO) {
            tabla.delete {
                filter {
                    eq("id", id)
                }
            }
        }

    /** Actualiza solo la cantidad de un material (descuento de stock). */
    suspend fun actualizarCantidad(id: Int, nuevaCantidad: Int) =
        withContext(Dispatchers.IO) {
            tabla.update(InventarioCantidadDto(cantidadM = nuevaCantidad)) {
                filter {
                    eq("id", id)
                }
            }
            Unit
        }

    /**
     * Suma (delta positivo) o resta (delta negativo) material con los botones
     * + y -, y deja el movimiento anotado en el historial.
     *
     * En el historial la cantidad se guarda con signo: 1 = se agregó,
     * -1 = se quitó, así después se puede saber cuánto entró y cuánto salió.
     * Nunca deja el stock en negativo.
     *
     * Devuelve la cantidad que quedó guardada.
     */
    suspend fun ajustarCantidadConHistorial(
        material: ModeloInventario,
        delta: Int
    ): Int = withContext(Dispatchers.IO) {

        val stockNuevo = ((material.cantidadM ?: 0) + delta).coerceAtLeast(0)

        // 1. Actualiza el stock
        tabla.update(InventarioCantidadDto(cantidadM = stockNuevo)) {
            filter {
                eq("id", material.id ?: 0)
            }
        }

        // 2. Anota el movimiento en el historial
        tablaHistorial.insert(
            HistorialInsertDto(
                idMaterial = material.id,
                nombreMaterial = material.nombreMaterial,
                fecha = java.time.LocalDate.now().toString(),
                cantidad = delta,
                unidadMedida = material.unidadMedida,
                costo = null
            )
        )

        stockNuevo
    }

    // ===== Historial de compras de material =====

    private val tablaHistorial = SupabaseClient.client.from("historial_inventario")

    /** Agrega más material y lo registra en el historial. */
    suspend fun agregarMaterial(
        material: ModeloInventario,
        cantidadAAgregar: Int,
        costo: Long?
    ) = withContext(Dispatchers.IO) {

        val stockNuevo = (material.cantidadM ?: 0) + cantidadAAgregar

        // 1. Suma la cantidad al inventario
        tabla.update(InventarioCantidadDto(cantidadM = stockNuevo)) {
            filter {
                eq("id", material.id ?: 0)
            }
        }

        // 2. Guarda el registro en el historial
        tablaHistorial.insert(
            HistorialInsertDto(
                idMaterial = material.id,
                nombreMaterial = material.nombreMaterial,
                fecha = java.time.LocalDate.now().toString(),
                cantidad = cantidadAAgregar,
                unidadMedida = material.unidadMedida,
                costo = costo
            )
        )

        stockNuevo
    }

    /** Elimina TODOS los materiales (usado al importar una copia de seguridad). */
    suspend fun eliminarTodosLosMateriales() =
        withContext(Dispatchers.IO) {
            tabla.delete {
                filter {
                    gte("id", 0)
                }
            }
            Unit
        }

    suspend fun subirImagen(
        nombreArchivo: String,
        imagen: ByteArray
    ): String =
        withContext(Dispatchers.IO) {

            val bucket =
                SupabaseClient.client.storage.from("materiales")

            bucket.upload(
                path = nombreArchivo,
                data = imagen
            ) {
                upsert = true
            }

            bucket.publicUrl(nombreArchivo)
        }
}