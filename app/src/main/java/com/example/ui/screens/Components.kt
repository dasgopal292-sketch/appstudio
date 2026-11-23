package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.CartItem
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
import com.example.util.formatInr

@Composable
fun StudioPixyLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(ChampagneGold, BrightGold, Color(0xFF9E7B23))
                )
            )
            .border(1.dp, BrightGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Studio Pixy Logo",
            tint = ObsidianBlack,
            modifier = Modifier.size(size * 0.56f)
        )
    }
}

@Composable
fun GoldDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = ChampagneGold.copy(alpha = 0.22f)
    )
}

@Composable
fun StudioImageView(
    imageUrl: String?,
    presetKey: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val fallbackDrawable = remember(presetKey) {
        when (presetKey) {
            "frames" -> R.drawable.img_photo_frames
            "wedding" -> R.drawable.img_wedding_shoot
            "portrait", "passport" -> R.drawable.img_studio_portrait
            else -> R.drawable.img_hero_studio
        }
    }

    val context = LocalContext.current
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            placeholder = painterResource(id = fallbackDrawable),
            error = painterResource(id = fallbackDrawable),
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Image(
            painter = painterResource(id = fallbackDrawable),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun ServiceItemCard(
    service: StudioService,
    showHindi: Boolean,
    onSelectDetails: () -> Unit,
    onAddToCart: () -> Unit,
    onWhatsAppOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("service_card_${service.id}")
            .clickable { onSelectDetails() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudioImageView(
                    imageUrl = service.imageUrl,
                    presetKey = service.presetKey,
                    contentDescription = service.name,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = service.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = IvoryText,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = GoldContainer,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = formatInr(service.priceInr),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = BrightGold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (showHindi && service.hindiName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = service.hindiName,
                            style = MaterialTheme.typography.labelMedium,
                            color = ChampagneGold.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${service.unitLabel} • ${service.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedStone,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("add_to_cart_${service.id}"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.65f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightGold),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = "Add to Cart",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showHindi) "Add / जोड़ें" else "Add to Cart",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Button(
                    onClick = onWhatsAppOrder,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("whatsapp_order_${service.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WhatsAppGreen,
                        contentColor = ObsidianBlack
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "WhatsApp Order",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WhatsApp",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppOrderSheet(
    service: StudioService,
    showHindi: Boolean,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSubmitOrder: (qty: Int, name: String, phone: String, requirements: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var quantity by remember(service.id) { mutableIntStateOf(1) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val totalEstimate = service.priceInr * quantity.toLong()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CharcoalSurface,
        contentColor = IvoryText
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WhatsApp Quick Order",
                        style = MaterialTheme.typography.headlineSmall,
                        color = BrightGold
                    )
                    if (showHindi) {
                        Text(
                            text = "व्हाट्सएप पर तुरंत ऑर्डर भेजें",
                            style = MaterialTheme.typography.labelMedium,
                            color = MutedStone
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_order_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedStone)
                }
            }

            Surface(
                color = ElevatedCard,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(service.name, style = MaterialTheme.typography.titleMedium, color = IvoryText)
                        Text(
                            "${formatInr(service.priceInr)} • ${service.unitLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedStone
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ObsidianBlack)
                                .testTag("sheet_qty_minus")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease quantity", tint = BrightGold)
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IvoryText
                        )
                        IconButton(
                            onClick = { if (quantity < 500) quantity++ },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ObsidianBlack)
                                .testTag("sheet_qty_plus")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase quantity", tint = BrightGold)
                        }
                    }
                }
            }

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ChampagneGold,
                unfocusedBorderColor = SubtleBorder,
                focusedLabelColor = BrightGold,
                unfocusedLabelColor = MutedStone,
                focusedTextColor = IvoryText,
                unfocusedTextColor = IvoryText
            )

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it; validationError = null },
                label = { Text(if (showHindi) "Your Name / आपका नाम *" else "Your Name *") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_customer_name_input")
            )

            OutlinedTextField(
                value = customerPhone,
                onValueChange = { customerPhone = it; validationError = null },
                label = { Text(if (showHindi) "Phone / WhatsApp Number *" else "Phone Number *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_customer_phone_input")
            )

            OutlinedTextField(
                value = requirements,
                onValueChange = { requirements = it },
                label = { Text(if (showHindi) "Custom Requirements / विशेष निर्देश" else "Custom Requirements (Size, Date, Notes)") },
                minLines = 2,
                maxLines = 3,
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("order_requirements_input")
            )

            validationError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Surface(
                color = GoldContainer,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (showHindi) "Estimated Total / अनुमानित कुल" else "Estimated Total",
                        style = MaterialTheme.typography.titleSmall,
                        color = IvoryText
                    )
                    Text(
                        text = formatInr(totalEstimate),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BrightGold
                    )
                }
            }

            Button(
                onClick = {
                    val cleanName = customerName.trim()
                    val digits = customerPhone.filter { it.isDigit() }
                    when {
                        cleanName.length < 2 -> validationError = "Please enter your name (min 2 letters)."
                        digits.length < 7 -> validationError = "Please enter a valid phone number."
                        else -> onSubmitOrder(quantity, cleanName, customerPhone.trim(), requirements.trim())
                    }
                },
                enabled = !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_whatsapp_order_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WhatsAppGreen,
                    contentColor = ObsidianBlack
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBusy) "Saving Order..." else "Send Order on WhatsApp",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
