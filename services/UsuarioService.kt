package com.example.jobsterrabackend.services

import com.example.jobsterrabackend.models.entities.*
import com.example.jobsterrabackend.repositories.UsuarioRepository
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class UsuarioService(private val repository: UsuarioRepository) {

    suspend fun getAllUsuarios(): List<Usuario> {
        return repository.findAll()
    }

    suspend fun getUsuarioById(id: Int): Usuario? {
        return repository.findById(id)
    }

    suspend fun getUsuarioByEmail(email: String): Usuario? {
        return repository.findByEmail(email)
    }

    //  Método para buscar usuario por Firebase UID
    suspend fun getUsuarioByFirebaseUid(firebaseUid: String): Usuario? {
        return repository.findByFirebaseUid(firebaseUid)
    }

    suspend fun createUsuario(usuarioCreate: UsuarioCreate): Usuario? {
        try {
            // Validar datos
            if (usuarioCreate.email.isBlank() || usuarioCreate.nombre.isBlank() || usuarioCreate.password.isBlank()) {
                throw IllegalArgumentException("Email, nombre y contraseña son obligatorios")
            }

            // Verificar si el email ya existe
            val existingUser = repository.findByEmail(usuarioCreate.email)
            if (existingUser != null) {
                throw IllegalArgumentException("El email ya está en uso")
            }


            // Crear el usuario
            val result = repository.create(
                usuarioCreate.nombre,
                usuarioCreate.email,
                usuarioCreate.password,
                usuarioCreate.telefono,
                usuarioCreate.biografia,
                usuarioCreate.tipoUsuarioId,
                usuarioCreate.firebaseUid
            )

            return result

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    //  Crear usuario desde Firebase Auth
    suspend fun createUsuarioFromFirebase(firebaseUser: FirebaseUserData): Usuario? {
        // Verificar si ya existe un usuario con este Firebase UID
        val existingUser = repository.findByFirebaseUid(firebaseUser.firebaseUid)
        if (existingUser != null) {
            return existingUser // Usuario ya existe, lo devolvemos
        }

        // Verificar si el email ya está en uso por otro usuario
        val emailUser = repository.findByEmail(firebaseUser.email)
        if (emailUser != null) {
            throw IllegalArgumentException("El email ya está en uso por otro usuario")
        }

        // Crear nuevo usuario desde Firebase
        return repository.createFromFirebase(
            nombre = firebaseUser.nombre,
            email = firebaseUser.email,
            firebaseUid = firebaseUser.firebaseUid,
            telefono = firebaseUser.telefono,
            tipoUsuarioId = firebaseUser.tipoUsuarioId ?: 2 // Por defecto: candidato
        )
    }

    suspend fun updateUsuario(id: Int, usuarioUpdate: UsuarioUpdate): Boolean {
        // Validar que el usuario existe
        val existingUser = repository.findById(id)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        // Validar formato del email si se está actualizando
        if (usuarioUpdate.email != null && !isValidEmail(usuarioUpdate.email)) {
            throw IllegalArgumentException("Formato de email inválido")
        }

        return repository.update(
            id,
            usuarioUpdate.nombre,
            usuarioUpdate.email,
            usuarioUpdate.telefono,
            usuarioUpdate.biografia,
            usuarioUpdate.estado
        )
    }

    suspend fun deleteUsuario(id: Int): Boolean {
        return repository.delete(id)
    }

    suspend fun login(usuarioLogin: UsuarioLogin): LoginResponse? {
        val usuario = repository.findByEmail(usuarioLogin.email)
            ?: throw IllegalArgumentException("Email o contraseña incorrectos")

        val passwordValid = repository.verifyPassword(usuarioLogin.email, usuarioLogin.password)
        if (!passwordValid) {
            throw IllegalArgumentException("Email o contraseña incorrectos")
        }

        // Generar un token simple (en una implementación real, usaríamos JWT)
        val token = generateToken(usuario.id)

        return LoginResponse(
            usuario = usuario,
            token = token
        )
    }

    //  Login con Firebase Auth
    suspend fun loginWithFirebase(firebaseToken: String): LoginResponse? {
        // En una implementación real, aquí verificarías el token de Firebase
        // Por ahora, asumimos que el token es válido y contiene el UID

        // Extraer UID del token (esto es simplificado)
        val firebaseUid = extractFirebaseUid(firebaseToken)
            ?: throw IllegalArgumentException("Token de Firebase inválido")

        val usuario = repository.findByFirebaseUid(firebaseUid)
            ?: throw IllegalArgumentException("Usuario no encontrado con este token de Firebase")

        // Generar token de sesión
        val sessionToken = generateToken(usuario.id)

        return LoginResponse(
            usuario = usuario,
            token = sessionToken
        )
    }

    suspend fun changePassword(id: Int, cambioPassword: CambioPassword): Boolean {
        // Verificar que el usuario existe
        val usuario = repository.findById(id)
            ?: throw IllegalArgumentException("Usuario no encontrado")

        // Verificar contraseña actual
        val passwordValid = repository.verifyPassword(usuario.email, cambioPassword.passwordActual)
        if (!passwordValid) {
            throw IllegalArgumentException("Contraseña actual incorrecta")
        }

        // Validar complejidad de la nueva contraseña
        if (cambioPassword.passwordNueva.length < 8) {
            throw IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres")
        }

        // Hashear la nueva contraseña
        val newPasswordHash = BCrypt.hashpw(cambioPassword.passwordNueva, BCrypt.gensalt())

        // Actualizar la contraseña
        return repository.updatePassword(id, newPasswordHash)
    }

    // Método para validar formato de email (simple)
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        return email.matches(emailRegex.toRegex())
    }

    // Método para generar un token (simple, en una implementación real usaríamos JWT)
    private fun generateToken(userId: Int): String {
        // En una implementación real, se utilizaría una biblioteca JWT
        // Este es solo un ejemplo muy simple
        return "token_${userId}_${UUID.randomUUID()}"
    }

    //  Extraer UID de Firebase token 
    private fun extractFirebaseUid(token: String): String? {
        // En una implementación real, usarías Firebase Admin SDK para verificar el token
        // Por ahora, simulamos la extracción del UID
        return if (token.startsWith("firebase_")) {
            token.substring(9) // Remover "firebase_" prefix
        } else {
            null
        }
    }
}