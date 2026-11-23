package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ServiceCategory
import com.example.data.model.StudioService
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IvoryText
import com.example.ui.theme.MutedStone
import com.example.ui.theme.SubtleBorder

@Composable
fun ServicesScreen(
    services: List<StudioService>,
    searchQuery: String,
    selectedCategory: ServiceCategory?,
    showHindi: Boolean,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (ServiceCategory?) -> Unit,
    onSelectService: (StudioService) -> Unit,
    onAddToCart: (StudioService) -> Unit,
    onWhatsAppOrder: (StudioService) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (showHindi) "Services & Rate List / सेवाएं और रेट लिस्ट" else "Services & Live Rate List",
                style = MaterialTheme.typography.headlineSmall,
                color = BrightGold
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        if (showHindi) "Search frames, passport, wedding, prints..." else "Search services...",
                        color = MutedStone
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = BrightGold) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = MutedStone)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChampagneGold,
                    unfocusedBorderColor = SubtleBorder,
                    focusedContainerColor = CharcoalSurface,
                    unfocusedContainerColor = CharcoalSurface,
                    focusedTextColor = IvoryText,
                    unfocusedTextColor = IvoryText
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("services_search_input")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) },
                        label = { Text("All (${services.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldContainer,
                            selectedLabelColor = BrightGold,
                            containerColor = CharcoalSurface,
                            labelColor = MutedStone
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (selectedCategory == null) ChampagneGold else SubtleBorder
                        ),
                        modifier = Modifier.testTag("filter_chip_all")
                    )
                }
                items(ServiceCategory.entries) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(if (isSelected) null else cat) },
                        label = {
                            Text(if (showHindi) "${cat.shortLabel} (${cat.hindiTitle})" else cat.title)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldContainer,
                            selectedLabelColor = BrightGold,
                            containerColor = CharcoalSurface,
                            labelColor = MutedStone
                        ),
                        border = BorderStroke(1.dp, if (isSelected) ChampagneGold else SubtleBorder),
                        modifier = Modifier.testTag("filter_chip_${cat.name.lowercase()}")
                    )
                }
            }
        }

        if (services.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = ChampagneGold.copy(alpha = 0.5f),
                    modifier = Modifier.height(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No matching services found",
                    style = MaterialTheme.typography.titleMedium,
                    color = IvoryText
                )
                Text(
                    text = "Try clearing the search filter or select All categories.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedStone
                )
            }
        } else {
            val grouped = if (selectedCategory != null) {
                mapOf(selectedCategory to services)
            } else {
                ServiceCategory.entries.associateWith { cat ->
                    services.filter { it.categoryEnum() == cat }
                }.filterValues { it.isNotEmpty() }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                grouped.forEach { (category, list) ->
                    item(key = "header_${category.name}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showHindi) "${category.title} • ${category.hindiTitle}" else category.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = BrightGold
                            )
                            Text(
                                text = "${list.size} items",
                                style = MaterialTheme.typography.labelMedium,
                                color = MutedStone
                            )
                        }
                    }
                    items(list, key = { it.id }) { service ->
                        ServiceItemCard(
                            service = service,
                            showHindi = showHindi,
                            onSelectDetails = { onSelectService(service) },
                            onAddToCart = { onAddToCart(service) },
                            onWhatsAppOrder = { onWhatsAppOrder(service) }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}
