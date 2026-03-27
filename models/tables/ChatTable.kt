package com.example.jobsterrabackend.models.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object ChatTable : Table("Chat") {
    val id = integer("id").autoIncrement()
    val firebaseChatId = varchar("firebase_chat_id", 128)
    val usuario1Id = integer("usuario1_id").references(UsuarioTable.id)
    val usuario2Id = integer("usuario2_id").references(UsuarioTable.id)
    val ofertaId = integer("oferta_id").references(OfertaTable.id).nullable()
    val ultimoMensajeFecha = timestamp("ultimo_mensaje_fecha").nullable()
    val estado = enumerationByName("estado", 20, Estado::class).default(Estado.activo)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    enum class Estado {
        activo, archivado
    }

    // Índice para evitar duplicados en los chats entre las mismas personas para la misma oferta
    init {
        uniqueIndex("unique_chat", usuario1Id, usuario2Id, ofertaId)
    }
}