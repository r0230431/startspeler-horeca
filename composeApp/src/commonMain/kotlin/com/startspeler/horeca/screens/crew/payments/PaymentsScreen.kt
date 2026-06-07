package com.startspeler.horeca.screens.crew.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startspeler.horeca.data.api.DiscountApi
import com.startspeler.horeca.data.api.OrdersApi
import com.startspeler.horeca.data.api.PaymentsApi
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.discounts.DiscountResponse
import com.startspeler.horeca.data.models.discounts.DiscountType
import com.startspeler.horeca.data.models.orders.OrderLineResponse
import com.startspeler.horeca.data.models.orders.OrderResponse
import com.startspeler.horeca.data.models.payments.CreatePaymentRequest
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewBackground
import com.startspeler.horeca.ui.theme.crew.CrewBorder
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextMuted
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary
import com.startspeler.horeca.ui.theme.crew.ErrorRed
import com.startspeler.horeca.ui.theme.crew.StockAvailable
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class PaymentMethodUi(val apiValue: String, val label: String) {
    CASH("CASH", "Cash"),
    BANCONTACT("BANCONTACT", "Bancontact"),
    PAYCONIQ("PAYCONIQ", "Payconiq")
}

private data class CustomerPaymentGroup(
    val key: String,
    val customerId: Int?,
    val displayName: String,
    val orders: List<OrderResponse>
) {
    val totalAmount: Double get() = orders.sumOf { it.totalAmount }
    val totalItems: Int get() = orders.sumOf { order -> order.lines.sumOf { it.quantity } }
}

private val DiscountResponse.displayLabel: String
    get() = when (discountType) {
        DiscountType.PERCENTAGE -> "$name (${discountValue.cleanNumber()}%)"
        DiscountType.FIXED_AMOUNT -> "$name (€ ${discountValue.toMoneyString()})"
    }

private val OrderResponse.totalAmount: Double
        get() = lines.sumOf { line -> (line.unitPriceSnapshot ?: 0.0) * line.quantity }

private fun String.toSafeDouble(): Double = toDoubleOrNull() ?: 0.0
private fun String.cleanNumber(): String = removeSuffix(".00").removeSuffix(",00")

private fun Double.toMoneyString(): String {
    val rounded = (this * 100).roundToInt()
    val euros = rounded / 100
    val cents = (rounded % 100).toString().padStart(2, '0')
    return "$euros,$cents"
}

private fun String.toMoneyString(): String = toSafeDouble().toMoneyString()

@Composable
fun PaymentsScreen() {
    val ordersApi = remember { OrdersApi() }
    val paymentsApi = remember { PaymentsApi() }
    val discountApi = remember { DiscountApi() }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var allOpenOrders by remember { mutableStateOf<List<OrderResponse>>(emptyList()) }
    var discounts by remember { mutableStateOf<List<DiscountResponse>>(emptyList()) }
    var selectedCustomerKey by remember { mutableStateOf<String?>(null) }
    var isSubmittingPayment by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    fun loadData(showLoading: Boolean) {
        scope.launch {
            if (showLoading) isLoading = true else isRefreshing = true
            try {
                val unpaidOrders = ordersApi.getOrdersFiltered(onlyUnpaid = true)
                allOpenOrders = unpaidOrders
                    .filter { it.status.equals("DELIVERED", ignoreCase = true) && it.paymentId == null }
                    .sortedByDescending { it.id }
                discounts = discountApi.getDiscounts().filter { it.isActive }
                errorMessage = null
            } catch (e: Exception) {
                if (allOpenOrders.isEmpty()) {
                    errorMessage = e.message ?: "Fout bij ophalen van openstaande rekeningen."
                }
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) { loadData(showLoading = true) }

    val groupedCustomers = remember(allOpenOrders) {
        allOpenOrders
            .groupBy { order ->
                val normalizedName = order.orderedByName.trim().ifBlank { "Onbekend" }
                val customerPart = order.customerId?.toString() ?: "name:${normalizedName.lowercase()}"
                "$customerPart|$normalizedName"
            }
            .map { (key, orders) ->
                CustomerPaymentGroup(
                    key = key,
                    customerId = orders.first().customerId,
                    displayName = orders.first().orderedByName.ifBlank { "Onbekend" },
                    orders = orders.sortedByDescending { it.id }
                )
            }
            .sortedByDescending { it.orders.maxOfOrNull { order -> order.id } ?: 0 }
    }

    val filteredCustomers = remember(groupedCustomers, searchQuery) {
        groupedCustomers.filter { group ->
            searchQuery.isBlank() || group.displayName.contains(searchQuery, ignoreCase = true)
        }
    }

    val selectedCustomer = groupedCustomers.firstOrNull { it.key == selectedCustomerKey }

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
                    text = "Betalingen",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CrewTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Klanten met onbetaalde geleverde bestellingen",
                    color = CrewTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
            } else {
                IconButton(onClick = { loadData(showLoading = false) }) {
                    Icon(Icons.Default.Payments, contentDescription = "Vernieuwen")
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = CrewTextMuted)
            },
            placeholder = { Text("Zoek klant op naam") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CrewAccent,
                unfocusedBorderColor = CrewBorder
            )
        )

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
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
                            text = "Openstaande rekeningen konden niet worden geladen.",
                            color = ErrorRed,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(errorMessage ?: "", color = CrewTextSecondary)
                        Button(onClick = { loadData(showLoading = true) }) {
                            Text("Opnieuw proberen")
                        }
                    }
                }
            }

            filteredCustomers.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CrewSurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) {
                                "Geen openstaande rekeningen gevonden."
                            } else {
                                "Geen klanten gevonden voor deze zoekopdracht."
                            },
                            color = CrewTextSecondary
                        )
                    }
                }
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredCustomers, key = { it.key }) { group ->
                        CustomerPaymentCard(
                            group = group,
                            onClick = { selectedCustomerKey = group.key }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }

    selectedCustomer?.let { group ->
        PaymentDialog(
            group = group,
            discounts = discounts,
            isSubmitting = isSubmittingPayment,
            onDismiss = {
                if (!isSubmittingPayment) selectedCustomerKey = null
            },
            onConfirm = { method, discountIds ->
                scope.launch {
                    isSubmittingPayment = true
                    val result = paymentsApi.createPayment(
                        CreatePaymentRequest(
                            paymentMethod = method.apiValue,
                            orderIds = group.orders.map { it.id },
                            discountIds = discountIds
                        )
                    )
                    when (result) {
                        is ApiResult.Success -> {
                            successMessage = "Betaling voor ${group.displayName} geregistreerd."
                            selectedCustomerKey = null
                            loadData(showLoading = false)
                        }
                        is ApiResult.Error -> {
                            errorMessage = result.message
                        }
                    }
                    isSubmittingPayment = false
                }
            }
        )
    }

    if (successMessage != null) {
        AlertDialog(
            onDismissRequest = { successMessage = null },
            confirmButton = {
                Button(onClick = { successMessage = null }) {
                    Text("OK")
                }
            },
            title = { Text("Betaling opgeslagen") },
            text = { Text(successMessage!!) }
        )
    }
}

@Composable
private fun CustomerPaymentCard(
    group: CustomerPaymentGroup,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = CrewAccent.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = CrewOnAccent
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = group.displayName,
                    fontWeight = FontWeight.Bold,
                    color = CrewTextPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${group.orders.size} ${if (group.orders.size == 1) "bestelling" else "bestellingen"} · ${group.totalItems} ${if (group.totalItems == 1) "item" else "items"}",
                    color = CrewTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "€ ${group.totalAmount.toMoneyString()}",
                color = CrewAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun PaymentDialog(
    group: CustomerPaymentGroup,
    discounts: List<DiscountResponse>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethodUi, List<Int>) -> Unit
) {
    var selectedMethod by remember(group.key) { mutableStateOf<PaymentMethodUi?>(null) }
    var selectedDiscountIds by remember(group.key) { mutableStateOf<Set<Int>>(emptySet()) }
    var localError by remember(group.key) { mutableStateOf<String?>(null) }

    val subtotal = group.totalAmount
    val discountAmount = calculateTotalDiscountAmount(subtotal, discounts, selectedDiscountIds)
    val total = (subtotal - discountAmount).coerceAtLeast(0.0)


    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = {},
        text = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CrewSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = group.displayName,
                            color = CrewTextPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "${group.orders.size} openstaande geleverde bestellingen",
                            color = CrewTextSecondary
                        )
                    }

                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        group.orders.forEach { order ->
                            OrderSection(order = order)
                        }
                    }

                    HorizontalDivider()

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Kortingen",
                            color = CrewTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (discounts.isEmpty()) {
                            Text(
                                text = "Geen kortingen beschikbaar.",
                                color = CrewTextMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                discounts.forEach { discount ->
                                    val checked = discount.id in selectedDiscountIds
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedDiscountIds = if (checked)
                                                    selectedDiscountIds - discount.id
                                                else
                                                    selectedDiscountIds + discount.id
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = { isChecked ->
                                                selectedDiscountIds = if (isChecked)
                                                    selectedDiscountIds + discount.id
                                                else
                                                    selectedDiscountIds - discount.id
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = CrewAccent)
                                        )
                                        Text(
                                            text = discount.displayLabel,
                                            color = CrewTextPrimary,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SummaryRow(label = "Subtotaal", value = "€ ${subtotal.toMoneyString()}")
                        SummaryRow(
                            label = "Korting",
                            value = "- € ${discountAmount.toMoneyString()}",
                            valueColor = if (discountAmount > 0.0) StockAvailable else CrewTextSecondary
                        )
                        SummaryRow(
                            label = "Te betalen",
                            value = "€ ${total.toMoneyString()}",
                            emphasized = true
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Betaalmethode",
                            color = CrewTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentMethodButton(
                                modifier = Modifier.weight(1f),
                                label = "Cash",
                                icon = Icons.Default.AttachMoney,
                                selected = selectedMethod == PaymentMethodUi.CASH,
                                onClick = { selectedMethod = PaymentMethodUi.CASH }
                            )
                            PaymentMethodButton(
                                modifier = Modifier.weight(1f),
                                label = "Bancontact",
                                icon = Icons.Default.CreditCard,
                                selected = selectedMethod == PaymentMethodUi.BANCONTACT,
                                onClick = { selectedMethod = PaymentMethodUi.BANCONTACT }
                            )
                            PaymentMethodButton(
                                modifier = Modifier.weight(1f),
                                label = "Payconiq",
                                icon = Icons.Default.PhoneAndroid,
                                selected = selectedMethod == PaymentMethodUi.PAYCONIQ,
                                onClick = { selectedMethod = PaymentMethodUi.PAYCONIQ }
                            )
                        }
                    }

                    if (localError != null) {
                        Text(localError!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            val method = selectedMethod
                            if (method == null) {
                                localError = "Selecteer eerst een betaalmethode."
                            } else {
                                localError = null
                                onConfirm(method, selectedDiscountIds.toList())
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrewAccent,
                            contentColor = CrewOnAccent
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Betaling bevestigen", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5E7EB),
                            contentColor = CrewTextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Annuleren")
                    }
                }
            }
        }
    )
}

@Composable
private fun OrderSection(order: OrderResponse) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CrewBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Bestelling #${order.id}",
                    color = CrewTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tafel ${order.tableNumber ?: order.tableId}",
                    color = CrewTextSecondary
                )
            }

            order.lines.forEach { line ->
                OrderLineRow(line)
            }

            HorizontalDivider(color = CrewBorder)
            SummaryRow(
                label = "Totaal bestelling",
                value = "€ ${order.totalAmount.toMoneyString()}",
                emphasized = true
            )
        }
    }
}

@Composable
private fun OrderLineRow(line: OrderLineResponse) {
    val unitPrice = line.unitPriceSnapshot ?: 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${line.quantity}x ${line.productNameSnapshot}",
            modifier = Modifier.weight(1f),
            color = CrewTextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("€ ${unitPrice.toMoneyString()}", color = CrewTextSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = CrewTextPrimary,
    emphasized: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            color = if (emphasized) CrewTextPrimary else CrewTextSecondary,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            color = valueColor,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun PaymentMethodButton(
    modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) CrewAccent else CrewSurface,
        tonalElevation = if (selected) 2.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) CrewAccent else CrewBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) CrewOnAccent else CrewTextSecondary
            )
            Text(
                text = label,
                color = if (selected) CrewOnAccent else CrewTextPrimary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun calculateTotalDiscountAmount(
    subtotal: Double,
    discounts: List<DiscountResponse>,
    selectedIds: Set<Int>
): Double {
    val selected = discounts.filter { it.id in selectedIds }
    if (selected.isEmpty()) return 0.0

    // Eerst % kortingen op subtotaal berekenen
    val percentTotal = selected
        .filter { it.discountType == DiscountType.PERCENTAGE }
        .sumOf { subtotal * (it.discountValue.toSafeDouble() / 100.0) }

    // Daarna vaste bedragen
    val fixedTotal = selected
        .filter { it.discountType == DiscountType.FIXED_AMOUNT }
        .sumOf { it.discountValue.toSafeDouble() }

    return (percentTotal + fixedTotal).coerceAtMost(subtotal)
}

