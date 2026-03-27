package com.example.jobsterrabackend.models.entities

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class TipoContrato(
    val id: Int = 0,
    val nombre: String,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

// Clase para crear un nuevo tipo de contrato
@Serializable
data class TipoContratoCreate(
    val nombre: String
)