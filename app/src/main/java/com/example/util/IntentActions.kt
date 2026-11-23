package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.CartItem
import com.example.data.model.StudioConfig
import java.net.URLEncoder
import java.text.NumberFormat
import java.util.Locale

fun formatInr(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    return "₹${formatter.format(amount)}"
}

fun buildWhatsAppOrderMessage(
    config: StudioConfig,
    items: List<CartItem>,
    customerName: String,
    customerPhone: String,
    requirements: String,
    orderId: String?
): String {
    val total = items.sumOf { it.lineTotalInr }
    return buildString {
        appendLine("Hello *${config.businessName}*! I would like to place an order:")
        if (!orderId.isNullOrBlank()) {
            appendLine("*Order Ref:* #$orderId")
        }
        appendLine()
        appendLine("*Customer:* $customerName")
        appendLine("*Phone:* $customerPhone")
        appendLine()
        appendLine("*Selected Services:*")
        items.forEachIndexed { idx, item ->
            appendLine("${idx + 1}. ${item.service.name} x${item.quantity} — ${formatInr(item.lineTotalInr)} (${item.service.unitLabel})")
        }
        appendLine()
        appendLine("*Estimated Total:* ${formatInr(total)}")
        if (requirements.isNotBlank()) {
            appendLine("*Custom Requirements:* $requirements")
        }
        appendLine()
        append("Please confirm availability & delivery time. Thank you!")
    }
}

fun openWhatsAppWithMessage(
    context: Context,
    phoneNumber: String,
    message: String
) {
    val cleanedDigits = phoneNumber.filter { it.isDigit() }
    val targetDigits = when {
        cleanedDigits.length == 10 -> "91$cleanedDigits"
        cleanedDigits.isNotEmpty() -> cleanedDigits
        else -> "919876543210"
    }
    val encoded = URLEncoder.encode(message, "UTF-8")
    val waUri = Uri.parse("https://wa.me/$targetDigits?text=$encoded")
    val viewIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(viewIntent)
    } catch (_: Exception) {
        val shareFallback = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(Intent.createChooser(shareFallback, "Send Order via WhatsApp"))
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open WhatsApp on this device.", Toast.LENGTH_SHORT).show()
        }
    }
}

fun dialStudioPhone(context: Context, phoneNumber: String) {
    val cleaned = phoneNumber.filter { it.isDigit() || it == '+' }
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleaned")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "No phone dialer found.", Toast.LENGTH_SHORT).show()
    }
}

fun openGoogleMapsLocation(context: Context, mapsQuery: String, address: String) {
    val query = Uri.encode(mapsQuery.ifBlank { address })
    val geoIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(geoIntent)
    } catch (_: Exception) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        try {
            context.startActivity(webIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open Google Maps.", Toast.LENGTH_SHORT).show()
        }
    }
}

fun shareStudioApp(context: Context, config: StudioConfig) {
    val shareText = buildString {
        appendLine("Check out *${config.businessName}* — ${config.tagline}!")
        appendLine("Photo Frames, Urgent Passport Photos, Wedding Photography & High-Quality Prints.")
        appendLine("WhatsApp / Call: ${config.phoneNumber}")
        append("Address: ${config.address}")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, config.businessName)
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Share ${config.businessName}"))
    } catch (_: Exception) {
        Toast.makeText(context, "Unable to share right now.", Toast.LENGTH_SHORT).show()
    }
}
