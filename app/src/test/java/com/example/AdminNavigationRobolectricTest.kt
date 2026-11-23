package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.data.SeedCatalog
import com.example.ui.AdminSessionState
import com.example.ui.UiState
import com.example.ui.screens.AdminDashboardContent
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.StudioPixyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdminNavigationRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun topBarAdminLoginButton_isAlwaysVisible_andNavigatesToAdminScreen() {
        composeRule.waitForIdle()

        // Must be visible immediately on Home Screen without scrolling
        composeRule.onNodeWithTag("topbar_admin_login_button")
            .assertIsDisplayed()
        composeRule.onNodeWithText("ADMIN LOGIN")
            .assertIsDisplayed()

        // Click ADMIN LOGIN button -> must navigate to AdminScreen login gate
        composeRule.onNodeWithTag("topbar_admin_login_button")
            .performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Studio Pixy Admin").assertIsDisplayed()
        composeRule.onNodeWithText("Admin Portal Login").assertIsDisplayed()
        composeRule.onNodeWithTag("admin_google_signin_button").assertIsDisplayed()

        // Dashboard tabs must NOT be shown while unauthenticated
        assertTrue(
            composeRule.onAllNodesWithText("Services & Prices").fetchSemanticsNodes().isEmpty()
        )
        assertTrue(
            composeRule.onAllNodesWithText("Orders").fetchSemanticsNodes().isEmpty()
        )
        assertTrue(
            composeRule.onAllNodesWithText("Studio & Offers").fetchSemanticsNodes().isEmpty()
        )

        // Back button returns to Customer Home screen and ADMIN LOGIN stays visible
        composeRule.onNodeWithTag("admin_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("topbar_admin_login_button").assertIsDisplayed()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdminScreenComponentsRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun homeScreenAdminCard_staysVisibleRegardlessOfAuthState() {
        var clicked = false
        composeRule.setContent {
            StudioPixyTheme {
                HomeScreen(
                    config = SeedCatalog.defaultConfig,
                    services = SeedCatalog.defaultServices,
                    showHindi = false,
                    isAdminSignedIn = true,
                    onNavigateToCategory = {},
                    onSelectService = {},
                    onAddToCart = {},
                    onWhatsAppOrderService = {},
                    onOpenHub = {},
                    onOpenAdmin = { clicked = true }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("home_admin_login_card"))
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("home_admin_login_button")
            .assertIsDisplayed()
            .performClick()
        assertTrue(clicked)
    }

    @Test
    fun adminScreen_unauthenticated_neverShowsDashboard() {
        composeRule.setContent {
            StudioPixyTheme {
                AdminScreen(
                    adminSession = AdminSessionState(
                        user = null,
                        isChecking = false,
                        isAuthorizedAdmin = false
                    ),
                    services = SeedCatalog.defaultServices,
                    isCloudCatalogSeeded = false,
                    ordersState = UiState.Success(emptyList()),
                    config = SeedCatalog.defaultConfig,
                    offers = SeedCatalog.defaultOffers,
                    isBusy = false,
                    onBackToStore = {},
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

        composeRule.onNodeWithText("Admin Portal Login").assertIsDisplayed()
        composeRule.onNodeWithTag("admin_google_signin_button").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("Services & Prices").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun adminDashboard_authorized_displaysThreeRequiredTabs() {
        composeRule.setContent {
            StudioPixyTheme {
                AdminDashboardContent(
                    services = SeedCatalog.defaultServices,
                    isCloudCatalogSeeded = false,
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

        composeRule.onNodeWithText("Services & Prices").assertIsDisplayed()
        composeRule.onNodeWithText("Orders").assertIsDisplayed()
        composeRule.onNodeWithText("Studio & Offers").assertIsDisplayed()

        // Switch tabs and verify each tab renders cleanly
        composeRule.onNodeWithTag("admin_tab_1").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("No customer orders yet").assertIsDisplayed()

        composeRule.onNodeWithTag("admin_tab_2").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Business & Contact Settings").assertIsDisplayed()
    }
}
