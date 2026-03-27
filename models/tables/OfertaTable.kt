package com.example.jobsterrabackend.models.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp

object OfertaTable : Table("Oferta") {
    val id = integer("id").autoIncrement()
    val titulo = varchar("titulo", 150)
    val descripcion = text("descripcion")
    val puesto = varchar("puesto", 150)
    val salarioMin = decimal("salario_min", 10, 2).nullable()
    val salarioMax = decimal("salario_max", 10, 2).nullable()
    val fechaPublicacion = date("fecha_publicacion")
    val ubicacion = varchar("ubicacion", 100).nullable()
    val modalidad = enumerationByName("modalidad", 20, Modalidad::class).default(Modalidad.presencial)
    val estado = enumerationByName("estado", 20, Estado::class).default(Estado.activa)
    val tipoContratoId = integer("tipo_contrato_id").references(TipoContratoTable.id)
    val empresaId = integer("empresa_id").references(UsuarioTable.id)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id)

    enum class Modalidad {
        presencial, remoto, hibrido
    }

    enum class Estado {
        activa, cerrada, pausada
    }
}