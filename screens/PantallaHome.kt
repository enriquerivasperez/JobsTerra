package com.example.jobsterra.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
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
fun PantallaHome(
    usuario: Usuario,
    authViewModel: AuthViewModel,
    ofertasViewModel: OfertasViewModel = viewModel(),
    onNavigateToMisOfertas: () -> Unit = {},
    onNavigateToChats: () -> Unit = {},
    onNavigateToComunidad: () -> Unit = {},
    onNavigateToPerfil: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onNavigateToOferta: (Int) -> Unit = {},


) {
    var searchText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todas") }
    var showFilterMenu by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Estados de las ofertas
    val ofertasItems by ofertasViewModel.getOfertasAsItems().collectAsState()
    val isLoadingOfertas by ofertasViewModel.isLoading.collectAsState()
    val errorOfertas by ofertasViewModel.error.collectAsState()

    // Filtrar ofertas localmente para la búsqueda y filtros
    val ofertasFiltradas = remember(ofertasItems, searchText, selectedFilter) {
        ofertasItems.filter { oferta ->
            val matchesSearch = if (searchText.isBlank()) true else {
                oferta.titulo.contains(searchText, ignoreCase = true) ||
                        oferta.empresa.contains(searchText, ignoreCase = true)
            }

            val matchesFilter = if (selectedFilter == "Todas") true else {
                oferta.modalidad == selectedFilter || oferta.tipoContrato == selectedFilter
            }

            matchesSearch && matchesFilter
        }
    }

    // Manejar búsqueda: buscar entre todas las ofertas cuando el usuario escribe
    LaunchedEffect(searchText) {
        if (searchText.isNotBlank() && !isSearching) {
            // Usuario empezó a buscar, cargar TODAS las ofertas
            isSearching = true
            ofertasViewModel.buscarOfertas(searchText)
        } else if (searchText.isBlank() && isSearching) {
            // Usuario borró la búsqueda, volver a mostrar ofertas iniciales
            isSearching = false
            ofertasViewModel.cargarOfertas()
        } else if (searchText.isNotBlank() && isSearching) {
            // Usuario sigue buscando, actualizar búsqueda
            ofertasViewModel.buscarOfertas(searchText)
        }
    }

    // Scroll infinito removido - solo mostramos ofertas iniciales o resultados de búsqueda

    val filtros = listOf("Todas", "Remoto", "Presencial", "Híbrido", "Indefinido", "Temporal")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¡Hola, ${usuario.nombre}!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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
                                    onNavigateToConfiguracion()
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
                    selected = true,
                    onClick = { /* Ya estamos en home */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1976D2),
                        selectedTextColor = Color(0xFF1976D2),
                        indicatorColor = Color(0xFF1976D2).copy(alpha = 0.1f)
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Mis Ofertas") },
                    label = { Text("Mis Ofertas") },
                    selected = false,
                    onClick = onNavigateToMisOfertas
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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Barra de búsqueda
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Buscar ofertas de trabajo...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color(0xFF1976D2)
                        )
                    },
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(
                                onClick = { searchText = "" }
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        focusedLabelColor = Color(0xFF1976D2)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                // Filtros
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Filtros",
                        tint = Color(0xFF1976D2)
                    )

                    Box {
                        FilterChip(
                            onClick = { showFilterMenu = true },
                            label = { Text(selectedFilter) },
                            selected = selectedFilter != "Todas",
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Expandir"
                                )
                            }
                        )

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            filtros.forEach { filtro ->
                                DropdownMenuItem(
                                    text = { Text(filtro) },
                                    onClick = {
                                        selectedFilter = filtro
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Estadísticas rápidas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EstadisticaCard(
                        titulo = if (isSearching) "Encontradas" else "Ofertas",
                        valor = "${ofertasFiltradas.size}",
                        icono = Icons.AutoMirrored.Filled.List,
                        modifier = Modifier.weight(1f)
                    )

                    EstadisticaCard(
                        titulo = if (isSearching) "Total" else "Nuevas",
                        valor = "${ofertasItems.size}",
                        icono = Icons.Default.Notifications,
                        modifier = Modifier.weight(1f)
                    )

                    EstadisticaCard(
                        titulo = "Guardadas",
                        valor = "${ofertasItems.count { it.esFavorita }}",
                        icono = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSearching) {
                            if (searchText.isNotBlank()) "Resultados para \"$searchText\""
                            else "Ofertas Recientes"
                        } else "Ofertas Recientes",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    if (isSearching && ofertasFiltradas.size > 10) {
                        Text(
                            text = "Mostrando ${ofertasFiltradas.size} de 30",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Error
            errorOfertas?.let { error ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                    text = if (error.contains("favorito"))
                                        "Error al cambiar favorito"
                                    else
                                        "Error al cargar ofertas",
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

                            Row {
                                Button(
                                    onClick = {
                                        ofertasViewModel.clearError()
                                        if (!error.contains("favorito")) {
                                            if (isSearching && searchText.isNotBlank()) {
                                                ofertasViewModel.buscarOfertas(searchText)
                                            } else {
                                                ofertasViewModel.cargarOfertas()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1976D2)
                                    )
                                ) {
                                    Text(if (error.contains("favorito")) "Cerrar" else "Reintentar")
                                }

                                if (error.contains("favorito")) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(
                                        onClick = { ofertasViewModel.clearError() }
                                    ) {
                                        Text("Ignorar", color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Loading indicator si está cargando
            if (isLoadingOfertas && ofertasItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = Color(0xFF1976D2)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (isSearching) "Buscando ofertas..." else "Cargando ofertas...",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Ofertas
            items(ofertasFiltradas) { oferta ->
                OfertaCard(
                    oferta = oferta,
                    onFavoritoClick = {
                        ofertasViewModel.toggleFavorito(oferta.id)
                    },
                    onOfertaClick = {
                        onNavigateToOferta(oferta.id)
                    }
                )
            }

            // Mensaje si no hay resultados de búsqueda
            if (isSearching && ofertasFiltradas.isEmpty() && !isLoadingOfertas) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Sin resultados",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No se encontraron ofertas",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Text(
                                text = "Intenta con otros términos de búsqueda",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Indicador de carga al final removido - no hay scroll infinito

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun EstadisticaCard(
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
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
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = valor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            Text(
                text = titulo,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun OfertaCard(
    oferta: OfertaItem,
    onFavoritoClick: () -> Unit,
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