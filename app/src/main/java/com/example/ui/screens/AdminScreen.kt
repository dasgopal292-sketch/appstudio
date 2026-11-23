package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.auth.onAdminGoogleSignInClicked
import com.example.auth.signOutAdmin
import com.example.data.ImageFormatValidator
import com.example.data.StudioRepository
import com.example.data.ValidatedPhotoPayload
import com.example.data.model.CustomerOrder
import com.example.data.model.OrderStatus
import com.example.data.model.ServiceCategory
import com.example.data.model.SpecialOffer
import com.example.data.model.StudioConfig
import com.example.data.model.StudioService
import com.example.ui.AdminSessionState
import com.example.ui.UiState
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IvoryText
import com.example.ui.theme.MutedStone
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.WhatsAppGreen
import com.example.util.dialStudioPhone
import com.example.util.formatInr
import com.example.util.openWhatsAppWithMessage
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(
    adminSession: AdminSessionState,
    services: List<StudioService>,
    isCloudCatalogSeeded: Boolean,
    ordersState: UiState<List<CustomerOrder>>,
    config: StudioConfig,
    offers: List<SpecialOffer>,
    isBusy: Boolean,
    onBackToStore: () -> Unit,
    onSeedCatalog: () -> Unit,
    onSaveService: (StudioService, Boolean) -> Unit,
    onDeleteService: (String) -> Unit,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onDeleteOrder: (String) -> Unit,
    onSaveConfig: (StudioConfig) -> Unit,
    onSaveOffer: (SpecialOffer) -> Unit,
    onDeleteOffer: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSaveServiceWithPhoto: (StudioService, Boolean, ValidatedPhotoPayload?) -> Unit = { svc, upd, _ -> onSaveService(svc, upd) },
    onUploadServicePhoto: (StudioService, ValidatedPhotoPayload, (Boolean, String?) -> Unit) -> Unit = { _, _, cb -> cb(false, "Storage unavailable") },
    onDeleteServicePhoto: (StudioService, (Boolean, String?) -> Unit) -> Unit = { _, cb -> cb(false, "Storage unavailable") }
) {
    BackHandler { onBackToStore() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Header Bar for Admin
        Surface(color = CharcoalSurface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToStore,
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Customer App",
                            tint = BrightGold
                        )
                    }
                    Text("Studio Pixy Admin", style = MaterialTheme.typography.titleLarge, color = BrightGold)
                }
                if (adminSession.user != null) {
                    IconButton(
                        onClick = { signOutAdmin(credentialManager, scope) },
                        modifier = Modifier.testTag("admin_signout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign out", tint = CrimsonError)
                    }
                }
            }
        }

        when {
            adminSession.isChecking -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = BrightGold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Verifying admin credentials...", color = MutedStone)
                }
            }

            adminSession.user == null -> {
                AdminSignInGate(
                    isLoading = isSigningIn,
                    error = authError,
                    onSignInClick = {
                        isSigningIn = true
                        authError = null
                        onAdminGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            scope = scope,
                            onAuthSuccess = { isSigningIn = false },
                            onAuthError = { msg -> isSigningIn = false; authError = msg },
                            onAuthCancelled = { isSigningIn = false }
                        )
                    }
                )
            }

            !adminSession.isAuthorizedAdmin -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Access Restricted", style = MaterialTheme.typography.headlineSmall, color = IvoryText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Signed in as ${adminSession.user.email ?: "unknown"}, which is not an authorized Studio Pixy Admin (${StudioRepository.BOOTSTRAP_ADMIN_EMAIL}).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedStone
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { signOutAdmin(credentialManager, scope) },
                        colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Sign Out & Switch Account", fontWeight = FontWeight.Bold)
                    }
                }
            }

            else -> {
                AdminDashboardContent(
                    services = services,
                    isCloudCatalogSeeded = isCloudCatalogSeeded,
                    ordersState = ordersState,
                    config = config,
                    offers = offers,
                    isBusy = isBusy,
                    onSeedCatalog = onSeedCatalog,
                    onSaveService = onSaveService,
                    onSaveServiceWithPhoto = onSaveServiceWithPhoto,
                    onUploadServicePhoto = onUploadServicePhoto,
                    onDeleteServicePhoto = onDeleteServicePhoto,
                    onDeleteService = onDeleteService,
                    onUpdateOrderStatus = onUpdateOrderStatus,
                    onDeleteOrder = onDeleteOrder,
                    onSaveConfig = onSaveConfig,
                    onSaveOffer = onSaveOffer,
                    onDeleteOffer = onDeleteOffer
                )
            }
        }
    }
}

@Composable
private fun AdminSignInGate(
    isLoading: Boolean,
    error: String?,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        StudioPixyLogo(size = 64.dp)
        Spacer(modifier = Modifier.height(18.dp))
        Text("Admin Portal Login", style = MaterialTheme.typography.headlineMedium, color = BrightGold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Only authorized Studio Pixy owners can edit live service rates, view customer orders, or update studio contact details.",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedStone
        )
        Spacer(modifier = Modifier.height(22.dp))
        Button(
            onClick = onSignInClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("admin_google_signin_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = ObsidianBlack, strokeWidth = 2.dp)
            } else {
                Text("Sign in with Google (Admin)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = CrimsonError, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            border = BorderStroke(1.dp, SubtleBorder)
        ) {
            Text(
                text = "Security Note: Firebase Auth here is provisioned with Google Sign-In (${StudioRepository.BOOTSTRAP_ADMIN_EMAIL}). If you also want Email/Password sign-in, enable Email/Password provider in Firebase Console.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedStone,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
internal fun AdminDashboardContent(
    services: List<StudioService>,
    isCloudCatalogSeeded: Boolean,
    ordersState: UiState<List<CustomerOrder>>,
    config: StudioConfig,
    offers: List<SpecialOffer>,
    isBusy: Boolean,
    onSeedCatalog: () -> Unit,
    onSaveService: (StudioService, Boolean) -> Unit,
    onDeleteService: (String) -> Unit,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onDeleteOrder: (String) -> Unit,
    onSaveConfig: (StudioConfig) -> Unit,
    onSaveOffer: (SpecialOffer) -> Unit,
    onDeleteOffer: (String) -> Unit,
    onSaveServiceWithPhoto: (StudioService, Boolean, ValidatedPhotoPayload?) -> Unit = { svc, upd, _ -> onSaveService(svc, upd) },
    onUploadServicePhoto: (StudioService, ValidatedPhotoPayload, (Boolean, String?) -> Unit) -> Unit = { _, _, cb -> cb(false, "Storage unavailable") },
    onDeleteServicePhoto: (StudioService, (Boolean, String?) -> Unit) -> Unit = { _, cb -> cb(false, "Storage unavailable") }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Services & Prices", "Orders", "Studio & Offers")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CharcoalSurface,
            contentColor = BrightGold,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BrightGold
                )
            }
        ) {
            tabs.forEachIndexed { idx, label ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.testTag("admin_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> AdminServicesTab(
                services = services,
                isCloudCatalogSeeded = isCloudCatalogSeeded,
                isBusy = isBusy,
                onSeedCatalog = onSeedCatalog,
                onSaveServiceWithPhoto = onSaveServiceWithPhoto,
                onUploadServicePhoto = onUploadServicePhoto,
                onDeleteServicePhoto = onDeleteServicePhoto,
                onDeleteService = onDeleteService
            )
            1 -> AdminOrdersTab(
                ordersState = ordersState,
                onUpdateOrderStatus = onUpdateOrderStatus,
                onDeleteOrder = onDeleteOrder
            )
            2 -> AdminStudioConfigTab(
                config = config,
                offers = offers,
                isBusy = isBusy,
                onSaveConfig = onSaveConfig,
                onSaveOffer = onSaveOffer,
                onDeleteOffer = onDeleteOffer
            )
        }
    }
}

@Composable
private fun AdminServicesTab(
    services: List<StudioService>,
    isCloudCatalogSeeded: Boolean,
    isBusy: Boolean,
    onSeedCatalog: () -> Unit,
    onSaveServiceWithPhoto: (StudioService, Boolean, ValidatedPhotoPayload?) -> Unit,
    onUploadServicePhoto: (StudioService, ValidatedPhotoPayload, (Boolean, String?) -> Unit) -> Unit,
    onDeleteServicePhoto: (StudioService, (Boolean, String?) -> Unit) -> Unit,
    onDeleteService: (String) -> Unit
) {
    var editingService by remember { mutableStateOf<StudioService?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var filterCat by remember { mutableStateOf<ServiceCategory?>(null) }
    var servicePendingDelete by remember { mutableStateOf<StudioService?>(null) }
    var photoManagingService by remember { mutableStateOf<StudioService?>(null) }

    val displayed = services.filter { filterCat == null || it.categoryEnum() == filterCat }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        isCreatingNew = true
                        editingService = StudioService(
                            category = (filterCat ?: ServiceCategory.PHOTO_FRAMES).name,
                            priceInr = 250L,
                            unitLabel = "Per Item",
                            presetKey = "frames",
                            sortOrder = (services.size + 1).toLong()
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("admin_add_service_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Service", fontWeight = FontWeight.Bold)
                }

                if (!isCloudCatalogSeeded) {
                    OutlinedButton(
                        onClick = onSeedCatalog,
                        enabled = !isBusy,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("admin_sync_catalog_button"),
                        border = BorderStroke(1.dp, BrightGold),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Cloud")
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = filterCat == null,
                        onClick = { filterCat = null },
                        label = { Text("All (${services.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldContainer,
                            selectedLabelColor = BrightGold
                        )
                    )
                }
                items(ServiceCategory.entries) { cat ->
                    FilterChip(
                        selected = filterCat == cat,
                        onClick = { filterCat = if (filterCat == cat) null else cat },
                        label = { Text(cat.shortLabel) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldContainer,
                            selectedLabelColor = BrightGold
                        )
                    )
                }
            }
        }

        items(displayed, key = { it.id }) { service ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isCreatingNew = false
                        editingService = service
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                border = BorderStroke(1.dp, SubtleBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StudioImageView(
                            imageUrl = service.imageUrl,
                            presetKey = service.presetKey,
                            contentDescription = service.name,
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, ChampagneGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(service.name, style = MaterialTheme.typography.titleSmall, color = IvoryText)
                            Text(
                                "${formatInr(service.priceInr)} • ${service.unitLabel} • ${if (service.available) "Active" else "Hidden"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrightGold
                            )
                            if (!service.imageUrl.isNullOrBlank()) {
                                Surface(
                                    color = GoldContainer,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = "Custom Cloud Photo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrightGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { isCreatingNew = false; editingService = service },
                            modifier = Modifier.testTag("admin_edit_service_${service.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit service", tint = BrightGold)
                        }
                        IconButton(
                            onClick = { servicePendingDelete = service },
                            modifier = Modifier.testTag("admin_delete_service_${service.id}")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete service", tint = CrimsonError)
                        }
                    }

                    OutlinedButton(
                        onClick = { photoManagingService = service },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = GoldContainer.copy(alpha = 0.5f),
                            contentColor = BrightGold
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_change_photo_${service.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Change Photo",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Change Photo",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    photoManagingService?.let { svc ->
        val latestService = services.firstOrNull { it.id == svc.id } ?: svc
        ServicePhotoManagerDialog(
            service = latestService,
            isBusy = isBusy,
            onDismiss = { photoManagingService = null },
            onSavePhoto = onUploadServicePhoto,
            onDeletePhoto = onDeleteServicePhoto
        )
    }

    servicePendingDelete?.let { svc ->
        AlertDialog(
            onDismissRequest = { servicePendingDelete = null },
            containerColor = CharcoalSurface,
            titleContentColor = BrightGold,
            textContentColor = IvoryText,
            title = { Text("Delete Service?") },
            text = { Text("Remove \"${svc.name}\" from the Studio Pixy rate list?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteService(svc.id)
                        servicePendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonError, contentColor = IvoryText)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { servicePendingDelete = null }) {
                    Text("Cancel", color = MutedStone)
                }
            }
        )
    }

    editingService?.let { svc ->
        ServiceEditorDialog(
            initial = svc,
            isNew = isCreatingNew,
            isBusy = isBusy,
            onDismiss = { editingService = null },
            onSave = { updated, pendingPhoto ->
                onSaveServiceWithPhoto(updated, !isCreatingNew, pendingPhoto)
                editingService = null
            },
            onDeleteSavedPhoto = { targetSvc, cb ->
                onDeleteServicePhoto(targetSvc, cb)
            }
        )
    }
}

@Composable
private fun ServiceEditorDialog(
    initial: StudioService,
    isNew: Boolean,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSave: (StudioService, ValidatedPhotoPayload?) -> Unit,
    onDeleteSavedPhoto: (StudioService, (Boolean, String?) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(initial.name) }
    var hindiName by remember { mutableStateOf(initial.hindiName) }
    var category by remember { mutableStateOf(initial.categoryEnum()) }
    var priceText by remember { mutableStateOf(initial.priceInr.toString()) }
    var unitLabel by remember { mutableStateOf(initial.unitLabel) }
    var description by remember { mutableStateOf(initial.description) }
    var imageUrl by remember { mutableStateOf(initial.imageUrl ?: "") }
    var presetKey by remember { mutableStateOf(initial.presetKey ?: "frames") }
    var available by remember { mutableStateOf(initial.available) }
    var pendingPreviewUri by remember { mutableStateOf<Uri?>(null) }
    var pendingPayload by remember { mutableStateOf<ValidatedPhotoPayload?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val validated = ImageFormatValidator.readAndValidateFromUri(context, uri)
                    validated.onSuccess { payload ->
                        pendingPreviewUri = uri
                        pendingPayload = payload
                        error = null
                    }.onFailure { err ->
                        error = err.localizedMessage ?: "Only JPG, JPEG, and PNG images are supported."
                    }
                }
            }
        }
    )

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
        title = { Text(if (isNew) "Add New Service" else "Edit Service & Price") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(440.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        label = { Text("Service Name *") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth().testTag("editor_service_name")
                    )
                }
                item {
                    OutlinedTextField(
                        value = hindiName,
                        onValueChange = { hindiName = it },
                        label = { Text("Hindi Subtitle") },
                        singleLine = true,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = MutedStone)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(ServiceCategory.entries) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.shortLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldContainer,
                                    selectedLabelColor = BrightGold
                                )
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter { c -> c.isDigit() }; error = null },
                            label = { Text("Price (₹) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.weight(1f).testTag("editor_service_price")
                        )
                        OutlinedTextField(
                            value = unitLabel,
                            onValueChange = { unitLabel = it },
                            label = { Text("Unit") },
                            singleLine = true,
                            colors = fieldColors,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        minLines = 2,
                        maxLines = 3,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Service Photo Preview (JPG, JPEG, PNG)", style = MaterialTheme.typography.labelMedium, color = MutedStone)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianBlack)
                                .border(1.dp, ChampagneGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        ) {
                            if (pendingPreviewUri != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context).data(pendingPreviewUri).crossfade(true).build(),
                                    contentDescription = "Selected photo preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                StudioImageView(
                                    imageUrl = imageUrl.ifBlank { null },
                                    presetKey = presetKey,
                                    contentDescription = name,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                border = BorderStroke(1.dp, ChampagneGold),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightGold),
                                modifier = Modifier.weight(1f).testTag("editor_pick_photo_button")
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (pendingPreviewUri != null || imageUrl.isNotBlank()) "Replace Photo" else "Pick Photo", maxLines = 1)
                            }

                            if (pendingPreviewUri != null || imageUrl.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        if (pendingPreviewUri != null) {
                                            pendingPreviewUri = null
                                            pendingPayload = null
                                        } else if (!isNew && initial.imageUrl != null) {
                                            onDeleteSavedPhoto(initial) { ok, err ->
                                                if (ok) imageUrl = "" else error = err
                                            }
                                        } else {
                                            imageUrl = ""
                                        }
                                    },
                                    border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.7f)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                                    modifier = Modifier.weight(1f).testTag("editor_delete_photo_button")
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete Photo", maxLines = 1)
                                }
                            }
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Visible to Customers", color = IvoryText)
                        Switch(checked = available, onCheckedChange = { available = it })
                    }
                }
                error?.let { errMsg ->
                    item { Text(errMsg, color = CrimsonError, style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toLongOrNull()
                    when {
                        name.trim().length < 2 -> error = "Service name must be at least 2 characters."
                        price == null || price < 0 -> error = "Enter a valid price in ₹."
                        else -> onSave(
                            initial.copy(
                                name = name.trim(),
                                hindiName = hindiName.trim().ifBlank { name.trim() },
                                category = category.name,
                                priceInr = price,
                                unitLabel = unitLabel.trim().ifBlank { "Per Item" },
                                description = description.trim().ifBlank { name.trim() },
                                imageUrl = imageUrl.trim().ifBlank { null },
                                presetKey = presetKey,
                                available = available
                            ),
                            pendingPayload
                        )
                    }
                },
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                modifier = Modifier.testTag("editor_save_service_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MutedStone)
            }
        }
    )
}

@Composable
private fun AdminOrdersTab(
    ordersState: UiState<List<CustomerOrder>>,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onDeleteOrder: (String) -> Unit
) {
    val context = LocalContext.current
    when (ordersState) {
        is UiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = BrightGold)
            }
        }
        is UiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(ordersState.message, color = CrimsonError)
            }
        }
        is UiState.Success -> {
            val orders = ordersState.data
            if (orders.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No customer orders yet", style = MaterialTheme.typography.titleMedium, color = IvoryText)
                    Text(
                        "Orders placed via WhatsApp Quick Order or Cart will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedStone
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.id }) { order ->
                        val status = order.statusEnum()
                        val badgeColor = when (status) {
                            OrderStatus.PENDING -> AmberPending
                            OrderStatus.CONFIRMED -> BrightGold
                            OrderStatus.COMPLETED -> EmeraldSuccess
                        }
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                            border = BorderStroke(1.dp, SubtleBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(order.customerName, style = MaterialTheme.typography.titleMedium, color = IvoryText)
                                        Text(order.customerPhone, style = MaterialTheme.typography.bodySmall, color = MutedStone)
                                    }
                                    Surface(
                                        color = badgeColor.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, badgeColor)
                                    ) {
                                        Text(
                                            text = status.label.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = badgeColor,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Text(order.itemsSummary, style = MaterialTheme.typography.bodyMedium, color = IvoryText.copy(alpha = 0.9f))
                                if (!order.requirements.isNullOrBlank()) {
                                    Text("Notes: ${order.requirements}", style = MaterialTheme.typography.bodySmall, color = ChampagneGold)
                                }
                                Text(
                                    "Total: ${formatInr(order.totalInr)} (${order.itemCount} items)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = BrightGold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OrderStatus.entries.forEach { target ->
                                        FilterChip(
                                            selected = status == target,
                                            onClick = { onUpdateOrderStatus(order.id, target) },
                                            label = { Text(target.label, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = GoldContainer,
                                                selectedLabelColor = BrightGold
                                            )
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { dialStudioPhone(context, order.customerPhone) },
                                        border = BorderStroke(1.dp, SubtleBorder)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Call")
                                    }
                                    Button(
                                        onClick = {
                                            openWhatsAppWithMessage(
                                                context,
                                                order.customerPhone,
                                                "Hello ${order.customerName}, your Studio Pixy order (${order.itemsSummary}) is currently ${status.label}."
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen, contentColor = ObsidianBlack)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reply")
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = { onDeleteOrder(order.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete order", tint = CrimsonError)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStudioConfigTab(
    config: StudioConfig,
    offers: List<SpecialOffer>,
    isBusy: Boolean,
    onSaveConfig: (StudioConfig) -> Unit,
    onSaveOffer: (SpecialOffer) -> Unit,
    onDeleteOffer: (String) -> Unit
) {
    var businessName by remember(config) { mutableStateOf(config.businessName) }
    var tagline by remember(config) { mutableStateOf(config.tagline) }
    var whatsapp by remember(config) { mutableStateOf(config.whatsappNumber) }
    var phone by remember(config) { mutableStateOf(config.phoneNumber) }
    var address by remember(config) { mutableStateOf(config.address) }
    var email by remember(config) { mutableStateOf(config.email) }
    var hours by remember(config) { mutableStateOf(config.openingHours) }
    var mapsQuery by remember(config) { mutableStateOf(config.mapsQuery) }
    var offerTitle by remember(config) { mutableStateOf(config.specialOfferTitle) }
    var offerDetails by remember(config) { mutableStateOf(config.specialOfferDetails) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ChampagneGold,
        unfocusedBorderColor = SubtleBorder,
        focusedLabelColor = BrightGold,
        unfocusedLabelColor = MutedStone,
        focusedTextColor = IvoryText,
        unfocusedTextColor = IvoryText
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Business & Contact Settings", style = MaterialTheme.typography.titleLarge, color = BrightGold)
        }
        item { OutlinedTextField(businessName, { businessName = it }, label = { Text("Business Name") }, colors = fieldColors, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(tagline, { tagline = it }, label = { Text("Tagline") }, colors = fieldColors, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(whatsapp, { whatsapp = it }, label = { Text("WhatsApp Number (with +91)") }, colors = fieldColors, modifier = Modifier.fillMaxWidth().testTag("admin_whatsapp_input")) }
        item { OutlinedTextField(phone, { phone = it }, label = { Text("Contact Phone Number") }, colors = fieldColors, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(address, { address = it }, label = { Text("Studio Address") }, colors = fieldColors, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(email, { email = it }, label = { Text("Business Email") }, colors = fieldColors, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(hours, { hours = it }, label = { Text("Opening Hours") }, colors = fieldColors, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(mapsQuery, { mapsQuery = it }, label = { Text("Google Maps Search / Coordinates") }, colors = fieldColors, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(offerTitle, { offerTitle = it }, label = { Text("Home Banner Offer Title") }, colors = fieldColors, modifier = Modifier.fillMaxWidth()) }
        item { OutlinedTextField(offerDetails, { offerDetails = it }, label = { Text("Home Banner Offer Details") }, colors = fieldColors, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(
                onClick = {
                    onSaveConfig(
                        config.copy(
                            businessName = businessName,
                            tagline = tagline,
                            whatsappNumber = whatsapp,
                            phoneNumber = phone,
                            address = address,
                            email = email,
                            openingHours = hours,
                            mapsQuery = mapsQuery,
                            specialOfferTitle = offerTitle,
                            specialOfferDetails = offerDetails
                        )
                    )
                },
                enabled = !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("admin_save_config_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianBlack),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Studio Details", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Promotional Offers", style = MaterialTheme.typography.titleLarge, color = BrightGold)
        }
        items(offers, key = { it.id }) { offer ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedCard),
                border = BorderStroke(1.dp, SubtleBorder)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(offer.title, style = MaterialTheme.typography.titleSmall, color = IvoryText)
                        Text("${offer.badge} • Code: ${offer.promoCode}", style = MaterialTheme.typography.bodySmall, color = BrightGold)
                    }
                    Switch(
                        checked = offer.active,
                        onCheckedChange = { checked -> onSaveOffer(offer.copy(active = checked)) }
                    )
                    IconButton(onClick = { onDeleteOffer(offer.id) }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete offer", tint = CrimsonError)
                    }
                }
            }
        }
    }
}
