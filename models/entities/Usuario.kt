package com.example.jobsterrabackend.models.entities

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val email: String,
    val telefono: String? = null,
    val biografia: String? = null,
    val fotoPerfilUrl: String? = null,
    val tipoUsuarioId: Int,
    val tipoUsuarioNombre: String? = null, // Nombre del tipo de usuario para mostrar
    val estado: String = "activo",
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val firebaseUid: String? = null
)

// Clase para crear un nuevo usuario (sin ID, timestamps, etc.)
@Serializable
data class UsuarioCreate(
    val nombre: String,
    val email: String,
    val password: String, // Contraseña en texto plano (será hasheada antes de guardarla)
    val telefono: String? = null,
    val biografia: String? = null,
    val tipoUsuarioId: Int,
    val firebaseUid: String? = null
)

// Clase para actualizar un usuario existente
@Serializable
data class UsuarioUpdate(
    val nombre: String? = null,
    val email: String? = null,
    val telefono: String? = null,
    val biografia: String? = null,
    val estado: String? = null
)

// Clase para login
@Serializable
data class UsuarioLogin(
    val email: String,
    val password: String
)

// Clase para cambiar contraseña
@Serializable
data class CambioPassword(
    val passwordActual: String,
    val passwordNueva: String
)

// Respuesta para login exitoso
@Serializable
data class LoginResponse(
    val usuario: Usuario,
    val token: String
)


// Datos del usuario de Firebase para registro
@Serializable
data class FirebaseUserData(
    val firebaseUid: String,
    val nombre: String,
    val email: String,
    val telefono: String? = null,
    val tipoUsuarioId: Int? = null
)

// Datos para login con Firebase
@Serializable
data class FirebaseLoginData(
    val firebaseToken: String
)