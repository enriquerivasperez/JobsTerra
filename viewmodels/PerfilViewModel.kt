package com.example.jobsterra.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobsterra.data.models.Usuario
import com.example.jobsterra.data.models.UsuarioUpdateRequest
import com.example.jobsterra.data.models.EstadisticasUsuario
import com.example.jobsterra.data.api.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val apiService: ApiService
) : ViewModel() {

    // Estados de actualización de perfil
    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _updateError = MutableStateFlow<String?>(null)
    val updateError: StateFlow<String?> = _updateError.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    // Estados de estadísticas
    private val _estadisticas = MutableStateFlow<EstadisticasUsuario?>(null)
    val estadisticas: StateFlow<EstadisticasUsuario?> = _estadisticas.asStateFlow()

    private val _isLoadingStats = MutableStateFlow(false)
    val isLoadingStats: StateFlow<Boolean> = _isLoadingStats.asStateFlow()


    fun actualizarPerfil(
        usuario: Usuario,
        nombre: String,
        telefono: String?,
        biografia: String?,
        onSuccess: ((Usuario) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                _isUpdating.value = true
                _updateError.value = null

                // Validar datos
                if (nombre.isBlank()) {
                    _updateError.value = "El nombre no puede estar vacío"
                    return@launch
                }

                // Crear request de actualización
                val updateRequest = UsuarioUpdateRequest(
                    nombre = nombre,
                    telefono = telefono,
                    biografia = biografia
                )

                // Llamar a tu ApiService
                val resultado = apiService.actualizarUsuario(usuario.id, updateRequest)

                if (resultado.isSuccess) {
                    _updateSuccess.value = true
                    val usuarioActualizado = resultado.getOrNull()
                    usuarioActualizado?.let { onSuccess?.invoke(it) }
                } else {
                    _updateError.value = resultado.exceptionOrNull()?.message
                        ?: "Error al actualizar el perfil"
                }

            } catch (e: Exception) {
                _updateError.value = "Error: ${e.message}"
            } finally {
                _isUpdating.value = false
            }
        }
    }


    fun cargarEstadisticas(usuarioId: Int) {
        viewModelScope.launch {
            try {
                _isLoadingStats.value = true

                // Obtener favoritos usando  ApiService
                val favoritosResult = apiService.getFavoritos()
                val ofertasFavoritas = if (favoritosResult.isSuccess) {
                    val favoritos = favoritosResult.getOrNull()
                    favoritos?.total ?: 0
                } else {
                    0
                }


                val estadisticas = EstadisticasUsuario(
                    ofertasFavoritas = ofertasFavoritas,
                    aplicacionesEnviadas = 0
                )

                _estadisticas.value = estadisticas

            } catch (e: Exception) {
                // Si falla, mostrar valores por defecto
                _estadisticas.value = EstadisticasUsuario()
            } finally {
                _isLoadingStats.value = false
            }
        }
    }


    fun clearUpdateError() {
        _updateError.value = null
    }


    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }
}