package com.startspeler.horeca.screens.crew.overviews

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.api.CatalogApi
import com.startspeler.horeca.data.api.DiscountApi
import com.startspeler.horeca.data.api.OrdersApi
import com.startspeler.horeca.data.api.PaymentsApi
import com.startspeler.horeca.data.api.TableApi
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.discounts.DiscountResponse
import com.startspeler.horeca.data.models.orders.OrderResponse
import com.startspeler.horeca.data.models.payments.PaymentResponse
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.data.models.tables.TableResponse
import com.startspeler.horeca.screens.crew.shared.OrderEditDialog
import com.startspeler.horeca.ui.theme.crew.CrewBackground
import com.startspeler.horeca.ui.theme.crew.CrewBorder
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary
import kotlinx.coroutines.launch

@Composable
fun OverviewsScreen(
    navigationRequest: OverviewNavigationRequest = OverviewNavigationRequest()
) {
    val ordersApi = remember { OrdersApi() }
    val paymentsApi = remember { PaymentsApi() }
    val discountApi = remember { DiscountApi() }
    val tableApi = remember { TableApi() }
    val catalogApi = remember { CatalogApi() }
    val scope = rememberCoroutineScope()

    val now = remember { currentBrusselsDateTime() }
    val defaultFrom = remember { defaultFromDateTime() }

    var selectedTab by remember { mutableStateOf(navigationRequest.selectedTab) }
    var focusedOrderIds by remember { mutableStateOf(navigationRequest.focusedOrderIds) }

    var orderFromDate by remember { mutableStateOf(formatDateInput(defaultFrom.date)) }
    var orderFromTime by remember { mutableStateOf(formatTimeInput(defaultFrom.time)) }
    var orderToDate by remember { mutableStateOf(formatDateInput(now.date)) }
    var orderToTime by remember { mutableStateOf(formatTimeInput(now.time)) }
    var orderCustomerQuery by remember { mutableStateOf("") }
    var orderStatusFilter by remember { mutableStateOf("ALL") }
    var orderPaidFilter by remember { mutableStateOf(PaidFilter.ALL) }
    var orderFiltersExpanded by remember { mutableStateOf(false) }

    var paymentFromDate by remember { mutableStateOf(formatDateInput(defaultFrom.date)) }
    var paymentFromTime by remember { mutableStateOf(formatTimeInput(defaultFrom.time)) }
    var paymentToDate by remember { mutableStateOf(formatDateInput(now.date)) }
    var paymentToTime by remember { mutableStateOf(formatTimeInput(now.time)) }
    var paymentMethodFilter by remember { mutableStateOf("ALL") }
    var paymentCustomerQuery by remember { mutableStateOf("") }
    var paymentDiscountQuery by remember { mutableStateOf("") }
    var paymentFiltersExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var orders by remember { mutableStateOf<List<OrderResponse>>(emptyList()) }
    var payments by remember { mutableStateOf<List<PaymentResponse>>(emptyList()) }
    var discounts by remember { mutableStateOf<List<DiscountResponse>>(emptyList()) }
    var tables by remember { mutableStateOf<List<TableResponse>>(emptyList()) }
    var products by remember { mutableStateOf<List<ProductResponse>>(emptyList()) }
    var selectedOrder by remember { mutableStateOf<OrderResponse?>(null) }

    fun resetOrderFilters() {
        val now = currentBrusselsDateTime()
        val defaultFrom = defaultFromDateTime()

        orderFromDate = formatDateInput(defaultFrom.date)
        orderFromTime = formatTimeInput(defaultFrom.time)
        orderToDate = formatDateInput(now.date)
        orderToTime = formatTimeInput(now.time)

        orderCustomerQuery = ""
        orderStatusFilter = "ALL"
        orderPaidFilter = PaidFilter.ALL
        focusedOrderIds = emptySet()
    }

    fun resetPaymentFilters() {
        val now = currentBrusselsDateTime()
        val defaultFrom = defaultFromDateTime()

        paymentFromDate = formatDateInput(defaultFrom.date)
        paymentFromTime = formatTimeInput(defaultFrom.time)
        paymentToDate = formatDateInput(now.date)
        paymentToTime = formatTimeInput(now.time)

        paymentMethodFilter = "ALL"
        paymentCustomerQuery = ""
        paymentDiscountQuery = ""
    }

    fun loadData(showLoading: Boolean) {
        scope.launch {
            if (showLoading) isLoading = true else isRefreshing = true
            val ordersResult = runCatching { ordersApi.getOrders() }
            val paymentsResult = paymentsApi.getPayments()
            val discountsResult = runCatching { discountApi.getDiscounts() }
            val tablesResult = runCatching { tableApi.getTables() }
            val productsResult = runCatching { catalogApi.getProducts() }

            errorMessage = null
            orders = ordersResult.getOrElse {
                errorMessage = it.message ?: "Bestellingen konden niet worden geladen."
                emptyList()
            }
            payments = when (paymentsResult) {
                is ApiResult.Success -> paymentsResult.data
                is ApiResult.Error -> {
                    errorMessage = paymentsResult.message
                    emptyList()
                }
            }
            discounts = discountsResult.getOrDefault(emptyList())
            tables = tablesResult.getOrDefault(emptyList())
            products = productsResult.getOrDefault(emptyList())
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        resetOrderFilters()
        resetPaymentFilters()
        loadData(showLoading = true)
    }

    LaunchedEffect(navigationRequest.requestId) {
        selectedTab = navigationRequest.selectedTab
        focusedOrderIds = navigationRequest.focusedOrderIds
        navigationRequest.paymentFiltersPreset?.let { preset ->
            preset.from?.let { from ->
                parseServerDateTime(from)?.let { parsed ->
                    paymentFromDate = formatDateInput(parsed.date)
                    paymentFromTime = formatTimeInput(parsed.time)
                }
            }
            preset.to?.let { to ->
                parseServerDateTime(to)?.let { parsed ->
                    paymentToDate = formatDateInput(parsed.date)
                    paymentToTime = formatTimeInput(parsed.time)
                }
            }
            paymentMethodFilter = preset.paymentMethod?.takeIf { it.isNotBlank() } ?: "ALL"
        }
    }

    val tableNumberById = remember(tables) { tables.associate { it.id to it.tableNumber } }
    val ordersById = remember(orders) { orders.associateBy { it.id } }
    val discountById = remember(discounts) { discounts.associateBy { it.id } }

    val orderFrom = remember(orderFromDate, orderFromTime) { parseDateTimeInputs(orderFromDate, orderFromTime) }
    val orderTo = remember(orderToDate, orderToTime) { parseDateTimeInputs(orderToDate, orderToTime) }
    val paymentFrom = remember(paymentFromDate, paymentFromTime) { parseDateTimeInputs(paymentFromDate, paymentFromTime) }
    val paymentTo = remember(paymentToDate, paymentToTime) { parseDateTimeInputs(paymentToDate, paymentToTime) }

    val paymentUiModels = remember(payments, ordersById, discountById) {
        payments.map { payment ->
            val relatedOrders = payment.orderIds.mapNotNull { ordersById[it] }
            val customerLabel = relatedOrders
                .map { it.orderedByName.ifBlank { "Onbekend" } }
                .distinct()
                .joinToString(", ")
                .ifBlank { "Onbekend" }
            val discountLabel = if (payment.discountIds.isNotEmpty()) {
                val names = payment.discountIds.mapNotNull { id -> discountById[id]?.name }.joinToString(", ")
                val label = names.ifBlank { "Korting" }
                "$label (€ ${payment.discountAmount.toMoneyString()})"
            } else if ((payment.discountAmount.toDoubleOrNull() ?: 0.0) > 0.0) {
                "Korting (€ ${payment.discountAmount.toMoneyString()})"
            } else {
                "Geen korting"
            }
            PaymentUiModel(
                payment = payment,
                customerLabel = customerLabel,
                discountLabel = discountLabel,
                methodLabel = paymentMethodLabel(payment.paymentMethod),
                paidAtLabel = payment.paidAt.replace('T', ' ')
            )
        }
    }

    val filteredOrders = remember(
        orders,
        orderFrom,
        orderTo,
        orderCustomerQuery,
        orderStatusFilter,
        orderPaidFilter,
        focusedOrderIds
    ) {
        orders.filter { order ->
            val createdAt = parseServerDateTime(order.createdAt)
            val epoch = createdAt?.toEpochMillis()
            val matchesDate = when {
                focusedOrderIds.isNotEmpty() -> true
                orderFrom == null || orderTo == null || epoch == null -> true
                else -> epoch in orderFrom.toEpochMillis()..orderTo.toEpochMillis()
            }
            val matchesCustomer = orderCustomerQuery.isBlank() ||
                order.orderedByName.contains(orderCustomerQuery, ignoreCase = true)
            val matchesStatus = orderStatusFilter == "ALL" || order.status.equals(orderStatusFilter, ignoreCase = true)
            val matchesPaid = when (orderPaidFilter) {
                PaidFilter.ALL -> true
                PaidFilter.UNPAID -> order.paymentId == null
                PaidFilter.PAID -> order.paymentId != null
            }
            val matchesFocused = focusedOrderIds.isEmpty() || order.id in focusedOrderIds
            matchesDate && matchesCustomer && matchesStatus && matchesPaid && matchesFocused
        }.sortedByDescending { parseServerDateTime(it.createdAt)?.toEpochMillis() ?: 0L }
    }

    val filteredPayments = remember(
        paymentUiModels,
        paymentFrom,
        paymentTo,
        paymentMethodFilter,
        paymentCustomerQuery,
        paymentDiscountQuery
    ) {
        paymentUiModels.filter { item ->
            val paidAt = parseServerDateTime(item.payment.paidAt)
            val epoch = paidAt?.toEpochMillis()
            val matchesDate = when {
                paymentFrom == null || paymentTo == null || epoch == null -> true
                else -> epoch in paymentFrom.toEpochMillis()..paymentTo.toEpochMillis()
            }
            val matchesMethod = paymentMethodFilter == "ALL" || item.payment.paymentMethod.equals(paymentMethodFilter, ignoreCase = true)
            val matchesCustomer = paymentCustomerQuery.isBlank() || item.customerLabel.contains(paymentCustomerQuery, ignoreCase = true)
            val matchesDiscount = paymentDiscountQuery.isBlank() || item.discountLabel.contains(paymentDiscountQuery, ignoreCase = true)
            matchesDate && matchesMethod && matchesCustomer && matchesDiscount
        }.sortedByDescending { parseServerDateTime(it.payment.paidAt)?.toEpochMillis() ?: 0L }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewBackground)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Overzichten",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CrewTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bestellingen en betalingen raadplegen en opvolgen",
                    color = CrewTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CrewSurface,
                    border = BorderStroke(1.dp, CrewBorder)
                ) {
                    IconButton(
                        onClick = {
                            val now = currentBrusselsDateTime()

                            orderToDate = formatDateInput(now.date)
                            orderToTime = formatTimeInput(now.time)
                            paymentToDate = formatDateInput(now.date)
                            paymentToTime = formatTimeInput(now.time)

                            loadData(showLoading = false)
                        },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Vernieuwen",
                            tint = CrewPrimary
                        )
                    }
                }
            }
        }

        PrimaryScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 0.dp) {
            OverviewTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label) }
                )
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null && orders.isEmpty() && payments.isEmpty() -> {
                FeedbackCard(
                    title = "Gegevens konden niet worden geladen",
                    message = errorMessage ?: "Onbekende fout",
                    isError = true
                )
            }

            selectedTab == OverviewTab.ORDERS -> {
                OrdersOverviewScreen(
                    orders = filteredOrders,
                    allOrders = orders,
                    products = products,
                    tableNumberById = tableNumberById,
                    filtersExpanded = orderFiltersExpanded,
                    onFiltersExpandedChange = { orderFiltersExpanded = it },
                    fromDate = orderFromDate,
                    fromTime = orderFromTime,
                    toDate = orderToDate,
                    toTime = orderToTime,
                    onFromDateChange = { orderFromDate = it },
                    onFromTimeChange = { orderFromTime = it },
                    onToDateChange = { orderToDate = it },
                    onToTimeChange = { orderToTime = it },
                    customerQuery = orderCustomerQuery,
                    onCustomerQueryChange = { orderCustomerQuery = it },
                    statusFilter = orderStatusFilter,
                    onStatusFilterChange = { orderStatusFilter = it },
                    paidFilter = orderPaidFilter,
                    onPaidFilterChange = { orderPaidFilter = it },
                    focusedOrderIds = focusedOrderIds,
                    onClearFocus = { focusedOrderIds = emptySet() },
                    onOrderSelected = { selectedOrder = it },
                    onResetFilters = { resetOrderFilters() },
                )
            }

            else -> {
                PaymentsOverviewScreen(
                    payments = filteredPayments,
                    filtersExpanded = paymentFiltersExpanded,
                    onFiltersExpandedChange = { paymentFiltersExpanded = it },
                    fromDate = paymentFromDate,
                    fromTime = paymentFromTime,
                    toDate = paymentToDate,
                    toTime = paymentToTime,
                    onFromDateChange = { paymentFromDate = it },
                    onFromTimeChange = { paymentFromTime = it },
                    onToDateChange = { paymentToDate = it },
                    onToTimeChange = { paymentToTime = it },
                    selectedMethod = paymentMethodFilter,
                    onMethodChange = { paymentMethodFilter = it },
                    customerQuery = paymentCustomerQuery,
                    onCustomerQueryChange = { paymentCustomerQuery = it },
                    discountQuery = paymentDiscountQuery,
                    onDiscountQueryChange = { paymentDiscountQuery = it },
                    onOpenRelatedOrders = { orderIds ->
                        selectedTab = OverviewTab.ORDERS
                        focusedOrderIds = orderIds.toSet()
                        orderFromDate = paymentFromDate
                        orderFromTime = paymentFromTime
                        orderToDate = paymentToDate
                        orderToTime = paymentToTime
                        orderCustomerQuery = ""
                        orderStatusFilter = "ALL"
                        orderPaidFilter = PaidFilter.ALL
                    },
                    onResetFilters = { resetPaymentFilters() }
                )
            }
        }
    }

    selectedOrder?.let { order ->
        OrderEditDialog(
            order = order,
            products = products,
            tableNumber = tableNumberById[order.tableId],
            onDismiss = { selectedOrder = null },
            onOrderUpdated = {
                selectedOrder = null
                loadData(showLoading = false)
            }
        )
    }
}
