package com.example.jobsterrabackend.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

object DatabaseConfig {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init() {
        try {
            val config = HikariConfig().apply {
                driverClassName = "com.mysql.cj.jdbc.Driver"
                // Asegúrate de que la URL sea correcta - aquí apunta al contenedor Docker
                jdbcUrl = "jdbc:mysql://212.227.235.78:3306/jobsterra?useSSL=false&allowPublicKeyRetrieval=true"
                username = "root" // Cambia esto si usas un usuario diferente
                password = "Estepa78." // Cambia esto por la contraseña que configuraste para MySQL
                maximumPoolSize = 10
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
                validate()
            }
            val dataSource = HikariDataSource(config)
            Database.connect(dataSource)
            logger.info("Conexión a la base de datos inicializada correctamente")
        } catch (e: Exception) {
            logger.error("Error al inicializar la conexión a la base de datos: ${e.message}")
            throw e
        }
    }

    /**
     * Función de utilidad para ejecutar consultas en la base de datos usando corrutinas.
     * Esto permite realizar operaciones de base de datos de forma asíncrona.
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}