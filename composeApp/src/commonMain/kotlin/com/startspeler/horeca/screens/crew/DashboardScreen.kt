package com.startspeler.horeca.screens.crew

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.api.CatalogApi
import com.startspeler.horeca.data.api.OrdersApi
import com.startspeler.horeca.data.models.orders.OrderResponse
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.screens.crew.shared.OrderEditDialog
import com.startspeler.horeca.ui.theme.crew.*
import com.startspeler.horeca.ui.theme.customer.CustomerOrderColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(onNavigate: (String) -> Unit = {}) {
    val ordersApi = remember { OrdersApi() }
    val scope = rememberCoroutineScope()

    val catalogApi = remember { CatalogApi() }

    var allOrders by remember { mutableStateOf<List<OrderResponse>>(emptyList()) }
    var allProducts by remember { mutableStateOf<List<ProductResponse>>(emptyList()) }
    var selectedOrder by remember { mutableStateOf<OrderResponse?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf(OrderFilter.All) }

    fun loadOrders(showLoading: Boolean = true) {
        scope.launch {
            if (showLoading) {
                isLoading = true
            }

            try {
                val ordersResponse = ordersApi.getOrders()
                allOrders = ordersResponse.sortedByDescending { it.id }

                if (allProducts.isEmpty()) {
                    allProducts = catalogApi.getProducts()
                }

                errorMessage = null
            } catch (e: Exception) {
                e.printStackTrace()
                if (allOrders.isEmpty()) {
                    errorMessage = e.message ?: "Fout bij ophalen van bestellingen."
                }
            } finally {
                if (showLoading) {
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadOrders(showLoading = true)

        while (true) {
            delay(5000)
            loadOrders(showLoading = false)
        }
    }

    val dashboardOrders = allOrders
        .map { it.toDashboardUiModel() }
        .filter { it.status != DashboardOrderStatus.CANCELLED }

    val filteredOrders = when (selectedFilter) {
        OrderFilter.All -> dashboardOrders
        OrderFilter.InProgress -> dashboardOrders.filter { it.status == DashboardOrderStatus.IN_PROGRESS }
        OrderFilter.Ready -> dashboardOrders.filter { it.status == DashboardOrderStatus.READY }
        OrderFilter.Delivered -> dashboardOrders.filter { it.status == DashboardOrderStatus.DELIVERED }
    }

    fun updateStatus(orderId: Int, newStatus: String) {
        scope.launch {
            try {
                ordersApi.updateOrderStatus(orderId, newStatus)
                loadOrders(showLoading = false)
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message ?: "Fout bij wijzigen van bestelstatus."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewBackground)
            .verticalScroll(rememberScrollState())
            .border(
                width = 1.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(28.dp)
            )
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DashboardHeader()

        when {
            isLoading -> {
                Text(
                    text = "Bestellingen laden...",
                    color = CrewTextSecondary
                )
            }

            errorMessage != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CrewSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Er ging iets mis bij het ophalen van de bestellingen.",
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = errorMessage ?: "",
                            color = CrewTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(onClick = { loadOrders() }) {
                            Text("Opnieuw proberen")
                        }
                    }
                }
            }

            else -> {
                DashboardStatsSection(orders = dashboardOrders)
                QuickActionsSection(onNavigate = onNavigate)
                OrdersSection(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    orders = filteredOrders,
                    onUpdateStatus = { orderId, newStatus ->
                        updateStatus(orderId, newStatus)
                    },
                    onEditOrder = { orderId ->
                        selectedOrder = allOrders.firstOrNull { it.id == orderId }
                    }
                )
            }
        }
    }

    selectedOrder?.let { order ->
        OrderEditDialog(
            order = order,
            products = allProducts,
            tableNumber = order.tableNumber,
            onDismiss = { selectedOrder = null },
            onOrderUpdated = {
                selectedOrder = null
                loadOrders(showLoading = false)
            }
        )
    }
}

@Composable
private fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                color = CrewTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Overzicht van bestellingen en snelle acties",
                style = MaterialTheme.typography.bodyMedium,
                color = CrewTextSecondary
            )
        }
    }
}

@Composable
private fun DashboardStatsSection(
    orders: List<DashboardOrderUiModel>
) {
    val openOrders = orders.count {
        it.status == DashboardOrderStatus.IN_PROGRESS || it.status == DashboardOrderStatus.READY
    }
    val unpaidOrders = orders.count { !it.isPaid }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "Statistieken")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatisticCard(
                modifier = Modifier.weight(1f),
                title = "Open bestellingen",
                value = openOrders.toString(),
                subtitle = "In behandeling + klaar"
            )
            StatisticCard(
                modifier = Modifier.weight(1f),
                title = "Nog niet betaald",
                value = unpaidOrders.toString(),
                subtitle = "Openstaande rekeningen"
            )
        }
    }
}

@Composable
private fun QuickActionsSection(onNavigate: (String) -> Unit = {}) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "Snelle acties")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CrewSurface)
        ) {
            FlowRow(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    label = "Bestellen",
                    icon = Icons.Default.Restaurant,
                    onClick = { onNavigate("orders") }
                )
                QuickActionButton(
                    label = "Betalen",
                    icon = Icons.Default.Payment,
                    onClick = { onNavigate("payments") }
                )
                QuickActionButton(
                    label = "Kassa",
                    icon = Icons.Default.PointOfSale,
                    onClick = { onNavigate("cash_register") }
                )
            }
        }
    }
}

@Composable
private fun OrdersSection(
    selectedFilter: OrderFilter,
    onFilterSelected: (OrderFilter) -> Unit,
    orders: List<DashboardOrderUiModel>,
    onUpdateStatus: (Int, String) -> Unit,
    onEditOrder: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title = "Lopende bestellingen")

        OrderFilterRow(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            orders.forEach { order ->
                OrderCard(
                    order = order,
                    onUpdateStatus = onUpdateStatus,
                    onEditOrder = onEditOrder
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = CrewPrimary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun StatisticCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CrewSurface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = CrewTextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = CrewPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = CrewTextSecondary
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CrewAccent,
            contentColor = CrewOnAccent
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OrderFilterRow(
    selectedFilter: OrderFilter,
    onFilterSelected: (OrderFilter) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OrderFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(filter.label)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrewPrimaryDark,
                    selectedLabelColor = Color.White,
                    containerColor = CrewSurface,
                    labelColor = CrewTextSecondary
                )
            )
        }
    }
}

@Composable
private fun OrderCard(
    order: DashboardOrderUiModel,
    onUpdateStatus: (Int, String) -> Unit,
    onEditOrder: (Int) -> Unit
) {
    val statusColor = when (order.status) {
        DashboardOrderStatus.IN_PROGRESS -> OrderPending
        DashboardOrderStatus.READY -> OrderReady
        DashboardOrderStatus.DELIVERED -> OrderDelivered
        else -> OrderCancelled
    }

    val sourceColor = when (order.source) {
        OrderSource.CUSTOMER -> OrderFromQr
        OrderSource.EMPLOYEE -> CrewPrimaryDark
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        border = BorderStroke(
            width = 1.dp,
            color = sourceColor.copy(alpha = 0.22f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Tafel ${order.tableNumber} · ${order.customerName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = CrewTextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = order.timeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = CrewTextSecondary
                        )

                        OrderSourceBadge(source = order.source)
                    }
                }

                StatusBadge(
                    text = order.status.label,
                    color = statusColor
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}x ${item.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrewTextPrimary
                        )
                        Text(
                            text = item.priceLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrewTextSecondary
                        )
                    }
                }
            }

            if (order.note != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CrewSurfaceVariant,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Opmerking: ${order.note}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = CrewTextSecondary
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (order.status) {
                    DashboardOrderStatus.IN_PROGRESS -> {
                        SecondaryActionButton(
                            label = "Wijzigen",
                            modifier = Modifier.weight(1f),
                            onClick = { onEditOrder(order.id) }
                        )
                        PrimaryActionButton(
                            label = "Staat klaar",
                            onClick = { onUpdateStatus(order.id, "READY") },
                            modifier = Modifier.weight(1f),
                            containerColor = OrderReady,
                            contentColor = Color.White
                        )
                    }
                    DashboardOrderStatus.READY -> {
                        PrimaryActionButton(
                            label = "Werd afgeleverd",
                            onClick ={ onUpdateStatus(order.id, "DELIVERED") },
                            modifier = Modifier.weight(1f),
                            containerColor = OrderDelivered,
                            contentColor = Color.White
                        )
                    }
                    DashboardOrderStatus.DELIVERED -> {
                        SecondaryActionButton(
                            label = if (order.isPaid) "Betaald" else "Nog niet betaald",
                            modifier = Modifier.weight(1f),
                            enabled = false
                        )
                    }
                    DashboardOrderStatus.CANCELLED -> {
                        // No actions for cancelled orders
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.45f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OrderSourceBadge(
    source: OrderSource
) {
    val (label, color, icon) = when (source) {
        OrderSource.CUSTOMER -> Triple("Klant", CustomerOrderColor, Icons.Default.PhoneAndroid)
        OrderSource.EMPLOYEE -> Triple("Medewerker", CrewPrimaryDark, Icons.Default.Badge)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.28f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
            )

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PrimaryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CrewAccent,
    contentColor: Color = CrewOnAccent
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SecondaryActionButton(
    label: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CrewSurfaceVariant,
            contentColor = CrewTextPrimary,
            disabledContainerColor = CrewSurfaceVariant.copy(alpha = 0.6f),
            disabledContentColor = CrewTextSecondary
        )
    ) {
        Text(label, fontWeight = FontWeight.Medium)
    }
}

private enum class OrderFilter(val label: String) {
    All("Alle"),
    InProgress("In behandeling"),
    Ready("Klaar"),
    Delivered("Geleverd")
}

private enum class DashboardOrderStatus(val label: String) {
    IN_PROGRESS("In behandeling"),
    READY("Klaar"),
    DELIVERED("Geleverd"),
    CANCELLED("Geannuleerd")
}

private enum class OrderSource {
    EMPLOYEE,
    CUSTOMER
}

private data class DashboardOrderUiModel(
    val id: Int,
    val tableNumber: Int,
    val customerName: String,
    val timeLabel: String,
    val source: OrderSource,
    val status: DashboardOrderStatus,
    val isPaid: Boolean,
    val items: List<DashboardOrderLineUiModel>,
    val note: String?
)

private data class DashboardOrderLineUiModel(
    val name: String,
    val quantity: Int,
    val priceLabel: String
)

private fun OrderResponse.toDashboardUiModel(): DashboardOrderUiModel {
    return DashboardOrderUiModel(
        id = id,
        tableNumber = tableNumber ?: tableId,
        customerName = orderedByName,
        timeLabel = createdAt.toTimeLabel(),
        source = when (orderSource.uppercase()) {
            "CUSTOMER" -> OrderSource.CUSTOMER
            else -> OrderSource.EMPLOYEE
        },
        status = when (status.uppercase()) {
            "READY", "KLAAR" -> DashboardOrderStatus.READY
            "DELIVERED", "GELEVERD" -> DashboardOrderStatus.DELIVERED
            "CANCELLED", "GEANNULEERD" -> DashboardOrderStatus.CANCELLED
            else -> DashboardOrderStatus.IN_PROGRESS
        },
        isPaid = paymentId != null,
        items = lines.map {
            DashboardOrderLineUiModel(
                name = it.productNameSnapshot,
                quantity = it.quantity,
                priceLabel = formatPrice(it.lineTotal ?: ((it.unitPriceSnapshot ?: 0.0) * it.quantity))
            )
        },
        note = note
    )
}

private fun formatPrice(value: Double): String {
    // Multiplatform: fallback to basic formatting if advanced not available
    val rounded = value.toString().let {
        val idx = it.indexOf('.')
        if (idx == -1) "$it,00"
        else {
            val decimals = it.substring(idx + 1).padEnd(2, '0').take(2)
            it.substring(0, idx) + "," + decimals
        }
    }
    return "€$rounded"
}

private fun String.toTimeLabel(): String {
    return try {
        // verwacht bv. 2026-04-05T19:12:00
        substring(11, 16)
    } catch (_: Exception) {
        this
    }
}