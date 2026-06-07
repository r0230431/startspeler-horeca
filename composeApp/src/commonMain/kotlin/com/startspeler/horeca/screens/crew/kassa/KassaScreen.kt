package com.startspeler.horeca.screens.crew.kassa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.api.PaymentsApi
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.payments.PaymentSummaryResponse
import com.startspeler.horeca.screens.crew.overviews.CollapsibleFilterCard
import com.startspeler.horeca.screens.crew.overviews.CompactDateTimeInputs
import com.startspeler.horeca.screens.crew.overviews.PaymentFiltersPreset
import com.startspeler.horeca.screens.crew.overviews.buildDateRangeSummary
import com.startspeler.horeca.screens.crew.overviews.currentBrusselsDateTime
import com.startspeler.horeca.screens.crew.overviews.defaultFromDateTime
import com.startspeler.horeca.screens.crew.overviews.formatDateInput
import com.startspeler.horeca.screens.crew.overviews.formatTimeInput
import com.startspeler.horeca.screens.crew.overviews.parseDateTimeInputs
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewBackground
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary
import kotlinx.coroutines.launch

@Composable
fun KassaScreen(
    onOpenPaymentMethodDetails: (PaymentFiltersPreset) -> Unit
) {
    val paymentsApi = remember { PaymentsApi() }
    val scope = rememberCoroutineScope()

    val now = remember { currentBrusselsDateTime() }
    val defaultFrom = remember { defaultFromDateTime() }

    var fromDate by remember { mutableStateOf(formatDateInput(defaultFrom.date)) }
    var fromTime by remember { mutableStateOf(formatTimeInput(defaultFrom.time)) }
    var toDate by remember { mutableStateOf(formatDateInput(now.date)) }
    var toTime by remember { mutableStateOf(formatTimeInput(now.time)) }
    var filtersExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var summary by remember { mutableStateOf<PaymentSummaryResponse?>(null) }

    fun loadSummary(showLoading: Boolean) {
        val from = parseDateTimeInputs(fromDate, fromTime)?.toString()
        val to = parseDateTimeInputs(toDate, toTime)?.toString()
        scope.launch {
            if (showLoading) isLoading = true else isRefreshing = true
            when (val result = paymentsApi.getPaymentSummary(from = from, to = to)) {
                is ApiResult.Success -> {
                    summary = result.data
                    errorMessage = null
                }
                is ApiResult.Error -> {
                    errorMessage = result.message
                    summary = null
                }
            }
            isLoading = false
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadSummary(showLoading = true)
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
                    text = "Kassa",
                    style = MaterialTheme.typography.headlineMedium,
                    color = CrewTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Totaalbedrag per betaalmethode voor de geselecteerde periode",
                    color = CrewTextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isRefreshing) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
            } else {
                IconButton(onClick = { loadSummary(showLoading = false) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Vernieuwen")
                }
            }
        }

        CollapsibleFilterCard(
            title = "Periode",
            summary = buildDateRangeSummary(fromDate, fromTime, toDate, toTime),
            expanded = filtersExpanded,
            onToggle = { filtersExpanded = !filtersExpanded }
        ) {
            CompactDateTimeInputs(
                fromDate = fromDate,
                fromTime = fromTime,
                toDate = toDate,
                toTime = toTime,
                onFromDateChange = { fromDate = it },
                onFromTimeChange = { fromTime = it },
                onToDateChange = { toDate = it },
                onToTimeChange = { toTime = it }
            )
            Button(onClick = { loadSummary(showLoading = false) }) {
                Text("Pas filters toe")
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Card(colors = CardDefaults.cardColors(containerColor = CrewSurface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Kassaoverzicht kon niet worden geladen", fontWeight = FontWeight.SemiBold, color = CrewTextPrimary)
                        Text(errorMessage ?: "Onbekende fout", color = CrewTextSecondary)
                    }
                }
            }

            else -> {
                val totals = summary?.totalsByMethod.orEmpty()
                if (totals.isEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = CrewSurface)) {
                        Text(
                            text = "Er zijn geen betalingen voor de gekozen periode.",
                            modifier = Modifier.padding(16.dp),
                            color = CrewTextSecondary
                        )
                    }
                } else {
                    totals.forEach { total ->
                        KassaMethodCard(
                            method = total.paymentMethod,
                            amount = total.totalAmount,
                            onClick = {
                                onOpenPaymentMethodDetails(
                                    PaymentFiltersPreset(
                                        from = parseDateTimeInputs(fromDate, fromTime)?.toString(),
                                        to = parseDateTimeInputs(toDate, toTime)?.toString(),
                                        paymentMethod = total.paymentMethod
                                    )
                                )
                            }
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CrewPrimary),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Totaal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CrewAccent
                        )
                        Text(
                            text = "€ ${summary?.grandTotal?.replace('.', ',') ?: "0,00"}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KassaMethodCard(
    method: String,
    amount: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = iconForMethod(method),
                    contentDescription = null,
                    tint = CrewAccent,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(methodLabel(method), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = CrewTextPrimary)
                    Text("Klik voor detailoverzicht", style = MaterialTheme.typography.bodySmall, color = CrewTextSecondary)
                }
            }
            Text(
                text = "€ ${amount.replace('.', ',')}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CrewPrimary
            )
        }
    }
}

private fun methodLabel(method: String): String = when (method.uppercase()) {
    "CASH" -> "Cash"
    "BANCONTACT", "CARD" -> "Bancontact"
    "PAYCONIQ", "MOBILE" -> "Payconiq"
    else -> method
}

private fun iconForMethod(method: String): ImageVector = when (method.uppercase()) {
    "CASH" -> Icons.Default.LocalAtm
    "BANCONTACT", "CARD" -> Icons.Default.CreditCard
    "PAYCONIQ", "MOBILE" -> Icons.Default.PhoneAndroid
    else -> Icons.Default.Payments
}
