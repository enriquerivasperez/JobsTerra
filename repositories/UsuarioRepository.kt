package com.example.jobsterrabackend.repositories

import com.example.jobsterrabackend.config.DatabaseConfig
import com.example.jobsterrabackend.models.entities.Usuario
import com.example.jobsterrabackend.models.entities.toEpochMilliOrNull
import com.example.jobsterrabackend.models.tables.TipoUsuarioTable
import com.example.jobsterrabackend.models.tables.UsuarioTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant

class UsuarioRepository {

    suspend fun findAll(): List<Usuario> = DatabaseConfig.dbQuery {
        (UsuarioTable innerJoin TipoUsuarioTable)
            .selectAll()
            .map { toUsuario(it) }
    }

    suspend fun findById(id: Int): Usuario? = DatabaseConfig.dbQuery {
        try {
            val result = (UsuarioTable innerJoin TipoUsuarioTable)
                .select { UsuarioTable.id eq id }
                .map { toUsuario(it) }
                .singleOrNull()

            return@dbQuery result
        } catch (e: Exception) {
            e.printStackTrace()
            return@dbQuery null
        }
    }

    suspend fun findByEmail(email: String): Usuario? = DatabaseConfig.dbQuery {
        (UsuarioTable innerJoin TipoUsuarioTable)
            .select { UsuarioTable.email eq email }
            .map { toUsuario(it) }
            .singleOrNull()
    }

    suspend fun create(
        nombre: String,
        email: String,
        password: String,
        telefono: String?,
        biografia: String?,
        tipoUsuarioId: Int,
        firebaseUid: String? = null
    ): Usuario? = DatabaseConfig.dbQuery {
        try {
            // Verificar si el email ya existe
            val existingUser = UsuarioTable
                .select { UsuarioTable.email eq email }
                .singleOrNull()

            if (existingUser != null) {
                return@dbQuery null // Email ya existe
            }

            // Hashear la contraseña
            val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())

            val now = Instant.now()

            val id = UsuarioTable.insert {
                it[UsuarioTable.nombre] = nombre
                it[UsuarioTable.email] = email
                it[UsuarioTable.firebaseUid] = firebaseUid
                it[UsuarioTable.passwordHash] = passwordHash
                it[UsuarioTable.telefono] = telefono
                it[UsuarioTable.biografia] = biografia
                it[UsuarioTable.tipoUsuarioId] = tipoUsuarioId
                it[UsuarioTable.estado] = UsuarioTable.Estado.activo
                it[UsuarioTable.createdAt] = now
                it[UsuarioTable.updatedAt] = now
            } get UsuarioTable.id

            // Consulta directa para evitar problemas de timing
            val result = (UsuarioTable innerJoin TipoUsuarioTable)
                .select { UsuarioTable.id eq id }
                .map { toUsuario(it) }
                .singleOrNull()

            return@dbQuery result

        } catch (e: Exception) {
            e.printStackTrace()
            return@dbQuery null
        }
    }

    suspend fun update(
        id: Int,
        nombre: String?,
        email: String?,
        telefono: String?,
        biografia: String?,
        estado: String?
    ): Boolean = DatabaseConfig.dbQuery {
        // Verificar si el usuario existe
        val existingUser = UsuarioTable
            .select { UsuarioTable.id eq id }
            .singleOrNull() ?: return@dbQuery false

        // Si se proporciona un nuevo email, verificar que no exista ya
        if (email != null && email != existingUser[UsuarioTable.email]) {
            val existingEmail = UsuarioTable
                .select { UsuarioTable.email eq email }
                .singleOrNull()

            if (existingEmail != null) {
                return@dbQuery false // Email ya existe
            }
        }

        UsuarioTable.update({ UsuarioTable.id eq id }) {
            nombre?.let { updateNombre -> it[UsuarioTable.nombre] = updateNombre }
            email?.let { updateEmail -> it[UsuarioTable.email] = updateEmail }
            telefono?.let { updateTelefono -> it[UsuarioTable.telefono] = updateTelefono }
            biografia?.let { updateBiografia -> it[UsuarioTable.biografia] = updateBiografia }
            estado?.let { updateEstado ->
                it[UsuarioTable.estado] = when (updateEstado) {
                    "activo" -> UsuarioTable.Estado.activo
                    "inactivo" -> UsuarioTable.Estado.inactivo
                    "suspendido" -> UsuarioTable.Estado.suspendido
                    else -> existingUser[UsuarioTable.estado]
                }
            }
            it[UsuarioTable.updatedAt] = Instant.now()
        } > 0
    }

    suspend fun updatePassword(id: Int, newPasswordHash: String): Boolean = DatabaseConfig.dbQuery {
        UsuarioTable.update({ UsuarioTable.id eq id }) {
            it[passwordHash] = newPasswordHash
            it[updatedAt] = Instant.now()
        } > 0
    }

    suspend fun delete(id: Int): Boolean = DatabaseConfig.dbQuery {
        UsuarioTable.deleteWhere { UsuarioTable.id eq id } > 0
    }

    suspend fun verifyPassword(email: String, password: String): Boolean = DatabaseConfig.dbQuery {
        val user = UsuarioTable
            .select { UsuarioTable.email eq email }
            .singleOrNull() ?: return@dbQuery false

        BCrypt.checkpw(password, user[UsuarioTable.passwordHash])
    }

    private fun toUsuario(row: ResultRow): Usuario =
        Usuario(
            id = row[UsuarioTable.id],
            nombre = row[UsuarioTable.nombre],
            email = row[UsuarioTable.email],
            telefono = row[UsuarioTable.telefono],
            biografia = row[UsuarioTable.biografia],
            fotoPerfilUrl = row[UsuarioTable.fotoPerfilUrl],
            tipoUsuarioId = row[UsuarioTable.tipoUsuarioId],
            tipoUsuarioNombre = row[TipoUsuarioTable.nombre],
            estado = row[UsuarioTable.estado].name,
            createdAt = row[UsuarioTable.createdAt].toEpochMilliOrNull(),
            updatedAt = row[UsuarioTable.updatedAt].toEpochMilliOrNull(),
            firebaseUid = row[UsuarioTable.firebaseUid]

        )
    // Buscar usuario por Firebase UID
    suspend fun findByFirebaseUid(firebaseUid: String): Usuario? = DatabaseConfig.dbQuery {
        (UsuarioTable innerJoin TipoUsuarioTable)
            .select { UsuarioTable.firebaseUid eq firebaseUid }
            .map { toUsuario(it) }
            .singleOrNull()
    }

    // Crear usuario desde Firebase Auth
    suspend fun createFromFirebase(
        nombre: String,
        email: String,
        firebaseUid: String,
        telefono: String?,
        tipoUsuarioId: Int
    ): Usuario? = DatabaseConfig.dbQuery {
        // Verificar si el email ya existe
        val existingUser = UsuarioTable
            .select { UsuarioTable.email eq email }
            .singleOrNull()

        if (existingUser != null) {
            return@dbQuery null // Email ya existe
        }

        // Verificar si el Firebase UID ya existe
        val existingFirebaseUser = UsuarioTable
            .select { UsuarioTable.firebaseUid eq firebaseUid }
            .singleOrNull()

        if (existingFirebaseUser != null) {
            return@dbQuery findById(existingFirebaseUser[UsuarioTable.id])
        }

        val now = Instant.now()
        val id = UsuarioTable.insert {
            it[UsuarioTable.nombre] = nombre
            it[UsuarioTable.email] = email
            it[UsuarioTable.firebaseUid] = firebaseUid
            it[UsuarioTable.telefono] = telefono
            it[UsuarioTable.biografia] = null
            it[UsuarioTable.passwordHash] = null
            it[UsuarioTable.tipoUsuarioId] = tipoUsuarioId
            it[UsuarioTable.estado] = UsuarioTable.Estado.activo
            it[UsuarioTable.createdAt] = now
            it[UsuarioTable.updatedAt] = now
        } get UsuarioTable.id

        findById(id)
    }

}