package com.example.jobsterrabackend.services

import com.example.jobsterrabackend.models.entities.TipoContrato
import com.example.jobsterrabackend.models.entities.TipoContratoCreate
import com.example.jobsterrabackend.repositories.TipoContratoRepository

class TipoContratoService(private val repository: TipoContratoRepository) {

    suspend fun getAllTiposContrato(): List<TipoContrato> {
        return repository.findAll()
    }

    suspend fun getTipoContratoById(id: Int): TipoContrato? {
        return repository.findById(id)
    }

    suspend fun createTipoContrato(tipoContratoCreate: TipoContratoCreate): TipoContrato? {
        return repository.create(tipoContratoCreate.nombre)
    }

    suspend fun updateTipoContrato(id: Int, tipoContratoCreate: TipoContratoCreate): Boolean {
        return repository.update(id, tipoContratoCreate.nombre)
    }

    suspend fun deleteTipoContrato(id: Int): Boolean {
        return repository.delete(id)
    }
}