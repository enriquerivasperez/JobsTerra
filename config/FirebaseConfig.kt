package com.example.jobsterrabackend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.slf4j.LoggerFactory
import java.io.InputStream

object FirebaseConfig {
    private val logger = LoggerFactory.getLogger(FirebaseConfig::class.java)

    lateinit var auth: FirebaseAuth
    lateinit var storage: Storage

    fun init() {
        try {
            logger.info("Inicializando Firebase...")

            // Cargar credenciales desde resources
            val serviceAccountStream: InputStream = this::class.java.classLoader
                .getResourceAsStream("firebase-service-account.json")
                ?: throw IllegalStateException("No se encontró el archivo firebase-service-account.json")

            val credentials = GoogleCredentials.fromStream(serviceAccountStream)

            // Configurar Firebase
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId("jobsterra-8e675")
                .setStorageBucket("jobsterra-8e675.appspot.com")
                .build()

            // Inicializar Firebase App si no existe
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info("Firebase App inicializada correctamente")
            }

            // Configurar Auth
            auth = FirebaseAuth.getInstance()
            logger.info("Firebase Auth configurado")

            // Configurar Storage
            storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId("jobsterra-8e675")
                .build()
                .service
            logger.info("Firebase Storage configurado")

            logger.info("Firebase inicializado exitosamente")

        } catch (e: Exception) {
            logger.error("Error al inicializar Firebase: ${e.message}", e)
            throw e
        }
    }
}