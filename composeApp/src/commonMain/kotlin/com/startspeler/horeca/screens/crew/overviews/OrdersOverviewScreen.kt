package com.startspeler.horeca.screens.crew.overviews

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.orders.OrderResponse
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.ui.theme.crew.*

internal data class EditableOrderLine(
    val productId: Int?,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int
) {
    val lineTotal: Double get() = unitPrice * quantity
}

@Composable
internal fun OrdersOverviewScreen(
    orders: List<OrderResponse>,
    allOrders: List<OrderResponse>,
    products: List<ProductResponse>,
    tableNumberById: Map<Int, Int>,
    filtersExpanded: Boolean,
    onFiltersExpandedChange: (Boolean) -> Unit,
    fromDate: String,
    fromTime: String,
    toDate: String,
    toTime: String,
    onFromDateChange: (String) -> Unit,
    onFromTimeChange: (String) -> Unit,
    onToDateChange: (String) -> Unit,
    onToTimeChange: (String) -> Unit,
    customerQuery: String,
    onCustomerQueryChange: (String) -> Unit,
    statusFilter: String,
    onStatusFilterChange: (String) -> Unit,
    paidFilter: PaidFilter,
    onPaidFilterChange: (PaidFilter) -> Unit,
    focusedOrderIds: Set<Int>,
    onClearFocus: () -> Unit,
    onOrderSelected: (OrderResponse) -> Unit,
    onResetFilters: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CollapsibleFilterCard(
            title = "Filters bestellingen",
            summary = buildDateRangeSummary(fromDate, fromTime, toDate, toTime),
            expanded = filtersExpanded,
            onToggle = { onFiltersExpandedChange(!filtersExpanded) }
        ) {
            CompactDateTimeInputs(
                fromDate = fromDate,
                fromTime = fromTime,
                toDate = toDate,
                toTime = toTime,
                onFromDateChange = onFromDateChange,
                onFromTimeChange = onFromTimeChange,
                onToDateChange = onToDateChange,
                onToTimeChange = onToTimeChange
            )
            OutlinedTextField(
                value = customerQuery,
                onValueChange = onCustomerQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Klantnaam") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            FilterChipRow(
                selected = statusFilter,
                values = listOf(
                    "ALL" to "Alle statussen",
                    "IN_PROGRESS" to "In behandeling",
                    "READY" to "Klaar",
                    "DELIVERED" to "Geleverd",
                    "CANCELLED" to "Geannuleerd"
                ),
                onSelected = onStatusFilterChange
            )
            FilterChipRow(
                selected = paidFilter.name,
                values = PaidFilter.entries.map { it.name to it.label },
                onSelected = { selected -> onPaidFilterChange(PaidFilter.valueOf(selected)) }
            )
            Button(
                onClick = onResetFilters,
                colors = ButtonDefaults.buttonColors(containerColor = CrewPrimary, contentColor = CrewOnPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Filters wissen")
            }
        }

        if (focusedOrderIds.isNotEmpty()) {
            InfoBanner(
                text = "Je bekijkt momenteel enkel de bestellingen die bij een geselecteerde betaling horen.",
                actionLabel = "Wis focus",
                onAction = onClearFocus
            )
        }

        if (orders.isEmpty()) {
            FeedbackCard(
                title = "Geen bestellingen gevonden",
                message = if (allOrders.isEmpty()) {
                    "Er zijn nog geen bestellingen beschikbaar."
                } else {
                    "Geen bestellingen binnen de gekozen filters."
                }
            )
        } else {
            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(orders, key = { it.id }) { order ->
                    OrderOverviewCard(
                        order = order,
                        tableNumber = tableNumberById[order.tableId],
                        onClick = { onOrderSelected(order) }
                    )
                }
                item { Spacer(Modifier.size(12.dp)) }
            }
        }
    }
}

@Composable
private fun OrderOverviewCard(
    order: OrderResponse,
    tableNumber: Int?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Bestelling #${order.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CrewTextPrimary
                    )
                    Text(
                        text = "${order.orderedByName} • tafel ${tableNumber ?: order.tableId}",
                        color = CrewTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                OverviewStatusBadge(status = order.status)
            }

            Text(
                text = order.createdAt.replace('T', ' '),
                style = MaterialTheme.typography.bodySmall,
                color = CrewTextMuted
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrderSourceBadge(orderSource = order.orderSource)
                PaymentStateBadge(isPaid = order.paymentId != null)
            }
        }
    }
}