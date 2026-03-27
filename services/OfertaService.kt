package com.example.jobsterrabackend.services

import com.example.jobsterrabackend.models.entities.*
import com.example.jobsterrabackend.repositories.OfertaRepository
import com.example.jobsterrabackend.repositories.UsuarioRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class OfertaService(
    private val repository: OfertaRepository,
    private val usuarioRepository: UsuarioRepository
) {

    suspend fun getAllOfertas(): List<Oferta> {
        return repository.findAll()
    }

    suspend fun getOfertaById(id: Int): Oferta? {
        return repository.findById(id)
    }

    suspend fun getOfertasByEmpresaId(empresaId: Int): List<Oferta> {
        return repository.findByEmpresaId(empresaId)
    }

    suspend fun searchOfertas(filtro: OfertaFiltro, usuarioId: Int? = null): PaginaOfertas {
        return repository.findByFiltro(filtro, usuarioId)
    }

    suspend fun createOferta(ofertaCreate: OfertaCreate): Oferta? {
        // Validar datos
        if (ofertaCreate.titulo.isBlank() || ofertaCreate.descripcion.isBlank() || ofertaCreate.puesto.isBlank()) {
            throw IllegalArgumentException("Título, descripción y puesto son obligatorios")
        }

        // Validar que la empresa existe
        val empresa = usuarioRepository.findById(ofertaCreate.empresaId)
            ?: throw IllegalArgumentException("Empresa no encontrada")

        // Validar que la empresa es de tipo "Empresa"
        if (empresa.tipoUsuarioNombre?.lowercase() != "empresa") {
            throw IllegalArgumentException("Solo las empresas pueden publicar ofertas")
        }

        // Validar modalidad
        val modalidadValida = listOf("presencial", "remoto", "hibrido").contains(ofertaCreate.modalidad.lowercase())
        if (!modalidadValida) {
            throw IllegalArgumentException("Modalidad inválida")
        }

        // Validar salarios
        if (ofertaCreate.salarioMin != null && ofertaCreate.salarioMax != null) {
            if (ofertaCreate.salarioMin > ofertaCreate.salarioMax) {
                throw IllegalArgumentException("El salario mínimo no puede ser mayor que el máximo")
            }
        }

        // Validar y parsear la fecha de publicación
        val fechaPublicacion = if (ofertaCreate.fechaPublicacion != null) {
            try {
                LocalDate.parse(ofertaCreate.fechaPublicacion, DateTimeFormatter.ISO_DATE)
            } catch (e: DateTimeParseException) {
                throw IllegalArgumentException("Formato de fecha inválido. Use ISO_DATE (YYYY-MM-DD)")
            }
        } else {
            LocalDate.now() // Si no se proporciona fecha, usar la fecha actual
        }

        // Crear la oferta
        return repository.create(
            ofertaCreate.titulo,
            ofertaCreate.descripcion,
            ofertaCreate.puesto,
            ofertaCreate.salarioMin,
            ofertaCreate.salarioMax,
            fechaPublicacion,
            ofertaCreate.ubicacion,
            ofertaCreate.modalidad,
            ofertaCreate.tipoContratoId,
            ofertaCreate.empresaId
        )
    }

    suspend fun updateOferta(id: Int, ofertaUpdate: OfertaUpdate, usuarioId: Int): Boolean {
        // Verificar que la oferta existe
        val oferta = repository.findById(id)
            ?: throw IllegalArgumentException("Oferta no encontrada")

        // Verificar que el usuario es dueño de la oferta o un administrador
        val usuario = usuarioRepository.findById(usuarioId)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        if (oferta.empresaId != usuarioId && usuario.tipoUsuarioNombre?.lowercase() != "admin") {
            throw IllegalArgumentException("No tienes permisos para actualizar esta oferta")
        }

        // Validar modalidad si se proporciona
        ofertaUpdate.modalidad?.let { modalidad ->
            val modalidadValida = listOf("presencial", "remoto", "hibrido").contains(modalidad.lowercase())
            if (!modalidadValida) {
                throw IllegalArgumentException("Modalidad inválida")
            }
        }

        // Validar estado si se proporciona
        ofertaUpdate.estado?.let { estado ->
            val estadoValido = listOf("activa", "cerrada", "pausada").contains(estado.lowercase())
            if (!estadoValido) {
                throw IllegalArgumentException("Estado inválido")
            }
        }

        // Validar salarios
        if (ofertaUpdate.salarioMin != null && ofertaUpdate.salarioMax != null) {
            if (ofertaUpdate.salarioMin > ofertaUpdate.salarioMax) {
                throw IllegalArgumentException("El salario mínimo no puede ser mayor que el máximo")
            }
        } else if (ofertaUpdate.salarioMin != null && oferta.salarioMax != null) {
            if (ofertaUpdate.salarioMin > oferta.salarioMax) {
                throw IllegalArgumentException("El salario mínimo no puede ser mayor que el máximo")
            }
        } else if (ofertaUpdate.salarioMax != null && oferta.salarioMin != null) {
            if (oferta.salarioMin > ofertaUpdate.salarioMax) {
                throw IllegalArgumentException("El salario mínimo no puede ser mayor que el máximo")
            }
        }

        // Actualizar la oferta
        return repository.update(
            id,
            ofertaUpdate.titulo,
            ofertaUpdate.descripcion,
            ofertaUpdate.puesto,
            ofertaUpdate.salarioMin,
            ofertaUpdate.salarioMax,
            ofertaUpdate.ubicacion,
            ofertaUpdate.modalidad,
            ofertaUpdate.estado,
            ofertaUpdate.tipoContratoId
        )
    }

    suspend fun deleteOferta(id: Int, usuarioId: Int): Boolean {
        // Verificar que la oferta existe
        val oferta = repository.findById(id)
            ?: throw IllegalArgumentException("Oferta no encontrada")

        // Verificar que el usuario es dueño de la oferta o un administrador
        val usuario = usuarioRepository.findById(usuarioId)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        if (oferta.empresaId != usuarioId && usuario.tipoUsuarioNombre?.lowercase() != "admin") {
            throw IllegalArgumentException("No tienes permisos para eliminar esta oferta")
        }

        return repository.delete(id)
    }
}