package com.Fenix.manami

/**
 * Utilidades para los campos de medida.
 *
 * En español el teclado numérico escribe coma decimal ("30,5"), pero Kotlin
 * solo entiende punto ("30.5"). Acá se aceptan los dos, y también se filtra
 * lo que se tipea para que solo queden números y un separador.
 */
object MedidasUtils {

    /**
     * Convierte un texto escrito por el usuario a número decimal.
     * Acepta coma ("30,5") y punto ("30.5"). Devuelve null si no es un número.
     */
    fun parseDecimal(texto: String?): Float? {
        if (texto.isNullOrBlank()) return null
        return texto.trim().replace(',', '.').toFloatOrNull()
    }

    /**
     * Filtra lo que se escribe en un campo de medida:
     * deja pasar solo dígitos y un solo separador (coma o punto).
     */
    fun filtrarDecimal(entrada: String): String {
        var separadorVisto = false
        val resultado = StringBuilder()

        for (c in entrada) {
            when {
                c.isDigit() -> resultado.append(c)
                c == '.' || c == ',' -> {
                    if (!separadorVisto) {
                        resultado.append('.')
                        separadorVisto = true
                    }
                }
            }
        }
        return resultado.toString()
    }
}
