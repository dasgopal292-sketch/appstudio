package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.ImageFormatValidator
import com.example.data.ValidatedPhotoPayload
import com.example.data.model.StudioService
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IvoryText
import com.example.ui.theme.MutedStone
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SubtleBorder
import kotlinx.coroutines.launch

@Composable
fun ServicePhotoManagerDialog(
    service: StudioService,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onSavePhoto: (StudioService, ValidatedPhotoPayload, (Boolean, String?) -> Unit) -> Unit,
    onDeletePhoto: (StudioService, (Boolean, String?) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingUri by remember(service.id) { mutableStateOf<Uri?>(null) }
    var pendingPayload by remember(service.id) { mutableStateOf<ValidatedPhotoPayload?>(null) }
    var isReadingImage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                isReadingImage = true
                errorMessage = null
                scope.launch {
                    val result = ImageFormatValidator.readAndValidateFromUri(context, uri)
                    isReadingImage = false
                    result.onSuccess { payload ->
                        pendingUri = uri
                        pendingPayload = payload
                        errorMessage = null
                    }.onFailure { err ->
                        pendingUri = null
                        pendingPayload = null
                        errorMessage = err.localizedMessage ?: "Unsupported image. Choose JPG, JPEG, or PNG."
                    }
                }
            }
        }
    )

    val launchGalleryPicker: () -> Unit = {
        galleryLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val hasSavedCustomPhoto = !service.imageUrl.isNullOrBlank()
    val hasPendingPreview = pendingUri != null && pendingPayload != null
    val operationActive = isBusy || isReadingImage

    AlertDialog(
        onDismissRequest = { if (!operationActive) onDismiss() },
        containerColor = CharcoalSurface,
        titleContentColor = BrightGold,
        textContentColor = IvoryText,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Service Photo Manager", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedStone
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Photo Preview Area (shows pending local gallery image BEFORE saving, or existing cloud/default image)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ObsidianBlack)
                        .border(1.dp, ChampagneGold.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .testTag("photo_preview_box")
                ) {
                    if (hasPendingPreview) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(pendingUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected Photo Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        StudioImageView(
                            imageUrl = service.imageUrl,
                            presetKey = service.presetKey,
                            contentDescription = service.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    val badgeText = when {
                        hasPendingPreview -> "Preview (Unsaved) • ${pendingPayload!!.format.displayLabel} • ${pendingPayload!!.formattedSize}"
                        hasSavedCustomPhoto -> "Saved in Firebase Storage"
                        else -> "Default Studio Preview"
                    }
                    val badgeColor = if (hasPendingPreview) EmeraldSuccess else BrightGold

                    Surface(
                        color = ObsidianBlack.copy(alpha = 0.84f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (hasPendingPreview) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (operationActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ObsidianBlack.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = BrightGold)
                                Text(
                                    text = if (isReadingImage) "Validating photo..." else "Syncing with Firebase...",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = IvoryText
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Supported formats: JPG, JPEG, PNG (max 10 MB). Preview before saving.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedStone
                )

                errorMessage?.let { msg ->
                    Surface(
                        color = CrimsonError.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = msg,
                            color = CrimsonError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        )
                    }
                }

                // Primary Gallery / Replace / Delete actions
                if (hasSavedCustomPhoto || hasPendingPreview) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = launchGalleryPicker,
                            enabled = !operationActive,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ChampagneGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrightGold),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("photo_replace_button")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replace Photo", maxLines = 1, style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                if (hasPendingPreview && !hasSavedCustomPhoto) {
                                    pendingUri = null
                                    pendingPayload = null
                                    errorMessage = null
                                } else {
                                    errorMessage = null
                                    onDeletePhoto(service) { success, err ->
                                        if (success) {
                                            pendingUri = null
                                            pendingPayload = null
                                            onDismiss()
                                        } else {
                                            errorMessage = err ?: "Could not delete photo."
                                        }
                                    }
                                }
                            },
                            enabled = !operationActive,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.75f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("photo_delete_button")
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Photo", maxLines = 1, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                } else {
                    Button(
                        onClick = launchGalleryPicker,
                        enabled = !operationActive,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldContainer,
                            contentColor = BrightGold
                        ),
                        border = BorderStroke(1.dp, ChampagneGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("photo_select_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Photo from Gallery", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            if (hasPendingPreview) {
                Button(
                    onClick = {
                        val payload = pendingPayload ?: return@Button
                        errorMessage = null
                        onSavePhoto(service, payload) { success, err ->
                            if (success) {
                                onDismiss()
                            } else {
                                errorMessage = err ?: "Photo upload failed."
                            }
                        }
                    },
                    enabled = !operationActive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChampagneGold,
                        contentColor = ObsidianBlack
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("photo_save_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Photo", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !operationActive,
                modifier = Modifier.testTag("photo_cancel_button")
            ) {
                Text(if (hasPendingPreview) "Cancel" else "Close", color = MutedStone)
            }
        }
    )
}
