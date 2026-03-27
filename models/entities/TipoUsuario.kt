package com.example.jobsterrabackend.models.entities

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class TipoUsuario(
    val id: Int = 0,
    val nombre: String,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)

// Clase para crear un nuevo tipo de usuario
@Serializable
data class TipoUsuarioCreate(
    val nombre: String
)

// Función de extensión para convertir Instant a Long
fun Instant?.toEpochMilliOrNull(): Long? = this?.toEpochMilli()