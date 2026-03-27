package com.example.jobsterrabackend.models.entities

import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: Int = 0,
    val firebaseChatId: String,
    val usuario1Id: Int,
    val usuario2Id: Int,
    val ofertaId: Int? = null,
    val ultimoMensajeFecha: Long? = null,
    val estado: String = "activo", // activo, archivado
    val createdAt: Long? = null,
    val updatedAt: Long? = null,

    // Campos adicionales para mostrar información útil en la UI
    val otroUsuarioId: Int? = null, // ID del otro usuario (relativo al usuario actual)
    val otroUsuarioNombre: String? = null, // Nombre del otro usuario
    val otroUsuarioFoto: String? = null, // Foto de perfil del otro usuario
    val ofertaTitulo: String? = null, // Título de la oferta (si hay una)
    val ultimoMensaje: String? = null // Último mensaje (puede venir de Firebase)
)

// Clase para crear un nuevo chat
@Serializable
data class ChatCreate(
    val usuario2Id: Int, // El usuario1Id será el usuario autenticado
    val ofertaId: Int? = null,
    val mensajeInicial: String? = null
)

// Clase para actualizar un chat
@Serializable
data class ChatUpdate(
    val estado: String? = null // Sólo podemos actualizar el estado por ahora
)

// Clase para representar un mensaje
@Serializable
data class Mensaje(
    val id: String, // ID de Firebase
    val chatId: Int, // ID de MySQL
    val emisorId: Int,
    val texto: String,
    val fechaEnvio: Long,
    val leido: Boolean = false,
    val adjuntos: List<String> = emptyList() // URLs de adjuntos en Firebase Storage
)

// Clase para enviar un nuevo mensaje
@Serializable
data class MensajeCreate(
    val chatId: Int,
    val texto: String,
    val adjuntos: List<String> = emptyList()
)

// Clase para la paginación de chats
@Serializable
data class PaginaChats(
    val chats: List<Chat>,
    val total: Int,
    val pagina: Int,
    val elementosPorPagina: Int,
    val totalPaginas: Int
)

// Clase para la paginación de mensajes
@Serializable
data class PaginaMensajes(
    val mensajes: List<Mensaje>,
    val total: Int,
    val pagina: Int,
    val elementosPorPagina: Int,
    val totalPaginas: Int
)