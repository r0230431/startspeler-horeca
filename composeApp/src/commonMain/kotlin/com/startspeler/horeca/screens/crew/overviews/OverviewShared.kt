package com.startspeler.horeca.screens.crew.overviews

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.payments.PaymentResponse
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewBorder
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewPrimaryDark
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextMuted
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary
import com.startspeler.horeca.ui.theme.crew.ErrorRed
import com.startspeler.horeca.ui.theme.crew.OrderCancelled
import com.startspeler.horeca.ui.theme.crew.OrderDelivered
import com.startspeler.horeca.ui.theme.crew.OrderPending
import com.startspeler.horeca.ui.theme.crew.OrderReady
import com.startspeler.horeca.ui.theme.customer.CustomerOrderColor
import com.startspeler.horeca.ui.theme.customer.PaidBackgroundColor
import com.startspeler.horeca.ui.theme.customer.PaidTextColor
import com.startspeler.horeca.ui.theme.customer.UnpaidBackgroundColor
import com.startspeler.horeca.ui.theme.customer.UnpaidTextColor
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

internal enum class PaidFilter(val label: String) {
        ALL("Alles"),
        UNPAID("Onbetaald"),
        PAID("Betaald")
}

enum class OverviewTab(val label: String) {
        ORDERS("Bestellingen"),
        PAYMENTS("Betalingen")
}

data class PaymentFiltersPreset(
        val from: String? = null,
        val to: String? = null,
        val paymentMethod: String? = null
)

data class OverviewNavigationRequest(
        val selectedTab: OverviewTab = OverviewTab.ORDERS,
        val paymentFiltersPreset: PaymentFiltersPreset? = null,
        val focusedOrderIds: Set<Int> = emptySet(),
        val requestId: Long = 0L
)

internal data class PaymentUiModel(
        val payment: PaymentResponse,
        val customerLabel: String,
        val discountLabel: String,
        val methodLabel: String,
        val paidAtLabel: String
)

internal val AppTimeZone: TimeZone
        get() =
                runCatching {
                        TimeZone.currentSystemDefault().also { tz ->
                                Clock.System.now()
                                        .toLocalDateTime(
                                                tz
                                        ) // validate it actually works on this platform
                        }
                }
                        .getOrElse { TimeZone.UTC }

internal fun currentBrusselsDateTime(): LocalDateTime =
        Clock.System.now().toLocalDateTime(AppTimeZone)

internal fun defaultFromDateTime(): LocalDateTime {
        val now = currentBrusselsDateTime()
        return LocalDateTime(now.date, LocalTime(hour = 9, minute = 0, second = 0))
}

internal fun formatDateInput(date: LocalDate): String =
        "${date.day.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"

internal fun formatTimeInput(time: LocalTime): String =
        "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

internal fun formatDateTimeSummary(dateTime: LocalDateTime): String =
        "${formatDateInput(dateTime.date)} ${formatTimeInput(dateTime.time)}"

internal fun parseDateInput(value: String): LocalDate? {
        val parts = value.trim().split("/")
        if (parts.size != 3) return null
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        return runCatching { LocalDate(year, month, day) }.getOrNull()
}

internal fun parseTimeInput(value: String): LocalTime? {
        val parts = value.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        return runCatching { LocalTime(hour, minute, 0) }.getOrNull()
}

internal fun parseDateTimeInputs(date: String, time: String): LocalDateTime? {
        val parsedDate = parseDateInput(date) ?: return null
        val parsedTime = parseTimeInput(time) ?: return null
        return parsedDate.atTime(parsedTime)
}

internal fun LocalDateTime.toEpochMillis(): Long = toInstant(AppTimeZone).toEpochMilliseconds()

internal fun parseServerDateTime(value: String?): LocalDateTime? {
        if (value.isNullOrBlank()) return null
        return runCatching { LocalDateTime.parse(value) }.getOrNull()
}

internal fun Double.toMoneyString(): String {
        val rounded = kotlin.math.round(this * 100) / 100
        val parts = rounded.toString().split('.')
        val euros = parts[0]
        val cents = parts.getOrNull(1)?.padEnd(2, '0')?.take(2) ?: "00"
        return "$euros,$cents"
}

internal fun String.toMoneyString(): String = (toDoubleOrNull() ?: 0.0).toMoneyString()

internal fun buildDateRangeSummary(
        fromDate: String,
        fromTime: String,
        toDate: String,
        toTime: String
): String {
        return "$fromDate $fromTime - $toDate $toTime"
}

internal fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

internal fun daysInMonth(year: Int, month: Int): Int =
        when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear(year)) 29 else 28
                else -> 30
        }

internal fun buildYearOptions(selectedYear: Int): List<Int> {
        val currentYear = currentBrusselsDateTime().date.year
        val start = minOf(currentYear - 3, selectedYear - 1)
        val end = maxOf(currentYear + 1, selectedYear + 1)
        return (start..end).toList()
}

internal fun paymentMethodLabel(method: String): String =
        when (method.uppercase()) {
                "CASH" -> "Cash"
                "BANCONTACT", "CARD" -> "Bancontact"
                "PAYCONIQ", "MOBILE" -> "Payconiq"
                else ->
                        method.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
        }

internal fun paymentMethodIcon(method: String): ImageVector =
        when (method.uppercase()) {
                "CASH" -> Icons.Default.LocalAtm
                "BANCONTACT", "CARD" -> Icons.Default.CreditCard
                "PAYCONIQ", "MOBILE" -> Icons.Default.PhoneAndroid
                else -> Icons.Default.Payments
        }

internal fun statusLabel(status: String): String =
        when (status.uppercase()) {
                "IN_PROGRESS" -> "In behandeling"
                "READY" -> "Klaar"
                "DELIVERED" -> "Geleverd"
                "CANCELLED" -> "Geannuleerd"
                else -> status
        }

@Composable
internal fun CollapsibleFilterCard(
        title: String,
        summary: String,
        expanded: Boolean,
        onToggle: () -> Unit,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
) {
        Card(
                modifier = modifier.fillMaxWidth(),
                border = BorderStroke(2.dp, CrewAccent),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CrewAccent.copy(alpha = 0.05f)),
        ) {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text = title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = CrewTextPrimary
                                        )
                                        Text(
                                                text = summary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CrewTextMuted
                                        )
                                }
                                IconButton(onClick = onToggle) {
                                        Icon(
                                                imageVector =
                                                        if (expanded) Icons.Default.ExpandLess
                                                        else Icons.Default.ExpandMore,
                                                contentDescription =
                                                        if (expanded) "Filters sluiten"
                                                        else "Filters openen",
                                                tint = CrewAccent
                                        )
                                }
                        }

                        AnimatedVisibility(visible = expanded) {
                                Column(
                                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) { content() }
                        }
                }
        }
}

@Composable
internal fun CompactDateTimeInputs(
        fromDate: String,
        fromTime: String,
        toDate: String,
        toTime: String,
        onFromDateChange: (String) -> Unit,
        onFromTimeChange: (String) -> Unit,
        onToDateChange: (String) -> Unit,
        onToTimeChange: (String) -> Unit,
        modifier: Modifier = Modifier
) {
        Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
                Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(
                                "Van",
                                modifier = Modifier.width(36.dp),
                                color = CrewTextMuted,
                                style = MaterialTheme.typography.bodySmall
                        )

                        CompactDatePickerField(
                                value = fromDate,
                                label = "DD/MM/JJJJ",
                                onValueSelected = onFromDateChange,
                                modifier = Modifier.weight(1f)
                        )

                        CompactTimePickerField(
                                value = fromTime,
                                label = "UU:MM",
                                onValueSelected = onFromTimeChange,
                                modifier = Modifier.weight(0.7f)
                        )
                }

                Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(
                                "Tot",
                                modifier = Modifier.width(36.dp),
                                color = CrewTextMuted,
                                style = MaterialTheme.typography.bodySmall
                        )

                        CompactDatePickerField(
                                value = toDate,
                                label = "DD/MM/JJJJ",
                                onValueSelected = onToDateChange,
                                modifier = Modifier.weight(1f)
                        )

                        CompactTimePickerField(
                                value = toTime,
                                label = "UU:MM",
                                onValueSelected = onToTimeChange,
                                modifier = Modifier.weight(0.7f)
                        )
                }
        }
}

@Composable
private fun CompactDatePickerField(
        value: String,
        label: String,
        onValueSelected: (String) -> Unit,
        modifier: Modifier = Modifier
) {
        var showDialog by remember { mutableStateOf(false) }

        CompactPickerField(
                value = value,
                label = label,
                icon = Icons.Default.DateRange,
                modifier = modifier,
                onClick = { showDialog = true }
        )

        if (showDialog) {
                DateSelectionDialog(
                        initialDate = parseDateInput(value) ?: currentBrusselsDateTime().date,
                        onDismiss = { showDialog = false },
                        onConfirm = { selected ->
                                onValueSelected(formatDateInput(selected))
                                showDialog = false
                        }
                )
        }
}

@Composable
private fun CompactTimePickerField(
        value: String,
        label: String,
        onValueSelected: (String) -> Unit,
        modifier: Modifier = Modifier
) {
        var showDialog by remember { mutableStateOf(false) }

        CompactPickerField(
                value = value,
                label = label,
                icon = Icons.Default.AccessTime,
                modifier = modifier,
                onClick = { showDialog = true }
        )

        if (showDialog) {
                TimeSelectionDialog(
                        initialTime = parseTimeInput(value) ?: currentBrusselsDateTime().time,
                        onDismiss = { showDialog = false },
                        onConfirm = { selected ->
                                onValueSelected(formatTimeInput(selected))
                                showDialog = false
                        }
                )
        }
}

@Composable
private fun CompactPickerField(
        value: String,
        label: String,
        icon: ImageVector,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
        OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CrewBorder),
                colors =
                        ButtonDefaults.outlinedButtonColors(
                                containerColor = CrewSurface,
                                contentColor = CrewTextPrimary
                        )
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Column(modifier = Modifier.weight(1f)) {
                                Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CrewTextMuted
                                )
                                Text(
                                        text = value.ifBlank { "Kies..." },
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                                if (value.isBlank()) CrewTextMuted
                                                else CrewTextPrimary,
                                        fontWeight = FontWeight.Medium
                                )
                        }

                        Icon(imageVector = icon, contentDescription = null, tint = CrewAccent)
                }
        }
}

@Composable
private fun DateSelectionDialog(
        initialDate: LocalDate,
        onDismiss: () -> Unit,
        onConfirm: (LocalDate) -> Unit
) {
        var selectedDay by remember { mutableStateOf(initialDate.day) }
        var selectedMonth by remember { mutableStateOf(initialDate.monthNumber) }
        var selectedYear by remember { mutableStateOf(initialDate.year) }

        val maxDay = daysInMonth(selectedYear, selectedMonth)

        LaunchedEffect(selectedMonth, selectedYear) {
                if (selectedDay > maxDay) selectedDay = maxDay
        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Text(
                                text = "Kies een datum",
                                color = CrewTextPrimary,
                                fontWeight = FontWeight.SemiBold
                        )
                },
                text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                        text =
                                                "${selectedDay.toString().padStart(2, '0')}/${selectedMonth.toString().padStart(2, '0')}/$selectedYear",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CrewAccent,
                                        fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        PickerDropdownSelector(
                                                label = "Dag",
                                                selectedValue = selectedDay,
                                                values = (1..maxDay).toList(),
                                                valueFormatter = { it.toString().padStart(2, '0') },
                                                onValueSelected = { selectedDay = it },
                                                modifier = Modifier.weight(1f)
                                        )

                                        PickerDropdownSelector(
                                                label = "Maand",
                                                selectedValue = selectedMonth,
                                                values = (1..12).toList(),
                                                valueFormatter = { it.toString().padStart(2, '0') },
                                                onValueSelected = { selectedMonth = it },
                                                modifier = Modifier.weight(1f)
                                        )

                                        PickerDropdownSelector(
                                                label = "Jaar",
                                                selectedValue = selectedYear,
                                                values = buildYearOptions(selectedYear),
                                                valueFormatter = { it.toString() },
                                                onValueSelected = { selectedYear = it },
                                                modifier = Modifier.weight(1.2f)
                                        )
                                }
                        }
                },
                confirmButton = {
                        TextButton(
                                onClick = {
                                        onConfirm(
                                                LocalDate(selectedYear, selectedMonth, selectedDay)
                                        )
                                }
                        ) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Annuleren") } },
                containerColor = CrewSurface
        )
}

@Composable
private fun TimeSelectionDialog(
        initialTime: LocalTime,
        onDismiss: () -> Unit,
        onConfirm: (LocalTime) -> Unit
) {
        var selectedHour by remember { mutableStateOf(initialTime.hour) }
        var selectedMinute by remember { mutableStateOf(initialTime.minute) }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Text(
                                text = "Kies een tijd",
                                color = CrewTextPrimary,
                                fontWeight = FontWeight.SemiBold
                        )
                },
                text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                        text =
                                                "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CrewAccent,
                                        fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        PickerDropdownSelector(
                                                label = "Uur",
                                                selectedValue = selectedHour,
                                                values = (0..23).toList(),
                                                valueFormatter = { it.toString().padStart(2, '0') },
                                                onValueSelected = { selectedHour = it },
                                                modifier = Modifier.weight(1f)
                                        )

                                        PickerDropdownSelector(
                                                label = "Min",
                                                selectedValue = selectedMinute,
                                                values = (0..59).toList(),
                                                valueFormatter = { it.toString().padStart(2, '0') },
                                                onValueSelected = { selectedMinute = it },
                                                modifier = Modifier.weight(1f)
                                        )
                                }
                        }
                },
                confirmButton = {
                        TextButton(
                                onClick = { onConfirm(LocalTime(selectedHour, selectedMinute, 0)) }
                        ) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Annuleren") } },
                containerColor = CrewSurface
        )
}

@Composable
private fun <T> PickerDropdownSelector(
        label: String,
        selectedValue: T,
        values: List<T>,
        valueFormatter: (T) -> String,
        onValueSelected: (T) -> Unit,
        modifier: Modifier = Modifier
) {
        var expanded by remember { mutableStateOf(false) }

        Column(modifier = modifier) {
                Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = CrewTextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box {
                        OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, CrewBorder),
                                colors =
                                        ButtonDefaults.outlinedButtonColors(
                                                containerColor = CrewSurface,
                                                contentColor = CrewTextPrimary
                                        )
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                text = valueFormatter(selectedValue),
                                                style = MaterialTheme.typography.bodyMedium
                                        )
                                        Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = CrewAccent
                                        )
                                }
                        }

                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                values.forEach { value ->
                                        DropdownMenuItem(
                                                text = { Text(valueFormatter(value)) },
                                                onClick = {
                                                        onValueSelected(value)
                                                        expanded = false
                                                }
                                        )
                                }
                        }
                }
        }
}

// @Composable
// internal fun CompactDateTimeInputs(
//    fromDate: String,
//    fromTime: String,
//    toDate: String,
//    toTime: String,
//    onFromDateChange: (String) -> Unit,
//    onFromTimeChange: (String) -> Unit,
//    onToDateChange: (String) -> Unit,
//    onToTimeChange: (String) -> Unit,
//    modifier: Modifier = Modifier
// ) {
//    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp))
// {
//        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment =
// Alignment.CenterVertically) {
//            Text("Van", modifier = Modifier.width(36.dp), color = CrewTextMuted, style =
// MaterialTheme.typography.bodySmall)
//            CompactInputField(value = fromDate, onValueChange = onFromDateChange, label =
// "DD/MM/JJJJ", modifier = Modifier.weight(1f))
//            CompactInputField(value = fromTime, onValueChange = onFromTimeChange, label = "UU:MM",
// modifier = Modifier.weight(0.7f))
//        }
//        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment =
// Alignment.CenterVertically) {
//            Text("Tot", modifier = Modifier.width(36.dp), color = CrewTextMuted, style =
// MaterialTheme.typography.bodySmall)
//            CompactInputField(value = toDate, onValueChange = onToDateChange, label =
// "DD/MM/JJJJ", modifier = Modifier.weight(1f))
//            CompactInputField(value = toTime, onValueChange = onToTimeChange, label = "UU:MM",
// modifier = Modifier.weight(0.7f))
//        }
//    }
// }
//
// @Composable
// internal fun CompactInputField(
//    value: String,
//    onValueChange: (String) -> Unit,
//    label: String,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true
// ) {
//    OutlinedTextField(
//        value = value,
//        onValueChange = onValueChange,
//        modifier = modifier,
//        singleLine = true,
//        enabled = enabled,
//        label = { Text(label) },
//        textStyle = MaterialTheme.typography.bodySmall,
//        colors = OutlinedTextFieldDefaults.colors(
//            focusedBorderColor = CrewAccent,
//            unfocusedBorderColor = CrewBorder
//        )
//    )
// }

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FilterChipRow(
        selected: String,
        values: List<Pair<String, String>>,
        onSelected: (String) -> Unit
) {
        FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                values.forEach { (value, label) ->
                        FilterChip(
                                selected = selected == value,
                                onClick = { onSelected(value) },
                                label = { Text(label) },
                                colors =
                                        FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = CrewAccent,
                                                selectedLabelColor = CrewOnAccent
                                        )
                        )
                }
        }
}

@Composable
internal fun FeedbackCard(title: String, message: String, isError: Boolean = false) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CrewSurface),
                shape = RoundedCornerShape(20.dp)
        ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isError) ErrorRed else CrewTextPrimary
                        )
                        Text(text = message, color = CrewTextSecondary)
                }
        }
}

@Composable
internal fun InfoBanner(text: String, actionLabel: String, onAction: () -> Unit) {
        Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CrewAccent.copy(alpha = 0.18f))
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                        Text(text = text, modifier = Modifier.weight(1f), color = CrewTextPrimary)
                        androidx.compose.material3.Button(onClick = onAction) { Text(actionLabel) }
                }
        }
}

@Composable
internal fun OverviewStatusBadge(status: String) {
        val background =
                when (status.uppercase()) {
                        "READY" -> OrderReady.copy(alpha = 0.14f)
                        "DELIVERED" -> OrderDelivered.copy(alpha = 0.14f)
                        "CANCELLED" -> OrderCancelled.copy(alpha = 0.14f)
                        else -> OrderPending.copy(alpha = 0.14f)
                }
        val textColor =
                when (status.uppercase()) {
                        "READY" -> OrderReady
                        "DELIVERED" -> OrderDelivered
                        "CANCELLED" -> OrderCancelled
                        else -> OrderPending
                }

        Surface(color = background, shape = RoundedCornerShape(999.dp)) {
                Text(
                        text = statusLabel(status),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                )
        }
}

@Composable
internal fun OrderSourceBadge(orderSource: String) {
        val isCustomer = orderSource.equals("CUSTOMER", ignoreCase = true)
        val color = if (!isCustomer) CrewPrimaryDark else CustomerOrderColor
        val icon = if (!isCustomer) Icons.Default.Badge else Icons.Default.PhoneAndroid
        val label = if (!isCustomer) "Medewerker" else "Klant"

        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier =
                        Modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                                .border(1.dp, color.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        fontWeight = FontWeight.SemiBold
                )
        }
}

@Composable
internal fun PaymentStateBadge(isPaid: Boolean) {
        val background = if (isPaid) PaidBackgroundColor else UnpaidBackgroundColor
        val textColor = if (isPaid) PaidTextColor else UnpaidTextColor
        val icon = if (isPaid) Icons.Default.CheckCircle else Icons.Default.Warning
        val label = if (isPaid) "Betaald" else "Niet betaald"

        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier =
                        Modifier.background(background, RoundedCornerShape(999.dp))
                                .border(
                                        1.dp,
                                        textColor.copy(alpha = 0.18f),
                                        RoundedCornerShape(999.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
                Icon(
                        icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                )
                Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                )
        }
}
