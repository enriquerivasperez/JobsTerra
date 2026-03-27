package com.example.jobsterra.data.services

import android.util.Log
import com.example.jobsterra.data.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {
    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Usuario> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Por ahora creamos un usuario básico
                // Más adelante integraremos con tu backend
                val usuario = Usuario(
                    id = 1, // Temporal
                    nombre = firebaseUser.displayName ?: "Usuario",
                    email = firebaseUser.email ?: email,
                    tipoUsuarioId = 1
                )
                Result.success(usuario)
            } else {
                Result.failure(Exception("Error de autenticación"))
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("Usuario no encontrado"))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Email o contraseña incorrectos"))
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error en login", e)
            Result.failure(Exception("Error al iniciar sesión"))
        }
    }

    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        nombre: String
    ): Result<Usuario> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val usuario = Usuario(
                    id = 1, // Temporal
                    nombre = nombre,
                    email = email,
                    tipoUsuarioId = 1
                )
                Result.success(usuario)
            } else {
                Result.failure(Exception("Error al crear la cuenta"))
            }
        } catch (e: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        } catch (e: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("Ya existe una cuenta con este email"))
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error en registro", e)
            Result.failure(Exception("Error al crear la cuenta"))
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}