package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.StudioService
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IvoryText
import com.example.ui.theme.MutedStone
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WhatsAppGreen
import com.example.util.formatInr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailSheet(
    service: StudioService,
    showHindi: Boolean,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit,
    onWhatsAppOrder: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val category = service.categoryEnum()

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = GoldContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (showHindi) "${category.title} • ${category.hindiTitle}" else category.title,
                        style = MaterialTheme.typography.labelMedium,
                        color = BrightGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MutedStone)
                }
            }

            StudioImageView(
                imageUrl = service.imageUrl,
                presetKey = service.presetKey,
                contentDescription = service.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Text(service.name, style = MaterialTheme.typography.headlineSmall, color = IvoryText)
            if (showHindi && service.hindiName.isNotBlank()) {
                Text(service.hindiName, style = MaterialTheme.typography.titleSmall, color = ChampagneGold)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = formatInr(service.priceInr),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrightGold
                )
                Text("• ${service.unitLabel}", style = MaterialTheme.typography.titleSmall, color = MutedStone)
            }

            Text(service.description, style = MaterialTheme.typography.bodyLarge, color = IvoryText.copy(alpha = 0.9f))

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onAddToCart()
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("detail_add_to_cart_button"),
                    border = BorderStroke(1.dp, ChampagneGold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add to Cart")
                }

                Button(
                    onClick = {
                        onDismiss()
                        onWhatsAppOrder()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("detail_whatsapp_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = ObsidianBlack),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
