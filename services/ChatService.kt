package com.example.jobsterrabackend.services

import com.example.jobsterrabackend.models.entities.*
import com.example.jobsterrabackend.repositories.ChatRepository
import com.example.jobsterrabackend.repositories.OfertaRepository
import com.example.jobsterrabackend.repositories.UsuarioRepository
import java.time.Instant

class ChatService(
    private val repository: ChatRepository,
    private val usuarioRepository: UsuarioRepository,
    private val ofertaRepository: OfertaRepository
) {

    suspend fun getChatById(id: Int): Chat? {
        return repository.findById(id)
    }

    suspend fun getChatsByUsuarioId(usuarioId: Int, pagina: Int = 1, elementosPorPagina: Int = 20): PaginaChats {
        return repository.findByUsuarioId(usuarioId, pagina, elementosPorPagina)
    }

    suspend fun createChat(usuarioId: Int, chatCreate: ChatCreate): Chat? {
        // Verificar que el usuario existe
        val usuario1 = usuarioRepository.findById(usuarioId)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        // Verificar que el otro usuario existe
        val usuario2 = usuarioRepository.findById(chatCreate.usuario2Id)
            ?: throw IllegalArgumentException("Destinatario no encontrado")

        // Verificar que no se esté intentando crear un chat consigo mismo
        if (usuarioId == chatCreate.usuario2Id) {
            throw IllegalArgumentException("No puedes iniciar un chat contigo mismo")
        }

        // Verificar la oferta si se proporciona
        if (chatCreate.ofertaId != null) {
            val oferta = ofertaRepository.findById(chatCreate.ofertaId)
                ?: throw IllegalArgumentException("Oferta no encontrada")

            // Verificar que la oferta esté activa
            if (oferta.estado.lowercase() != "activa") {
                throw IllegalArgumentException("No se puede iniciar un chat para una oferta que no está activa")
            }

            // Verificar que uno de los usuarios sea el dueño de la oferta
            if (oferta.empresaId != usuarioId && oferta.empresaId != chatCreate.usuario2Id) {
                throw IllegalArgumentException("Al menos uno de los usuarios debe ser el dueño de la oferta")
            }
        }

        // Verificar si ya existe un chat entre estos usuarios (para esta oferta)
        val existingChat = repository.findByUsuarios(usuarioId, chatCreate.usuario2Id, chatCreate.ofertaId)
        if (existingChat != null) {
            return existingChat
        }

        // Crear el chat
        val chat = repository.create(usuarioId, chatCreate.usuario2Id, chatCreate.ofertaId)

        // Aquí normalmente enviaríamos el primer mensaje a Firebase si se proporciona
        // Por ahora, lo dejamos comentado hasta implementar Firebase
        /*
        if (!chatCreate.mensajeInicial.isNullOrBlank() && chat != null) {
            // Enviar mensaje inicial a Firebase
            // firebaseService.sendMessage(chat.firebaseChatId, usuarioId, chatCreate.mensajeInicial)

            // Actualizar la fecha del último mensaje
            repository.updateUltimoMensaje(chat.id, Instant.now())
        }
        */

        return chat
    }

    suspend fun updateChatEstado(id: Int, chatUpdate: ChatUpdate, usuarioId: Int): Boolean {
        // Verificar que el chat existe
        val chat = repository.findById(id)
            ?: throw IllegalArgumentException("Chat no encontrado")

        // Verificar que el usuario es participante del chat
        if (chat.usuario1Id != usuarioId && chat.usuario2Id != usuarioId) {
            throw IllegalArgumentException("No tienes permisos para actualizar este chat")
        }

        // Verificar que el estado es válido
        if (chatUpdate.estado == null) {
            throw IllegalArgumentException("Debe proporcionar un estado")
        }

        val estadoValido = listOf("activo", "archivado").contains(chatUpdate.estado.lowercase())
        if (!estadoValido) {
            throw IllegalArgumentException("Estado inválido")
        }

        return repository.updateEstado(id, chatUpdate.estado)
    }

    suspend fun deleteChat(id: Int, usuarioId: Int): Boolean {
        // Verificar que el chat existe
        val chat = repository.findById(id)
            ?: throw IllegalArgumentException("Chat no encontrado")

        // Verificar que el usuario es participante del chat
        if (chat.usuario1Id != usuarioId && chat.usuario2Id != usuarioId) {
            throw IllegalArgumentException("No tienes permisos para eliminar este chat")
        }

        // Aquí normalmente eliminaríamos los mensajes de Firebase
        // Por ahora, lo dejamos comentado hasta implementar Firebase
        /*
        // Eliminar mensajes de Firebase
        firebaseService.deleteChat(chat.firebaseChatId)
        */

        return repository.delete(id)
    }

    suspend fun sendMessage(usuarioId: Int, mensajeCreate: MensajeCreate): Mensaje? {
        // Verificar que el chat existe
        val chat = repository.findById(mensajeCreate.chatId)
            ?: throw IllegalArgumentException("Chat no encontrado")

        // Verificar que el usuario es participante del chat
        if (chat.usuario1Id != usuarioId && chat.usuario2Id != usuarioId) {
            throw IllegalArgumentException("No tienes permisos para enviar mensajes en este chat")
        }

        // Verificar que el mensaje no está vacío
        if (mensajeCreate.texto.isBlank() && mensajeCreate.adjuntos.isEmpty()) {
            throw IllegalArgumentException("El mensaje no puede estar vacío")
        }

        // Aquí normalmente enviaríamos el mensaje a Firebase
        // Por ahora, lo dejamos comentado hasta implementar Firebase
        /*
        // Enviar mensaje a Firebase
        val messageId = firebaseService.sendMessage(
            chat.firebaseChatId,
            usuarioId,
            mensajeCreate.texto,
            mensajeCreate.adjuntos
        )
        */

        // Actualizar la fecha del último mensaje
        val now = Instant.now()
        repository.updateUltimoMensaje(chat.id, now)

        // Crear un objeto Mensaje temporal
        // En una implementación real, obtendríamos el ID real de Firebase
        return Mensaje(
            id = "temp_" + System.currentTimeMillis(), // En realidad, esto vendría de Firebase
            chatId = chat.id,
            emisorId = usuarioId,
            texto = mensajeCreate.texto,
            fechaEnvio = now.toEpochMilli(),
            leido = false,
            adjuntos = mensajeCreate.adjuntos
        )
    }

    suspend fun getMessages(
        chatId: Int,
        usuarioId: Int,
        pagina: Int = 1,
        elementosPorPagina: Int = 50
    ): PaginaMensajes {
        // Verificar que el chat existe
        val chat = repository.findById(chatId)
            ?: throw IllegalArgumentException("Chat no encontrado")

        // Verificar que el usuario es participante del chat
        if (chat.usuario1Id != usuarioId && chat.usuario2Id != usuarioId) {
            throw IllegalArgumentException("No tienes permisos para ver los mensajes de este chat")
        }

        // Aquí normalmente obtendríamos los mensajes de Firebase
        // Por ahora, devolvemos una lista vacía hasta implementar Firebase
        /*
        // Obtener mensajes de Firebase
        val messages = firebaseService.getChatMessages(
            chat.firebaseChatId,
            pagina,
            elementosPorPagina
        )
        */

        // Lista temporal vacía
        val messages = emptyList<Mensaje>()

        return PaginaMensajes(
            mensajes = messages,
            total = 0,
            pagina = pagina,
            elementosPorPagina = elementosPorPagina,
            totalPaginas = 0
        )
    }

    suspend fun markMessagesAsRead(chatId: Int, usuarioId: Int): Boolean {
        // Verificar que el chat existe
        val chat = repository.findById(chatId)
            ?: throw IllegalArgumentException("Chat no encontrado")

        // Verificar que el usuario es participante del chat
        if (chat.usuario1Id != usuarioId && chat.usuario2Id != usuarioId) {
            throw IllegalArgumentException("No tienes permisos para marcar mensajes como leídos en este chat")
        }

        // Aquí normalmente marcaríamos los mensajes como leídos en Firebase
        // Por ahora, lo dejamos comentado hasta implementar Firebase
        /*
        // Marcar mensajes como leídos en Firebase
        return firebaseService.markMessagesAsRead(
            chat.firebaseChatId,
            usuarioId
        )
        */

        // Devolver true temporalmente
        return true
    }
}