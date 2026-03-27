package com.example.jobsterra.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobsterra.data.models.*
import com.example.jobsterra.data.repository.OfertasRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OfertasViewModel(
    private val ofertasRepository: OfertasRepository = OfertasRepository()
) : ViewModel() {

    private val _ofertas = MutableStateFlow<List<Oferta>>(emptyList())
    val ofertas: StateFlow<List<Oferta>> = _ofertas.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Control de paginación para scroll infinito
    private var paginaActual = 1
    private var hayMasPaginas = true

    init {
        cargarOfertas()
    }

    fun cargarOfertas() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            paginaActual = 1

            try {
                val result = ofertasRepository.obtenerOfertas(pagina = 1, elementosPorPagina = 10)

                if (result.isSuccess) {
                    val nuevasOfertas = result.getOrThrow()
                    _ofertas.value = nuevasOfertas
                    hayMasPaginas = nuevasOfertas.size >= 10
                    cargarFavoritos()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Error al cargar ofertas"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Método para cargar más ofertas (scroll infinito)
    fun cargarMasOfertas() {
        if (!hayMasPaginas || _isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = ofertasRepository.obtenerOfertas(
                    pagina = paginaActual + 1,
                    elementosPorPagina = 10
                )

                if (result.isSuccess) {
                    val nuevasOfertas = result.getOrThrow()
                    if (nuevasOfertas.isNotEmpty()) {
                        // Agregar nuevas ofertas a las existentes (scroll infinito)
                        _ofertas.value = _ofertas.value + nuevasOfertas
                        paginaActual += 1
                        hayMasPaginas = nuevasOfertas.size >= 10
                    } else {
                        // No hay más ofertas
                        hayMasPaginas = false
                    }
                } else {
                    _error.value = "Error al cargar más ofertas"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun buscarOfertas(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            paginaActual = 1

            try {
                val result = if (query.isBlank()) {
                    // Si no hay búsqueda, cargar ofertas iniciales (10)
                    ofertasRepository.obtenerOfertas(pagina = 1, elementosPorPagina = 10)
                } else {
                    // Si hay búsqueda, buscar entre TODAS las ofertas
                    ofertasRepository.obtenerOfertas(
                        texto = query,
                        pagina = 1,
                        elementosPorPagina = 50 // Límite alto para obtener todas
                    )
                }

                if (result.isSuccess) {
                    _ofertas.value = result.getOrThrow()
                    hayMasPaginas = query.isBlank() // Solo paginación si no hay búsqueda
                    cargarFavoritos()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Error en la búsqueda"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorito(ofertaId: Int) {
        viewModelScope.launch {
            try {
                println("🔄 VM: Iniciando toggleFavorito para oferta ID: $ofertaId")

                // Encontrar la oferta actual para ver su estado
                val ofertaActual = _ofertas.value.find { it.id == ofertaId }
                println("🔍 VM: Oferta encontrada: ${ofertaActual?.titulo}, esFavorita actual: ${ofertaActual?.esFavorita}")

                // Actualización optimista (cambiar primero en la UI)
                _ofertas.value = _ofertas.value.map { oferta ->
                    if (oferta.id == ofertaId) {
                        val nuevaOferta = oferta.copy(esFavorita = !oferta.esFavorita)
                        println("🔄 VM: Cambiando estado local de ${oferta.esFavorita} a ${nuevaOferta.esFavorita}")
                        nuevaOferta
                    } else {
                        oferta
                    }
                }

                println("🌐 VM: Llamando al repository.toggleFavorito($ofertaId)")
                val result = ofertasRepository.toggleFavorito(ofertaId)
                println("📡 VM: Resultado del repository: isSuccess = ${result.isSuccess}")

                if (result.isSuccess) {
                    println("✅ VM: Toggle favorito exitoso, limpiando errores")
                    _error.value = null
                } else {
                    println("❌ VM: Toggle favorito falló, revirtiendo cambio")
                    // Revertir si falló
                    _ofertas.value = _ofertas.value.map { oferta ->
                        if (oferta.id == ofertaId) {
                            oferta.copy(esFavorita = !oferta.esFavorita)
                        } else {
                            oferta
                        }
                    }
                    _error.value = "Error al cambiar favorito"
                }
            } catch (e: Exception) {
                println("🔥 VM: Excepción en toggleFavorito: ${e.message}")
                e.printStackTrace()

                // Revertir si hubo excepción
                _ofertas.value = _ofertas.value.map { oferta ->
                    if (oferta.id == ofertaId) {
                        oferta.copy(esFavorita = !oferta.esFavorita)
                    } else {
                        oferta
                    }
                }
                _error.value = "Error de conexión: ${e.message}"
            }
        }
    }

    // Función para obtener ofertas como OfertaItem (para PantallaHome)
    fun getOfertasAsItems(): StateFlow<List<OfertaItem>> {
        return ofertas.map { ofertas ->
            ofertas.toOfertaItems() // Usar la función de conversión del Models.kt
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun limpiarError() {
        _error.value = null
    }
    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        ofertasRepository.cleanup()
    }


    private fun cargarFavoritos() {
        viewModelScope.launch {
            try {

                val ofertasActualizadas = _ofertas.value.map { oferta ->
                    val result = ofertasRepository.checkFavorito(oferta.id)
                    val esFavorito = result.isSuccess && result.getOrThrow()


                    if (esFavorito) {
                        oferta.copy(esFavorita = true)
                    } else {
                        oferta.copy(esFavorita = false)
                    }
                }

                _ofertas.value = ofertasActualizadas

            } catch (e: Exception) {
                println("🔥 VM: Error cargando favoritos: ${e.message}")
                e.printStackTrace()
            }
        }
    }

}