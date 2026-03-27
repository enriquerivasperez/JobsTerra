package com.example.jobsterrabackend.models.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object UsuarioTable : Table("Usuario") {
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val email = varchar("email", 100)
    val firebaseUid = varchar("firebase_uid", 128).nullable().uniqueIndex()
    val passwordHash = varchar("password_hash", 255).nullable()
    val telefono = varchar("telefono", 20).nullable()
    val biografia = text("biografia").nullable()
    val fotoPerfilUrl = varchar("foto_perfil_url", 255).nullable()
    val tipoUsuarioId = integer("tipoUsuario_id").references(TipoUsuarioTable.id)
    val estado = enumerationByName("estado", 20, Estado::class)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    enum class Estado {
        activo, inactivo, suspendido
    }
}