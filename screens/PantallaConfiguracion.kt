package com.example.jobsterra.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jobsterra.data.models.Usuario
import com.example.jobsterra.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracion(
    usuario: Usuario,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Aplicación
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Aplicación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingRow(
                        icon = Icons.Default.Info,
                        title = "Versión",
                        subtitle = "1.0.0 (Beta)",
                        trailing = null
                    )
                }
            }

            // Soporte y Privacidad
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Soporte y Privacidad",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingRow(
                        icon = Icons.Default.Info,
                        title = "Centro de ayuda",
                        subtitle = "Preguntas frecuentes y soporte",
                        trailing = {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Ayuda",
                                tint = Color.Gray
                            )
                        },
                        onClick = { showHelpDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingRow(
                        icon = Icons.Default.Lock,
                        title = "Política de privacidad",
                        subtitle = "Cómo manejamos tus datos",
                        trailing = {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Privacidad",
                                tint = Color.Gray
                            )
                        },
                        onClick = { showPrivacyDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingRow(
                        icon = Icons.Default.Edit,
                        title = "Términos de uso",
                        subtitle = "Condiciones del servicio",
                        trailing = {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Términos",
                                tint = Color.Gray
                            )
                        },
                        onClick = { showTermsDialog = true }
                    )
                }
            }

            // Cuenta
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Cuenta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SettingRow(
                        icon = Icons.Default.ExitToApp,
                        title = "Cerrar sesión",
                        subtitle = "Salir de tu cuenta",
                        trailing = {
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = "Cerrar sesión",
                                tint = Color.Gray
                            )
                        },
                        onClick = { showLogoutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Dialog de cerrar sesión
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión") },
            text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.signOut()
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Text("Cerrar Sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de centro de ayuda
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("Centro de Ayuda") },
            text = {
                Column {
                    Text(
                        text = "Preguntas Frecuentes:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text("• ¿Cómo busco ofertas de trabajo?")
                    Text("Usa la barra de búsqueda en la pantalla principal.")
                    Text("")

                    Text("• ¿Cómo guardo ofertas como favoritas?")
                    Text("Toca el ícono de corazón en cada oferta.")
                    Text("")

                    Text("• ¿Cómo edito mi perfil?")
                    Text("Ve a Mi Perfil y toca el botón de editar.")
                    Text("")

                    Text("• ¿Necesitas más ayuda?")
                    Text("Contacta con soporte: soporte@jobsterra.com")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Dialog de política de privacidad
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Política de Privacidad") },
            text = {
                Column {
                    Text(
                        text = "En JobsTerra respetamos tu privacidad.",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text("• Recopilamos solo la información necesaria para brindarte el mejor servicio.")
                    Text("")

                    Text("• Tu información personal está protegida y encriptada.")
                    Text("")

                    Text("• No compartimos tus datos con terceros sin tu consentimiento.")
                    Text("")

                    Text("• Puedes solicitar la eliminación de tus datos en cualquier momento.")
                    Text("")

                    Text("Última actualización: Mayo 2025")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Dialog de términos de uso
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Términos de Uso") },
            text = {
                Column {
                    Text(
                        text = "Al usar JobsTerra, aceptas:",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text("• Usar la aplicación de manera responsable y legal.")
                    Text("")

                    Text("• Proporcionar información veraz en tu perfil.")
                    Text("")

                    Text("• Respetar a otros usuarios y empresas.")
                    Text("")

                    Text("• No usar la plataforma para actividades fraudulentas.")
                    Text("")

                    Text("• Cumplir con las políticas de la comunidad.")
                    Text("")

                    Text("Nos reservamos el derecho de suspender cuentas que violen estos términos.")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    textColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        trailing?.invoke()
    }
}