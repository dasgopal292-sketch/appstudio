package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ServiceCategory
import com.example.data.model.StudioConfig
import com.example.data.model.StudioService
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
import com.example.util.formatInr
import com.example.util.openGoogleMapsLocation
import com.example.util.openWhatsAppWithMessage

@Composable
fun HomeScreen(
    config: StudioConfig,
    services: List<StudioService>,
    showHindi: Boolean,
    isAdminSignedIn: Boolean,
    onNavigateToCategory: (ServiceCategory?) -> Unit,
    onSelectService: (StudioService) -> Unit,
    onAddToCart: (StudioService) -> Unit,
    onWhatsAppOrderService: (StudioService) -> Unit,
    onOpenHub: () -> Unit,
    onOpenAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeServices = services.filter { it.available }
    val popularFrames = activeServices.filter { it.categoryEnum() == ServiceCategory.PHOTO_FRAMES }.take(5)
    val passportPackages = activeServices.filter { it.categoryEnum() == ServiceCategory.PASSPORT_PHOTOGRAPHY }.take(4)
    val photographyPackages = activeServices.filter { it.categoryEnum() == ServiceCategory.PHOTOGRAPHY_SERVICES }.take(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_lazy_column"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Welcome Hero Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_studio),
                    contentDescription = "Studio Pixy Welcome Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    ObsidianBlack.copy(alpha = 0.25f),
                                    ObsidianBlack.copy(alpha = 0.88f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = ObsidianBlack.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BrightGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (showHindi) "प्रीमियम फोटो स्टूडियो एवं प्रिंटिंग" else "PREMIUM PHOTO STUDIO & LAB",
                                style = MaterialTheme.typography.labelSmall,
                                color = BrightGold
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = config.businessName.uppercase(),
                            style = MaterialTheme.typography.displaySmall,
                            color = IvoryText
                        )
                        Text(
                            text = config.tagline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = IvoryText.copy(alpha = 0.88f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    openWhatsAppWithMessage(
                                        context,
                                        config.whatsappNumber,
                                        "Hello ${config.businessName}! I would like to inquire about photography & printing services."
                                    )
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("home_whatsapp_chat_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WhatsAppGreen,
                                    contentColor = ObsidianBlack
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp Order", fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { onNavigateToCategory(null) },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("home_rate_list_button"),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BrightGold),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightGold)
                            ) {
                                Text("Rate List")
                            }
                        }
                    }
                }
            }
        }

        // 2. Special Offer Spotlight
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onOpenHub() }
                    .testTag("home_offer_banner"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = GoldContainer),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = config.specialOfferTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = BrightGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = config.specialOfferDetails,
                            style = MaterialTheme.typography.bodyMedium,
                            color = IvoryText.copy(alpha = 0.9f)
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Offers",
                        tint = BrightGold
                    )
                }
            }
        }

        // 3. Our Services Grid (4 Categories)
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionHeader(
                    title = if (showHindi) "Our Services / हमारी सेवाएं" else "Our Services",
                    actionLabel = "See All",
                    onAction = { onNavigateToCategory(null) }
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryQuickTile(
                        title = "Photo Frames",
                        subtitle = if (showHindi) "फोटो फ्रेम" else "4x6 to 20x24",
                        icon = Icons.Default.Collections,
                        onClick = { onNavigateToCategory(ServiceCategory.PHOTO_FRAMES) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_cat_frames")
                    )
                    CategoryQuickTile(
                        title = "Passport Photo",
                        subtitle = if (showHindi) "पासपोर्ट / वीज़ा" else "10-Min Express",
                        icon = Icons.Default.Badge,
                        onClick = { onNavigateToCategory(ServiceCategory.PASSPORT_PHOTOGRAPHY) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_cat_passport")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryQuickTile(
                        title = "Photography",
                        subtitle = if (showHindi) "शादी एवं इवेंट्स" else "Wedding & Shoots",
                        icon = Icons.Default.CameraAlt,
                        onClick = { onNavigateToCategory(ServiceCategory.PHOTOGRAPHY_SERVICES) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_cat_photography")
                    )
                    CategoryQuickTile(
                        title = "Photo Printing",
                        subtitle = if (showHindi) "प्रिंट एवं रिस्टोर" else "Prints & Restore",
                        icon = Icons.Default.Print,
                        onClick = { onNavigateToCategory(ServiceCategory.PHOTO_PRINTING) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_cat_printing")
                    )
                }
            }
        }

        // 4. Popular Photo Frames
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(
                        title = if (showHindi) "Popular Photo Frames / लोकप्रिय फ्रेम" else "Popular Photo Frames",
                        actionLabel = "All Frames",
                        onAction = { onNavigateToCategory(ServiceCategory.PHOTO_FRAMES) }
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(popularFrames, key = { it.id }) { frame ->
                        HorizontalSpotlightCard(
                            service = frame,
                            showHindi = showHindi,
                            onClick = { onSelectService(frame) },
                            onWhatsAppOrder = { onWhatsAppOrderService(frame) }
                        )
                    }
                }
            }
        }

        // 5. Passport Photo Packages
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionHeader(
                    title = if (showHindi) "Passport Photo Packages / पासपोर्ट पैकेज" else "Passport Photo Packages",
                    actionLabel = "View All",
                    onAction = { onNavigateToCategory(ServiceCategory.PASSPORT_PHOTOGRAPHY) }
                )
                passportPackages.forEach { svc ->
                    ServiceItemCard(
                        service = svc,
                        showHindi = showHindi,
                        onSelectDetails = { onSelectService(svc) },
                        onAddToCart = { onAddToCart(svc) },
                        onWhatsAppOrder = { onWhatsAppOrderService(svc) }
                    )
                }
            }
        }

        // 6. Photography Packages
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SectionHeader(
                        title = if (showHindi) "Photography Packages / फोटोग्राफी पैकेज" else "Photography Packages",
                        actionLabel = "All Shoots",
                        onAction = { onNavigateToCategory(ServiceCategory.PHOTOGRAPHY_SERVICES) }
                    )
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photographyPackages, key = { it.id }) { pkg ->
                        HorizontalSpotlightCard(
                            service = pkg,
                            showHindi = showHindi,
                            onClick = { onSelectService(pkg) },
                            onWhatsAppOrder = { onWhatsAppOrderService(pkg) }
                        )
                    }
                }
            }
        }

        // 7. Contact Us Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (showHindi) "Contact Us / संपर्क करें" else "Contact Studio Pixy",
                        style = MaterialTheme.typography.headlineSmall,
                        color = BrightGold
                    )
                    Text(
                        text = "${config.address}\n${config.openingHours}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IvoryText.copy(alpha = 0.88f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { dialStudioPhone(context, config.phoneNumber) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("home_call_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChampagneGold,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
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
                                    "Hello ${config.businessName}! I need assistance with an order."
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("home_contact_whatsapp_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WhatsAppGreen,
                                contentColor = ObsidianBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
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
                                .testTag("home_maps_button"),
                            border = BorderStroke(1.dp, ChampagneGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightGold),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Maps", maxLines = 1)
                        }
                    }
                }
            }
        }

        // 8. Always-visible Admin Login Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onOpenAdmin() }
                    .testTag("home_admin_login_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedCard),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.65f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Login",
                            tint = BrightGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ADMIN LOGIN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrightGold
                        )
                        Text(
                            text = if (showHindi) "Owner Portal • सेवाएं और रेट बदलें" else "Studio owner sign-in to manage services, prices & orders",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedStone
                        )
                    }
                    Button(
                        onClick = onOpenAdmin,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("home_admin_login_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ChampagneGold,
                            contentColor = ObsidianBlack
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Text(
                            text = "ADMIN LOGIN",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = IvoryText,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelLarge,
            color = BrightGold,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onAction() }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CategoryQuickTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = BrightGold, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = IvoryText, maxLines = 1)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MutedStone, maxLines = 1)
            }
        }
    }
}

@Composable
private fun HorizontalSpotlightCard(
    service: StudioService,
    showHindi: Boolean,
    onClick: () -> Unit,
    onWhatsAppOrder: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(216.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ElevatedCard),
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.28f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
            ) {
                StudioImageView(
                    imageUrl = service.imageUrl,
                    presetKey = service.presetKey,
                    contentDescription = service.name,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    color = ObsidianBlack.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = formatInr(service.priceInr),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrightGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = IvoryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (showHindi && service.hindiName.isNotBlank()) {
                    Text(
                        text = service.hindiName,
                        style = MaterialTheme.typography.labelSmall,
                        color = ChampagneGold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = service.unitLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedStone,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onWhatsAppOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen,
                        contentColor = ObsidianBlack
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Order Now", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
