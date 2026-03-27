package com.example.jobsterra.data.repository

import com.example.jobsterra.data.api.ApiService
import com.example.jobsterra.data.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val apiService = ApiService()

    // Obtener usuario actual (primero de MySQL, luego Firebase como fallback)
    suspend fun getCurrentUser(): Usuario? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            // Intentar obtener datos completos desde MySQL
            val apiResult = apiService.getUserByFirebaseUid(firebaseUser.uid)

            if (apiResult.isSuccess) {
                // Usuario encontrado en MySQL - datos completos
                apiResult.getOrNull()
            } else {
                // Usuario no encontrado en MySQL - usar datos básicos de Firebase
                Usuario(
                    id = firebaseUser.uid.hashCode(),
                    nombre = firebaseUser.displayName ?: "Usuario",
                    email = firebaseUser.email ?: "",
                    telefono = firebaseUser.phoneNumber,
                    biografia = null,
                    fotoPerfilUrl = null, // Sin photoURL por ahora
                    estado = "activo",
                    tipoUsuarioId = 1,
                    tipoUsuarioNombre = "Candidato"
                )
            }
        } else {
            null
        }
    }

    // Registrar usuario en Firebase Y MySQL
    suspend fun registrarUsuario(email: String, password: String, nombre: String): Result<Usuario> {
        return try {
            // 1. Crear usuario en Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Error al crear usuario")

            // 2. Actualizar perfil con el nombre
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(nombre)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // 3. Crear usuario en MySQL
            val usuarioCreate = UsuarioCreate(
                nombre = nombre,
                email = email,
                password = password,
                telefono = null,
                biografia = null,
                tipoUsuarioId = 1,
                firebaseUid = firebaseUser.uid
            )

            val apiResult = apiService.createUser(usuarioCreate)

            if (apiResult.isSuccess) {
                // Usuario creado exitosamente en MySQL
                Result.success(apiResult.getOrThrow())
            } else {
                // Error al crear en MySQL, pero Firebase ya existe
                // Crear usuario básico para que la app funcione
                val usuarioBasico = Usuario(
                    id = firebaseUser.uid.hashCode(),
                    nombre = nombre,
                    email = email,
                    telefono = null,
                    biografia = null,
                    fotoPerfilUrl = null,
                    estado = "activo",
                    tipoUsuarioId = 1,
                    tipoUsuarioNombre = "Candidato",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                Result.success(usuarioBasico)
            }

        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("email address is already in use") == true ->
                    "Este email ya está registrado"
                e.message?.contains("weak password") == true ->
                    "La contraseña debe tener al menos 8 caracteres"
                e.message?.contains("email address is badly formatted") == true ->
                    "Formato de email inválido"
                else -> e.message ?: "Error desconocido"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    // Iniciar sesión en Firebase y obtener datos de MySQL
    suspend fun iniciarSesion(email: String, password: String): Result<Usuario> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: throw Exception("Error al iniciar sesión")

            // Intentar obtener datos completos desde MySQL
            val apiResult = apiService.getUserByFirebaseUid(firebaseUser.uid)

            val usuario = if (apiResult.isSuccess) {
                // Usuario encontrado en MySQL con datos completos
                apiResult.getOrThrow()
            } else {
                // Usuario no encontrado en MySQL, usar datos básicos de Firebase
                Usuario(
                    id = firebaseUser.uid.hashCode(),
                    nombre = firebaseUser.displayName ?: "Usuario",
                    email = firebaseUser.email ?: "",
                    telefono = firebaseUser.phoneNumber,
                    biografia = null,
                    fotoPerfilUrl = null, // Sin photoURL por ahora
                    estado = "activo",
                    tipoUsuarioId = 1,
                    tipoUsuarioNombre = "Candidato"
                )
            }

            Result.success(usuario)

        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("user not found") == true ->
                    "Usuario no encontrado"
                e.message?.contains("wrong password") == true ->
                    "Contraseña incorrecta"
                e.message?.contains("invalid email") == true ->
                    "Email inválido"
                else -> e.message ?: "Credenciales incorrectas"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    // Cerrar sesión
    fun cerrarSesion() {
        auth.signOut()
    }

    // Obtener Firebase UID
    fun getFirebaseUid(): String? {
        return auth.currentUser?.uid
    }
}