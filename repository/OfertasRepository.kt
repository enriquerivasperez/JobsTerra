package com.example.jobsterra.data.repository

import com.example.jobsterra.data.api.ApiService
import com.example.jobsterra.data.models.*

class OfertasRepository(
    private val apiService: ApiService = ApiService()
) {

    // Obtener ofertas con filtros desde tu API MySQL
    suspend fun obtenerOfertas(
        texto: String? = null,
        ubicacion: String? = null,
        modalidad: String? = null,
        pagina: Int = 1,
        elementosPorPagina: Int = 20
    ): Result<List<Oferta>> {
        return try {
            val result = apiService.getOfertas(
                texto = texto,
                ubicacion = ubicacion,
                modalidad = modalidad,
                pagina = pagina,
                elementosPorPagina = elementosPorPagina
            )

            if (result.isSuccess) {
                val paginaOfertas = result.getOrThrow()
                Result.success(paginaOfertas.ofertas)
            } else {
                result.map { emptyList<Oferta>() }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtener oferta por ID
    suspend fun obtenerOfertaPorId(id: Int): Result<Oferta> {
        return apiService.getOfertaById(id)
    }

    // Buscar ofertas (alias para filtrar)
    suspend fun buscarOfertas(query: String): Result<List<Oferta>> {
        return obtenerOfertas(texto = query.takeIf { it.isNotBlank() })
    }

    // Alternar favorito
    suspend fun toggleFavorito(ofertaId: Int): Result<Boolean> {
        return try {

            val result = apiService.toggleFavorito(ofertaId)


            if (result.isFailure) {
                val error = result.exceptionOrNull()
            }

            result
        } catch (e: Exception) {
            println("Excepción: ${e.message}")
            Result.failure(e)
        }
    }





    // Limpiar recursos
    fun cleanup() {
        apiService.close()
    }

    suspend fun checkFavorito(ofertaId: Int): Result<Boolean> {
        return apiService.checkFavorito(ofertaId)
    }
}