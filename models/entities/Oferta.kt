package com.example.jobsterrabackend.models.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Oferta(
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val puesto: String,
    val salarioMin: Double? = null, // Cambiado de BigDecimal a Double
    val salarioMax: Double? = null, // Cambiado de BigDecimal a Double
    val fechaPublicacion: String, // Formato ISO: YYYY-MM-DD
    val ubicacion: String? = null,
    val modalidad: String, // presencial, remoto, hibrido
    val estado: String, // activa, cerrada, pausada
    val tipoContratoId: Int,
    val tipoContratoNombre: String? = null, // Nombre del tipo de contrato para mostrar
    val empresaId: Int,
    val empresaNombre: String? = null, // Nombre de la empresa para mostrar
    val empresaLogoUrl: String? = null, // Logo de la empresa
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val esFavorita: Boolean = false // Campo calculado, no almacenado en BD
)

// Clase para crear una nueva oferta
@Serializable
data class OfertaCreate(
    val titulo: String,
    val descripcion: String,
    val puesto: String,
    val salarioMin: Double? = null, // Cambiado de BigDecimal a Double
    val salarioMax: Double? = null, // Cambiado de BigDecimal a Double
    val fechaPublicacion: String? = null, // Si es null, se usa la fecha actual
    val ubicacion: String? = null,
    val modalidad: String = "presencial",
    val tipoContratoId: Int,
    val empresaId: Int // Normalmente será el ID del usuario autenticado
)

// Clase para actualizar una oferta existente
@Serializable
data class OfertaUpdate(
    val titulo: String? = null,
    val descripcion: String? = null,
    val puesto: String? = null,
    val salarioMin: Double? = null, // Cambiado de BigDecimal a Double
    val salarioMax: Double? = null, // Cambiado de BigDecimal a Double
    val ubicacion: String? = null,
    val modalidad: String? = null,
    val estado: String? = null,
    val tipoContratoId: Int? = null
)

// Clase para los filtros de búsqueda de ofertas
@Serializable
data class OfertaFiltro(
    val texto: String? = null, // Búsqueda en título, descripción y puesto
    val ubicacion: String? = null,
    val modalidad: String? = null,
    val tipoContratoId: Int? = null,
    val empresaId: Int? = null,
    val salarioMinimo: Double? = null, // Cambiado de BigDecimal a Double
    val fechaDesde: String? = null, // Formato ISO: YYYY-MM-DD
    val estado: String? = "activa",
    val ordenarPor: String = "fechaPublicacion", // fechaPublicacion, titulo, empresa
    val ordenAscendente: Boolean = false,
    val pagina: Int = 1,
    val elementosPorPagina: Int = 10
)

// Clase para la paginación de resultados
@Serializable
data class PaginaOfertas(
    val ofertas: List<Oferta>,
    val total: Int,
    val pagina: Int,
    val elementosPorPagina: Int,
    val totalPaginas: Int
)