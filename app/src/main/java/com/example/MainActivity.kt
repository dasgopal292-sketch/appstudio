package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraRoll
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.CameraRoll
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.StudioRepository
import com.example.data.model.CartItem
import com.example.data.model.StudioService
import com.example.ui.StudioViewModel
import com.example.ui.UiState
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ServiceDetailSheet
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.StudioHubScreen
import com.example.ui.screens.StudioPixyLogo
import com.example.ui.screens.WhatsAppOrderSheet
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IvoryText
import com.example.ui.theme.MutedStone
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.StudioPixyTheme
import com.example.util.buildWhatsAppOrderMessage
import com.example.util.openWhatsAppWithMessage
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_CUSTOMER_HOME = "customer_home"
const val ROUTE_ADMIN = "admin"

private enum class StudioTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    SERVICES("Services", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    CART("Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    STUDIO("Studio", Icons.Filled.CameraRoll, Icons.Outlined.CameraRoll)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudioPixyTheme {
                StudioPixyApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioPixyApp(
    viewModel: StudioViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) { "APPLICATION_KEY missing" }
                if (com.google.firebase.FirebaseApp.getApps(app).isEmpty()) {
                    com.google.firebase.FirebaseApp.initializeApp(app)
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                StudioViewModel(StudioRepository(db))
            }
        }
    )
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val servicesState by viewModel.servicesState.collectAsStateWithLifecycle()
    val filteredServices by viewModel.filteredServices.collectAsStateWithLifecycle()
    val isCloudCatalogSeeded by viewModel.isCloudCatalogSeeded.collectAsStateWithLifecycle()
    val studioConfig by viewModel.studioConfig.collectAsStateWithLifecycle()
    val offers by viewModel.offers.collectAsStateWithLifecycle()
    val reviews by viewModel.reviews.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartTotalInr by viewModel.cartTotalInr.collectAsStateWithLifecycle()
    val cartTotalCount by viewModel.cartTotalCount.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val showHindi by viewModel.hindiLabelsEnabled.collectAsStateWithLifecycle()
    val adminSession by viewModel.adminSession.collectAsStateWithLifecycle()
    val adminOrdersState by viewModel.adminOrdersState.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(StudioTab.HOME) }
    var detailService by remember { mutableStateOf<StudioService?>(null) }
    var whatsAppOrderService by remember { mutableStateOf<StudioService?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val allServices = (servicesState as? UiState.Success)?.data.orEmpty()
    val navigateToAdmin: () -> Unit = {
        navController.navigate(ROUTE_ADMIN) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = ROUTE_CUSTOMER_HOME
    ) {
        composable(ROUTE_ADMIN) {
            Scaffold(
                containerColor = ObsidianBlack,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                AdminScreen(
                    adminSession = adminSession,
                    services = allServices,
                    isCloudCatalogSeeded = isCloudCatalogSeeded,
                    ordersState = adminOrdersState,
                    config = studioConfig,
                    offers = offers,
                    isBusy = isBusy,
                    onBackToStore = { navController.popBackStack() },
                    onSeedCatalog = { viewModel.adminSeedCatalog() },
                    onSaveService = { svc, isUpdate -> viewModel.adminSaveService(svc, isUpdate) },
                    onSaveServiceWithPhoto = { svc, isUpdate, photo -> viewModel.adminSaveServiceWithPhoto(svc, isUpdate, photo) },
                    onUploadServicePhoto = { svc, payload, cb -> viewModel.adminUploadServicePhoto(svc, payload, cb) },
                    onDeleteServicePhoto = { svc, cb -> viewModel.adminDeleteServicePhoto(svc, cb) },
                    onDeleteService = { id -> viewModel.adminDeleteService(id) },
                    onUpdateOrderStatus = { id, st -> viewModel.adminUpdateOrderStatus(id, st) },
                    onDeleteOrder = { id -> viewModel.adminDeleteOrder(id) },
                    onSaveConfig = { cfg -> viewModel.adminSaveStudioConfig(cfg) },
                    onSaveOffer = { offer -> viewModel.adminSaveOffer(offer) },
                    onDeleteOffer = { id -> viewModel.adminDeleteOffer(id) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        composable(ROUTE_CUSTOMER_HOME) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().background(ObsidianBlack)) {
                val useNavigationRail = maxWidth >= 600.dp

                Scaffold(
                    containerColor = ObsidianBlack,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = ObsidianBlack,
                                titleContentColor = IvoryText
                            ),
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StudioPixyLogo(size = 32.dp)
                                    Column {
                                        Text(
                                            text = studioConfig.businessName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BrightGold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (showHindi) "EN • हिं" else "Photo Studio",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedStone,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            },
                            actions = {
                                Surface(
                                    color = if (showHindi) GoldContainer else CharcoalSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.55f)),
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.toggleHindiLabels() }
                                        .testTag("toggle_hindi_button")
                                ) {
                                    Text(
                                        text = if (showHindi) "हिं" else "EN",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BrightGold,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                                Button(
                                    onClick = navigateToAdmin,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ChampagneGold,
                                        contentColor = ObsidianBlack
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .padding(end = 8.dp)
                                        .height(38.dp)
                                        .minimumInteractiveComponentSize()
                                        .testTag("topbar_admin_login_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = "Admin Login",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ADMIN LOGIN",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (!useNavigationRail) {
                            NavigationBar(
                                containerColor = CharcoalSurface,
                                contentColor = IvoryText
                            ) {
                                StudioTab.entries.forEach { tab ->
                                    val selected = currentTab == tab
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            if (tab == StudioTab.CART && cartTotalCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge(containerColor = BrightGold, contentColor = ObsidianBlack) {
                                                            Text(cartTotalCount.toString())
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                        contentDescription = tab.label
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.label
                                                )
                                            }
                                        },
                                        label = { Text(tab.label, maxLines = 1) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = BrightGold,
                                            selectedTextColor = BrightGold,
                                            indicatorColor = GoldContainer,
                                            unselectedIconColor = MutedStone,
                                            unselectedTextColor = MutedStone
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavigationRail) {
                            NavigationRail(
                                containerColor = CharcoalSurface,
                                contentColor = IvoryText,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Spacer(modifier = Modifier.size(12.dp))
                                StudioTab.entries.forEach { tab ->
                                    val selected = currentTab == tab
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            if (tab == StudioTab.CART && cartTotalCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge(containerColor = BrightGold, contentColor = ObsidianBlack) {
                                                            Text(cartTotalCount.toString())
                                                        }
                                                    }
                                                ) {
                                                    Icon(if (selected) tab.selectedIcon else tab.unselectedIcon, tab.label)
                                                }
                                            } else {
                                                Icon(if (selected) tab.selectedIcon else tab.unselectedIcon, tab.label)
                                            }
                                        },
                                        label = { Text(tab.label) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = BrightGold,
                                            selectedTextColor = BrightGold,
                                            indicatorColor = GoldContainer,
                                            unselectedIconColor = MutedStone,
                                            unselectedTextColor = MutedStone
                                        )
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            when (currentTab) {
                                StudioTab.HOME -> HomeScreen(
                                    config = studioConfig,
                                    services = allServices,
                                    showHindi = showHindi,
                                    isAdminSignedIn = adminSession.isAuthorizedAdmin,
                                    onNavigateToCategory = { cat ->
                                        viewModel.selectCategory(cat)
                                        currentTab = StudioTab.SERVICES
                                    },
                                    onSelectService = { svc -> detailService = svc },
                                    onAddToCart = { svc -> viewModel.addToCart(svc) },
                                    onWhatsAppOrderService = { svc -> whatsAppOrderService = svc },
                                    onOpenHub = { currentTab = StudioTab.STUDIO },
                                    onOpenAdmin = navigateToAdmin
                                )

                                StudioTab.SERVICES -> ServicesScreen(
                                    services = filteredServices,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    showHindi = showHindi,
                                    onSearchChange = viewModel::setSearchQuery,
                                    onSelectCategory = viewModel::selectCategory,
                                    onSelectService = { svc -> detailService = svc },
                                    onAddToCart = { svc -> viewModel.addToCart(svc) },
                                    onWhatsAppOrder = { svc -> whatsAppOrderService = svc }
                                )

                                StudioTab.CART -> CartScreen(
                                    cartItems = cartItems,
                                    totalInr = cartTotalInr,
                                    showHindi = showHindi,
                                    isBusy = isBusy,
                                    onUpdateQuantity = viewModel::updateCartQuantity,
                                    onRemoveItem = viewModel::removeFromCart,
                                    onBrowseServices = { currentTab = StudioTab.SERVICES },
                                    onPlaceOrder = { name, phone, reqs ->
                                        val snapshotItems = cartItems
                                        viewModel.submitWhatsAppOrder(
                                            items = snapshotItems,
                                            customerName = name,
                                            customerPhone = phone,
                                            requirements = reqs,
                                            clearCartAfter = true
                                        ) { orderId ->
                                            val msg = buildWhatsAppOrderMessage(
                                                config = studioConfig,
                                                items = snapshotItems,
                                                customerName = name,
                                                customerPhone = phone,
                                                requirements = reqs,
                                                orderId = orderId
                                            )
                                            openWhatsAppWithMessage(context, studioConfig.whatsappNumber, msg)
                                        }
                                    }
                                )

                                StudioTab.STUDIO -> StudioHubScreen(
                                    config = studioConfig,
                                    offers = offers,
                                    reviews = reviews,
                                    showHindi = showHindi,
                                    isBusy = isBusy,
                                    onSubmitReview = viewModel::submitReview,
                                    onOpenAdmin = navigateToAdmin
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    detailService?.let { svc ->
        ServiceDetailSheet(
            service = svc,
            showHindi = showHindi,
            onDismiss = { detailService = null },
            onAddToCart = { viewModel.addToCart(svc) },
            onWhatsAppOrder = { whatsAppOrderService = svc }
        )
    }

    whatsAppOrderService?.let { svc ->
        WhatsAppOrderSheet(
            service = svc,
            showHindi = showHindi,
            isBusy = isBusy,
            onDismiss = { whatsAppOrderService = null },
            onSubmitOrder = { qty, name, phone, reqs ->
                val items = listOf(CartItem(service = svc, quantity = qty))
                viewModel.submitWhatsAppOrder(
                    items = items,
                    customerName = name,
                    customerPhone = phone,
                    requirements = reqs,
                    clearCartAfter = false
                ) { orderId ->
                    whatsAppOrderService = null
                    val msg = buildWhatsAppOrderMessage(
                        config = studioConfig,
                        items = items,
                        customerName = name,
                        customerPhone = phone,
                        requirements = reqs,
                        orderId = orderId
                    )
                    openWhatsAppWithMessage(context, studioConfig.whatsappNumber, msg)
                }
            }
        )
    }
}
