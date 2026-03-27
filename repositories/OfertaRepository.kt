package com.example.jobsterrabackend.repositories

import com.example.jobsterrabackend.config.DatabaseConfig
import com.example.jobsterrabackend.models.entities.Oferta
import com.example.jobsterrabackend.models.entities.OfertaFiltro
import com.example.jobsterrabackend.models.entities.PaginaOfertas
import com.example.jobsterrabackend.models.entities.toEpochMilliOrNull
import com.example.jobsterrabackend.models.tables.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OfertaRepository {

    suspend fun findAll(): List<Oferta> = DatabaseConfig.dbQuery {
        (OfertaTable
            .innerJoin(TipoContratoTable)
            .innerJoin(UsuarioTable))
            .selectAll()
            .map { toOferta(it) }
    }

    suspend fun findById(id: Int): Oferta? = DatabaseConfig.dbQuery {
        (OfertaTable
            .innerJoin(TipoContratoTable)
            .innerJoin(UsuarioTable))
            .select { OfertaTable.id eq id }
            .map { toOferta(it) }
            .singleOrNull()
    }

    suspend fun findByEmpresaId(empresaId: Int): List<Oferta> = DatabaseConfig.dbQuery {
        (OfertaTable
            .innerJoin(TipoContratoTable)
            .innerJoin(UsuarioTable))
            .select { OfertaTable.empresaId eq empresaId }
            .map { toOferta(it) }
    }

    suspend fun create(
        titulo: String,
        descripcion: String,
        puesto: String,
        salarioMin: Double?,
        salarioMax: Double?,
        fechaPublicacion: LocalDate,
        ubicacion: String?,
        modalidad: String,
        tipoContratoId: Int,
        empresaId: Int
    ): Oferta? = DatabaseConfig.dbQuery {
        val now = Instant.now()

        val id = OfertaTable.insert {
            it[OfertaTable.titulo] = titulo
            it[OfertaTable.descripcion] = descripcion
            it[OfertaTable.puesto] = puesto
            it[OfertaTable.salarioMin] = salarioMin?.toBigDecimal()
            it[OfertaTable.salarioMax] = salarioMax?.toBigDecimal()
            it[OfertaTable.fechaPublicacion] = fechaPublicacion
            it[OfertaTable.ubicacion] = ubicacion
            it[OfertaTable.modalidad] = when (modalidad) {
                "remoto" -> OfertaTable.Modalidad.remoto
                "hibrido" -> OfertaTable.Modalidad.hibrido
                else -> OfertaTable.Modalidad.presencial
            }
            it[OfertaTable.estado] = OfertaTable.Estado.activa
            it[OfertaTable.tipoContratoId] = tipoContratoId
            it[OfertaTable.empresaId] = empresaId
            it[OfertaTable.createdAt] = now
            it[OfertaTable.updatedAt] = now
        } get OfertaTable.id

        findById(id)
    }

    suspend fun update(
        id: Int,
        titulo: String?,
        descripcion: String?,
        puesto: String?,
        salarioMin: Double?,
        salarioMax: Double?,
        ubicacion: String?,
        modalidad: String?,
        estado: String?,
        tipoContratoId: Int?
    ): Boolean = DatabaseConfig.dbQuery {
        OfertaTable.update({ OfertaTable.id eq id }) {
            titulo?.let { updateTitulo -> it[OfertaTable.titulo] = updateTitulo }
            descripcion?.let { updateDescripcion -> it[OfertaTable.descripcion] = updateDescripcion }
            puesto?.let { updatePuesto -> it[OfertaTable.puesto] = updatePuesto }
            salarioMin?.let { updateSalarioMin -> it[OfertaTable.salarioMin] = updateSalarioMin.toBigDecimal() }
            salarioMax?.let { updateSalarioMax -> it[OfertaTable.salarioMax] = updateSalarioMax.toBigDecimal() }
            ubicacion?.let { updateUbicacion -> it[OfertaTable.ubicacion] = updateUbicacion }
            modalidad?.let { updateModalidad ->
                it[OfertaTable.modalidad] = when (updateModalidad) {
                    "remoto" -> OfertaTable.Modalidad.remoto
                    "hibrido" -> OfertaTable.Modalidad.hibrido
                    else -> OfertaTable.Modalidad.presencial
                }
            }
            estado?.let { updateEstado ->
                it[OfertaTable.estado] = when (updateEstado) {
                    "cerrada" -> OfertaTable.Estado.cerrada
                    "pausada" -> OfertaTable.Estado.pausada
                    else -> OfertaTable.Estado.activa
                }
            }
            tipoContratoId?.let { updateTipoContratoId -> it[OfertaTable.tipoContratoId] = updateTipoContratoId }
            it[OfertaTable.updatedAt] = Instant.now()
        } > 0
    }

    suspend fun delete(id: Int): Boolean = DatabaseConfig.dbQuery {
        OfertaTable.deleteWhere { OfertaTable.id eq id } > 0
    }

    suspend fun findByFiltro(filtro: OfertaFiltro, usuarioId: Int? = null): PaginaOfertas = DatabaseConfig.dbQuery {
        // Preparar la query base con los joins necesarios
        var query = (OfertaTable
            .innerJoin(TipoContratoTable)
            .innerJoin(UsuarioTable))
            .selectAll()

        // Aplicar filtros
        val whereConditions = mutableListOf<Op<Boolean>>()

        // Si se especifica estado, filtrar por estado
        filtro.estado?.let { estado ->
            whereConditions.add(
                OfertaTable.estado eq when (estado) {
                    "cerrada" -> OfertaTable.Estado.cerrada
                    "pausada" -> OfertaTable.Estado.pausada
                    else -> OfertaTable.Estado.activa
                }
            )
        }

        // Búsqueda de texto en título, descripción o puesto
        filtro.texto?.let { texto ->
            whereConditions.add(
                (OfertaTable.titulo like "%$texto%") or
                        (OfertaTable.descripcion like "%$texto%") or
                        (OfertaTable.puesto like "%$texto%")
            )
        }

        // Filtro por ubicación
        filtro.ubicacion?.let { ubicacion ->
            whereConditions.add(OfertaTable.ubicacion like "%$ubicacion%")
        }

        // Filtro por modalidad
        filtro.modalidad?.let { modalidad ->
            whereConditions.add(
                OfertaTable.modalidad eq when (modalidad) {
                    "remoto" -> OfertaTable.Modalidad.remoto
                    "hibrido" -> OfertaTable.Modalidad.hibrido
                    else -> OfertaTable.Modalidad.presencial
                }
            )
        }

        // Filtro por tipo de contrato
        filtro.tipoContratoId?.let { tipoContratoId ->
            whereConditions.add(OfertaTable.tipoContratoId eq tipoContratoId)
        }

        // Filtro por empresa
        filtro.empresaId?.let { empresaId ->
            whereConditions.add(OfertaTable.empresaId eq empresaId)
        }

        // Filtro por salario mínimo
        filtro.salarioMinimo?.let { salarioMinimo ->
            whereConditions.add(OfertaTable.salarioMin greaterEq salarioMinimo.toBigDecimal())
        }

        // Filtro por fecha desde
        filtro.fechaDesde?.let { fechaDesde ->
            val date = LocalDate.parse(fechaDesde, DateTimeFormatter.ISO_DATE)
            whereConditions.add(OfertaTable.fechaPublicacion greaterEq date)
        }

        // Aplicar todas las condiciones WHERE
        if (whereConditions.isNotEmpty()) {
            val whereClause = whereConditions.reduce { acc, op -> acc and op }
            query = query.andWhere { whereClause }
        }

        // Contar el total de resultados (antes de aplicar paginación)
        val total = query.count()

        // Aplicar ordenamiento
        query = when (filtro.ordenarPor) {
            "titulo" -> if (filtro.ordenAscendente) query.orderBy(OfertaTable.titulo to SortOrder.ASC)
            else query.orderBy(OfertaTable.titulo to SortOrder.DESC)
            "empresa" -> if (filtro.ordenAscendente) query.orderBy(UsuarioTable.nombre to SortOrder.ASC)
            else query.orderBy(UsuarioTable.nombre to SortOrder.DESC)
            else -> if (filtro.ordenAscendente) query.orderBy(OfertaTable.fechaPublicacion to SortOrder.ASC)
            else query.orderBy(OfertaTable.fechaPublicacion to SortOrder.DESC)
        }

        // Aplicar paginación
        val offset = (filtro.pagina - 1) * filtro.elementosPorPagina
        query = query.limit(filtro.elementosPorPagina, offset.toLong())

        // Obtener los resultados y mapearlos a Oferta
        val ofertas = query.map { row ->
            val oferta = toOferta(row)

            // Si se proporciona un ID de usuario, verificar si la oferta es favorita
            val esFavorita = if (usuarioId != null) {
                FavoritoTable.select {
                    (FavoritoTable.usuarioId eq usuarioId) and (FavoritoTable.ofertaId eq oferta.id)
                }.count() > 0
            } else false

            oferta.copy(esFavorita = esFavorita)
        }

        // Calcular el total de páginas
        val totalPaginas = (total + filtro.elementosPorPagina - 1) / filtro.elementosPorPagina

        PaginaOfertas(
            ofertas = ofertas,
            total = total.toInt(),
            pagina = filtro.pagina,
            elementosPorPagina = filtro.elementosPorPagina,
            totalPaginas = totalPaginas.toInt()
        )
    }

    private fun toOferta(row: ResultRow): Oferta =
        Oferta(
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
            esFavorita = false // Se establece después si es necesario
        )
}