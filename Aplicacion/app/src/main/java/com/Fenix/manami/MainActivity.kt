package com.Fenix.manami

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.lifecycleScope



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        obtenerDatosDeSupabase()


    }

    private fun obtenerDatosDeSupabase() {
        lifecycleScope.launch {
            Log.d("SUPABASE_LOG", "Iniciando consulta a Supabase...")
            try {
                val listaPedidos = SupabaseClient.client
                    .from("Pedidos")
                    .select()
                    .decodeList<Pedido>()

                Log.d("SUPABASE_LOG", "Cantidad de pedidos recibidos: ${listaPedidos.size}")

                for (pedido in listaPedidos) {
                    Log.d("SUPABASE_LOG", "ID: ${pedido.id} - Cliente: ${pedido.Cliente}")
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_LOG", "Error al conectar: ${e.message}", e)
            }
        }
    }




}