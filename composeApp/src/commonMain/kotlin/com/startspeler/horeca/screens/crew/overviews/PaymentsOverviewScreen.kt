package com.startspeler.horeca.screens.crew.overviews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.ui.theme.crew.CrewOnPrimary
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextMuted
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary

@Composable
internal fun PaymentsOverviewScreen(
    payments: List<PaymentUiModel>,
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
    selectedMethod: String,
    onMethodChange: (String) -> Unit,
    customerQuery: String,
    onCustomerQueryChange: (String) -> Unit,
    discountQuery: String,
    onDiscountQueryChange: (String) -> Unit,
    onOpenRelatedOrders: (List<Int>) -> Unit,
    onResetFilters: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CollapsibleFilterCard(
            title = "Filters betalingen",
            summary = buildDateRangeSummary(fromDate, fromTime, toDate, toTime),
            expanded = filtersExpanded,
            onToggle = { onFiltersExpandedChange(!filtersExpanded) },
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
            FilterChipRow(
                selected = selectedMethod,
                values = listOf(
                    "ALL" to "Alle methodes",
                    "CASH" to "Cash",
                    "BANCONTACT" to "Bancontact",
                    "PAYCONIQ" to "Payconiq"
                ),
                onSelected = onMethodChange
            )
            OutlinedTextField(
                value = customerQuery,
                onValueChange = onCustomerQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Klantnaam") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            OutlinedTextField(
                value = discountQuery,
                onValueChange = onDiscountQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Korting") }
            )
            Button(
                onClick = onResetFilters,
                colors = ButtonDefaults.buttonColors(containerColor = CrewPrimary, contentColor = CrewOnPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Filters wissen")
            }
        }

        if (payments.isEmpty()) {
            FeedbackCard(
                title = "Geen betalingen gevonden",
                message = "Er zijn geen betalingen binnen de gekozen filters."
            )
        } else {
            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(payments, key = { it.payment.id }) { item ->
                    PaymentOverviewCard(
                        item = item,
                        onOpenRelatedOrders = { onOpenRelatedOrders(item.payment.orderIds) }
                    )
                }
                item { Spacer(Modifier.size(12.dp)) }
            }
        }
    }
}

@Composable
private fun PaymentOverviewCard(
    item: PaymentUiModel,
    onOpenRelatedOrders: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.customerLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CrewTextPrimary
                    )
                    Text(
                        text = item.paidAtLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrewTextMuted
                    )
                }
                Text(
                    text = "€ ${item.payment.totalAmount.toMoneyString()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CrewPrimary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethodBadge(item.payment.paymentMethod)
                DiscountBadge(item.discountLabel)
            }

            Button(onClick = onOpenRelatedOrders) {
                Text("Bekijk bestellingen")
            }
        }
    }
}

@Composable
private fun PaymentMethodBadge(method: String) {
    androidx.compose.material3.Surface(
        color = CrewPrimary.copy(alpha = 0.08f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(paymentMethodIcon(method), contentDescription = null, tint = CrewPrimary, modifier = Modifier.size(14.dp))
            Text(paymentMethodLabel(method), style = MaterialTheme.typography.labelMedium, color = CrewPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DiscountBadge(label: String) {
    androidx.compose.material3.Surface(
        color = CrewPrimary.copy(alpha = 0.08f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = CrewPrimary, modifier = Modifier.size(14.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = CrewPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}
