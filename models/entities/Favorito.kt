package com.example.jobsterrabackend.models.entities

import kotlinx.serialization.Serializable

@Serializable
data class Favorito(
    val id: Int = 0,
    val usuarioId: Int,
    val ofertaId: Int,
    val fecha: Long, // Epoch millis
    val oferta: Oferta? = null, // Datos de la oferta (para mostrar en listados)
    val createdAt: Long? = null
)

// Clase para crear un nuevo favorito
@Serializable
data class FavoritoCreate(
    val ofertaId: Int
    // usuarioId se tomará del usuario autenticado
)

// Respuesta para listado de favoritos
@Serializable
data class PaginaFavoritos(
    val favoritos: List<Favorito>,
    val total: Int,
    val pagina: Int,
    val elementosPorPagina: Int,
    val totalPaginas: Int
)