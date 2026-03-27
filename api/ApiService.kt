package com.example.jobsterra.data.api

import com.example.jobsterra.data.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.Serializable


class ApiService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }
    }

    private val baseUrl = Constants.BASE_URL + "/api"

    private fun getFirebaseUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    // === MÉTODOS DE USUARIOS ===
    suspend fun createUser(usuario: UsuarioCreate): Result<Usuario> {
        return try {
            val response: HttpResponse = client.post("$baseUrl/usuarios") {
                contentType(ContentType.Application.Json)
                setBody(usuario)
            }

            if (response.status.isSuccess()) {
                val usuarioCreado = response.body<Usuario>()
                Result.success(usuarioCreado)
            } else {
                // 👇 MEJORAR ESTA PARTE
                val errorBody = response.bodyAsText()
                println("🔥 Status: ${response.status}")
                println("🔥 Error body: $errorBody")
                Result.failure(Exception("Error ${response.status.value}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getUserByFirebaseUid(firebaseUid: String): Result<Usuario> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/usuarios/firebase/$firebaseUid")

            if (response.status.isSuccess()) {
                val usuario = response.body<Usuario>()
                Result.success(usuario)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // === MÉTODOS DE OFERTAS (ya existentes) ===
    suspend fun getOfertas(
        texto: String? = null,
        ubicacion: String? = null,
        modalidad: String? = null,
        pagina: Int = 1,
        elementosPorPagina: Int = 20
    ): Result<PaginaOfertas> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/ofertas") {
                texto?.takeIf { it.isNotBlank() }?.let { parameter("texto", it) }
                ubicacion?.takeIf { it.isNotBlank() }?.let { parameter("ubicacion", it) }
                modalidad?.takeIf { it.isNotBlank() && it != "Todas" }
                    ?.let { parameter("modalidad", it) }
                parameter("pagina", pagina)
                parameter("elementosPorPagina", elementosPorPagina)
                parameter("estado", "activa")
            }

            if (response.status.isSuccess()) {
                val paginaOfertas = response.body<PaginaOfertas>()
                Result.success(paginaOfertas)
            } else {
                Result.failure(Exception("Error del servidor: ${response.status.value}"))
            }

        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getOfertaById(id: Int): Result<Oferta> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/ofertas/$id")

            if (response.status.isSuccess()) {
                val oferta = response.body<Oferta>()
                Result.success(oferta)
            } else {
                Result.failure(Exception("Oferta no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    // En tu ApiService.kt, añade logs detallados a toggleFavorito:

    suspend fun toggleFavorito(ofertaId: Int): Result<Boolean> {
        return try {
            println("🌐 API: Iniciando toggleFavorito para oferta ID: $ofertaId")

            val firebaseUid = getFirebaseUid()
            println("🌐 API: Firebase UID obtenido: ${firebaseUid?.take(10)}...") // Solo primeros 10 chars por seguridad

            if (firebaseUid == null) {
                println("❌ API: Firebase UID es null")
                return Result.failure(Exception("Usuario no autenticado"))
            }

            val url = "$baseUrl/favoritos/toggle/$ofertaId"
            println("🌐 API: URL completa: $url")
            println("🌐 API: Enviando header Authorization: Bearer ${firebaseUid.take(10)}...")

            val response: HttpResponse = client.post(url) {
                header("Authorization", "Bearer $firebaseUid")
            }

            println("🌐 API: Código de respuesta: ${response.status.value}")
            println("🌐 API: Descripción: ${response.status.description}")

            val responseBody = response.bodyAsText()
            println("🌐 API: Cuerpo de respuesta: $responseBody")

            if (response.status.isSuccess()) {
                println("✅ API: Toggle favorito exitoso")
                Result.success(true)
            } else {
                println("❌ API: Error HTTP ${response.status.value}: $responseBody")
                Result.failure(Exception("Error al cambiar favorito: HTTP ${response.status.value}"))
            }
        } catch (e: Exception) {
            println("🔥 API: Excepción en toggleFavorito: ${e.message}")
            println("🔥 API: Tipo de excepción: ${e.javaClass.simpleName}")
            e.printStackTrace()
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun checkApiStatus(): Result<String> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/status")
            if (response.status.isSuccess()) {
                Result.success("API conectada")
            } else {
                Result.failure(Exception("API no disponible"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No se puede conectar: ${e.message}"))
        }
    }

    fun close() {
        client.close()
    }

    suspend fun checkFavorito(ofertaId: Int): Result<Boolean> {
        return try {
            val firebaseUid = getFirebaseUid()
                ?: return Result.success(false)

            val response: HttpResponse = client.get("$baseUrl/favoritos/check/$ofertaId") {
                header("Authorization", "Bearer $firebaseUid")
            }

            if (response.status.isSuccess()) {
                val esFavorito = response.body<Boolean>()  // ← Parsear como Boolean directo
                Result.success(esFavorito)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.success(false)
        }
    }

    // metodo que actualiza un usuario
    suspend fun actualizarUsuario(usuarioId: Int, updateData: UsuarioUpdateRequest): Result<Usuario> {
        return try {
            val firebaseUid = getFirebaseUid()
            if (firebaseUid == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }

            val response: HttpResponse = client.put("$baseUrl/usuarios/$usuarioId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $firebaseUid")
                setBody(updateData)
            }

            if (response.status.isSuccess()) {
                val usuarioActualizado = response.body<Usuario>()
                Result.success(usuarioActualizado)
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(Exception("Error ${response.status.value}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getFavoritos(): Result<PaginaFavoritos> {
        return try {
            val firebaseUid = getFirebaseUid()
            if (firebaseUid == null) {
                return Result.failure(Exception("Usuario no autenticado"))
            }

            val response: HttpResponse = client.get("$baseUrl/favoritos") {
                header("Authorization", "Bearer $firebaseUid")
                parameter("pagina", 1)
                parameter("elementosPorPagina", 1000)
            }

            if (response.status.isSuccess()) {
                val paginaFavoritos = response.body<PaginaFavoritos>()
                Result.success(paginaFavoritos)
            } else {
                val responseBody = response.bodyAsText()
                Result.failure(Exception("Error al obtener favoritos: ${response.status} - $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getAllUsuarios(): Result<List<Usuario>> {
        return try {
            val response: HttpResponse = client.get("$baseUrl/usuarios")

            if (response.status.isSuccess()) {
                val usuarios = response.body<List<Usuario>>()
                Result.success(usuarios)
            } else {
                Result.failure(Exception("Error al obtener usuarios"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

}