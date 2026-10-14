package com.Fenix.manami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.Fenix.manami.pedidos.ui.DetallePedido
import com.Fenix.manami.pedidos.ui.CrearPedidoPantalla
import com.Fenix.manami.pedidos.ui.CrearDetallePedido
import com.Fenix.manami.pedidos.ui.VistaPedido
import com.Fenix.manami.inventario.ui.VistaMateriales
import com.Fenix.manami.inventario.ui.CrearMaterial
import com.Fenix.manami.inventario.ui.AgregarMaterial
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Restaura/relanza la descarga automática de copia de seguridad
        RespaldoProgramado.iniciar(applicationContext)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    val session = SupabaseClient.client.auth.currentSessionOrNull()
                    val rutaInicial = if (session != null) "home" else "login"

                    // Callback de cierre de sesión
                    val cerrarSesion: () -> Unit = {
                        lifecycleScope.launch {
                            SupabaseClient.client.auth.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    // Callback de navegación del menú
                    val navegar: (String) -> Unit = { ruta ->
                        when (ruta) {
                            "home" -> navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                            "pedidos" -> navController.navigate("pedidos")
                            "inventario" -> navController.navigate("inventario")
                            "copia_seguridad" -> navController.navigate("copia_seguridad")
                            "chatbot" -> navController.navigate("chatbot")
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = rutaInicial
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
                                },
                                onNavigate = navegar,
                                onCerrarSesion = cerrarSesion
                            )
                        }

                        // RUTA PEDIDOS
                        composable("pedidos") {
                            VistaPedido(
                                onAgregarPedidoClick = {
                                    navController.navigate("CrearPedido")
                                },
                                onPedidoClick = { pedidoId ->
                                    navController.navigate("detalle_pedido/$pedidoId")
                                },
                                onEliminarPedido = { /* el ViewModel maneja la eliminación */ },
                                onNavigate = navegar,
                                onCerrarSesion = cerrarSesion
                            )
                        }

                        // CREAR PEDIDO
                        composable("CrearPedido") {
                            CrearPedidoPantalla(
                                onPedidoCreado = { pedidoId ->
                                    // Redirigir a crear detalle del pedido
                                    navController.navigate("crear_detalle_pedido/$pedidoId") {
                                        popUpTo("pedidos") { inclusive = false }
                                    }
                                },
                                onVolver = {
                                    navController.popBackStack()
                                },
                                onNavigate = navegar,
                                onCerrarSesion = cerrarSesion
                            )
                        }

                        // CREAR DETALLE DEL PEDIDO
                        composable(
                            route = "crear_detalle_pedido/{pedidoId}",
                            arguments = listOf(navArgument("pedidoId") {
                                type = NavType.IntType
                            })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("pedidoId") ?: 0

                            CrearDetallePedido(
                                pedidoId = id,
                                onVolver = { navController.popBackStack() },
                                onNavigate = navegar,
                                onCerrarSesion = cerrarSesion,
                                onGuardado = {
                                    navController.navigate("detalle_pedido/$id") {
                                        popUpTo("pedidos") { inclusive = false }
                                    }
                                }
                            )
                        }

                        // DETALLE DEL PEDIDO
                        composable(
                            route = "detalle_pedido/{pedidoId}",
                            arguments = listOf(navArgument("pedidoId") {
                                type = NavType.IntType
                            })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("pedidoId") ?: 0

                            DetallePedido(
                                pedidoId = id,
                                onVolver = { navController.popBackStack() },
                                onNavigate = navegar,
                                onCerrarSesion = cerrarSesion,
                                onAgregarTrabajo = { idPedido ->
                                    navController.navigate("crear_detalle_pedido/$idPedido")
                                }
                            )
                        }

                        // RUTA INVENTARIO
                        composable("inventario") {
                            VistaMateriales(
                                onAgregarMaterialClick = {
                                    navController.navigate("crearMaterial")
                                },
                                onMaterialClick = { id ->
                                    // Abrir el material para agregar más cantidad
                                    navController.navigate("agregarMaterial/$id")
                                },
                                onNavigate = navegar,
                                onCerrarSesion = cerrarSesion
                            )
                        }

                        // CREAR MATERIAL
                        composable("crearMaterial") {
                            CrearMaterial(
                                onVolver = {
                                    navController.popBackStack()
                                },
                                onGuardar = {
                                    navController.popBackStack()
                                },
                                onNavigate = navegar,
                                onCerrarSesion = cerrarSesion
                            )
                        }

                        // AGREGAR MATERIAL (al material existente)
                        composable(
                            route = "agregarMaterial/{idMaterial}",
                            arguments = listOf(navArgument("idMaterial") { type = NavType.IntType })
                        ) { entrada ->
                            AgregarMaterial(
                                materialId = entrada.arguments?.getInt("idMaterial") ?: 0,
                                onVolver = {
                                    navController.popBackStack()
                                },
                                onNavigate = navegar,
                                onCerrarSesion = cerrarSesion
                            )
                        }

                        // COPIA DE SEGURIDAD
                        composable("copia_seguridad") {
                            CopiaSeguridad(
                                onVolver = {
                                    navController.popBackStack()
                                },
                                onNavigate = navegar,
                                onCerrarSesion = cerrarSesion
                            )
                        }

                        // CHATBOT
                        composable("chatbot") {
                            Chatbot(
                                onVolver = {
                                    navController.popBackStack()
                                },
                                onNavigate = navegar,
                                onCerrarSesion = cerrarSesion
                            )
                        }
                    }
                }
            }
        }
    }
}
