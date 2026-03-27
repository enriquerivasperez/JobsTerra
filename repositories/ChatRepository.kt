package com.example.jobsterrabackend.repositories

import com.example.jobsterrabackend.config.DatabaseConfig
import com.example.jobsterrabackend.models.entities.Chat
import com.example.jobsterrabackend.models.entities.PaginaChats
import com.example.jobsterrabackend.models.entities.toEpochMilliOrNull
import com.example.jobsterrabackend.models.tables.ChatTable
import com.example.jobsterrabackend.models.tables.OfertaTable
import com.example.jobsterrabackend.models.tables.UsuarioTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.*

class ChatRepository {

    suspend fun findById(id: Int): Chat? = DatabaseConfig.dbQuery {
        ChatTable.select { ChatTable.id eq id }
            .map { toChat(it) }
            .singleOrNull()
    }

    suspend fun findByFirebaseChatId(firebaseChatId: String): Chat? = DatabaseConfig.dbQuery {
        ChatTable.select { ChatTable.firebaseChatId eq firebaseChatId }
            .map { toChat(it) }
            .singleOrNull()
    }

    suspend fun findByUsuarioId(
        usuarioId: Int,
        pagina: Int = 1,
        elementosPorPagina: Int = 20
    ): PaginaChats = DatabaseConfig.dbQuery {
        // Consulta para chats donde el usuario es uno de los participantes
        val query = (ChatTable
            .leftJoin(OfertaTable, { ChatTable.ofertaId }, { OfertaTable.id })
            .leftJoin(UsuarioTable, { ChatTable.usuario1Id }, { UsuarioTable.id }))
            .select { (ChatTable.usuario1Id eq usuarioId) or (ChatTable.usuario2Id eq usuarioId) }
            .orderBy(ChatTable.ultimoMensajeFecha to SortOrder.DESC_NULLS_LAST)

        // Contar el total de resultados
        val total = query.count()

        // Aplicar paginación
        val offset = (pagina - 1) * elementosPorPagina
        val paginatedQuery = query.limit(elementosPorPagina, offset.toLong())

        // Obtener los resultados y mapearlos a Chat
        val chats = paginatedQuery.map { row ->
            val chat = toChat(row)

            // Determinar quién es el "otro usuario" para mostrar en la UI
            val otroUsuarioId = if (chat.usuario1Id == usuarioId) chat.usuario2Id else chat.usuario1Id

            // Obtener los datos del otro usuario
            val otroUsuario = UsuarioTable.select { UsuarioTable.id eq otroUsuarioId }
                .singleOrNull()

            // Obtener el título de la oferta si existe
            val ofertaTitulo = if (chat.ofertaId != null) {
                OfertaTable.select { OfertaTable.id eq chat.ofertaId }
                    .singleOrNull()?.get(OfertaTable.titulo)
            } else null

            // Devolver el chat con información adicional
            chat.copy(
                otroUsuarioId = otroUsuarioId,
                otroUsuarioNombre = otroUsuario?.get(UsuarioTable.nombre),
                otroUsuarioFoto = otroUsuario?.get(UsuarioTable.fotoPerfilUrl),
                ofertaTitulo = ofertaTitulo
            )
        }

        // Calcular el total de páginas
        val totalPaginas = (total + elementosPorPagina - 1) / elementosPorPagina

        PaginaChats(
            chats = chats,
            total = total.toInt(),
            pagina = pagina,
            elementosPorPagina = elementosPorPagina,
            totalPaginas = totalPaginas.toInt()
        )
    }

    suspend fun findByUsuarios(usuario1Id: Int, usuario2Id: Int, ofertaId: Int? = null): Chat? = DatabaseConfig.dbQuery {
        // Buscar chat existente entre estos usuarios para esta oferta
        val query = if (ofertaId != null) {
            ChatTable.select {
                (((ChatTable.usuario1Id eq usuario1Id) and (ChatTable.usuario2Id eq usuario2Id)) or
                        ((ChatTable.usuario1Id eq usuario2Id) and (ChatTable.usuario2Id eq usuario1Id))) and
                        (ChatTable.ofertaId eq ofertaId)
            }
        } else {
            ChatTable.select {
                ((ChatTable.usuario1Id eq usuario1Id) and (ChatTable.usuario2Id eq usuario2Id)) or
                        ((ChatTable.usuario1Id eq usuario2Id) and (ChatTable.usuario2Id eq usuario1Id))
            }
        }

        query.map { toChat(it) }.singleOrNull()
    }

    suspend fun create(
        usuario1Id: Int,
        usuario2Id: Int,
        ofertaId: Int? = null
    ): Chat? = DatabaseConfig.dbQuery {
        // Verificar si ya existe un chat entre estos usuarios para esta oferta
        val existingChat = findByUsuarios(usuario1Id, usuario2Id, ofertaId)
        if (existingChat != null) {
            return@dbQuery existingChat
        }

        // Generar un ID único para Firebase
        val firebaseChatId = UUID.randomUUID().toString()

        val now = Instant.now()
        val id = ChatTable.insert {
            it[ChatTable.firebaseChatId] = firebaseChatId
            it[ChatTable.usuario1Id] = usuario1Id
            it[ChatTable.usuario2Id] = usuario2Id
            it[ChatTable.ofertaId] = ofertaId
            it[ChatTable.estado] = ChatTable.Estado.activo
            it[ChatTable.createdAt] = now
            it[ChatTable.updatedAt] = now
        } get ChatTable.id

        findById(id)
    }

    suspend fun updateEstado(id: Int, estado: String): Boolean = DatabaseConfig.dbQuery {
        ChatTable.update({ ChatTable.id eq id }) {
            it[ChatTable.estado] = when (estado) {
                "archivado" -> ChatTable.Estado.archivado
                else -> ChatTable.Estado.activo
            }
            it[ChatTable.updatedAt] = Instant.now()
        } > 0
    }

    suspend fun updateUltimoMensaje(id: Int, fechaMensaje: Instant): Boolean = DatabaseConfig.dbQuery {
        ChatTable.update({ ChatTable.id eq id }) {
            it[ChatTable.ultimoMensajeFecha] = fechaMensaje
            it[ChatTable.updatedAt] = Instant.now()
        } > 0
    }

    suspend fun delete(id: Int): Boolean = DatabaseConfig.dbQuery {
        ChatTable.deleteWhere { ChatTable.id eq id } > 0
    }

    private fun toChat(row: ResultRow): Chat =
        Chat(
            id = row[ChatTable.id],
            firebaseChatId = row[ChatTable.firebaseChatId],
            usuario1Id = row[ChatTable.usuario1Id],
            usuario2Id = row[ChatTable.usuario2Id],
            ofertaId = row[ChatTable.ofertaId],
            ultimoMensajeFecha = row[ChatTable.ultimoMensajeFecha]?.toEpochMilliOrNull(),
            estado = row[ChatTable.estado].name,
            createdAt = row[ChatTable.createdAt].toEpochMilliOrNull(),
            updatedAt = row[ChatTable.updatedAt].toEpochMilliOrNull()
        )
}