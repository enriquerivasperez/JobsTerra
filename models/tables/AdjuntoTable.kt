package com.example.jobsterrabackend.models.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object AdjuntoTable : Table("Adjunto") {
    val id = integer("id").autoIncrement()
    val usuarioId = integer("usuario_id").references(UsuarioTable.id)
    val tipo = enumerationByName("tipo", 20, Tipo::class)
    val referenciaId = integer("referencia_id").nullable()
    val firebaseStoragePath = varchar("firebase_storage_path", 255)
    val nombreOriginal = varchar("nombre_original", 255)
    val tamanoBytes = integer("tamano_bytes").nullable()
    val tipoMime = varchar("tipo_mime", 100).nullable()
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)

    enum class Tipo {
        perfil, cv, oferta, mensaje
    }
}