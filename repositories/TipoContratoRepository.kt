package com.example.jobsterrabackend.repositories

import com.example.jobsterrabackend.config.DatabaseConfig
import com.example.jobsterrabackend.models.entities.TipoContrato
import com.example.jobsterrabackend.models.entities.toEpochMilliOrNull
import com.example.jobsterrabackend.models.tables.TipoContratoTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

class TipoContratoRepository {

    suspend fun findAll(): List<TipoContrato> = DatabaseConfig.dbQuery {
        TipoContratoTable.selectAll()
            .map { toTipoContrato(it) }
    }

    suspend fun findById(id: Int): TipoContrato? = DatabaseConfig.dbQuery {
        TipoContratoTable.select { TipoContratoTable.id eq id }
            .map { toTipoContrato(it) }
            .singleOrNull()
    }

    suspend fun create(nombre: String): TipoContrato? = DatabaseConfig.dbQuery {
        val now = Instant.now()
        val id = TipoContratoTable.insert {
            it[TipoContratoTable.nombre] = nombre
            it[createdAt] = now
            it[updatedAt] = now
        } get TipoContratoTable.id

        findById(id)
    }

    suspend fun update(id: Int, nombre: String): Boolean = DatabaseConfig.dbQuery {
        TipoContratoTable.update({ TipoContratoTable.id eq id }) {
            it[TipoContratoTable.nombre] = nombre
            it[updatedAt] = Instant.now()
        } > 0
    }

    suspend fun delete(id: Int): Boolean = DatabaseConfig.dbQuery {
        TipoContratoTable.deleteWhere { TipoContratoTable.id eq id } > 0
    }

    private fun toTipoContrato(row: ResultRow): TipoContrato =
        TipoContrato(
            id = row[TipoContratoTable.id],
            nombre = row[TipoContratoTable.nombre],
            createdAt = row[TipoContratoTable.createdAt].toEpochMilliOrNull(),
            updatedAt = row[TipoContratoTable.updatedAt].toEpochMilliOrNull()
        )
}