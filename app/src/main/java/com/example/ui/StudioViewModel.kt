package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.authStateFlow
import com.example.data.SeedCatalog
import com.example.data.StudioRepository
import com.example.data.ValidatedPhotoPayload
import com.example.data.model.CartItem
import com.example.data.model.CustomerOrder
import com.example.data.model.CustomerReview
import com.example.data.model.OrderStatus
import com.example.data.model.ServiceCategory
import com.example.data.model.SpecialOffer
import com.example.data.model.StudioConfig
import com.example.data.model.StudioService
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class AdminSessionState(
    val user: FirebaseUser? = null,
    val isChecking: Boolean = false,
    val isAuthorizedAdmin: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class StudioViewModel(
    private val repository: StudioRepository,
    private val auth: FirebaseAuth = Firebase.auth
) : ViewModel() {

    private val _hindiLabelsEnabled = MutableStateFlow(true)
    val hindiLabelsEnabled: StateFlow<Boolean> = _hindiLabelsEnabled.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ServiceCategory?>(null)
    val selectedCategory: StateFlow<ServiceCategory?> = _selectedCategory.asStateFlow()

    private val _cart = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    val cart: StateFlow<Map<String, CartItem>> = _cart.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    // Public catalog: merges live Firestore services with bundled SeedCatalog fallback when cloud is unseeded
    val servicesState: StateFlow<UiState<List<StudioService>>> = repository.observeServices()
        .map<List<StudioService>, UiState<List<StudioService>>> { cloudList ->
            val effective = if (cloudList.isEmpty()) SeedCatalog.defaultServices else cloudList
            UiState.Success(effective)
        }
        .catch { error ->
            val firestoreError = unwrapFirestore(error)
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Falling back to default catalog due to Firestore error", firestoreError ?: error)
            emit(UiState.Success(SeedCatalog.defaultServices))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = UiState.Success(SeedCatalog.defaultServices)
        )

    val isCloudCatalogSeeded: StateFlow<Boolean> = repository.observeServices()
        .map { it.isNotEmpty() }
        .catch { error ->
            val firestoreError = unwrapFirestore(error)
            if (firestoreError == null && error is CancellationException) throw error
            emit(false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = false
        )

    val studioConfig: StateFlow<StudioConfig> = repository.observeStudioConfig()
        .map { it ?: SeedCatalog.defaultConfig }
        .catch { error ->
            val firestoreError = unwrapFirestore(error)
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Using default StudioConfig", firestoreError ?: error)
            emit(SeedCatalog.defaultConfig)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SeedCatalog.defaultConfig
        )

    val offers: StateFlow<List<SpecialOffer>> = repository.observeOffers()
        .map { if (it.isEmpty()) SeedCatalog.defaultOffers else it }
        .catch { error ->
            val firestoreError = unwrapFirestore(error)
            if (firestoreError == null && error is CancellationException) throw error
            emit(SeedCatalog.defaultOffers)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SeedCatalog.defaultOffers
        )

    val reviews: StateFlow<List<CustomerReview>> = repository.observeReviews()
        .map { if (it.isEmpty()) SeedCatalog.defaultReviews else it }
        .catch { error ->
            val firestoreError = unwrapFirestore(error)
            if (firestoreError == null && error is CancellationException) throw error
            emit(SeedCatalog.defaultReviews)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SeedCatalog.defaultReviews
        )

    val filteredServices: StateFlow<List<StudioService>> = combine(
        servicesState,
        _searchQuery,
        _selectedCategory
    ) { state, query, category ->
        val all = (state as? UiState.Success)?.data.orEmpty().filter { it.available }
        val q = query.trim().lowercase()
        all.filter { service ->
            val catMatch = category == null || service.categoryEnum() == category
            val textMatch = q.isEmpty() ||
                service.name.lowercase().contains(q) ||
                service.hindiName.lowercase().contains(q) ||
                service.description.lowercase().contains(q)
            catMatch && textMatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = SeedCatalog.defaultServices
    )

    val cartItems: StateFlow<List<CartItem>> = _cart
        .map { it.values.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val cartTotalInr: StateFlow<Long> = _cart
        .map { map -> map.values.sumOf { it.lineTotalInr } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0L)

    val cartTotalCount: StateFlow<Int> = _cart
        .map { map -> map.values.sumOf { it.quantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    // Admin authentication state & admin-gated orders listener
    val adminSession: StateFlow<AdminSessionState> = auth.authStateFlow()
        .flatMapLatest { user ->
            flow {
                if (user == null) {
                    emit(AdminSessionState(user = null, isChecking = false, isAuthorizedAdmin = false))
                } else {
                    emit(AdminSessionState(user = user, isChecking = true, isAuthorizedAdmin = false))
                    val authorized = repository.checkIsAuthorizedAdmin()
                    if (authorized) {
                        repository.seedDefaultCatalogIfNeeded()
                    }
                    emit(AdminSessionState(user = user, isChecking = false, isAuthorizedAdmin = authorized))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = AdminSessionState(user = auth.currentUser, isChecking = auth.currentUser != null)
        )

    val adminOrdersState: StateFlow<UiState<List<CustomerOrder>>> = adminSession
        .flatMapLatest { session ->
            if (!session.isAuthorizedAdmin || session.user == null) {
                flowOf(UiState.Success(emptyList()))
            } else {
                repository.observeOrders()
                    .map<List<CustomerOrder>, UiState<List<CustomerOrder>>> { UiState.Success(it) }
                    .catch { error ->
                        val firestoreError = unwrapFirestore(error)
                        if (firestoreError == null && error is CancellationException) throw error
                        val rootError = firestoreError ?: error
                        Log.w(TAG, "Error observing orders", rootError)
                        emit(UiState.Error(rootError.localizedMessage ?: "Failed to load orders"))
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = UiState.Loading
        )

    fun toggleHindiLabels() {
        _hindiLabelsEnabled.update { !it }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: ServiceCategory?) {
        _selectedCategory.value = category
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun postStatusMessage(msg: String) {
        _statusMessage.value = msg
    }

    fun addToCart(service: StudioService, qty: Int = 1) {
        _cart.update { current ->
            val mutable = current.toMutableMap()
            val existing = mutable[service.id]
            val newQty = ((existing?.quantity ?: 0) + qty).coerceIn(1, 500)
            mutable[service.id] = CartItem(service = service, quantity = newQty)
            mutable
        }
        _statusMessage.value = "Added ${service.name} to cart"
    }

    fun updateCartQuantity(serviceId: String, delta: Int) {
        _cart.update { current ->
            val mutable = current.toMutableMap()
            val existing = mutable[serviceId] ?: return@update current
            val next = existing.quantity + delta
            if (next <= 0) {
                mutable.remove(serviceId)
            } else {
                mutable[serviceId] = existing.copy(quantity = next.coerceAtMost(500))
            }
            mutable
        }
    }

    fun removeFromCart(serviceId: String) {
        _cart.update { current ->
            val mutable = current.toMutableMap()
            mutable.remove(serviceId)
            mutable
        }
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun submitWhatsAppOrder(
        items: List<CartItem>,
        customerName: String,
        customerPhone: String,
        requirements: String,
        clearCartAfter: Boolean,
        onReadyToLaunchWhatsApp: (orderId: String?) -> Unit
    ) {
        if (items.isEmpty()) {
            _statusMessage.value = "Please select at least one service."
            return
        }
        val cleanName = customerName.trim()
        val cleanPhone = customerPhone.trim()
        if (cleanName.length < 2) {
            _statusMessage.value = "Please enter your name (at least 2 characters)."
            return
        }
        if (cleanPhone.filter { it.isDigit() }.length < 7) {
            _statusMessage.value = "Please enter a valid 10-digit phone number."
            return
        }

        val summary = items.joinToString(", ") {
            "${it.quantity}x ${it.service.name} (₹${it.lineTotalInr})"
        }.take(1200)
        val total = items.sumOf { it.lineTotalInr }
        val totalCount = items.sumOf { it.quantity }.toLong()

        val order = CustomerOrder(
            customerName = cleanName,
            customerPhone = cleanPhone,
            requirements = requirements.trim().take(600).ifBlank { null },
            itemsSummary = summary,
            itemCount = totalCount,
            totalInr = total,
            status = OrderStatus.PENDING.name
        )

        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.submitOrder(order)
            _isBusy.value = false
            result.onSuccess { orderId ->
                if (clearCartAfter) clearCart()
                _statusMessage.value = "Order saved (#${orderId.take(6).uppercase()})! Opening WhatsApp..."
                onReadyToLaunchWhatsApp(orderId.take(6).uppercase())
            }.onFailure { err ->
                // Even if offline, allow customer to still send order via WhatsApp
                _statusMessage.value = "Opening WhatsApp (cloud sync pending: ${err.localizedMessage ?: "offline"})"
                onReadyToLaunchWhatsApp(null)
            }
        }
    }

    fun submitReview(authorName: String, rating: Int, comment: String, serviceTag: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.submitReview(authorName, rating, comment, serviceTag)
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = "Thank you! Your review has been published."
            }.onFailure { e ->
                _statusMessage.value = e.localizedMessage ?: "Could not submit review."
            }
        }
    }

    fun adminSaveService(service: StudioService, isUpdate: Boolean) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveService(service, isUpdate)
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = if (isUpdate) "Service updated!" else "Service added!"
            }.onFailure { e ->
                _statusMessage.value = e.localizedMessage ?: "Failed to save service."
            }
        }
    }

    fun adminSaveServiceWithPhoto(
        service: StudioService,
        isUpdate: Boolean,
        pendingPhoto: ValidatedPhotoPayload?
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val effectiveService = if (!isUpdate && service.id.isBlank()) {
                service.copy(id = "svc_${System.currentTimeMillis()}")
            } else {
                service
            }
            val saveResult = repository.saveService(effectiveService, isUpdate)
            if (saveResult.isFailure) {
                _isBusy.value = false
                _statusMessage.value = saveResult.exceptionOrNull()?.localizedMessage ?: "Failed to save service."
                return@launch
            }
            if (pendingPhoto != null) {
                val photoResult = repository.uploadAndSaveServicePhoto(effectiveService, pendingPhoto)
                _isBusy.value = false
                photoResult.onSuccess {
                    _statusMessage.value = "Service & photo saved to cloud!"
                }.onFailure { e ->
                    _statusMessage.value = "Service saved, but photo upload failed: ${e.localizedMessage ?: "error"}"
                }
            } else {
                _isBusy.value = false
                _statusMessage.value = if (isUpdate) "Service updated!" else "Service added!"
            }
        }
    }

    fun adminUploadServicePhoto(
        service: StudioService,
        payload: ValidatedPhotoPayload,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.uploadAndSaveServicePhoto(service, payload)
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = "Photo updated for ${service.name}!"
                onComplete(true, null)
            }.onFailure { e ->
                val msg = e.localizedMessage ?: "Failed to upload photo to Firebase Storage."
                _statusMessage.value = msg
                onComplete(false, msg)
            }
        }
    }

    fun adminDeleteServicePhoto(
        service: StudioService,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.deleteServicePhoto(service)
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = "Custom photo removed for ${service.name}."
                onComplete(true, null)
            }.onFailure { e ->
                val msg = e.localizedMessage ?: "Failed to delete photo."
                _statusMessage.value = msg
                onComplete(false, msg)
            }
        }
    }

    fun adminDeleteService(serviceId: String) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.deleteService(serviceId)
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = "Service deleted."
            }.onFailure { e ->
                _statusMessage.value = e.localizedMessage ?: "Failed to delete service."
            }
        }
    }

    fun adminSeedCatalog() {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.seedDefaultCatalogIfNeeded()
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = "Default Studio Pixy catalog synced to Firestore."
            }.onFailure { e ->
                _statusMessage.value = e.localizedMessage ?: "Sync failed."
            }
        }
    }

    fun adminSaveStudioConfig(config: StudioConfig) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveStudioConfig(config)
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = "Studio contact & business info updated!"
            }.onFailure { e ->
                _statusMessage.value = e.localizedMessage ?: "Failed to save business info."
            }
        }
    }

    fun adminUpdateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            val result = repository.updateOrderStatus(orderId, status)
            result.onSuccess {
                _statusMessage.value = "Order marked as ${status.label}."
            }.onFailure { e ->
                _statusMessage.value = e.localizedMessage ?: "Failed to update order."
            }
        }
    }

    fun adminDeleteOrder(orderId: String) {
        viewModelScope.launch {
            val result = repository.deleteOrder(orderId)
            result.onSuccess {
                _statusMessage.value = "Order removed."
            }.onFailure { e ->
                _statusMessage.value = e.localizedMessage ?: "Failed to delete order."
            }
        }
    }

    fun adminSaveOffer(offer: SpecialOffer) {
        viewModelScope.launch {
            _isBusy.value = true
            val result = repository.saveOffer(offer)
            _isBusy.value = false
            result.onSuccess {
                _statusMessage.value = "Special offer saved!"
            }.onFailure { e ->
                _statusMessage.value = e.localizedMessage ?: "Failed to save offer."
            }
        }
    }

    fun adminDeleteOffer(offerId: String) {
        viewModelScope.launch {
            val result = repository.deleteOffer(offerId)
            result.onSuccess {
                _statusMessage.value = "Offer deleted."
            }.onFailure { e ->
                _statusMessage.value = e.localizedMessage ?: "Failed to delete offer."
            }
        }
    }

    private fun unwrapFirestore(error: Throwable): FirebaseFirestoreException? =
        generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()

    private companion object {
        const val TAG = "StudioVM"
        const val STOP_TIMEOUT_MS = 5000L
    }
}
