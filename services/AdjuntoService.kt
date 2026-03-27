package com.example.jobsterrabackend.services

import com.example.jobsterrabackend.models.entities.Adjunto
import com.example.jobsterrabackend.models.entities.AdjuntoCreate
import com.example.jobsterrabackend.models.entities.AdjuntoResponse
import com.example.jobsterrabackend.repositories.AdjuntoRepository
import com.example.jobsterrabackend.repositories.UsuarioRepository
import java.util.*

class AdjuntoService(
    private val repository: AdjuntoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val firebaseService: FirebaseService = FirebaseService()
) {

    suspend fun getAdjuntoById(id: Int): Adjunto? {
        return repository.findById(id)
    }

    suspend fun getAdjuntosByUsuarioIdAndTipo(usuarioId: Int, tipo: String): List<Adjunto> {
        validarTipo(tipo)
        return repository.findByUsuarioIdAndTipo(usuarioId, tipo)
    }

    suspend fun getAdjuntosByReferenciaIdAndTipo(referenciaId: Int, tipo: String): List<Adjunto> {
        validarTipo(tipo)
        return repository.findByReferenciaIdAndTipo(referenciaId, tipo)
    }

    suspend fun createAdjunto(
        usuarioId: Int,
        adjuntoCreate: AdjuntoCreate,
        fileBytes: ByteArray,
        fileName: String
    ): AdjuntoResponse {
        // Verificar que el usuario existe
        val usuario = usuarioRepository.findById(usuarioId)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        // Validar tipo
        validarTipo(adjuntoCreate.tipo)

        // Validar tamaño del archivo (máximo 10MB)
        val maxFileSizeBytes = 10 * 1024 * 1024 // 10MB
        if (fileBytes.size > maxFileSizeBytes) {
            throw IllegalArgumentException("El archivo es demasiado grande. Máximo 10MB")
        }

        // Validar tipo MIME si está disponible
        adjuntoCreate.tipoMime?.let { tipoMime ->
            val tiposMimePermitidos = listOf(
                "image/jpeg", "image/png", "image/gif", "application/pdf",
                "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain"
            )

            if (!tiposMimePermitidos.contains(tipoMime.lowercase())) {
                throw IllegalArgumentException("Tipo de archivo no permitido")
            }
        }

        // Generar un nombre único para evitar colisiones
        val uniqueFileName = "${UUID.randomUUID()}_${adjuntoCreate.nombreOriginal}"

        // Determinar la ruta en Firebase Storage
        val storagePath = when (adjuntoCreate.tipo) {
            "perfil" -> "usuarios/$usuarioId/perfil/$uniqueFileName"
            "cv" -> "usuarios/$usuarioId/cv/$uniqueFileName"
            "oferta" -> {
                adjuntoCreate.referenciaId ?: throw IllegalArgumentException("Referencia ID es obligatorio para adjuntos de tipo 'oferta'")
                "ofertas/${adjuntoCreate.referenciaId}/$uniqueFileName"
            }
            "mensaje" -> {
                adjuntoCreate.referenciaId ?: throw IllegalArgumentException("Referencia ID es obligatorio para adjuntos de tipo 'mensaje'")
                "chats/${adjuntoCreate.referenciaId}/adjuntos/$uniqueFileName"
            }
            else -> throw IllegalArgumentException("Tipo de adjunto no válido")
        }

        // Subir archivo a Firebase Storage
        val url = firebaseService.uploadFile(storagePath, fileBytes, adjuntoCreate.tipoMime)

        // Guardar referencia en la base de datos
        val adjunto = repository.create(
            usuarioId,
            adjuntoCreate.tipo,
            adjuntoCreate.referenciaId,
            storagePath,
            adjuntoCreate.nombreOriginal,
            fileBytes.size,
            adjuntoCreate.tipoMime
        )

        if (adjunto == null) {
            // Si falla la BD, intentar eliminar el archivo de Storage
            firebaseService.deleteFile(storagePath)
            throw IllegalStateException("Error al guardar referencia del adjunto en la base de datos")
        }

        return AdjuntoResponse(
            id = adjunto.id,
            url = url,
            nombreOriginal = adjunto.nombreOriginal,
            tipoMime = adjunto.tipoMime
        )
    }

    suspend fun deleteAdjunto(id: Int, usuarioId: Int): Boolean {
        // Verificar que el adjunto existe
        val adjunto = repository.findById(id)
            ?: throw IllegalArgumentException("Adjunto no encontrado")

        // Verificar permisos
        if (adjunto.usuarioId != usuarioId) {
            val usuario = usuarioRepository.findById(usuarioId)
                ?: throw IllegalArgumentException("Usuario no encontrado")

            if (usuario.tipoUsuarioNombre?.lowercase() != "admin") {
                throw IllegalArgumentException("No tienes permisos para eliminar este adjunto")
            }
        }

        // Eliminar archivo de Firebase Storage
        firebaseService.deleteFile(adjunto.firebaseStoragePath)

        // Eliminar referencia de la base de datos
        return repository.delete(id)
    }

    suspend fun deleteAdjuntosByReferenciaIdAndTipo(referenciaId: Int, tipo: String, usuarioId: Int): Int {
        validarTipo(tipo)

        // Obtener los adjuntos
        val adjuntos = repository.findByReferenciaIdAndTipo(referenciaId, tipo)

        // Verificar permisos
        val usuario = usuarioRepository.findById(usuarioId)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        val esAdmin = usuario.tipoUsuarioNombre?.lowercase() == "admin"

        if (!esAdmin) {
            val noTieneDerecho = adjuntos.any { it.usuarioId != usuarioId }
            if (noTieneDerecho) {
                throw IllegalArgumentException("No tienes permisos para eliminar algunos de estos adjuntos")
            }
        }

        // Eliminar archivos de Firebase Storage
        adjuntos.forEach { adjunto ->
            firebaseService.deleteFile(adjunto.firebaseStoragePath)
        }

        // Eliminar referencias de la base de datos
        return repository.deleteByReferenciaIdAndTipo(referenciaId, tipo)
    }

    private fun validarTipo(tipo: String) {
        val tiposValidos = listOf("perfil", "cv", "oferta", "mensaje")
        if (!tiposValidos.contains(tipo.lowercase())) {
            throw IllegalArgumentException("Tipo de adjunto no válido")
        }
    }
}