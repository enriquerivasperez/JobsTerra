package com.example.jobsterrabackend.models.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object FavoritoTable : Table("Favorito") {
    val id = integer("id").autoIncrement()
    val usuarioId = integer("usuario_id").references(UsuarioTable.id)
    val ofertaId = integer("oferta_id").references(OfertaTable.id)
    val fecha = timestamp("fecha")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)

    // Definimos un índice único para evitar duplicados
    init {
        uniqueIndex("unique_favorito", usuarioId, ofertaId)
    }
}