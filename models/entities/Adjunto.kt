package com.example.jobsterrabackend.models.entities

import kotlinx.serialization.Serializable

@Serializable
data class Adjunto(
    val id: Int = 0,
    val usuarioId: Int,
    val tipo: String, // perfil, cv, oferta, mensaje
    val referenciaId: Int? = null,
    val firebaseStoragePath: String,
    val nombreOriginal: String,
    val tamanoBytes: Int? = null,
    val tipoMime: String? = null,
    val createdAt: Long? = null,

    // URL pública para acceder al archivo
    val url: String? = null
)

// Clase para crear un nuevo adjunto
@Serializable
data class AdjuntoCreate(
    val tipo: String, // perfil, cv, oferta, mensaje
    val referenciaId: Int? = null,
    val nombreOriginal: String,
    val tamanoBytes: Int? = null,
    val tipoMime: String? = null
    // El archivo real se subirá mediante multipart form-data
)

// Respuesta después de subir un archivo
@Serializable
data class AdjuntoResponse(
    val id: Int,
    val url: String,
    val nombreOriginal: String,
    val tipoMime: String?
)