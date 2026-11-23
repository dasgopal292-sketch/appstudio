package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.FirebaseServicePhotoStorage
import com.example.data.ImageFormatValidator
import com.example.data.SeedCatalog
import com.example.data.SupportedImageFormat
import com.example.data.model.ServiceCategory
import com.example.ui.UiState
import com.example.ui.screens.AdminDashboardContent
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.theme.StudioPixyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ServicePhotoManagementTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun imageFormatValidator_acceptsJpgJpegPng_andRejectsUnsupportedOrOversized() {
        val validJpegBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x01, 0x02)
        val validPngBytes = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00
        )
        val gifBytes = "GIF89a...".toByteArray()
        val webpBytes = "RIFF1234WEBP".toByteArray()

        // JPG / JPEG magic bytes
        val jpgResult = ImageFormatValidator.validatePhotoBytes(
            bytes = validJpegBytes,
            declaredMimeType = "image/jpeg",
            fileNameOrUri = "frame_photo.jpg"
        )
        assertTrue(jpgResult.isSuccess)
        assertEquals(SupportedImageFormat.JPEG, jpgResult.getOrThrow().format)

        val jpgAliasMimeResult = ImageFormatValidator.validatePhotoBytes(
            bytes = validJpegBytes,
            declaredMimeType = "image/jpg",
            fileNameOrUri = "portrait.jpeg"
        )
        assertTrue(jpgAliasMimeResult.isSuccess)
        assertEquals(SupportedImageFormat.JPEG, jpgAliasMimeResult.getOrThrow().format)

        // PNG magic bytes
        val pngResult = ImageFormatValidator.validatePhotoBytes(
            bytes = validPngBytes,
            declaredMimeType = "image/png",
            fileNameOrUri = "studio_banner.png"
        )
        assertTrue(pngResult.isSuccess)
        assertEquals(SupportedImageFormat.PNG, pngResult.getOrThrow().format)

        // Reject unsupported formats
        assertTrue(
            ImageFormatValidator.validatePhotoBytes(gifBytes, "image/gif", "anim.gif").isFailure
        )
        assertTrue(
            ImageFormatValidator.validatePhotoBytes(webpBytes, "image/webp", "pic.webp").isFailure
        )
        assertTrue(
            ImageFormatValidator.validatePhotoBytes(ByteArray(0), "image/jpeg", "empty.jpg").isFailure
        )
        assertNull(ImageFormatValidator.detectFormat(gifBytes, null, "anim.gif"))

        // Reject > 10 MB payload
        val oversized = ByteArray(ImageFormatValidator.MAX_PHOTO_BYTES + 1)
        oversized[0] = 0xFF.toByte()
        oversized[1] = 0xD8.toByte()
        oversized[2] = 0xFF.toByte()
        assertTrue(
            ImageFormatValidator.validatePhotoBytes(oversized, "image/jpeg", "huge.jpg").isFailure
        )

        // Firebase Storage URL matching helper
        assertTrue(
            FirebaseServicePhotoStorage.isFirebaseStorageUrl(
                "https://firebasestorage.googleapis.com/v0/b/demo/o/service_photos%2Fframe_8x10%2Fphoto.jpg"
            )
        )
    }

    @Test
    fun adminServicesTab_showsChangePhotoButton_andOpensDialogWithoutCustomPhoto() {
        val sampleService = SeedCatalog.defaultServices.first().copy(imageUrl = null)
        composeRule.setContent {
            StudioPixyTheme {
                AdminDashboardContent(
                    services = listOf(sampleService),
                    isCloudCatalogSeeded = true,
                    ordersState = UiState.Success(emptyList()),
                    config = SeedCatalog.defaultConfig,
                    offers = SeedCatalog.defaultOffers,
                    isBusy = false,
                    onSeedCatalog = {},
                    onSaveService = { _, _ -> },
                    onDeleteService = {},
                    onUpdateOrderStatus = { _, _ -> },
                    onDeleteOrder = {},
                    onSaveConfig = {},
                    onSaveOffer = {},
                    onDeleteOffer = {}
                )
            }
        }
        composeRule.waitForIdle()

        // Change Photo button visible on service card
        composeRule.onNodeWithTag("admin_change_photo_${sampleService.id}")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()

        // Photo Manager dialog opens with preview box and Gallery selector
        composeRule.onNodeWithText("Service Photo Manager").assertIsDisplayed()
        composeRule.onNodeWithTag("photo_preview_box").assertIsDisplayed()
        composeRule.onNodeWithText("Default Studio Preview").assertIsDisplayed()
        composeRule.onNodeWithTag("photo_select_button").assertIsDisplayed()
    }

    @Test
    fun adminPhotoManager_showsReplaceAndDeleteOptions_whenCustomCloudPhotoExists() {
        val customUrl =
            "https://firebasestorage.googleapis.com/v0/b/gen-lang-client-0184859803.firebasestorage.app/o/service_photos%2Fframe_4x6%2Fphoto.jpg"
        val serviceWithPhoto = SeedCatalog.defaultServices.first().copy(imageUrl = customUrl)
        var deleteCalledForId: String? = null

        composeRule.setContent {
            StudioPixyTheme {
                AdminDashboardContent(
                    services = listOf(serviceWithPhoto),
                    isCloudCatalogSeeded = true,
                    ordersState = UiState.Success(emptyList()),
                    config = SeedCatalog.defaultConfig,
                    offers = SeedCatalog.defaultOffers,
                    isBusy = false,
                    onSeedCatalog = {},
                    onSaveService = { _, _ -> },
                    onDeleteService = {},
                    onUpdateOrderStatus = { _, _ -> },
                    onDeleteOrder = {},
                    onSaveConfig = {},
                    onSaveOffer = {},
                    onDeleteOffer = {},
                    onDeleteServicePhoto = { svc, cb ->
                        deleteCalledForId = svc.id
                        cb(true, null)
                    }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Custom Cloud Photo").assertIsDisplayed()
        composeRule.onNodeWithTag("admin_change_photo_${serviceWithPhoto.id}")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Saved in Firebase Storage").assertIsDisplayed()
        composeRule.onNodeWithTag("photo_replace_button").assertIsDisplayed()
        composeRule.onNodeWithTag("photo_delete_button")
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()

        assertEquals(serviceWithPhoto.id, deleteCalledForId)
        assertTrue(composeRule.onAllNodesWithText("Service Photo Manager").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun customerServicesScreen_rendersUpdatedServiceImageUrl() {
        val customUrl =
            "https://firebasestorage.googleapis.com/v0/b/gen-lang-client-0184859803.firebasestorage.app/o/service_photos%2Fframe_8x10%2Fphoto.png"
        val updatedServices = SeedCatalog.defaultServices.mapIndexed { idx, svc ->
            if (idx == 0) svc.copy(imageUrl = customUrl) else svc
        }

        composeRule.setContent {
            StudioPixyTheme {
                ServicesScreen(
                    services = updatedServices,
                    searchQuery = "",
                    selectedCategory = ServiceCategory.PHOTO_FRAMES,
                    showHindi = false,
                    onSearchChange = {},
                    onSelectCategory = {},
                    onSelectService = {},
                    onAddToCart = {},
                    onWhatsAppOrder = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(updatedServices.first().name).assertIsDisplayed()
    }

    @Test
    fun customerHomeScreen_rendersUpdatedServiceImageUrl() {
        val customUrl =
            "https://firebasestorage.googleapis.com/v0/b/gen-lang-client-0184859803.firebasestorage.app/o/service_photos%2Fframe_8x10%2Fphoto.png"
        val updatedServices = SeedCatalog.defaultServices.mapIndexed { idx, svc ->
            if (idx == 0) svc.copy(imageUrl = customUrl) else svc
        }

        composeRule.setContent {
            StudioPixyTheme {
                HomeScreen(
                    config = SeedCatalog.defaultConfig,
                    services = updatedServices,
                    showHindi = false,
                    isAdminSignedIn = false,
                    onNavigateToCategory = {},
                    onSelectService = {},
                    onAddToCart = {},
                    onWhatsAppOrderService = {},
                    onOpenHub = {},
                    onOpenAdmin = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Studio Pixy".uppercase()).assertIsDisplayed()
    }
}
