package com.example.jobsterrabackend.services

import com.example.jobsterrabackend.models.entities.Favorito
import com.example.jobsterrabackend.models.entities.FavoritoCreate
import com.example.jobsterrabackend.models.entities.PaginaFavoritos
import com.example.jobsterrabackend.repositories.FavoritoRepository
import com.example.jobsterrabackend.repositories.OfertaRepository
import com.example.jobsterrabackend.repositories.UsuarioRepository

class FavoritoService(
    private val repository: FavoritoRepository,
    private val ofertaRepository: OfertaRepository,
    private val usuarioRepository: UsuarioRepository
) {

    suspend fun getFavoritosByUsuarioId(usuarioId: Int, pagina: Int = 1, elementosPorPagina: Int = 10): PaginaFavoritos {
        return repository.findByUsuarioId(usuarioId, pagina, elementosPorPagina)
    }

    suspend fun getFavoritoById(id: Int): Favorito? {
        return repository.findById(id)
    }

    suspend fun isFavorito(usuarioId: Int, ofertaId: Int): Boolean {
        return repository.findByUsuarioIdAndOfertaId(usuarioId, ofertaId) != null
    }

    suspend fun createFavorito(usuarioId: Int, favoritoCreate: FavoritoCreate): Favorito? {
        // Verificar que el usuario existe
        val usuario = usuarioRepository.findById(usuarioId)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        // Verificar que la oferta existe
        val oferta = ofertaRepository.findById(favoritoCreate.ofertaId)
            ?: throw IllegalArgumentException("Oferta no encontrada")

        // Verificar que el usuario sea de tipo "Candidato"
        if (usuario.tipoUsuarioNombre?.lowercase() != "candidato") {
            throw IllegalArgumentException("Solo los candidatos pueden guardar ofertas como favoritas")
        }

        // Verificar que la oferta esté activa
        if (oferta.estado.lowercase() != "activa") {
            throw IllegalArgumentException("No se pueden guardar como favoritas ofertas que no estén activas")
        }

        // Verificar que no sea una oferta propia (si el usuario es también empresa)
        if (oferta.empresaId == usuarioId) {
            throw IllegalArgumentException("No puedes guardar como favorita tu propia oferta")
        }

        // Crear el favorito (si ya existe, el repositorio lo maneja)
        return repository.create(usuarioId, favoritoCreate.ofertaId)
    }

    suspend fun deleteFavorito(id: Int, usuarioId: Int): Boolean {
        // Verificar que el favorito existe
        val favorito = repository.findById(id)
            ?: throw IllegalArgumentException("Favorito no encontrado")

        // Verificar que el usuario es el dueño del favorito
        if (favorito.usuarioId != usuarioId) {
            throw IllegalArgumentException("No tienes permisos para eliminar este favorito")
        }

        return repository.delete(id)
    }

    suspend fun deleteFavoritoByOfertaId(usuarioId: Int, ofertaId: Int): Boolean {
        return repository.deleteByUsuarioIdAndOfertaId(usuarioId, ofertaId)
    }

    suspend fun toggleFavorito(usuarioId: Int, ofertaId: Int): Boolean {
        // Verificar si ya existe
        val existingFavorito = repository.findByUsuarioIdAndOfertaId(usuarioId, ofertaId)

        return if (existingFavorito != null) {
            // Si existe, eliminarlo
            repository.delete(existingFavorito.id)
        } else {
            // Si no existe, crearlo
            val favoritoCreate = FavoritoCreate(ofertaId)
            repository.create(usuarioId, ofertaId) != null
        }
    }
}