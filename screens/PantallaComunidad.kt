package com.example.jobsterra.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jobsterra.data.api.ApiService
import com.example.jobsterra.data.models.Usuario
import com.example.jobsterra.ui.viewmodels.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

data class PostComunidad(
    val id: Int,
    val usuario: Usuario,
    val contenido: String,
    val fechaPublicacion: String,
    val tipoPost: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaComunidad(
    usuario: Usuario,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToMisOfertas: () -> Unit = {},
    onNavigateToChats: () -> Unit = {}
) {
    val apiService = remember { ApiService() }
    var usuarios by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var posts by remember { mutableStateOf<List<PostComunidad>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Cargar usuarios y generar posts
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val resultado = apiService.getAllUsuarios() // Necesitarás crear este método
            if (resultado.isSuccess) {
                usuarios = resultado.getOrNull() ?: emptyList()
                posts = generarPosts(usuarios)
            } else {
                error = "Error al cargar la comunidad"
            }
        } catch (e: Exception) {
            error = "Error de conexión"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comunidad", color = Color.White) },
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
                    selected = true,
                    onClick = { /* Ya estamos en comunidad */ },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1976D2),
                        selectedTextColor = Color(0xFF1976D2),
                        indicatorColor = Color(0xFF1976D2).copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF1976D2))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Cargando comunidad...", color = Color.Gray)
                        }
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(error!!, color = Color.Red)
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Header de bienvenida
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF1976D2).copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "¡Bienvenido a la Comunidad!",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1976D2)
                                    )
                                    Text(
                                        text = "Conecta con empresas y otros profesionales",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Posts de la comunidad
                        items(posts) { post ->
                            PostCard(post = post)
                        }

                        // Espaciado final
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(post: PostComunidad) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del post (usuario)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(getAvatarColor(post.usuario.nombre)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getInitials(post.usuario.nombre),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.usuario.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (post.usuario.tipoUsuarioId == 1) "Empresa" else "Candidato",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${post.fechaPublicacion}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Icono del tipo de post
                Icon(
                    if (post.usuario.tipoUsuarioId == 1) Icons.Default.Settings else Icons.Default.Person,
                    contentDescription = "Tipo",
                    tint = if (post.usuario.tipoUsuarioId == 1) Color(0xFF1976D2) else Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contenido del post
            Text(
                text = post.contenido,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Acciones del post
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Me gusta",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(5..42).random()}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Comentarios",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(0..8).random()}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Compartir",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Compartir",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// Función para generar contenido de posts según el tipo de usuario
private fun generarPosts(usuarios: List<Usuario>): List<PostComunidad> {
    val posts = mutableListOf<PostComunidad>()
    var postId = 1

    val contenidoEmpresas = listOf(
        "¡Estamos buscando desarrolladores Junior y Senior para unirse a nuestro equipo! Excelente ambiente de trabajo y beneficios.",
        "Hemos abierto una nueva oficina en Madrid. ¡Muchas oportunidades de crecimiento disponibles!",
        "Nuestra empresa ha sido reconocida como una de las mejores para trabajar. ¡Únete a nosotros!",
        "Buscamos talento en el área de marketing digital. Experiencia con redes sociales y SEO.",
        "¿Te apasiona la tecnología? Tenemos vacantes en desarrollo full-stack y DevOps.",
        "Cultura empresarial inclusiva y diversa. Trabajamos por un futuro mejor juntos.",
        "Lanzamos nuestro programa de prácticas profesionales. ¡Excelente oportunidad para estudiantes!"
    )

    val contenidoCandidatos = listOf(
        "¡Acabo de completar mi certificación en Kotlin! Muy emocionado por aplicar estos conocimientos.",
        "Consejos para entrevistas técnicas: practicar algoritmos y tener proyectos en GitHub actualizados.",
        "Después de 6 meses buscando, finalmente conseguí mi trabajo ideal. ¡Nunca rendirse!",
        "¿Alguien tiene experiencia con React Native? Me gustaría conectar y aprender.",
        "Excelente curso de diseño UX que acabo de terminar. Recomiendo totalmente la plataforma.",
        "Networking es clave en nuestra industria. ¡Siempre dispuesto a conectar con profesionales!",
        "Mi experiencia en la última entrevista: preparación y confianza son fundamentales."
    )

    usuarios.forEachIndexed { index, usuario ->
        val contenidoLista = if (usuario.tipoUsuarioId == 1) contenidoEmpresas else contenidoCandidatos
        val fechas = listOf("Hace 2h", "Hace 5h", "Hace 1d", "Hace 2d", "Hace 3d")

        // Cada usuario tiene 1-2 posts
        val numPosts = if (index < usuarios.size / 2) 1 else if ((index % 3) == 0) 2 else 1

        repeat(numPosts) { postIndex ->
            posts.add(
                PostComunidad(
                    id = postId++,
                    usuario = usuario,
                    contenido = contenidoLista.random(),
                    fechaPublicacion = fechas.random(),
                    tipoPost = if (usuario.tipoUsuarioId == 1) "empresa" else "candidato"
                )
            )
        }
    }

    return posts.shuffled().take(20)
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