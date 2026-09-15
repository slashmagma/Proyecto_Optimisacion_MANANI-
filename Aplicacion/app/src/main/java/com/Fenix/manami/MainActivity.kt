package com.Fenix.manami

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    // Revisa sesión para definir vista inicial
                    val session = SupabaseClient.client.auth.currentSessionOrNull()
                    val rutaInicial = if (session != null) "home" else "login"

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
                                onCerrarSesion = {
                                    lifecycleScope.launch {
                                        SupabaseClient.client.auth.signOut()
                                        navController.navigate("login") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}