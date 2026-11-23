package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.storage
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

enum class SupportedImageFormat(
    val extension: String,
    val mimeType: String,
    val displayLabel: String
) {
    JPEG("jpg", "image/jpeg", "JPG / JPEG"),
    PNG("png", "image/png", "PNG")
}

class ValidatedPhotoPayload(
    val bytes: ByteArray,
    val format: SupportedImageFormat,
    val displayName: String? = null
) {
    val sizeBytes: Int get() = bytes.size
    val formattedSize: String
        get() = if (sizeBytes < 1024 * 1024) {
            "${(sizeBytes / 1024).coerceAtLeast(1)} KB"
        } else {
            String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
        }
}

object ImageFormatValidator {
    const val MAX_PHOTO_BYTES = 10 * 1024 * 1024 // 10 MB, aligned with storage.rules

    private val ALLOWED_MIME_TYPES = setOf("image/jpeg", "image/jpg", "image/png")

    fun detectFormat(
        bytes: ByteArray,
        declaredMimeType: String? = null,
        fileNameOrUri: String? = null
    ): SupportedImageFormat? {
        if (bytes.isEmpty()) return null

        val magicFormat = detectByMagicBytes(bytes)
        if (magicFormat != null) return magicFormat

        // Reject known non-JPG/PNG signatures (GIF, WebP, BMP, PDF, MP4)
        if (looksLikeUnsupportedBinary(bytes)) return null

        val mime = declaredMimeType?.trim()?.lowercase()
        if (mime != null) {
            return when (mime) {
                "image/jpeg", "image/jpg" -> SupportedImageFormat.JPEG
                "image/png" -> SupportedImageFormat.PNG
                else -> null
            }
        }

        val lowerName = fileNameOrUri?.trim()?.lowercase() ?: return null
        return when {
            lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") -> SupportedImageFormat.JPEG
            lowerName.endsWith(".png") -> SupportedImageFormat.PNG
            else -> null
        }
    }

    fun validatePhotoBytes(
        bytes: ByteArray,
        declaredMimeType: String? = null,
        fileNameOrUri: String? = null
    ): Result<ValidatedPhotoPayload> {
        if (bytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Selected image file is empty."))
        }
        if (bytes.size > MAX_PHOTO_BYTES) {
            return Result.failure(IllegalArgumentException("Image is too large. Maximum allowed size is 10 MB."))
        }

        val mime = declaredMimeType?.trim()?.lowercase()
        if (mime != null && mime !in ALLOWED_MIME_TYPES && mime != "application/octet-stream") {
            return Result.failure(
                IllegalArgumentException("Unsupported format ($mime). Please select a JPG, JPEG, or PNG image.")
            )
        }

        val format = detectFormat(bytes, declaredMimeType, fileNameOrUri)
            ?: return Result.failure(
                IllegalArgumentException("Only JPG, JPEG, and PNG images are supported.")
            )

        return Result.success(
            ValidatedPhotoPayload(
                bytes = bytes,
                format = format,
                displayName = fileNameOrUri
            )
        )
    }

    suspend fun readAndValidateFromUri(context: Context, uri: Uri): Result<ValidatedPhotoPayload> =
        withContext(Dispatchers.IO) {
            try {
                val resolver = context.contentResolver
                val mimeType = resolver.getType(uri)
                val displayName = resolveDisplayName(context, uri) ?: uri.lastPathSegment

                val bytes = resolver.openInputStream(uri)?.use { input ->
                    val buffer = ByteArray(8192)
                    val out = ByteArrayOutputStream()
                    var total = 0
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        total += read
                        if (total > MAX_PHOTO_BYTES) {
                            return@withContext Result.failure(
                                IllegalArgumentException("Image exceeds 10 MB limit. Choose a smaller JPG or PNG image.")
                            )
                        }
                        out.write(buffer, 0, read)
                    }
                    out.toByteArray()
                } ?: return@withContext Result.failure(
                    IllegalArgumentException("Could not open selected photo from gallery.")
                )

                validatePhotoBytes(bytes, mimeType, displayName)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(IllegalArgumentException(e.localizedMessage ?: "Failed to read selected photo."))
            }
        }

    private fun resolveDisplayName(context: Context, uri: Uri): String? =
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) cursor.getString(idx) else null
                    } else null
                }
        } catch (_: Exception) {
            null
        }

    private fun detectByMagicBytes(bytes: ByteArray): SupportedImageFormat? {
        if (bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() &&
            bytes[1] == 0xD8.toByte() &&
            bytes[2] == 0xFF.toByte()
        ) {
            return SupportedImageFormat.JPEG
        }
        if (bytes.size >= 8 &&
            bytes[0] == 0x89.toByte() &&
            bytes[1] == 0x50.toByte() &&
            bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte() &&
            bytes[4] == 0x0D.toByte() &&
            bytes[5] == 0x0A.toByte() &&
            bytes[6] == 0x1A.toByte() &&
            bytes[7] == 0x0A.toByte()
        ) {
            return SupportedImageFormat.PNG
        }
        return null
    }

    private fun looksLikeUnsupportedBinary(bytes: ByteArray): Boolean {
        if (bytes.size >= 4 &&
            bytes[0] == 'G'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == '8'.code.toByte()
        ) return true
        if (bytes.size >= 12 &&
            bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()
        ) return true
        return false
    }
}

interface ServicePhotoStorage {
    suspend fun uploadServicePhoto(serviceId: String, payload: ValidatedPhotoPayload): Result<String>
    suspend fun deletePhotoByUrl(imageUrl: String?): Result<Unit>
}

class FirebaseServicePhotoStorage(
    private val storageProvider: () -> FirebaseStorage = { Firebase.storage }
) : ServicePhotoStorage {

    override suspend fun uploadServicePhoto(
        serviceId: String,
        payload: ValidatedPhotoPayload
    ): Result<String> {
        return try {
            val cleanId = serviceId.trim()
            require(cleanId.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$"))) {
                "Invalid service ID for photo storage."
            }
            val fileName = "photo_${System.currentTimeMillis()}.${payload.format.extension}"
            val storageRef = storageProvider().reference
                .child("service_photos")
                .child(cleanId)
                .child(fileName)

            val metadata = storageMetadata {
                contentType = payload.format.mimeType
            }

            storageRef.putBytes(payload.bytes, metadata).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePhotoByUrl(imageUrl: String?): Result<Unit> {
        val url = imageUrl?.trim().orEmpty()
        if (url.isEmpty() || !isFirebaseStorageUrl(url)) {
            return Result.success(Unit)
        }
        return try {
            storageProvider().getReferenceFromUrl(url).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: StorageException) {
            if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                Result.success(Unit)
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun isFirebaseStorageUrl(url: String): Boolean =
            url.startsWith("https://firebasestorage.googleapis.com/") ||
                url.startsWith("https://storage.googleapis.com/") ||
                url.startsWith("gs://")
    }
}
