package com.Fenix.manami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.Fenix.manami.pedidos.ui.ActualizarPedidoVista
import com.Fenix.manami.pedidos.ui.CrearPedidoPantalla
import com.Fenix.manami.pedidos.ui.VistaPedido
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    // Rastreamos la ruta actual
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val rutaActual = navBackStackEntry?.destination?.route

                    // Definimos la ruta inicial
                    val session = SupabaseClient.client.auth.currentSessionOrNull()
                    val rutaInicial = if (session != null) "home" else "login"

                    Scaffold(
                        topBar = {
                            if (rutaActual != "login") {
                                BarraSuperior(titulo = "Manami" ,
                                CerrarSesion = {
                                    lifecycleScope.launch {
                                        SupabaseClient.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                                )
                            }
                        }
                    ) { paddingValues ->
                        NavHost(
                            navController = navController,
                            startDestination = rutaInicial,
                            modifier = Modifier.padding(paddingValues)
                        ) {
                            // RUTA LOGIN
                            composable("login") {
                                Login(
                                    onLoginSuccess = {
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            // RUTA HOME
                            composable("home") {
                                Home(
                                    onNavegarAPedidos = {
                                        navController.navigate("pedidos")
                                    }
                                )
                            }
                            // RUTA PEDIDOS
                            composable("pedidos") {
                                VistaPedido(
                                    onAgregarPedidoClick = {
                                        navController.navigate("CrearPedido")
                                    },
                                    onPedidoClick = { pedidoId ->
                                        navController.navigate("editar_pedido/$pedidoId")
                                    }
                                )
                            }
                            // CREAR PEDIDO
                            composable("CrearPedido") {
                                CrearPedidoPantalla(
                                    onPedidoCreado = {
                                        navController.navigate("pedidos") {
                                            popUpTo("pedidos") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            // EDITAR Y/O ELIMINAR  PEDIDO

                            composable(
                                route = "editar_pedido/{pedidoId}",
                                arguments = listOf(navArgument("pedidoId") {
                                    type = NavType.IntType
                                })
                            ) { backStackEntry ->
                                val id = backStackEntry.arguments?.getInt("pedidoId") ?: 0

                                ActualizarPedidoVista(
                                    pedidoId = id,
                                    onPedidoActualizado = { navController.popBackStack() }
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}
