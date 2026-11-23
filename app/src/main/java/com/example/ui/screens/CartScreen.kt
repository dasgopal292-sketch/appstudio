package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CartItem
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IvoryText
import com.example.ui.theme.MutedStone
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WhatsAppGreen
import com.example.util.formatInr

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    totalInr: Long,
    showHindi: Boolean,
    isBusy: Boolean,
    onUpdateQuantity: (serviceId: String, delta: Int) -> Unit,
    onRemoveItem: (serviceId: String) -> Unit,
    onBrowseServices: () -> Unit,
    onPlaceOrder: (customerName: String, customerPhone: String, requirements: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    if (cartItems.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Empty Cart",
                tint = ChampagneGold.copy(alpha = 0.6f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (showHindi) "Your Cart is Empty / कार्ट खाली है" else "Your Cart is Empty",
                style = MaterialTheme.typography.headlineSmall,
                color = IvoryText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Add Photo Frames, Passport Photos, Printing or Photography packages to calculate your estimate and order on WhatsApp.",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedStone
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onBrowseServices,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("empty_cart_browse_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChampagneGold,
                    contentColor = ObsidianBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Browse Services & Rate List", fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (showHindi) "Your Order Cart / आपका ऑर्डर" else "Your Order Cart",
                style = MaterialTheme.typography.headlineSmall,
                color = BrightGold
            )
        }

        items(cartItems, key = { it.service.id }) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, SubtleBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StudioImageView(
                        imageUrl = item.service.imageUrl,
                        presetKey = item.service.presetKey,
                        contentDescription = item.service.name,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.service.name, style = MaterialTheme.typography.titleSmall, color = IvoryText)
                        Text(
                            "${formatInr(item.service.priceInr)} x ${item.quantity} = ${formatInr(item.lineTotalInr)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrightGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { onUpdateQuantity(item.service.id, -1) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ObsidianBlack)
                                .testTag("cart_minus_${item.service.id}")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = BrightGold, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = item.quantity.toString(),
                            style = MaterialTheme.typography.titleSmall,
                            color = IvoryText,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { onUpdateQuantity(item.service.id, 1) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ObsidianBlack)
                                .testTag("cart_plus_${item.service.id}")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = BrightGold, modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { onRemoveItem(item.service.id) },
                            modifier = Modifier.testTag("cart_remove_${item.service.id}")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Remove item", tint = CrimsonError)
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (showHindi) "Customer Details / ग्राहक जानकारी" else "Customer Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = BrightGold
                    )
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
                        label = { Text(if (showHindi) "Full Name / आपका नाम *" else "Full Name *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_customer_name")
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it; validationError = null },
                        label = { Text(if (showHindi) "Phone Number / मोबाइल नंबर *" else "Phone Number *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_customer_phone")
                    )
                    OutlinedTextField(
                        value = requirements,
                        onValueChange = { requirements = it },
                        label = { Text(if (showHindi) "Custom Requirements / विशेष निर्देश" else "Custom Requirements (Optional)") },
                        minLines = 2,
                        maxLines = 4,
                        colors = fieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_requirements")
                    )

                    validationError?.let {
                        Text(it, color = CrimsonError, style = MaterialTheme.typography.bodyMedium)
                    }

                    Surface(
                        color = GoldContainer,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showHindi) "Estimated Total / अनुमानित कुल" else "Estimated Total",
                                style = MaterialTheme.typography.titleMedium,
                                color = IvoryText
                            )
                            Text(
                                text = formatInr(totalInr),
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
                                else -> onPlaceOrder(cleanName, customerPhone.trim(), requirements.trim())
                            }
                        },
                        enabled = !isBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("cart_whatsapp_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WhatsAppGreen,
                            contentColor = ObsidianBlack
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBusy) "Submitting Order..." else "Order on WhatsApp",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onBrowseServices,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SubtleBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IvoryText)
                    ) {
                        Text("Add More Services")
                    }
                }
            }
        }
    }
}
