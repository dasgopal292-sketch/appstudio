package com.example.data

import android.content.Context
import com.example.R
import com.example.data.model.CustomerOrder
import com.example.data.model.CustomerReview
import com.example.data.model.OrderStatus
import com.example.data.model.ServiceCategory
import com.example.data.model.SpecialOffer
import com.example.data.model.StudioConfig
import com.example.data.model.StudioService
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class StudioRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
    private val photoStorage: ServicePhotoStorage = FirebaseServicePhotoStorage()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        const val BOOTSTRAP_ADMIN_EMAIL = "dasgopal292@gmail.com"
        private const val CONFIG_DOC_ID = "main"
    }

    fun observeServices(): Flow<List<StudioService>> =
        db.collection("services")
            .snapshots()
            .map { snapshot ->
                val list = snapshot.toObjects(
                    StudioService::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
                list.sortedWith(compareBy<StudioService> { it.sortOrder }.thenBy { it.name })
            }
            .catch { error ->
                val firestoreError = unwrapFirestoreException(error)
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.LIST, "services")
                throw error
            }

    fun observeStudioConfig(): Flow<StudioConfig?> =
        db.collection("studio_config")
            .document(CONFIG_DOC_ID)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        StudioConfig::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                val firestoreError = unwrapFirestoreException(error)
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.GET, "studio_config/$CONFIG_DOC_ID")
                throw error
            }

    fun observeOffers(): Flow<List<SpecialOffer>> =
        db.collection("offers")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    SpecialOffer::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val firestoreError = unwrapFirestoreException(error)
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.LIST, "offers")
                throw error
            }

    fun observeReviews(): Flow<List<CustomerReview>> =
        db.collection("reviews")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CustomerReview::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val firestoreError = unwrapFirestoreException(error)
                if (firestoreError == null && error is CancellationException) throw error
                handleFirestoreError(firestoreError ?: error, OperationType.LIST, "reviews")
                throw error
            }

    fun observeOrders(): Flow<List<CustomerOrder>> =
        db.collection("orders")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CustomerOrder::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val firestoreError = unwrapFirestoreException(error)
                if (firestoreError == null && error is CancellationException) throw error
                if (auth.currentUser != null) {
                    handleFirestoreError(firestoreError ?: error, OperationType.LIST, "orders")
                }
                throw error
            }

    suspend fun checkIsAuthorizedAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        if (user.isEmailVerified && user.email.equals(BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true)) {
            return true
        }
        return try {
            val doc = db.collection("admins").document(user.uid).get().await()
            doc.exists()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    suspend fun submitOrder(order: CustomerOrder): Result<String> {
        val docRef = db.collection("orders").document()
        val path = docRef.path
        return try {
            val name = order.customerName.trim()
            val phone = order.customerPhone.trim()
            require(name.length in 2..80) { "Customer name must be 2–80 characters." }
            require(phone.length in 7..20) { "Please enter a valid phone number." }
            require(order.itemsSummary.trim().length in 2..1200) { "Order summary is required." }
            require(order.itemCount in 1L..500L) { "Order must contain at least 1 item." }
            require(order.totalInr in 0L..10_000_000L) { "Order total is out of range." }

            docRef.set(order.toCreateMap()).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun submitReview(authorName: String, rating: Int, comment: String, serviceTag: String): Result<Unit> {
        val docRef = db.collection("reviews").document()
        val path = docRef.path
        return try {
            val cleanName = authorName.trim()
            val cleanComment = comment.trim()
            val cleanTag = serviceTag.trim().ifBlank { "Studio Pixy" }
            require(cleanName.length in 2..60) { "Name must be 2–60 characters." }
            require(rating in 1..5) { "Rating must be between 1 and 5." }
            require(cleanComment.length in 3..400) { "Review comment must be 3–400 characters." }
            require(cleanTag.length in 2..60) { "Service tag must be 2–60 characters." }

            val review = CustomerReview(
                authorName = cleanName,
                rating = rating.toLong(),
                comment = cleanComment,
                serviceTag = cleanTag
            )
            docRef.set(review.toCreateMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun saveService(service: StudioService, isUpdate: Boolean): Result<Unit> {
        val docRef = if (isUpdate && service.id.isNotBlank()) {
            db.collection("services").document(service.id)
        } else {
            val generatedId = service.id.takeIf { it.isNotBlank() && it.matches(Regex("^[a-zA-Z0-9_\\-]+$")) }
            if (generatedId != null) db.collection("services").document(generatedId)
            else db.collection("services").document()
        }
        val path = docRef.path
        return try {
            require(auth.currentUser != null) { "Admin must be signed in." }
            val cleanName = service.name.trim()
            val cleanHindi = service.hindiName.trim().ifBlank { cleanName }
            val cleanUnit = service.unitLabel.trim().ifBlank { "Per Item" }
            val cleanDesc = service.description.trim().ifBlank { cleanName }
            require(cleanName.length in 2..100) { "Service name must be 2–100 characters." }
            require(cleanHindi.length in 1..120) { "Hindi label must be 1–120 characters." }
            require(service.priceInr in 0L..10_000_000L) { "Price must be between ₹0 and ₹1,00,00,000." }
            require(cleanUnit.length in 1..50) { "Unit label must be 1–50 characters." }
            require(cleanDesc.length in 2..500) { "Description must be 2–500 characters." }

            if (isUpdate && service.id.isNotBlank()) {
                val existingDoc = docRef.get().await()
                if (existingDoc.exists()) {
                    val updates = buildMap<String, Any?> {
                        put("name", cleanName)
                        put("hindiName", cleanHindi)
                        put("category", ServiceCategory.fromRaw(service.category).name)
                        put("priceInr", service.priceInr)
                        put("unitLabel", cleanUnit)
                        put("description", cleanDesc)
                        put("imageUrl", service.imageUrl?.trim()?.takeIf { it.isNotEmpty() && it.length <= 600 })
                        put("presetKey", service.presetKey?.trim()?.takeIf { it.isNotEmpty() && it.length <= 50 })
                        put("available", service.available)
                        put("featured", service.featured)
                        put("sortOrder", service.sortOrder.coerceIn(0L, 10_000L))
                        put("updatedAt", FieldValue.serverTimestamp())
                    }
                    docRef.update(updates).await()
                } else {
                    docRef.set(service.toCreateMap()).await()
                }
            } else {
                docRef.set(service.toCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (isUpdate) OperationType.UPDATE else OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun uploadAndSaveServicePhoto(
        service: StudioService,
        payload: ValidatedPhotoPayload
    ): Result<String> {
        val targetId = service.id.trim().takeIf { it.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$")) }
            ?: return Result.failure(IllegalArgumentException("Save the service first before uploading a photo."))
        val docRef = db.collection("services").document(targetId)
        val path = docRef.path
        var uploadedUrl: String? = null
        return try {
            require(auth.currentUser != null) { "Admin must be signed in." }
            require(checkIsAuthorizedAdmin()) { "Only authorized Studio Pixy admins can upload service photos." }

            if (db.collection("services").limit(1).get().await().isEmpty) {
                seedDefaultCatalogIfNeeded().getOrThrow()
            }

            val previousUrl = service.imageUrl
            val downloadUrl = photoStorage.uploadServicePhoto(targetId, payload).getOrThrow()
            uploadedUrl = downloadUrl
            require(downloadUrl.length <= 600) { "Generated image URL exceeds allowed length." }

            val existingDoc = docRef.get().await()
            if (existingDoc.exists()) {
                docRef.update(
                    mapOf(
                        "imageUrl" to downloadUrl,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(service.copy(id = targetId, imageUrl = downloadUrl).toCreateMap()).await()
            }

            if (!previousUrl.isNullOrBlank() && previousUrl != downloadUrl) {
                photoStorage.deletePhotoByUrl(previousUrl)
            }

            Result.success(downloadUrl)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            uploadedUrl?.let { orphanUrl ->
                photoStorage.deletePhotoByUrl(orphanUrl)
            }
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteServicePhoto(service: StudioService): Result<Unit> {
        val targetId = service.id.trim().takeIf { it.matches(Regex("^[a-zA-Z0-9_\\-]{1,128}$")) }
            ?: return Result.failure(IllegalArgumentException("Invalid service ID."))
        val docRef = db.collection("services").document(targetId)
        val path = docRef.path
        return try {
            require(auth.currentUser != null) { "Admin must be signed in." }
            require(checkIsAuthorizedAdmin()) { "Only authorized Studio Pixy admins can delete service photos." }

            if (db.collection("services").limit(1).get().await().isEmpty) {
                seedDefaultCatalogIfNeeded().getOrThrow()
            }

            val existingDoc = docRef.get().await()
            if (existingDoc.exists()) {
                docRef.update(
                    mapOf(
                        "imageUrl" to null,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            photoStorage.deletePhotoByUrl(service.imageUrl).getOrThrow()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteService(serviceId: String): Result<Unit> {
        val path = "services/$serviceId"
        return try {
            require(auth.currentUser != null) { "Admin must be signed in." }
            require(serviceId.isNotBlank()) { "Service ID cannot be blank." }
            db.collection("services").document(serviceId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun seedDefaultCatalogIfNeeded(): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "Admin must be signed in." }
            val existingServices = db.collection("services").limit(1).get().await()
            val batch = db.batch()
            if (existingServices.isEmpty) {
                SeedCatalog.defaultServices.forEach { svc ->
                    val ref = db.collection("services").document(svc.id)
                    batch.set(ref, svc.toCreateMap())
                }
            }
            val configRef = db.collection("studio_config").document(CONFIG_DOC_ID)
            if (!configRef.get().await().exists()) {
                batch.set(configRef, SeedCatalog.defaultConfig.toWriteMap())
            }
            val existingOffers = db.collection("offers").limit(1).get().await()
            if (existingOffers.isEmpty) {
                SeedCatalog.defaultOffers.forEach { offer ->
                    batch.set(db.collection("offers").document(offer.id), offer.toCreateMap())
                }
            }
            val existingReviews = db.collection("reviews").limit(1).get().await()
            if (existingReviews.isEmpty) {
                SeedCatalog.defaultReviews.forEach { review ->
                    batch.set(db.collection("reviews").document(review.id), review.toCreateMap())
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "services")
            Result.failure(e)
        }
    }

    suspend fun saveStudioConfig(config: StudioConfig): Result<Unit> {
        val docRef = db.collection("studio_config").document(CONFIG_DOC_ID)
        return try {
            require(auth.currentUser != null) { "Admin must be signed in." }
            require(config.businessName.trim().length in 2..80) { "Business name must be 2–80 chars." }
            require(config.whatsappNumber.trim().length in 8..20) { "WhatsApp number must be 8–20 chars." }
            require(config.phoneNumber.trim().length in 8..20) { "Phone number must be 8–20 chars." }
            require(config.address.trim().length in 4..240) { "Address must be 4–240 chars." }
            require(config.email.trim().length in 5..120) { "Email must be 5–120 chars." }

            val exists = docRef.get().await().exists()
            if (exists) {
                docRef.update(config.toWriteMap()).await()
            } else {
                docRef.set(config.toWriteMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        val docRef = db.collection("orders").document(orderId)
        return try {
            require(auth.currentUser != null) { "Admin must be signed in." }
            require(orderId.isNotBlank()) { "Order ID cannot be blank." }
            docRef.update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteOrder(orderId: String): Result<Unit> {
        val docRef = db.collection("orders").document(orderId)
        return try {
            require(auth.currentUser != null) { "Admin must be signed in." }
            require(orderId.isNotBlank()) { "Order ID cannot be blank." }
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveOffer(offer: SpecialOffer): Result<Unit> {
        val docRef = if (offer.id.isNotBlank()) {
            db.collection("offers").document(offer.id)
        } else {
            db.collection("offers").document()
        }
        return try {
            require(auth.currentUser != null) { "Admin must be signed in." }
            require(offer.title.trim().length in 2..100) { "Offer title must be 2–100 chars." }
            require(offer.badge.trim().length in 1..40) { "Badge must be 1–40 chars." }
            require(offer.description.trim().length in 2..300) { "Description must be 2–300 chars." }
            require(offer.promoCode.trim().length in 1..30) { "Promo code must be 1–30 chars." }

            if (offer.id.isNotBlank() && docRef.get().await().exists()) {
                docRef.update(
                    mapOf(
                        "title" to offer.title.trim(),
                        "badge" to offer.badge.trim(),
                        "description" to offer.description.trim(),
                        "promoCode" to offer.promoCode.trim(),
                        "active" to offer.active,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(offer.toCreateMap()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteOffer(offerId: String): Result<Unit> {
        val docRef = db.collection("offers").document(offerId)
        return try {
            require(auth.currentUser != null) { "Admin must be signed in." }
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    private fun unwrapFirestoreException(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
}
