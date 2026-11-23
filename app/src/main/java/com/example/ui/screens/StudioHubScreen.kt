package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.SeedCatalog
import com.example.data.model.CustomerReview
import com.example.data.model.GalleryPhoto
import com.example.data.model.SpecialOffer
import com.example.data.model.StudioConfig
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IvoryText
import com.example.ui.theme.MutedStone
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WhatsAppGreen
import com.example.util.dialStudioPhone
import com.example.util.openGoogleMapsLocation
import com.example.util.openWhatsAppWithMessage
import com.example.util.shareStudioApp

@Composable
fun StudioHubScreen(
    config: StudioConfig,
    offers: List<SpecialOffer>,
    reviews: List<CustomerReview>,
    showHindi: Boolean,
    isBusy: Boolean,
    onSubmitReview: (name: String, rating: Int, comment: String, serviceTag: String) -> Unit,
    onOpenAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedGalleryPhoto by remember { mutableStateOf<GalleryPhoto?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Studio Gallery
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (showHindi) "Studio Gallery / स्टूडियो गैलरी" else "Studio Gallery",
                    style = MaterialTheme.typography.headlineSmall,
                    color = BrightGold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(SeedCatalog.galleryPhotos, key = { it.id }) { photo ->
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { selectedGalleryPhoto = photo }
                                .testTag("gallery_card_${photo.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.35f))
                        ) {
                            Column {
                                StudioImageView(
                                    imageUrl = null,
                                    presetKey = photo.presetKey,
                                    contentDescription = photo.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                )
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(photo.categoryLabel, style = MaterialTheme.typography.labelSmall, color = BrightGold)
                                    Text(photo.title, style = MaterialTheme.typography.titleSmall, color = IvoryText)
                                    if (showHindi) {
                                        Text(photo.hindiTitle, style = MaterialTheme.typography.labelSmall, color = MutedStone)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Special Offers & Discounts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (showHindi) "Special Offers & Discounts / विशेष ऑफर" else "Special Offers & Discounts",
                    style = MaterialTheme.typography.headlineSmall,
                    color = BrightGold
                )
                offers.filter { it.active }.forEach { offer ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GoldContainer),
                        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.LocalOffer, contentDescription = null, tint = BrightGold, modifier = Modifier.size(16.dp))
                                    Text(offer.badge, style = MaterialTheme.typography.labelMedium, color = BrightGold, fontWeight = FontWeight.Bold)
                                }
                                Surface(
                                    color = ObsidianBlack,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "CODE: ${offer.promoCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrightGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(offer.title, style = MaterialTheme.typography.titleMedium, color = IvoryText)
                            Text(offer.description, style = MaterialTheme.typography.bodyMedium, color = IvoryText.copy(alpha = 0.85f))
                        }
                    }
                }
            }
        }

        // 3. Customer Reviews
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showHindi) "Customer Reviews / ग्राहक समीक्षा" else "Customer Reviews",
                        style = MaterialTheme.typography.headlineSmall,
                        color = BrightGold
                    )
                    OutlinedButton(
                        onClick = { showReviewDialog = true },
                        modifier = Modifier.testTag("write_review_button"),
                        border = BorderStroke(1.dp, ChampagneGold),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Review")
                    }
                }
                reviews.forEach { review ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                        border = BorderStroke(1.dp, SubtleBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(review.authorName, style = MaterialTheme.typography.titleSmall, color = IvoryText)
                                Row {
                                    repeat(review.rating.toInt().coerceIn(1, 5)) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = BrightGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Text(review.serviceTag, style = MaterialTheme.typography.labelSmall, color = ChampagneGold)
                            Text(review.comment, style = MaterialTheme.typography.bodyMedium, color = MutedStone)
                        }
                    }
                }
            }
        }

        // 4. About & Contact Us
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StudioPixyLogo(size = 42.dp)
                        Column {
                            Text("About ${config.businessName}", style = MaterialTheme.typography.titleLarge, color = BrightGold)
                            Text(config.tagline, style = MaterialTheme.typography.labelMedium, color = MutedStone)
                        }
                    }
                    Text(
                        text = "${config.businessName} is your neighborhood luxury photography studio and same-day photo printing lab. We specialize in wedding storytelling, instant passport & visa portraits, handcrafted frames, and archival photo restoration.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IvoryText.copy(alpha = 0.9f)
                    )

                    GoldDivider()

                    InfoRow(Icons.Default.LocationOn, "Address", config.address)
                    InfoRow(Icons.Default.Schedule, "Opening Hours", config.openingHours)
                    InfoRow(Icons.Default.Call, "Phone / Call", config.phoneNumber)
                    InfoRow(Icons.Default.Email, "Email", config.email)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { dialStudioPhone(context, config.phoneNumber) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hub_call_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        Button(
                            onClick = {
                                openWhatsAppWithMessage(
                                    context,
                                    config.whatsappNumber,
                                    "Hello ${config.businessName}! I visited your app and have an inquiry."
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hub_whatsapp_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { openGoogleMapsLocation(context, config.mapsQuery, config.address) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hub_maps_button"),
                            border = BorderStroke(1.dp, ChampagneGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightGold),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Maps", maxLines = 1)
                        }
                    }
                }
            }
        }

        // 5. Share, Policies & Admin Entry
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedCard),
                border = BorderStroke(1.dp, SubtleBorder)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    HubActionRow(
                        icon = Icons.Default.Share,
                        title = "Share Studio Pixy App",
                        subtitle = "Send studio contact & services to friends",
                        onClick = { shareStudioApp(context, config) },
                        modifier = Modifier.testTag("share_app_row")
                    )
                    GoldDivider()
                    HubActionRow(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        subtitle = "How customer order details are handled",
                        onClick = { showPrivacyDialog = true },
                        modifier = Modifier.testTag("privacy_policy_row")
                    )
                    GoldDivider()
                    HubActionRow(
                        icon = Icons.Default.Gavel,
                        title = "Terms & Conditions",
                        subtitle = "Booking, frame sizing & print guidelines",
                        onClick = { showTermsDialog = true },
                        modifier = Modifier.testTag("terms_row")
                    )
                    GoldDivider()
                    HubActionRow(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Admin Panel (Owner Login)",
                        subtitle = "Manage prices, services, orders & studio info",
                        onClick = onOpenAdmin,
                        highlightGold = true,
                        modifier = Modifier.testTag("open_admin_panel_row")
                    )
                }
            }
        }
    }

    selectedGalleryPhoto?.let { photo ->
        AlertDialog(
            onDismissRequest = { selectedGalleryPhoto = null },
            containerColor = CharcoalSurface,
            titleContentColor = BrightGold,
            textContentColor = IvoryText,
            title = { Text(photo.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    StudioImageView(
                        imageUrl = null,
                        presetKey = photo.presetKey,
                        contentDescription = photo.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Text("${photo.categoryLabel} • ${photo.hindiTitle}", color = MutedStone)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedGalleryPhoto = null }) {
                    Text("Close", color = BrightGold)
                }
            }
        )
    }

    if (showReviewDialog) {
        WriteReviewDialog(
            isBusy = isBusy,
            onDismiss = { showReviewDialog = false },
            onSubmit = { name, rating, comment, tag ->
                onSubmitReview(name, rating, comment, tag)
                showReviewDialog = false
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = CharcoalSurface,
            titleContentColor = BrightGold,
            textContentColor = IvoryText,
            title = { Text("Privacy Policy") },
            text = {
                Text(
                    "Studio Pixy collects your name, phone number, and custom order notes solely to process your photography, frame, and printing requests and coordinate with you on WhatsApp. Customer order records are strictly private and visible only to authorized Studio Pixy administrators."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK", color = BrightGold)
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            containerColor = CharcoalSurface,
            titleContentColor = BrightGold,
            textContentColor = IvoryText,
            title = { Text("Terms & Conditions") },
            text = {
                Text(
                    "1. Listed ₹ prices are live estimates managed by Studio Pixy and may vary for custom dimensions or outstation shoots.\n2. Express passport orders are ready within 10–15 minutes during studio hours.\n3. Wedding & event bookings are confirmed upon studio acknowledgement via WhatsApp."
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Accept", color = BrightGold)
                }
            }
        )
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = label, tint = ChampagneGold, modifier = Modifier.size(18.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MutedStone)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = IvoryText)
        }
    }
}

@Composable
private fun HubActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    highlightGold: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (highlightGold) BrightGold else ChampagneGold,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (highlightGold) BrightGold else IvoryText
            )
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MutedStone)
        }
        Icon(Icons.Default.Info, contentDescription = null, tint = SubtleBorder, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun WriteReviewDialog(
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, rating: Int, comment: String, serviceTag: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var serviceTag by remember { mutableStateOf("Photo Frames") }
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) }
    var error by remember { mutableStateOf<String?>(null) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ChampagneGold,
        unfocusedBorderColor = SubtleBorder,
        focusedLabelColor = BrightGold,
        unfocusedLabelColor = MutedStone,
        focusedTextColor = IvoryText,
        unfocusedTextColor = IvoryText
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface,
        titleContentColor = BrightGold,
        textContentColor = IvoryText,
        title = { Text("Write a Review") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Rating:", style = MaterialTheme.typography.bodyMedium, color = IvoryText)
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= rating) BrightGold else SubtleBorder
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Your Name *") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = serviceTag,
                    onValueChange = { serviceTag = it },
                    label = { Text("Service Used") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it; error = null },
                    label = { Text("Your Experience *") },
                    minLines = 2,
                    maxLines = 4,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.trim().length < 2 -> error = "Enter your name (min 2 chars)."
                        comment.trim().length < 3 -> error = "Enter your review (min 3 chars)."
                        else -> onSubmit(name.trim(), rating, comment.trim(), serviceTag.trim())
                    }
                },
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack)
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedStone)
            }
        }
    )
}
