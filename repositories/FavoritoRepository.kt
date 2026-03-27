package com.example.jobsterrabackend.repositories

import com.example.jobsterrabackend.config.DatabaseConfig
import com.example.jobsterrabackend.models.entities.Favorito
import com.example.jobsterrabackend.models.entities.Oferta
import com.example.jobsterrabackend.models.entities.PaginaFavoritos
import com.example.jobsterrabackend.models.entities.toEpochMilliOrNull
import com.example.jobsterrabackend.models.tables.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

class FavoritoRepository {

    suspend fun findByUsuarioId(usuarioId: Int, pagina: Int = 1, elementosPorPagina: Int = 10): PaginaFavoritos = DatabaseConfig.dbQuery {
        // Seleccionar favoritos con sus ofertas asociadas
        val query = (FavoritoTable
            .innerJoin(OfertaTable)
            .innerJoin(TipoContratoTable, { OfertaTable.tipoContratoId }, { TipoContratoTable.id })
            .innerJoin(UsuarioTable, { OfertaTable.empresaId }, { UsuarioTable.id }))
            .select { FavoritoTable.usuarioId eq usuarioId }
            .orderBy(FavoritoTable.fecha to SortOrder.DESC)

        // Contar el total de resultados
        val total = query.count()

        // Aplicar paginación
        val offset = (pagina - 1) * elementosPorPagina
        val paginatedQuery = query.limit(elementosPorPagina, offset.toLong())

        // Obtener los resultados y mapearlos a Favorito con Oferta
        val favoritos = paginatedQuery.map { row ->
            val oferta = Oferta(
                id = row[OfertaTable.id],
                titulo = row[OfertaTable.titulo],
                descripcion = row[OfertaTable.descripcion],
                puesto = row[OfertaTable.puesto],
                salarioMin = row[OfertaTable.salarioMin]?.toDouble(),
                salarioMax = row[OfertaTable.salarioMax]?.toDouble(),
                fechaPublicacion = row[OfertaTable.fechaPublicacion].toString(),
                ubicacion = row[OfertaTable.ubicacion],
                modalidad = row[OfertaTable.modalidad].name,
                estado = row[OfertaTable.estado].name,
                tipoContratoId = row[OfertaTable.tipoContratoId],
                tipoContratoNombre = row[TipoContratoTable.nombre],
                empresaId = row[OfertaTable.empresaId],
                empresaNombre = row[UsuarioTable.nombre],
                empresaLogoUrl = row[UsuarioTable.fotoPerfilUrl],
                createdAt = row[OfertaTable.createdAt].toEpochMilliOrNull(),
                updatedAt = row[OfertaTable.updatedAt].toEpochMilliOrNull(),
                esFavorita = true
            )

            Favorito(
                id = row[FavoritoTable.id],
                usuarioId = row[FavoritoTable.usuarioId],
                ofertaId = row[FavoritoTable.ofertaId],
                fecha = row[FavoritoTable.fecha].toEpochMilliOrNull() ?: 0,
                oferta = oferta,
                createdAt = row[FavoritoTable.createdAt].toEpochMilliOrNull()
            )
        }

        // Calcular el total de páginas
        val totalPaginas = (total + elementosPorPagina - 1) / elementosPorPagina

        PaginaFavoritos(
            favoritos = favoritos,
            total = total.toInt(),
            pagina = pagina,
            elementosPorPagina = elementosPorPagina,
            totalPaginas = totalPaginas.toInt()
        )
    }

    suspend fun findById(id: Int): Favorito? = DatabaseConfig.dbQuery {
        FavoritoTable.select { FavoritoTable.id eq id }
            .map { toFavorito(it) }
            .singleOrNull()
    }

    suspend fun findByUsuarioIdAndOfertaId(usuarioId: Int, ofertaId: Int): Favorito? = DatabaseConfig.dbQuery {
        FavoritoTable
            .select { (FavoritoTable.usuarioId eq usuarioId) and (FavoritoTable.ofertaId eq ofertaId) }
            .map { toFavorito(it) }
            .singleOrNull()
    }

    suspend fun create(usuarioId: Int, ofertaId: Int): Favorito? = DatabaseConfig.dbQuery {
        // Verificar si ya existe un favorito para este usuario y oferta
        val existingFavorito = findByUsuarioIdAndOfertaId(usuarioId, ofertaId)
        if (existingFavorito != null) {
            return@dbQuery existingFavorito
        }

        val now = Instant.now()
        val id = FavoritoTable.insert {
            it[FavoritoTable.usuarioId] = usuarioId
            it[FavoritoTable.ofertaId] = ofertaId
            it[FavoritoTable.fecha] = now
            it[FavoritoTable.createdAt] = now
        } get FavoritoTable.id

        findById(id)
    }

    suspend fun delete(id: Int): Boolean = DatabaseConfig.dbQuery {
        FavoritoTable.deleteWhere { FavoritoTable.id eq id } > 0
    }

    suspend fun deleteByUsuarioIdAndOfertaId(usuarioId: Int, ofertaId: Int): Boolean = DatabaseConfig.dbQuery {
        FavoritoTable.deleteWhere {
            (FavoritoTable.usuarioId eq usuarioId) and (FavoritoTable.ofertaId eq ofertaId)
        } > 0
    }

    private fun toFavorito(row: ResultRow): Favorito =
        Favorito(
            id = row[FavoritoTable.id],
            usuarioId = row[FavoritoTable.usuarioId],
            ofertaId = row[FavoritoTable.ofertaId],
            fecha = row[FavoritoTable.fecha].toEpochMilliOrNull() ?: 0,
            createdAt = row[FavoritoTable.createdAt].toEpochMilliOrNull()
        )
}