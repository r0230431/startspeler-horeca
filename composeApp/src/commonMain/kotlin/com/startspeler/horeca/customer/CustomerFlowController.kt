package com.startspeler.horeca.customer

import com.startspeler.horeca.data.api.CatalogApi
import com.startspeler.horeca.data.api.CustomerApi
import com.startspeler.horeca.data.api.OrdersApi
import com.startspeler.horeca.data.api.TableApi
import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.orders.CreateOrderRequest
import com.startspeler.horeca.data.models.orders.CustomerCartItem
import com.startspeler.horeca.data.models.orders.OrderLineRequest
import com.startspeler.horeca.data.models.orders.OrderResponse
import com.startspeler.horeca.data.models.products.ProductResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CustomerFlowController(
    private val catalogApi: CatalogApi = CatalogApi(),
    private val customerApi: CustomerApi = CustomerApi(),
    private val ordersApi: OrdersApi = OrdersApi(),
    private val tableApi: TableApi = TableApi(),
) {
    private val _state = MutableStateFlow(CustomerFlowState())
    val state: StateFlow<CustomerFlowState> = _state.asStateFlow()

    fun initializeSession(session: CustomerSession) {
        _state.update {
            if (it.session == session) it else it.copy(
                session = session,
                entryError = null,
                orderError = null,
                orderSuccess = null,
            )
        }
    }

    suspend fun startSession(tableNumberInput: String, customerNameInput: String): Result<CustomerSession> {
        val normalizedTableNumber = tableNumberInput.trim()
        val normalizedCustomerName = customerNameInput.trim()

        if (normalizedTableNumber.isBlank()) {
            return Result.failure(IllegalArgumentException("Tafelnummer is verplicht."))
        }

        if (normalizedCustomerName.isBlank()) {
            return Result.failure(IllegalArgumentException("Naam is verplicht."))
        }

        val parsedTableNumber = normalizedTableNumber.toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Tafelnummer moet een geldig getal zijn."))

        return try {
            _state.update { it.copy(isSubmitting = true, entryError = null, orderError = null) }

            val table = tableApi.getPublicTableByNumber(parsedTableNumber)
            val customer = customerApi.createCustomer(normalizedCustomerName)
            val session = CustomerSession(customer = customer, table = table)

            _state.update {
                it.copy(
                    session = session,
                    isSubmitting = false,
                    entryError = null,
                    cartItems = emptyList(),
                    selectedCategoryId = null,
                    selectedProduct = null,
                    note = "",
                    orderSuccess = null,
                    orderError = null,
                )
            }

            Result.success(session)
        } catch (e: Exception) {
            val message = e.message?.takeIf { it.isNotBlank() }
                ?: "Klantflow kon niet worden opgestart."
            _state.update { it.copy(isSubmitting = false, entryError = message) }
            Result.failure(IllegalStateException(message, e))
        }
    }

    suspend fun loadCatalog() {
        try {
            _state.update { it.copy(isCatalogLoading = true, catalogError = null) }
            val categories = catalogApi.getCategories()
                .filter { it.isActive }
                .sortedBy { it.displayOrder }
            val products = catalogApi.getPublicProducts()
                .filter { it.isActive }
                .sortedBy { it.name.lowercase() }

            val categoryIdsWithProducts = products.map { it.categoryId }.toSet()
            val visibleCategories = categories.filter { it.id in categoryIdsWithProducts }

            _state.update {
                it.copy(
                    categories = visibleCategories,
                    products = products,
                    selectedCategoryId = it.selectedCategoryId?.takeIf { id -> id in categoryIdsWithProducts },
                    isCatalogLoading = false,
                    catalogError = null,
                )
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isCatalogLoading = false,
                    catalogError = e.message ?: "Producten konden niet geladen worden."
                )
            }
        }
    }

    suspend fun refreshCatalogSilently() {
        try {
            val categories = catalogApi.getCategories()
                .filter { it.isActive }
                .sortedBy { it.displayOrder }
            val products = catalogApi.getPublicProducts()
                .filter { it.isActive }
                .sortedBy { it.name.lowercase() }
            val categoryIdsWithProducts = products.map { it.categoryId }.toSet()
            val visibleCategories = categories.filter { it.id in categoryIdsWithProducts }
            _state.update {
                it.copy(
                    categories = visibleCategories,
                    products = products,
                    selectedCategoryId = it.selectedCategoryId?.takeIf { id -> id in categoryIdsWithProducts },
                )
            }
        } catch (_: Exception) {
            // silent refresh — fouten negeren
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun activateSearch() {
        _state.update {
            it.copy(
                isSearchActive = true,
                selectedCategoryId = null,
            )
        }
    }

    fun deactivateSearch() {
        _state.update { it.copy(isSearchActive = false, searchQuery = "") }
    }

    fun clearSearchAndFilters() {
        _state.update {
            it.copy(
                isSearchActive = false,
                searchQuery = "",
                selectedCategoryId = null,
            )
        }
    }

    fun selectCategory(categoryId: Int?) {
        _state.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun openProduct(product: ProductResponse) {
        _state.update { it.copy(selectedProduct = product) }
    }

    fun closeProduct() {
        _state.update { it.copy(selectedProduct = null) }
    }

    fun updateNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun addToCart(product: ProductResponse, quantity: Int) {
        if (quantity <= 0 || product.stock <= 0 || !product.isActive) return

        _state.update { current ->
            val existing = current.cartItems.firstOrNull { it.product.id == product.id }
            val nextItems = if (existing == null) {
                current.cartItems + CustomerCartItem(product = product, quantity = quantity.coerceAtMost(product.stock))
            } else {
                current.cartItems.map { item ->
                    if (item.product.id == product.id) {
                        item.copy(quantity = (item.quantity + quantity).coerceAtMost(product.stock))
                    } else {
                        item
                    }
                }
            }
            current.copy(cartItems = nextItems)
        }
    }

    fun updateCartQuantity(productId: Int, quantity: Int) {
        _state.update { current ->
            val nextItems = current.cartItems.mapNotNull { item ->
                if (item.product.id != productId) return@mapNotNull item
                if (quantity <= 0) return@mapNotNull null
                item.copy(quantity = quantity.coerceAtMost(item.product.stock))
            }
            current.copy(cartItems = nextItems)
        }
    }

    fun removeFromCart(productId: Int) {
        _state.update { current ->
            current.copy(cartItems = current.cartItems.filterNot { it.product.id == productId })
        }
    }

    fun clearCart() {
        _state.update { it.copy(cartItems = emptyList(), note = "", orderError = null) }
    }

    fun clearOrderFeedback() {
        _state.update { it.copy(orderError = null, orderSuccess = null) }
    }

    suspend fun placeOrder(): ApiResult<OrderResponse> {
        val current = _state.value
        val session = current.session
            ?: return ApiResult.Error("Geen actieve klant- of tafelsessie gevonden.")

        if (current.cartItems.isEmpty()) {
            val result = ApiResult.Error("Je winkelmandje is leeg.")
            _state.update { it.copy(orderError = result.message) }
            return result
        }

        return try {
            _state.update { it.copy(isPlacingOrder = true, orderError = null, orderSuccess = null) }

            val request = CreateOrderRequest(
                customerId = session.customer.id,
                orderedByName = session.displayName,
                tableId = session.table.id,
                note = current.note.trim().ifBlank { null },
                orderSource = "CUSTOMER",
                lines = current.cartItems.map { item ->
                    OrderLineRequest(
                        productId = item.product.id,
                        quantity = item.quantity,
                    )
                }
            )

            when (val response = ordersApi.createOrder(request)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            cartItems = emptyList(),
                            note = "",
                            isPlacingOrder = false,
                            orderSuccess = response.data,
                            orderError = null,
                            selectedProduct = null,
                        )
                    }
                    response
                }
                is ApiResult.Error -> {
                    _state.update {
                        it.copy(
                            isPlacingOrder = false,
                            orderError = response.message,
                            orderSuccess = null,
                        )
                    }
                    response
                }
            }
        } catch (e: Exception) {
            val message = e.message ?: "Bestelling kon niet geplaatst worden."
            _state.update {
                it.copy(
                    isPlacingOrder = false,
                    orderError = message,
                    orderSuccess = null,
                )
            }
            ApiResult.Error(message)
        }
    }
}

data class CustomerFlowState(
    val session: CustomerSession? = null,
    val categories: List<CategoryResponse> = emptyList(),
    val products: List<ProductResponse> = emptyList(),
    val cartItems: List<CustomerCartItem> = emptyList(),
    val selectedCategoryId: Int? = null,
    val selectedProduct: ProductResponse? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val note: String = "",
    val isSubmitting: Boolean = false,
    val isCatalogLoading: Boolean = false,
    val isPlacingOrder: Boolean = false,
    val entryError: String? = null,
    val catalogError: String? = null,
    val orderError: String? = null,
    val orderSuccess: OrderResponse? = null,
) {
    val filteredProducts: List<ProductResponse>
        get() = products.filter { product ->
            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val matchesSearch = searchQuery.isBlank() || product.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }

    val totalItems: Int get() = cartItems.sumOf { it.quantity }
    val totalPrice: Double get() = cartItems.sumOf { it.subtotal }
}
