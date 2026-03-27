package com.example.jobsterrabackend.services

import com.example.jobsterrabackend.models.entities.TipoUsuario
import com.example.jobsterrabackend.models.entities.TipoUsuarioCreate
import com.example.jobsterrabackend.repositories.TipoUsuarioRepository

class TipoUsuarioService(private val repository: TipoUsuarioRepository) {

    suspend fun getAllTiposUsuario(): List<TipoUsuario> {
        return repository.findAll()
    }

    suspend fun getTipoUsuarioById(id: Int): TipoUsuario? {
        return repository.findById(id)
    }

    suspend fun createTipoUsuario(tipoUsuarioCreate: TipoUsuarioCreate): TipoUsuario? {
        return repository.create(tipoUsuarioCreate.nombre)
    }

    suspend fun updateTipoUsuario(id: Int, tipoUsuarioCreate: TipoUsuarioCreate): Boolean {
        return repository.update(id, tipoUsuarioCreate.nombre)
    }

    suspend fun deleteTipoUsuario(id: Int): Boolean {
        return repository.delete(id)
    }
}