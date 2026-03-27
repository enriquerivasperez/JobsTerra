package com.example.jobsterrabackend.repositories

import com.example.jobsterrabackend.config.DatabaseConfig
import com.example.jobsterrabackend.models.entities.Adjunto
import com.example.jobsterrabackend.models.entities.toEpochMilliOrNull
import com.example.jobsterrabackend.models.tables.AdjuntoTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

class AdjuntoRepository {

    suspend fun findById(id: Int): Adjunto? = DatabaseConfig.dbQuery {
        AdjuntoTable.select { AdjuntoTable.id eq id }
            .map { toAdjunto(it) }
            .singleOrNull()
    }

    suspend fun findByUsuarioIdAndTipo(usuarioId: Int, tipo: String): List<Adjunto> = DatabaseConfig.dbQuery {
        AdjuntoTable.select {
            (AdjuntoTable.usuarioId eq usuarioId) and
                    (AdjuntoTable.tipo eq mapTipoToEnum(tipo))
        }
            .map { toAdjunto(it) }
    }

    suspend fun findByReferenciaIdAndTipo(referenciaId: Int, tipo: String): List<Adjunto> = DatabaseConfig.dbQuery {
        AdjuntoTable.select {
            (AdjuntoTable.referenciaId eq referenciaId) and
                    (AdjuntoTable.tipo eq mapTipoToEnum(tipo))
        }
            .map { toAdjunto(it) }
    }

    suspend fun create(
        usuarioId: Int,
        tipo: String,
        referenciaId: Int?,
        firebaseStoragePath: String,
        nombreOriginal: String,
        tamanoBytes: Int?,
        tipoMime: String?
    ): Adjunto? = DatabaseConfig.dbQuery {
        val now = Instant.now()
        val id = AdjuntoTable.insert {
            it[AdjuntoTable.usuarioId] = usuarioId
            it[AdjuntoTable.tipo] = mapTipoToEnum(tipo)
            it[AdjuntoTable.referenciaId] = referenciaId
            it[AdjuntoTable.firebaseStoragePath] = firebaseStoragePath
            it[AdjuntoTable.nombreOriginal] = nombreOriginal
            it[AdjuntoTable.tamanoBytes] = tamanoBytes
            it[AdjuntoTable.tipoMime] = tipoMime
            it[AdjuntoTable.createdAt] = now
        } get AdjuntoTable.id

        findById(id)
    }

    suspend fun delete(id: Int): Boolean = DatabaseConfig.dbQuery {
        AdjuntoTable.deleteWhere { AdjuntoTable.id eq id } > 0
    }

    suspend fun deleteByReferenciaIdAndTipo(referenciaId: Int, tipo: String): Int = DatabaseConfig.dbQuery {
        AdjuntoTable.deleteWhere {
            (AdjuntoTable.referenciaId eq referenciaId) and
                    (AdjuntoTable.tipo eq mapTipoToEnum(tipo))
        }
    }

    private fun mapTipoToEnum(tipo: String): AdjuntoTable.Tipo {
        return when (tipo.lowercase()) {
            "perfil" -> AdjuntoTable.Tipo.perfil
            "cv" -> AdjuntoTable.Tipo.cv
            "oferta" -> AdjuntoTable.Tipo.oferta
            "mensaje" -> AdjuntoTable.Tipo.mensaje
            else -> throw IllegalArgumentException("Tipo de adjunto no válido: $tipo")
        }
    }

    private fun toAdjunto(row: ResultRow): Adjunto {
        val baseStorageUrl = "https://firebasestorage.googleapis.com/v0/b/jobsterra-8e675.appspot.com/o/"
        val storagePath = row[AdjuntoTable.firebaseStoragePath]
        // Codificar la ruta para URL y añadir token de acceso si es necesario
        val encodedPath = java.net.URLEncoder.encode(storagePath, "UTF-8")
        val url = "$baseStorageUrl$encodedPath?alt=media"

        return Adjunto(
            id = row[AdjuntoTable.id],
            usuarioId = row[AdjuntoTable.usuarioId],
            tipo = row[AdjuntoTable.tipo].name,
            referenciaId = row[AdjuntoTable.referenciaId],
            firebaseStoragePath = storagePath,
            nombreOriginal = row[AdjuntoTable.nombreOriginal],
            tamanoBytes = row[AdjuntoTable.tamanoBytes],
            tipoMime = row[AdjuntoTable.tipoMime],
            createdAt = row[AdjuntoTable.createdAt].toEpochMilliOrNull(),
            url = url
        )
    }
}