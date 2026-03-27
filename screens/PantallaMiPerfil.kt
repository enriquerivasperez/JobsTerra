package com.example.jobsterra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jobsterra.data.api.ApiService
import com.example.jobsterra.data.models.Usuario
import com.example.jobsterra.ui.viewmodels.AuthViewModel
import com.example.jobsterra.ui.viewmodels.PerfilViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMiPerfil(
    usuario: Usuario,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    // Crear el PerfilViewModel con tu ApiService
    val perfilViewModel = remember {
        PerfilViewModel(apiService = ApiService())
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Estados del ViewModel
    val isUpdating by perfilViewModel.isUpdating.collectAsState()
    val updateError by perfilViewModel.updateError.collectAsState()
    val updateSuccess by perfilViewModel.updateSuccess.collectAsState()
    val estadisticas by perfilViewModel.estadisticas.collectAsState()

    // Variable para almacenar el usuario actualizado
    var usuarioActualizado by remember { mutableStateOf(usuario) }

    // Cargar estadísticas al iniciar
    LaunchedEffect(usuario.id) {
        perfilViewModel.cargarEstadisticas(usuario.id)
    }

    // Mostrar mensaje de éxito y recargar datos
    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            // NO limpiar inmediatamente el estado para que se vea el mensaje
            // El usuario lo cerrará manualmente
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
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
            // Avatar y nombre
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(getAvatarColor(usuarioActualizado.nombre)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getInitials(usuarioActualizado.nombre),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = usuario.nombre,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Text(
                        text = usuario.tipoUsuarioNombre ?: "Usuario",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Chip de estado
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                usuario.estado.replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (usuario.estado == "activo")
                                Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else Color(0xFFFF9800).copy(alpha = 0.1f),
                            labelColor = if (usuario.estado == "activo")
                                Color(0xFF4CAF50)
                            else Color(0xFFFF9800)
                        )
                    )
                }
            }

            // Información personal
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
                        text = "Información Personal",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email - siempre se muestra
                    InfoRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = usuarioActualizado.email.takeIf { !it.isNullOrBlank() } ?: "No hay email"
                    )

                    // Teléfono - siempre se muestra
                    InfoRow(
                        icon = Icons.Default.Phone,
                        label = "Teléfono",
                        value = usuarioActualizado.telefono.takeIf { !it.isNullOrBlank() } ?: "No hay teléfono"
                    )

                    InfoRow(
                        icon = Icons.Default.Info,
                        label = "Biografía",
                        value = usuarioActualizado.biografia.takeIf { !it.isNullOrBlank() } ?: "No tiene biografia"
                    )



                }
            }

            // Estadísticas
            estadisticas?.let { stats ->
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
                            text = "Estadísticas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            EstadisticaCard(
                                titulo = "Favoritas",
                                valor = "${stats.ofertasFavoritas}",
                                icono = Icons.Default.Favorite,
                                color = Color(0xFFE91E63),
                                modifier = Modifier.weight(1f)
                            )

                            EstadisticaCard(
                                titulo = "Aplicaciones",
                                valor = "${stats.aplicacionesEnviadas}",
                                icono = Icons.Default.Send,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Acciones
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
                        text = "Acciones",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ActionRow(
                        icon = Icons.Default.Edit,
                        title = "Editar Perfil",
                        subtitle = "Actualiza tu información personal",
                        onClick = { showEditDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    ActionRow(
                        icon = Icons.Default.ExitToApp,
                        title = "Cerrar Sesión",
                        subtitle = "Salir de tu cuenta",
                        onClick = { showLogoutDialog = true },
                        textColor = Color.Red
                    )
                }
            }

            // Error de actualización
            updateError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { perfilViewModel.clearUpdateError() }
                        ) {
                            Text("Cerrar", color = Color(0xFFD32F2F))
                        }
                    }
                }
            }

            // Mensaje de éxito
            if (updateSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Perfil actualizado correctamente",
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { perfilViewModel.clearUpdateSuccess() }
                        ) {
                            Text("Cerrar", color = Color(0xFF4CAF50))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Dialog de edición
    if (showEditDialog) {
        EditProfileDialog(
            usuario = usuarioActualizado,
            isUpdating = isUpdating,
            onDismiss = { showEditDialog = false },
            onUpdate = { nombre, telefono, biografia ->
                perfilViewModel.actualizarPerfil(
                    usuario = usuarioActualizado,
                    nombre = nombre,
                    telefono = telefono,
                    biografia = biografia,
                    onSuccess = { usuarioNuevo ->
                        // Actualizar los datos localmente
                        usuarioActualizado = usuarioActualizado.copy(
                            nombre = nombre,
                            telefono = telefono,
                            biografia = biografia
                        )
                    }
                )
                showEditDialog = false
            }
        )
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
                        containerColor = Color.Red
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
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = Color(0xFF1976D2),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
private fun EstadisticaCard(
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icono,
                contentDescription = titulo,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = valor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = titulo,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = "Ir",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    usuario: Usuario,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onUpdate: (String, String?, String?) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario.nombre) }
    var telefono by remember { mutableStateOf(usuario.telefono ?: "") }
    var biografia by remember { mutableStateOf(usuario.biografia ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = biografia,
                    onValueChange = { biografia = it },
                    label = { Text("Biografía (opcional)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(
                        nombre.trim(),
                        telefono.trim().ifBlank { null },
                        biografia.trim().ifBlank { null }
                    )
                },
                enabled = !isUpdating && nombre.trim().isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating
            ) {
                Text("Cancelar")
            }
        }
    )
}

// Funciones auxiliares
private fun getAvatarColor(nombre: String): Color {
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF81C784), Color(0xFF64B5F6),
        Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF4DB6AC)
    )
    return colors[nombre.hashCode().absoluteValue % colors.size]
}

private fun getInitials(nombre: String): String {
    return if (nombre.isBlank()) "U" else {
        nombre.trim().split(" ").take(2).map { it.first().uppercase() }.joinToString("")
    }
}

// Modelo para estadísticas
data class EstadisticasUsuario(
    val ofertasFavoritas: Int = 0,
    val aplicacionesEnviadas: Int = 0,
    val chatsActivos: Int = 0
)