package com.example.jobsterrabackend.services

import com.example.jobsterrabackend.config.FirebaseConfig
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class FirebaseService {
    private val logger = LoggerFactory.getLogger(FirebaseService::class.java)
    private val storage: Storage = FirebaseConfig.storage
    private val bucketName = "jobsterra-8e675.appspot.com"

    // Para Firestore (si lo necesitas en el futuro)
    // private val firestore: Firestore = FirebaseConfig.firestore

    /**
     * Subir un archivo a Firebase Storage
     */
    suspend fun uploadFile(
        storagePath: String,
        fileBytes: ByteArray,
        mimeType: String? = null
    ): String {
        return try {
            logger.info("Subiendo archivo a Firebase Storage: $storagePath")

            val blobId = BlobId.of(bucketName, storagePath)
            val blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(mimeType ?: "application/octet-stream")
                .build()

            val blob = storage.create(blobInfo, fileBytes)

            // Generar URL pública (válida por 365 días)
            val url = blob.signUrl(365, TimeUnit.DAYS).toString()

            logger.info("Archivo subido exitosamente: $storagePath")
            url
        } catch (e: Exception) {
            logger.error("Error al subir archivo a Firebase Storage: ${e.message}", e)
            throw RuntimeException("Error al subir archivo: ${e.message}")
        }
    }

    /**
     * Eliminar un archivo de Firebase Storage
     */
    suspend fun deleteFile(storagePath: String): Boolean {
        return try {
            logger.info("Eliminando archivo de Firebase Storage: $storagePath")

            val blobId = BlobId.of(bucketName, storagePath)
            val deleted = storage.delete(blobId)

            if (deleted) {
                logger.info("Archivo eliminado exitosamente: $storagePath")
            } else {
                logger.warn("No se pudo eliminar el archivo (posiblemente no existe): $storagePath")
            }

            deleted
        } catch (e: Exception) {
            logger.error("Error al eliminar archivo de Firebase Storage: ${e.message}", e)
            false
        }
    }

    /**
     * Obtener URL pública de un archivo
     */
    suspend fun getFileUrl(storagePath: String): String? {
        return try {
            val blobId = BlobId.of(bucketName, storagePath)
            val blob = storage.get(blobId) ?: return null

            // Generar URL con firma válida por 1 hora
            blob.signUrl(1, TimeUnit.HOURS).toString()
        } catch (e: Exception) {
            logger.error("Error al obtener URL del archivo: ${e.message}", e)
            null
        }
    }

    /**
     * Verificar si un archivo existe
     */
    suspend fun fileExists(storagePath: String): Boolean {
        return try {
            val blobId = BlobId.of(bucketName, storagePath)
            val blob = storage.get(blobId)
            blob != null && blob.exists()
        } catch (e: Exception) {
            logger.error("Error al verificar si existe el archivo: ${e.message}", e)
            false
        }
    }

    // MÉTODOS PARA FIRESTORE (PARA CHATS EN TIEMPO REAL) -

    /*
    Estos métodos son para cuando quieras implementar chats en tiempo real con Firestore.
    Por ahora están comentados, pero los puedes usar más adelante.

    suspend fun sendMessage(
        chatId: String,
        senderId: Int,
        message: String,
        attachments: List<String> = emptyList()
    ): String {
        return try {
            val messageData = mapOf(
                "id" to UUID.randomUUID().toString(),
                "chatId" to chatId,
                "senderId" to senderId,
                "message" to message,
                "attachments" to attachments,
                "timestamp" to Instant.now().toEpochMilli(),
                "read" to false
            )

            val docRef = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(messageData).get()

            logger.info("Mensaje enviado exitosamente: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            logger.error("Error al enviar mensaje: ${e.message}", e)
            throw RuntimeException("Error al enviar mensaje: ${e.message}")
        }
    }

    suspend fun getChatMessages(
        chatId: String,
        page: Int = 1,
        pageSize: Int = 50
    ): List<Map<String, Any>> {
        return try {
            val query = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(pageSize)

            val querySnapshot = query.get().get()

            querySnapshot.documents.map { doc ->
                doc.data ?: emptyMap()
            }
        } catch (e: Exception) {
            logger.error("Error al obtener mensajes: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun markMessagesAsRead(chatId: String, userId: Int): Boolean {
        return try {
            val query = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .whereEqualTo("read", false)
                .whereNotEqualTo("senderId", userId)

            val querySnapshot = query.get().get()

            querySnapshot.documents.forEach { doc ->
                doc.reference.update("read", true)
            }

            true
        } catch (e: Exception) {
            logger.error("Error al marcar mensajes como leídos: ${e.message}", e)
            false
        }
    }

    suspend fun deleteChat(chatId: String): Boolean {
        return try {
            // Eliminar todos los mensajes del chat
            val messagesQuery = firestore.collection("chats")
                .document(chatId)
                .collection("messages")

            val querySnapshot = messagesQuery.get().get()
            querySnapshot.documents.forEach { doc ->
                doc.reference.delete()
            }

            // Eliminar el documento del chat
            firestore.collection("chats").document(chatId).delete().get()

            logger.info("Chat eliminado exitosamente: $chatId")
            true
        } catch (e: Exception) {
            logger.error("Error al eliminar chat: ${e.message}", e)
            false
        }
    }
    */
}