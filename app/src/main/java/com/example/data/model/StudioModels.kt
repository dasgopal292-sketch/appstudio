package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ServiceCategory(
    val title: String,
    val hindiTitle: String,
    val shortLabel: String
) {
    PHOTO_FRAMES("Photo Frames", "फोटो फ्रेम", "Frames"),
    PASSPORT_PHOTOGRAPHY("Passport Photography", "पासपोर्ट फोटो", "Passport"),
    PHOTOGRAPHY_SERVICES("Photography Services", "फोटोग्राफी सेवाएं", "Shoots"),
    PHOTO_PRINTING("Photo Printing", "फोटो प्रिंटिंग", "Printing");

    companion object {
        fun fromRaw(raw: String?): ServiceCategory =
            entries.firstOrNull { it.name == raw } ?: PHOTO_FRAMES
    }
}

enum class OrderStatus(val label: String, val hindiLabel: String) {
    PENDING("Pending", "पेंडिंग"),
    CONFIRMED("Confirmed", "कन्फर्म"),
    COMPLETED("Completed", "पूर्ण");

    companion object {
        fun fromRaw(raw: String?): OrderStatus =
            entries.firstOrNull { it.name == raw } ?: PENDING
    }
}

@Keep
@IgnoreExtraProperties
data class StudioService(
    @DocumentId val id: String = "",
    val name: String = "",
    val hindiName: String = "",
    val category: String = ServiceCategory.PHOTO_FRAMES.name,
    val priceInr: Long = 0L,
    val unitLabel: String = "Per Item",
    val description: String = "",
    val imageUrl: String? = null,
    val presetKey: String? = "frames",
    val available: Boolean = true,
    val featured: Boolean = false,
    val sortOrder: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun categoryEnum(): ServiceCategory = ServiceCategory.fromRaw(category)

    fun toCreateMap(): Map<String, Any> = buildMap {
        put("name", name.trim())
        put("hindiName", hindiName.trim().ifBlank { name.trim() })
        put("category", categoryEnum().name)
        put("priceInr", priceInr.coerceIn(0L, 10_000_000L))
        put("unitLabel", unitLabel.trim().ifBlank { "Per Item" })
        put("description", description.trim().ifBlank { name.trim() })
        imageUrl?.trim()?.takeIf { it.isNotEmpty() }?.let { put("imageUrl", it) }
        presetKey?.trim()?.takeIf { it.isNotEmpty() }?.let { put("presetKey", it) }
        put("available", available)
        put("featured", featured)
        put("sortOrder", sortOrder.coerceIn(0L, 10_000L))
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@Keep
@IgnoreExtraProperties
data class StudioConfig(
    @DocumentId val id: String = "main",
    val businessName: String = "Studio Pixy",
    val tagline: String = "Timeless Frames, Portraits & Cinematic Memories",
    val whatsappNumber: String = "+919876543210",
    val phoneNumber: String = "+919876543210",
    val address: String = "Shop 12, Main Market Road, Near City Center",
    val email: String = "hello@studiopixy.in",
    val openingHours: String = "Mon - Sun: 9:30 AM - 9:00 PM",
    val mapsQuery: String = "Studio Pixy Photography Studio",
    val specialOfferTitle: String = "Festive Frame & Print Combo",
    val specialOfferDetails: String = "Flat 15% OFF on 12x18 & 16x20 Wedding Photo Frames this week!",
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toWriteMap(): Map<String, Any> = mapOf(
        "businessName" to businessName.trim(),
        "tagline" to tagline.trim(),
        "whatsappNumber" to whatsappNumber.trim(),
        "phoneNumber" to phoneNumber.trim(),
        "address" to address.trim(),
        "email" to email.trim(),
        "openingHours" to openingHours.trim(),
        "mapsQuery" to mapsQuery.trim(),
        "specialOfferTitle" to specialOfferTitle.trim(),
        "specialOfferDetails" to specialOfferDetails.trim(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class CustomerOrder(
    @DocumentId val id: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val requirements: String? = null,
    val itemsSummary: String = "",
    val itemCount: Long = 1L,
    val totalInr: Long = 0L,
    val status: String = OrderStatus.PENDING.name,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun statusEnum(): OrderStatus = OrderStatus.fromRaw(status)

    fun toCreateMap(): Map<String, Any> = buildMap {
        put("customerName", customerName.trim())
        put("customerPhone", customerPhone.trim())
        requirements?.trim()?.takeIf { it.isNotEmpty() }?.let { put("requirements", it) }
        put("itemsSummary", itemsSummary.trim())
        put("itemCount", itemCount.coerceIn(1L, 500L))
        put("totalInr", totalInr.coerceIn(0L, 10_000_000L))
        put("status", OrderStatus.PENDING.name)
        put("createdAt", FieldValue.serverTimestamp())
        put("updatedAt", FieldValue.serverTimestamp())
    }
}

@Keep
@IgnoreExtraProperties
data class SpecialOffer(
    @DocumentId val id: String = "",
    val title: String = "",
    val badge: String = "OFFER",
    val description: String = "",
    val promoCode: String = "",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "title" to title.trim(),
        "badge" to badge.trim(),
        "description" to description.trim(),
        "promoCode" to promoCode.trim(),
        "active" to active,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class CustomerReview(
    @DocumentId val id: String = "",
    val authorName: String = "",
    val rating: Long = 5L,
    val comment: String = "",
    val serviceTag: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "authorName" to authorName.trim(),
        "rating" to rating.coerceIn(1L, 5L),
        "comment" to comment.trim(),
        "serviceTag" to serviceTag.trim(),
        "createdAt" to FieldValue.serverTimestamp()
    )
}

data class CartItem(
    val service: StudioService,
    val quantity: Int = 1
) {
    val lineTotalInr: Long get() = service.priceInr * quantity.toLong()
}

data class GalleryPhoto(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val categoryLabel: String,
    val presetKey: String
)
