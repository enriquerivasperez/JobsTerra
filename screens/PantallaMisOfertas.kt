package com.example.jobsterra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jobsterra.data.models.Usuario
import com.example.jobsterra.data.models.OfertaItem
import com.example.jobsterra.ui.viewmodels.AuthViewModel
import com.example.jobsterra.ui.viewmodels.OfertasViewModel
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMisOfertas(
    usuario: Usuario,
    authViewModel: AuthViewModel,
    ofertasViewModel: OfertasViewModel = viewModel(),
    onNavigateToHome: () -> Unit = {},
    onNavigateToChats: () -> Unit = {},
    onNavigateToComunidad: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToOferta: (Int) -> Unit = {}

) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Guardadas", "Postuladas")

    // Estados de las ofertas
    val ofertasItems by ofertasViewModel.getOfertasAsItems().collectAsState()
    val isLoadingOfertas by ofertasViewModel.isLoading.collectAsState()
    val errorOfertas by ofertasViewModel.error.collectAsState()

    // CORREGIDO: Sin remember para que se actualice dinámicamente
    val ofertasGuardadas = ofertasItems.filter { it.esFavorita }

    val ofertasPostuladas = remember {
        // Mock: Algunas ofertas como postuladas
        ofertasItems.take(2)
    }

    // Refrescar ofertas cuando se cambia a la pestaña de favoritos
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 0) { // Pestaña "Guardadas"
            // Opcional: refrescar ofertas para asegurar datos actualizados
            // ofertasViewModel.cargarOfertas()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Ofertas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    var showUserMenu by remember { mutableStateOf(false) }

                    Box {
                        // Avatar clicable
                        IconButton(
                            onClick = { showUserMenu = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(getAvatarColor(usuario.nombre)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getInitials(usuario.nombre),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Menú desplegable
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Mi Perfil")
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    onNavigateToPerfil()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Settings, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Configuración")
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    // TODO: Navegar a configuración
                                }
                            )

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cerrar Sesión", color = Color.Red)
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    authViewModel.signOut()
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1976D2)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2)
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = onNavigateToHome
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Mis Ofertas") },
                    label = { Text("Mis Ofertas") },
                    selected = true,
                    onClick = { /* Ya estamos aquí */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1976D2),
                        selectedTextColor = Color(0xFF1976D2),
                        indicatorColor = Color(0xFF1976D2).copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Email, contentDescription = "Chats") },
                    label = { Text("Chats") },
                    selected = false,
                    onClick = onNavigateToChats
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Comunidad") },
                    label = { Text("Comunidad") },
                    selected = false,
                    onClick = onNavigateToComunidad
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF1976D2),
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Manejo de errores
            errorOfertas?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD32F2F).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Error al cargar ofertas",
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                ofertasViewModel.clearError()
                                ofertasViewModel.cargarOfertas()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1976D2)
                            )
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
            }

            // Contenido de las pestañas
            when (selectedTabIndex) {
                0 -> {
                    // Pestaña Guardadas - CORREGIDO con loading state
                    if (isLoadingOfertas) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF1976D2)
                            )
                        }
                    } else if (ofertasGuardadas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.FavoriteBorder,
                                    contentDescription = "Sin favoritos",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No tienes ofertas guardadas",
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Marca ofertas como favoritas para verlas aquí",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(ofertasGuardadas) { oferta ->
                                OfertaCard(
                                    oferta = oferta,
                                    onFavoritoClick = {
                                        ofertasViewModel.toggleFavorito(oferta.id)
                                    },
                                    onOfertaClick = {
                                        onNavigateToOferta(oferta.id)
                                    },
                                    showPostulateButton = true
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Pestaña Postuladas
                    if (ofertasPostuladas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Sin postulaciones",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No has postulado a ninguna oferta",
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Postúlate a ofertas para hacer seguimiento aquí",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(ofertasPostuladas) { oferta ->
                                OfertaCardPostulada(
                                    oferta = oferta,
                                    estado = "Pendiente", // Mock estado
                                    fechaPostulacion = "Hace 2 días", // Mock fecha
                                    onOfertaClick = {
                                        // TODO: Navegar a detalles
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfertaCard(
    oferta: OfertaItem,
    onFavoritoClick: () -> Unit,
    onOfertaClick: () -> Unit,
    showPostulateButton: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOfertaClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = oferta.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        text = oferta.empresa,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }

                IconButton(
                    onClick = onFavoritoClick
                ) {
                    Icon(
                        if (oferta.esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (oferta.esFavorita) Color.Red else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Ubicación",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = oferta.ubicacion,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = oferta.salario,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = oferta.descripcion,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    AssistChip(
                        onClick = { },
                        label = { Text(oferta.modalidad, fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF1976D2).copy(alpha = 0.1f),
                            labelColor = Color(0xFF1976D2)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    AssistChip(
                        onClick = { },
                        label = { Text(oferta.tipoContrato, fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f),
                            labelColor = Color(0xFFFF9800)
                        )
                    )
                }

                Text(
                    text = oferta.fechaPublicacion,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (showPostulateButton) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* TODO: Postular */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Postular")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Postular")
                }
            }
        }
    }
}

@Composable
private fun OfertaCardPostulada(
    oferta: OfertaItem,
    estado: String,
    fechaPostulacion: String,
    onOfertaClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOfertaClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = oferta.titulo,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                    Text(
                        text = oferta.empresa,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }

                // Estado de la postulación
                AssistChip(
                    onClick = { },
                    label = { Text(estado, fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (estado) {
                            "Pendiente" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                            "En proceso" -> Color(0xFF2196F3).copy(alpha = 0.1f)
                            "Aceptada" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            "Rechazada" -> Color(0xFFD32F2F).copy(alpha = 0.1f)
                            else -> Color.Gray.copy(alpha = 0.1f)
                        },
                        labelColor = when (estado) {
                            "Pendiente" -> Color(0xFFFF9800)
                            "En proceso" -> Color(0xFF2196F3)
                            "Aceptada" -> Color(0xFF4CAF50)
                            "Rechazada" -> Color(0xFFD32F2F)
                            else -> Color.Gray
                        }
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = oferta.descripcion,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Postulado $fechaPostulacion",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Funciones auxiliares para el avatar
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