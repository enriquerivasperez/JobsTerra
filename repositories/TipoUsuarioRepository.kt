package com.example.jobsterrabackend.repositories

import com.example.jobsterrabackend.config.DatabaseConfig
import com.example.jobsterrabackend.models.entities.TipoUsuario
import com.example.jobsterrabackend.models.entities.toEpochMilliOrNull
import com.example.jobsterrabackend.models.tables.TipoUsuarioTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

class TipoUsuarioRepository {

    suspend fun findAll(): List<TipoUsuario> = DatabaseConfig.dbQuery {
        TipoUsuarioTable.selectAll()
            .map { toTipoUsuario(it) }
    }

    suspend fun findById(id: Int): TipoUsuario? = DatabaseConfig.dbQuery {
        TipoUsuarioTable.select { TipoUsuarioTable.id eq id }
            .map { toTipoUsuario(it) }
            .singleOrNull()
    }

    suspend fun create(nombre: String): TipoUsuario? = DatabaseConfig.dbQuery {
        val now = Instant.now()
        val id = TipoUsuarioTable.insert {
            it[TipoUsuarioTable.nombre] = nombre
            it[createdAt] = now
            it[updatedAt] = now
        } get TipoUsuarioTable.id

        findById(id)
    }

    suspend fun update(id: Int, nombre: String): Boolean = DatabaseConfig.dbQuery {
        TipoUsuarioTable.update({ TipoUsuarioTable.id eq id }) {
            it[TipoUsuarioTable.nombre] = nombre
            it[updatedAt] = Instant.now()
        } > 0
    }

    suspend fun delete(id: Int): Boolean = DatabaseConfig.dbQuery {
        TipoUsuarioTable.deleteWhere { TipoUsuarioTable.id eq id } > 0
    }

    private fun toTipoUsuario(row: ResultRow): TipoUsuario =
        TipoUsuario(
            id = row[TipoUsuarioTable.id],
            nombre = row[TipoUsuarioTable.nombre],
            createdAt = row[TipoUsuarioTable.createdAt].toEpochMilliOrNull(),
            updatedAt = row[TipoUsuarioTable.updatedAt].toEpochMilliOrNull()
        )
}