package com.example.jobsterra.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jobsterra.data.models.OfertaItem
import com.example.jobsterra.ui.viewmodels.OfertasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaOferta(
    ofertaId: Int,
    ofertasViewModel: OfertasViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    // Estados
    val ofertasItems by ofertasViewModel.getOfertasAsItems().collectAsState()
    val isLoading by ofertasViewModel.isLoading.collectAsState()

    // Buscar la oferta específica
    val oferta = ofertasItems.find { it.id == ofertaId }

    // Si no encontramos la oferta, mostrar loading o error
    if (oferta == null) {
        LaunchedEffect(ofertaId) {
            // Si no está en la lista actual, cargar ofertas
            if (ofertasItems.isEmpty()) {
                ofertasViewModel.buscarOfertas("") // Cargar ofertas si la lista está vacía
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF1976D2))
            }
        } else {
            // Oferta no encontrada
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Oferta no encontrada",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )
                    ) {
                        Text("Volver")
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalles de la oferta",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                    IconButton(
                        onClick = { ofertasViewModel.toggleFavorito(oferta.id) }
                    ) {
                        Icon(
                            if (oferta.esFavorita) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (oferta.esFavorita) Color.Red else Color.White
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
        ) {
            // Header con información principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo de empresa (placeholder)
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1976D2).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = oferta.empresa.take(1).uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = oferta.titulo,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                            Text(
                                text = oferta.empresa,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Información básica
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoChip(
                            icon = Icons.Default.LocationOn,
                            text = oferta.ubicacion,
                            backgroundColor = Color(0xFFFF9800).copy(alpha = 0.1f),
                            textColor = Color(0xFFFF9800)
                        )

                        InfoChip(
                            icon = Icons.Default.Star,
                            text = oferta.salario,
                            backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            textColor = Color(0xFF4CAF50)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoChip(
                            icon = Icons.Default.Home,
                            text = oferta.modalidad,
                            backgroundColor = Color(0xFF1976D2).copy(alpha = 0.1f),
                            textColor = Color(0xFF1976D2)
                        )

                        InfoChip(
                            icon = Icons.Default.Info,
                            text = oferta.tipoContrato,
                            backgroundColor = Color(0xFF9C27B0).copy(alpha = 0.1f),
                            textColor = Color(0xFF9C27B0)
                        )
                    }
                }
            }

            // Descripción
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Descripción del puesto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = oferta.descripcion.ifEmpty {
                            "Esta es una excelente oportunidad para formar parte de nuestro equipo. " +
                                    "Buscamos personas motivadas y con ganas de crecer profesionalmente. " +
                                    "Ofrecemos un ambiente de trabajo dinámico y oportunidades de desarrollo."
                        },
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Información adicional
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Información adicional",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    InfoRow(
                        icon = Icons.Default.DateRange,
                        label = "Fecha de publicación",
                        value = oferta.fechaPublicacion
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow(
                        icon = Icons.Default.Person,
                        label = "Empresa",
                        value = oferta.empresa
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow(
                        icon = Icons.Default.Home,
                        label = "Modalidad",
                        value = oferta.modalidad
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow(
                        icon = Icons.Default.Info,
                        label = "Tipo de contrato",
                        value = oferta.tipoContrato
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // TODO: Compartir oferta
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1976D2)
                    )
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Compartir",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir")
                }

                Button(
                    onClick = {
                        // TODO: Postular a la oferta
                    },
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    )
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Postular",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Postular ahora",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}