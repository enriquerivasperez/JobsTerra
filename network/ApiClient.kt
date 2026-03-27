package com.example.jobsterra.data.network

import com.example.jobsterra.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                isLenient = true
            })
        }

        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.INFO
        }
    }

    companion object {
        private const val BASE_URL = Constants.BASE_URL
    }

    // ============================================================================
    // AUTENTICACIÓN
    // ============================================================================

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = client.post("$BASE_URL/api/usuarios/login") {
                contentType(ContentType.Application.Json)
                setBody(UsuarioLogin(email, password))
            }

            if (response.status.isSuccess()) {
                val loginResponse = response.body<LoginResponse>()
                Result.success(loginResponse)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(usuarioCreate: UsuarioCreate): Result<Usuario> {
        return try {
            val response = client.post("$BASE_URL/api/usuarios/registro") {
                contentType(ContentType.Application.Json)
                setBody(usuarioCreate)
            }

            if (response.status.isSuccess()) {
                val usuario = response.body<Usuario>()
                Result.success(usuario)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================================================
    // USUARIOS
    // ============================================================================

    suspend fun getUsuario(id: Int): Result<Usuario> {
        return try {
            val response = client.get("$BASE_URL/api/usuarios/$id")

            if (response.status.isSuccess()) {
                val usuario = response.body<Usuario>()
                Result.success(usuario)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================================================
    // OFERTAS
    // ============================================================================

    suspend fun getOfertas(filtro: OfertaFiltro): Result<PaginaOfertas> {
        return try {
            val response = client.get("$BASE_URL/api/ofertas") {
                url {
                    filtro.texto?.let { parameters.append("texto", it) }
                    filtro.ubicacion?.let { parameters.append("ubicacion", it) }
                    filtro.modalidad?.let { parameters.append("modalidad", it) }
                    filtro.tipoContratoId?.let { parameters.append("tipoContratoId", it.toString()) }
                    filtro.empresaId?.let { parameters.append("empresaId", it.toString()) }
                    filtro.salarioMinimo?.let { parameters.append("salarioMinimo", it.toString()) }
                    filtro.fechaDesde?.let { parameters.append("fechaDesde", it) }
                    parameters.append("estado", filtro.estado)
                    parameters.append("ordenarPor", filtro.ordenarPor)
                    parameters.append("ordenAscendente", filtro.ordenAscendente.toString())
                    parameters.append("pagina", filtro.pagina.toString())
                    parameters.append("elementosPorPagina", filtro.elementosPorPagina.toString())
                }
            }

            if (response.status.isSuccess()) {
                val paginaOfertas = response.body<PaginaOfertas>()
                Result.success(paginaOfertas)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOferta(id: Int): Result<Oferta> {
        return try {
            val response = client.get("$BASE_URL/api/ofertas/$id")

            if (response.status.isSuccess()) {
                val oferta = response.body<Oferta>()
                Result.success(oferta)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOferta(ofertaCreate: OfertaCreate): Result<Oferta> {
        return try {
            val response = client.post("$BASE_URL/api/ofertas") {
                contentType(ContentType.Application.Json)
                setBody(ofertaCreate)
            }

            if (response.status.isSuccess()) {
                val oferta = response.body<Oferta>()
                Result.success(oferta)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================================================
    // TIPOS DE USUARIO Y CONTRATO
    // ============================================================================

    suspend fun getTiposUsuario(): Result<List<TipoUsuario>> {
        return try {
            val response = client.get("$BASE_URL/api/tipos-usuario")

            if (response.status.isSuccess()) {
                val tipos = response.body<List<TipoUsuario>>()
                Result.success(tipos)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTiposContrato(): Result<List<TipoContrato>> {
        return try {
            val response = client.get("$BASE_URL/api/tipos-contrato")

            if (response.status.isSuccess()) {
                val tipos = response.body<List<TipoContrato>>()
                Result.success(tipos)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================================================
    // FAVORITOS
    // ============================================================================

    suspend fun toggleFavorito(ofertaId: Int): Result<Map<String, Boolean>> {
        return try {
            val response = client.post("$BASE_URL/api/favoritos/toggle/$ofertaId")

            if (response.status.isSuccess()) {
                val result = response.body<Map<String, Boolean>>()
                Result.success(result)
            } else {
                val error = response.body<ErrorResponse>()
                Result.failure(Exception(error.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFavorito(ofertaId: Int): Result<Boolean> {
        return try {
            val response = client.get("$BASE_URL/api/favoritos/check/$ofertaId")

            if (response.status.isSuccess()) {
                val result = response.body<Map<String, Boolean>>()
                Result.success(result["esFavorito"] ?: false)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============================================================================
    // UTILITARIOS
    // ============================================================================

    suspend fun getStatus(): Result<String> {
        return try {
            val response = client.get("$BASE_URL/api/status")

            if (response.status.isSuccess()) {
                val status = response.bodyAsText()
                Result.success(status)
            } else {
                Result.failure(Exception("Server not available"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        client.close()
    }
}