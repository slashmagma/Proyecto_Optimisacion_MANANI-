package com.Fenix.manami

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

/**
 * Posiciona el menú pegado al borde derecho de la pantalla:
 * - x: siempre alineado al borde derecho de la ventana (sin espacio).
 * - y: justo debajo de la cabecera.
 */
private class MenuDerechaProvider(
    private val bordeInferiorCabecera: () -> Float
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // Pegado al borde derecho de la ventana
        val x = (windowSize.width - popupContentSize.width).coerceAtLeast(0)

        // Debajo de la cabecera (si aún no se conoce, usa el anchor)
        val yCabecera = bordeInferiorCabecera()
        val yInicial = if (yCabecera > 0f) yCabecera else anchorBounds.bottom.toFloat()
        val yMax = (windowSize.height - popupContentSize.height).toFloat().coerceAtLeast(0f)
        val y = yInicial.coerceIn(0f, yMax)

        return IntOffset(x, y.roundToInt())
    }
}

@Composable
fun HeaderFenix(
    titulo: String = "Fenix",
    onNavigate: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var bordeInferiorCabecera by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier.onGloballyPositioned { coords ->
            bordeInferiorCabecera = coords.boundsInWindow().bottom
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color(0xFF9C27B0))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Lado izquierdo: texto + logo
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Logo Fenix: se carga desde Supabase Storage (con respaldo local)
                LogoFenixCircular(
                    tamano = 40.dp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }

            // Lado derecho: botón hamburguesa
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF6A1B9A))
                    .clickable { menuExpanded = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Menú desplegable pegado al borde derecho de la pantalla
        if (menuExpanded) {
            Popup(
                onDismissRequest = { menuExpanded = false },
                popupPositionProvider = remember(bordeInferiorCabecera) {
                    MenuDerechaProvider { bordeInferiorCabecera }
                },
                properties = PopupProperties(focusable = true)
            ) {
                Column(
                    modifier = Modifier
                        .width(200.dp)
                        .shadow(8.dp, RoundedCornerShape(4.dp))
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF9C27B0))
                        .padding(vertical = 8.dp)
                ) {
                    // Cerrar
                    MenuItem(
                        texto = "Cerrar",
                        icono = Icons.Default.Close,
                        onClick = {
                            menuExpanded = false
                        }
                    )

                    // Home
                    MenuItem(
                        texto = "Home",
                        icono = Icons.Default.Home,
                        onClick = {
                            menuExpanded = false
                            onNavigate("home")
                        }
                    )

                    // Pedidos
                    MenuItem(
                        texto = "Pedidos",
                        icono = Icons.Default.List,
                        onClick = {
                            menuExpanded = false
                            onNavigate("pedidos")
                        }
                    )

                    // Inventario
                    MenuItem(
                        texto = "Inventario",
                        icono = Icons.Default.Star,
                        onClick = {
                            menuExpanded = false
                            onNavigate("inventario")
                        }
                    )

                    // Copia de seguridad
                    MenuItem(
                        texto = "Copia de seguridad",
                        icono = Icons.Default.Share,
                        onClick = {
                            menuExpanded = false
                            onNavigate("copia_seguridad")
                        }
                    )

                    // Chatbot
                    MenuItem(
                        texto = "Asistente",
                        icono = Icons.AutoMirrored.Filled.Send,
                        onClick = {
                            menuExpanded = false
                            onNavigate("chatbot")
                        }
                    )

                    // Cerrar sesión
                    MenuItem(
                        texto = "Cerrar sesion",
                        icono = Icons.AutoMirrored.Filled.ExitToApp,
                        onClick = {
                            menuExpanded = false
                            onCerrarSesion()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuItem(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Text(
                text = texto,
                color = Color.White,
                fontSize = 16.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icono,
                contentDescription = texto,
                tint = Color.White
            )
        },
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}
