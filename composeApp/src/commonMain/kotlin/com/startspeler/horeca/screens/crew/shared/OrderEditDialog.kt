package com.startspeler.horeca.screens.crew.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.api.OrdersApi
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.orders.OrderLineRequest
import com.startspeler.horeca.data.models.orders.OrderResponse
import com.startspeler.horeca.data.models.orders.UpdateOrderRequest
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.screens.crew.overviews.EditableOrderLine
import com.startspeler.horeca.screens.crew.overviews.OrderSourceBadge
import com.startspeler.horeca.screens.crew.overviews.OverviewStatusBadge
import com.startspeler.horeca.screens.crew.overviews.PaymentStateBadge
import com.startspeler.horeca.screens.crew.overviews.toMoneyString
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewBackground
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewPrimaryDark
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextMuted
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary
import com.startspeler.horeca.ui.theme.crew.ErrorRed
import com.startspeler.horeca.ui.theme.crew.OrderDelivered
import com.startspeler.horeca.ui.theme.crew.OrderReady
import kotlinx.coroutines.launch

@Composable
internal fun OrderEditDialog(
    order: OrderResponse,
    products: List<ProductResponse>,
    tableNumber: Int?,
    onDismiss: () -> Unit,
    onOrderUpdated: () -> Unit
) {
    val ordersApi = remember { OrdersApi() }
    val scope = rememberCoroutineScope()
    val canEdit = order.paymentId == null &&
            !order.status.equals("DELIVERED", ignoreCase = true) &&
            !order.status.equals("CANCELLED", ignoreCase = true)

    var draftNote by remember(order.id) { mutableStateOf(order.note.orEmpty()) }
    var draftLines by remember(order.id) {
        mutableStateOf(order.lines.map {
            EditableOrderLine(
                productId = it.productId,
                productName = it.productNameSnapshot,
                unitPrice = it.unitPriceSnapshot ?: 0.0,
                quantity = it.quantity
            )
        })
    }
    var pendingStatus by remember(order.id) { mutableStateOf<String?>(null) }
    var addProductDialogOpen by remember(order.id) { mutableStateOf(false) }
    var validationMessage by remember(order.id) { mutableStateOf<String?>(null) }
    var isSaving by remember(order.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        confirmButton = {
            if (canEdit) {
                Button(
                    onClick = {
                        if (draftLines.isEmpty()) {
                            validationMessage = "Een bestelling moet minstens één product bevatten."
                            return@Button
                        }
                        if (draftLines.any { it.productId == null }) {
                            validationMessage =
                                "Eén of meerdere producten kunnen niet meer gewijzigd worden omdat de productlink ontbreekt."
                            return@Button
                        }
                        validationMessage = null
                        isSaving = true
                        scope.launch {
                            val updateResult = ordersApi.updateOrder(
                                orderId = order.id,
                                request = UpdateOrderRequest(
                                    customerId = order.customerId,
                                    orderedByName = order.orderedByName,
                                    tableId = order.tableId,
                                    note = draftNote.trim().ifBlank { null },
                                    orderSource = order.orderSource,
                                    lines = draftLines.map {
                                        OrderLineRequest(
                                            productId = it.productId!!,
                                            quantity = it.quantity
                                        )
                                    }
                                )
                            )

                            when (updateResult) {
                                is ApiResult.Error -> validationMessage = updateResult.message
                                is ApiResult.Success -> {
                                    if (pendingStatus != null && !pendingStatus.equals(
                                            order.status,
                                            ignoreCase = true
                                        )
                                    ) {
                                        when (val statusResult =
                                            ordersApi.updateOrderStatus(order.id, pendingStatus!!)) {
                                            is ApiResult.Error -> validationMessage = statusResult.message
                                            is ApiResult.Success -> onOrderUpdated()
                                        }
                                    } else {
                                        onOrderUpdated()
                                    }
                                }
                            }
                            isSaving = false
                        }
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = CrewAccent, contentColor = CrewOnAccent)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = CrewOnAccent
                        )
                    } else {
                        Text("Opslaan")
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isSaving,
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
            ) {
                Text(if (canEdit) "Annuleren" else "Sluiten")
            }
        },
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Bestelling #${order.id}")
                Text(
                    text = "${order.orderedByName} • tafel ${tableNumber ?: order.tableId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CrewTextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OverviewStatusBadge(order.status)
                    OrderSourceBadge(orderSource = order.orderSource)
                    PaymentStateBadge(isPaid = order.paymentId != null)
                }

                Text(
                    text = "Besteld op ${order.createdAt.replace('T', ' ')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrewTextMuted
                )

                if (validationMessage != null) {
                    Text(validationMessage!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                }

                draftLines.forEachIndexed { index, line ->
                    EditableOrderLineCard(
                        line = line,
                        canEdit = canEdit,
                        onDecrease = {
                            val current = draftLines[index]
                            draftLines = draftLines.toMutableList().also { list ->
                                if (current.quantity <= 1) list.removeAt(index)
                                else list[index] = current.copy(quantity = current.quantity - 1)
                            }
                        },
                        onIncrease = {
                            val current = draftLines[index]
                            draftLines = draftLines.toMutableList().also { list ->
                                list[index] = current.copy(quantity = current.quantity + 1)
                            }
                        },
                        onDelete = {
                            draftLines = draftLines.toMutableList().also { it.removeAt(index) }
                        }
                    )
                }

                if (canEdit) {
                    Button(
                        onClick = { addProductDialogOpen = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrewAccent, contentColor = CrewOnAccent)
                    ) {
                        Text("Product toevoegen")
                    }
                }

                OutlinedTextField(
                    value = draftNote,
                    onValueChange = { draftNote = it },
                    enabled = canEdit,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Opmerking") },
                    minLines = 3
                )

                if (canEdit) {
                    Text(
                        "Bestelstatus aanpassen bij opslaan",
                        style = MaterialTheme.typography.labelLarge,
                        color = CrewTextPrimary
                    )
                    StatusActionRow(
                        currentStatus = order.status,
                        pendingStatus = pendingStatus,
                        onPendingStatusChange = { pendingStatus = it }
                    )
                }
            }
        }
    )

    if (addProductDialogOpen) {
        AddProductDialog(
            products = products,
            onDismiss = { addProductDialogOpen = false },
            onProductSelected = { product ->
                val index = draftLines.indexOfFirst { it.productId == product.id }
                val parsedPrice = product.price.toDoubleOrNull() ?: 0.0
                draftLines = draftLines.toMutableList().also { lines ->
                    if (index >= 0) {
                        val current = lines[index]
                        lines[index] = current.copy(quantity = current.quantity + 1)
                    } else {
                        lines += EditableOrderLine(
                            productId = product.id,
                            productName = product.name,
                            unitPrice = parsedPrice,
                            quantity = 1
                        )
                    }
                }
                addProductDialogOpen = false
            }
        )
    }
}

@Composable
private fun EditableOrderLineCard(
    line: EditableOrderLine,
    canEdit: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = CrewSurface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(line.productName, fontWeight = FontWeight.Bold, color = CrewTextPrimary)
                    Text(
                        text = "€ ${line.unitPrice.toMoneyString()} per stuk",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrewTextSecondary
                    )
                }
                Text(
                    text = "€ ${line.lineTotal.toMoneyString()}",
                    color = CrewPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (canEdit) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RoundQuantityButton(
                            icon = Icons.Default.Remove,
                            onClick = onDecrease,
                            background = CrewBackground,
                            tint = CrewPrimaryDark
                        )
                        Text(
                            text = line.quantity.toString(),
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.Bold,
                            color = CrewTextPrimary
                        )
                        RoundQuantityButton(
                            icon = Icons.Default.Add,
                            onClick = onIncrease,
                            background = CrewAccent,
                            tint = CrewPrimaryDark
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Verwijderen", tint = ErrorRed)
                    }
                }
            } else {
                Text("Aantal: ${line.quantity}", color = CrewTextSecondary)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusActionRow(
    currentStatus: String,
    pendingStatus: String?,
    onPendingStatusChange: (String?) -> Unit
) {
    val normalizedStatus = currentStatus.uppercase()
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { onPendingStatusChange(null) }) {
            Text(if (pendingStatus == null) "Geen wijziging" else "Geen wijziging")
        }
        if (normalizedStatus == "IN_PROGRESS") {
            StatusActionButton(
                label = "Staat klaar",
                color = OrderReady,
                selected = pendingStatus == "READY",
                onClick = { onPendingStatusChange(if (pendingStatus == "READY") null else "READY") }
            )
            StatusActionButton(
                label = "Cancel bestelling",
                color = ErrorRed,
                selected = pendingStatus == "CANCELLED",
                onClick = { onPendingStatusChange(if (pendingStatus == "CANCELLED") null else "CANCELLED") }
            )
        } else if (normalizedStatus == "READY") {
            StatusActionButton(
                label = "Werd afgeleverd",
                color = OrderDelivered,
                selected = pendingStatus == "DELIVERED",
                onClick = { onPendingStatusChange(if (pendingStatus == "DELIVERED") null else "DELIVERED") }
            )
            StatusActionButton(
                label = "Cancel bestelling",
                color = ErrorRed,
                selected = pendingStatus == "CANCELLED",
                onClick = { onPendingStatusChange(if (pendingStatus == "CANCELLED") null else "CANCELLED") }
            )
        }
    }
}

@Composable
private fun RoundQuantityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    background: Color,
    tint: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(28.dp).background(background, CircleShape)
    ) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun StatusActionButton(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) color else color.copy(alpha = 0.16f),
            contentColor = if (selected) Color.White else color
        )
    ) {
        Text(label)
    }
}

@Composable
private fun AddProductDialog(
    products: List<ProductResponse>,
    onDismiss: () -> Unit,
    onProductSelected: (ProductResponse) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = remember(products, searchQuery) {
        products
            .filter { it.isActive && it.stock > 0 }
            .filter {
                searchQuery.isBlank() ||
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        (it.description?.contains(searchQuery, ignoreCase = true) == true)
            }
            .sortedBy { it.name }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Sluiten")
            }
        },
        title = { Text("Product toevoegen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Zoek product") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                if (filteredProducts.isEmpty()) {
                    Text("Geen producten gevonden.", color = CrewTextSecondary)
                } else {
                    Column(
                        modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredProducts.forEach { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onProductSelected(product) },
                                colors = CardDefaults.cardColors(containerColor = CrewBackground)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(product.name, fontWeight = FontWeight.SemiBold)
                                    product.description?.takeIf { it.isNotBlank() }?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CrewTextSecondary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "€ ${product.price.toMoneyString()} • voorraad ${product.stock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CrewTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
