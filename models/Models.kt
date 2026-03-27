package com.example.jobsterra.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

// MODELOS PRINCIPALES

@Serializable
@Parcelize
data class Usuario(
    val id: Int,
    val nombre: String,
    val email: String,
    val telefono: String? = null,
    val biografia: String? = null,
    val fotoPerfilUrl: String? = null,
    val estado: String = "activo",
    val tipoUsuarioId: Int,
    val tipoUsuarioNombre: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) : Parcelable

@Serializable
@Parcelize
data class Oferta(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val puesto: String,
    val salarioMin: Double? = null,
    val salarioMax: Double? = null,
    val fechaPublicacion: String,
    val ubicacion: String? = null,
    val modalidad: String = "presencial",
    val estado: String = "activa",
    val tipoContratoId: Int,
    val tipoContratoNombre: String? = null,
    val empresaId: Int,
    val empresaNombre: String? = null,
    val empresaLogoUrl: String? = null,
    val esFavorita: Boolean = false,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) : Parcelable

@Serializable
@Parcelize
data class TipoUsuario(
    val id: Int,
    val nombre: String,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) : Parcelable

@Serializable
@Parcelize
data class TipoContrato(
    val id: Int,
    val nombre: String,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
) : Parcelable

@Serializable
@Parcelize
data class Chat(
    val id: Int,
    val firebaseChatId: String,
    val usuario1Id: Int,
    val usuario2Id: Int,
    val ofertaId: Int? = null,
    val ultimoMensajeFecha: Long? = null,
    val estado: String = "activo",
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    // Campos adicionales para la UI
    val otroUsuarioId: Int? = null,
    val otroUsuarioNombre: String? = null,
    val otroUsuarioFoto: String? = null,
    val ofertaTitulo: String? = null
) : Parcelable

// MODELOS PARA REQUESTS

@Serializable
data class UsuarioCreate(
    val nombre: String,
    val email: String,
    val password: String,
    val telefono: String? = null,
    val biografia: String? = null,
    val tipoUsuarioId: Int,
    val firebaseUid: String
)

@Serializable
data class UsuarioLogin(
    val email: String,
    val password: String
)

@Serializable
data class OfertaCreate(
    val titulo: String,
    val descripcion: String,
    val puesto: String,
    val salarioMin: Double? = null,
    val salarioMax: Double? = null,
    val fechaPublicacion: String? = null,
    val ubicacion: String? = null,
    val modalidad: String = "presencial",
    val tipoContratoId: Int,
    val empresaId: Int
)

@Serializable
data class ChatCreate(
    val usuario2Id: Int,
    val ofertaId: Int? = null,
    val mensajeInicial: String? = null
)

// MODELOS PARA RESPONSES

@Serializable
data class LoginResponse(
    val usuario: Usuario,
    val token: String
)

@Serializable
data class PaginaOfertas(
    val ofertas: List<Oferta>,
    val total: Int,
    val pagina: Int,
    val elementosPorPagina: Int,
    val totalPaginas: Int
)

@Serializable
data class PaginaChats(
    val chats: List<Chat>,
    val total: Int,
    val pagina: Int,
    val elementosPorPagina: Int,
    val totalPaginas: Int
)

@Serializable
data class OfertaFiltro(
    val texto: String? = null,
    val ubicacion: String? = null,
    val modalidad: String? = null,
    val tipoContratoId: Int? = null,
    val empresaId: Int? = null,
    val salarioMinimo: Double? = null,
    val fechaDesde: String? = null,
    val estado: String = "activa",
    val ordenarPor: String = "fechaPublicacion",
    val ordenAscendente: Boolean = false,
    val pagina: Int = 1,
    val elementosPorPagina: Int = 10
)

// MODELOS DE ERROR

@Serializable
data class ErrorResponse(
    val status: Int,
    val message: String,
    val error: String? = null
)

// ESTADOS DE UI

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

// MODELOS PARA AUTENTICACIÓN CON FIREBASE

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val nombre: String,
    val apellido: String,
    val telefono: String? = null,
    val tipoUsuarioId: Int = 1
)

// Alias para mantener compatibilidad
typealias User = Usuario
typealias LoginRequest = UsuarioLogin

// ESTADOS DE AUTENTICACIÓN

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: Usuario) : AuthState()
    data class Error(val message: String) : AuthState()
}

// MODELO ADICIONAL PARA REGISTRO
@Serializable
data class UsuarioUpdate(
    val nombre: String? = null,
    val telefono: String? = null,
    val biografia: String? = null,
    val tipoUsuarioId: Int? = null
)

@Serializable
data class CambioPassword(
    val passwordActual: String,
    val passwordNueva: String,
    val confirmarPassword: String
)

// FUNCIONES DE CONVERSIÓN PARA COMPATIBILIDAD CON UI

// Modelo temporal para compatibilidad con PantallaHome
data class OfertaItem(
    val id: Int,
    val titulo: String,
    val empresa: String,
    val ubicacion: String,
    val salario: String,
    val tipoContrato: String,
    val modalidad: String,
    val fechaPublicacion: String,
    val descripcion: String,
    val esFavorita: Boolean = false
)

// Modelo para la solicitud de actualización de usuario
@Serializable
data class UsuarioUpdateRequest(
    val nombre: String? = null,
    val telefono: String? = null,
    val biografia: String? = null
)

// Modelo para la respuesta de favoritos paginada
@Serializable
data class PaginaFavoritos(
    val favoritos: List<FavoritoItem>,
    val total: Int,
    val pagina: Int,
    val elementosPorPagina: Int,
    val totalPaginas: Int
)

// Modelo para un item favorito (coincide con tu backend)
@Serializable
data class FavoritoItem(
    val id: Int,
    val usuarioId: Int,
    val ofertaId: Int,
    val fecha: Long,
    val oferta: Oferta? = null,
    val createdAt: Long? = null
)

// Modelo para estadísticas del usuario (para el ViewModel)
data class EstadisticasUsuario(
    val ofertasFavoritas: Int = 0,
    val aplicacionesEnviadas: Int = 0
)

// Función para convertir Oferta (de API) a OfertaItem (para UI)
fun Oferta.toOfertaItem(): OfertaItem {
    return OfertaItem(
        id = this.id,
        titulo = this.titulo,
        empresa = this.empresaNombre ?: "Empresa no especificada",
        ubicacion = this.ubicacion ?: "Ubicación no especificada",
        salario = when {
            this.salarioMin != null && this.salarioMax != null ->
                "${this.salarioMin!!.toInt()}-${this.salarioMax!!.toInt()}€"
            this.salarioMin != null ->
                "Desde ${this.salarioMin!!.toInt()}€"
            else -> "A convenir"
        },
        tipoContrato = this.tipoContratoNombre ?: "Tipo no especificado",
        modalidad = this.modalidad,
        fechaPublicacion = formatearFecha(this.fechaPublicacion),
        descripcion = this.descripcion,
        esFavorita = this.esFavorita
    )
}

// Función para formatear fechas de forma más amigable
private fun formatearFecha(fecha: String): String {
    return try {
        "Hace pocos días"
    } catch (e: Exception) {
        "Fecha no disponible"
    }
}

// Función para convertir lista de ofertas
fun List<Oferta>.toOfertaItems(): List<OfertaItem> {
    return this.map { it.toOfertaItem() }
}

// Constantes actualizadas
object Constants {
    const val BASE_URL = "http://212.227.235.78:8081"

    const val PREF_USER_TOKEN = "user_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_EMAIL = "user_email"
    const val PREF_USER_NAME = "user_name"
}