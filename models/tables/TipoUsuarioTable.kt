package com.example.jobsterrabackend.models.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object TipoUsuarioTable : Table("TipoUsuario") {
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 50)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)
}