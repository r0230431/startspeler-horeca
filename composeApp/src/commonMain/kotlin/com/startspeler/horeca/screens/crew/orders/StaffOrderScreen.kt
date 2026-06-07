package com.startspeler.horeca.screens.crew.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TableRestaurant
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
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.api.CatalogApi
import com.startspeler.horeca.data.api.CustomerApi
import com.startspeler.horeca.data.api.OrdersApi
import com.startspeler.horeca.data.api.TableApi
import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.customers.CustomerResponse
import com.startspeler.horeca.data.models.orders.CreateOrderRequest
import com.startspeler.horeca.data.models.orders.OrderLineRequest
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.data.models.tables.TableResponse
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewBackground
import com.startspeler.horeca.ui.theme.crew.CrewBorder
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextMuted
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary
import com.startspeler.horeca.ui.theme.crew.ErrorRed
import com.startspeler.horeca.ui.theme.crew.StockAvailable
import com.startspeler.horeca.ui.theme.crew.StockLow
import com.startspeler.horeca.ui.theme.crew.StockOut
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StaffOrderScreen(
    onOpenDashboard: () -> Unit = {}
) {
    val customerApi = remember { CustomerApi() }
    val tableApi = remember { TableApi() }
    val catalogApi = remember { CatalogApi() }
    val ordersApi = remember { OrdersApi() }
    val scope = rememberCoroutineScope()
    // Custom toaster message state
    data class ToasterMessage(val message: String, val color: Color)
    var toasterMessage by remember { mutableStateOf<ToasterMessage?>(null) }

    var currentStep by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    var customers by remember { mutableStateOf<List<CustomerResponse>>(emptyList()) }
    var tables by remember { mutableStateOf<List<TableResponse>>(emptyList()) }
    var categories by remember { mutableStateOf<List<CategoryResponse>>(emptyList()) }
    var products by remember { mutableStateOf<List<ProductResponse>>(emptyList()) }

    var customerQuery by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<CustomerResponse?>(null) }
    var orderedByName by remember { mutableStateOf("") }
    var selectedTable by remember { mutableStateOf<TableResponse?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var productQuery by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val cart = remember { mutableStateListOf<CartItemUi>() }

    var customerError by remember { mutableStateOf<String?>(null) }
    var tableError by remember { mutableStateOf<String?>(null) }
    var cartError by remember { mutableStateOf<String?>(null) }
    var showProductError by remember { mutableStateOf(false) }

    var showCreateCustomerDialog by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerError by remember { mutableStateOf<String?>(null) }

    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                customers = customerApi.getCustomers().sortedBy { it.username.lowercase() }
                tables = tableApi.getTables().sortedBy { it.tableNumber }
                categories = catalogApi.getCategories()
                    .filter { it.isActive }
                    .sortedBy { it.displayOrder }
                products = catalogApi.getProducts()
                    .filter { it.isActive }
                    .sortedBy { it.name.lowercase() }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Fout bij laden van data."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val filteredCustomers = remember(customers, customerQuery) {
        customers.filter {
            customerQuery.isBlank() || it.username.contains(customerQuery, ignoreCase = true)
        }
    }

    val filteredProducts = remember(products, productQuery, selectedCategoryId) {
        products.filter { product ->
            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val matchesQuery = productQuery.isBlank() ||
                product.name.contains(productQuery, ignoreCase = true) ||
                (product.description?.contains(productQuery, ignoreCase = true) == true)
            matchesCategory && matchesQuery
        }
    }

    fun currentQuantity(productId: Int): Int = cart.firstOrNull { it.product.id == productId }?.quantity ?: 0

    fun showToast(message: String, color: Color = Color(0xFF2E7D32)) {
        toasterMessage = ToasterMessage(message, color)
        scope.launch {
            delay(2000) // SnackbarDuration.Short ~2s
            if (toasterMessage?.message == message) {
                toasterMessage = null
            }
        }
    }

    fun addToCart(product: ProductResponse) {
        if (product.stock <= 0) return

        val index = cart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val current = cart[index]
            if (current.quantity < product.stock) {
                cart[index] = current.copy(quantity = current.quantity + 1)
                showToast("${product.name} toegevoegd")
            }
        } else {
            cart.add(CartItemUi(product = product, quantity = 1))
            showToast("${product.name} toegevoegd")
        }
        cartError = null
        showProductError = false
    }

    fun changeQuantity(productId: Int, newQuantity: Int) {
        val index = cart.indexOfFirst { it.product.id == productId }
        if (index == -1) return

        val current = cart[index]
        when {
            newQuantity <= 0 -> {
                cart.removeAt(index)
                showToast("${current.product.name} verwijderd", color = Color.Red)
            }
            else -> {
                val cappedQuantity = newQuantity.coerceAtMost(current.product.stock.coerceAtLeast(1))
                val oldQuantity = current.quantity
                cart[index] = current.copy(quantity = cappedQuantity)
                if (cappedQuantity < oldQuantity) {
                    showToast("${current.product.name} verwijderd", color = Color.Red)
                } else if (cappedQuantity > oldQuantity) {
                    showToast("${current.product.name} toegevoegd")
                }
            }
        }
    }

    fun resetFormAfterSubmit() {
        currentStep = 1
        customerQuery = ""
        selectedCustomer = null
        orderedByName = ""
        selectedTable = null
        selectedCategoryId = null
        productQuery = ""
        note = ""
        cart.clear()
        customerError = null
        tableError = null
        cartError = null
    }

    if (showCreateCustomerDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateCustomerDialog = false
                newCustomerName = ""
                newCustomerError = null
            },
            title = { Text("Nieuwe klant toevoegen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCustomerName,
                        onValueChange = {
                            newCustomerName = it
                            newCustomerError = null
                        },
                        label = { Text("Naam") },
                        singleLine = true,
                        isError = newCustomerError != null,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth()
                    )
                    newCustomerError?.let {
                        Text(it, color = ErrorRed)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newCustomerName.trim()
                        if (trimmed.isBlank()) {
                            newCustomerError = "Naam is verplicht."
                            return@Button
                        }

                        scope.launch {
                            try {
                                val created = customerApi.createCustomer(trimmed)
                                customers = (customers + created).sortedBy { it.username.lowercase() }
                                selectedCustomer = created
                                orderedByName = created.username
                                customerError = null
                                showCreateCustomerDialog = false
                                newCustomerName = ""
                                newCustomerError = null
                            } catch (e: Exception) {
                                newCustomerError = e.message ?: "Klant kon niet worden aangemaakt."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrewAccent, contentColor = CrewOnAccent)
                ) {
                    Text("Opslaan")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showCreateCustomerDialog = false
                        newCustomerName = ""
                        newCustomerError = null
                    }
                ) {
                    Text("Annuleren")
                }
            }
        )
    }

    submitError?.let { message ->
        AlertDialog(
            onDismissRequest = { submitError = null },
            title = { Text("Bestelling kon niet worden opgeslagen") },
            text = { Text(message) },
            confirmButton = { Button(onClick = { submitError = null }) { Text("OK") } }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onOpenDashboard()
            },
            title = { Text("Bestelling geplaatst") },
            text = { Text("De bestelling werd succesvol aangemaakt.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onOpenDashboard()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrewAccent, contentColor = CrewOnAccent)
                ) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp,
                    bottom = 24.dp
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Bestelling opnemen",
                style = MaterialTheme.typography.headlineMedium,
                color = CrewTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Voeg klant toe, selecteer tafel, kies producten, controleer winkelmandje en bevestig.",
                color = CrewTextSecondary
            )

            OrderStepIndicator(currentStep = currentStep)

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = CrewSurface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Data kon niet worden geladen.", color = ErrorRed, fontWeight = FontWeight.SemiBold)
                            Text(errorMessage!!, color = CrewTextSecondary)
                            Button(onClick = { loadData() }) {
                                Text("Opnieuw proberen")
                            }
                        }
                    }
                }

                else -> {
                    when (currentStep) {
                        1 -> StaffOrderCustomerStep(
                            customerQuery = customerQuery,
                            onCustomerQueryChange = { customerQuery = it },
                            customerError = customerError,
                            selectedCustomer = selectedCustomer,
                            filteredCustomers = filteredCustomers,
                            onSelectCustomer = { customer ->
                                selectedCustomer = customer
                                orderedByName = customer.username
                                customerError = null
                            },
                            onShowCreateCustomer = { showCreateCustomerDialog = true },
                            onNext = {
                                if (orderedByName.trim().isBlank()) {
                                    customerError = "Naam op bestelling is verplicht."
                                } else {
                                    currentStep = 2
                                }
                            }
                        )

                        2 -> StaffOrderTableStep(
                            tableError = tableError,
                            selectedTable = selectedTable,
                            tables = tables,
                            onSelectTable = { table ->
                                val isSelected = selectedTable?.id == table.id
                                selectedTable = if (isSelected) null else table
                                tableError = null
                            },
                            onBack = { currentStep = 1 },
                            onNext = {
                                if (selectedTable == null) {
                                    tableError = "Selecteer eerst een tafel."
                                } else {
                                    currentStep = 3
                                }
                            }
                        )

                        3 -> StaffOrderProductStep(
                            productQuery = productQuery,
                            onProductQueryChange = { productQuery = it },
                            onClearProductQuery = { productQuery = "" },
                            selectedCategoryId = selectedCategoryId,
                            categories = categories,
                            onSelectCategory = { selectedCategoryId = it },
                            filteredProducts = filteredProducts,
                            currentQuantity = { productId -> currentQuantity(productId) },
                            onAddToCart = { product -> addToCart(product) },
                            onRemoveFromCart = { product ->
                                val quantity = currentQuantity(product.id)
                                if (quantity > 0) {
                                    changeQuantity(product.id, quantity - 1)
                                }
                            },
                            cartQuantity = cart.sumOf { it.quantity },
                            onBack = { currentStep = 2 },
                            onNext = {
                                if (cart.isEmpty()) {
                                    showProductError = true
                                    scope.launch {
                                        delay(5000)
                                        showProductError = false
                                    }
                                } else {
                                    showProductError = false
                                    currentStep = 4
                                }
                            },
                            showProductError = showProductError
                        )

                        else -> StaffOrderCartStep(
                            cartError = cartError,
                            orderedByName = orderedByName,
                            selectedTable = selectedTable,
                            cart = cart,
                            note = note,
                            onNoteChange = { note = it },
                            total = cart.sumOf { it.quantity * (it.product.price.toDoubleOrNull() ?: 0.0) },
                            onDecrease = { item -> changeQuantity(item.product.id, item.quantity - 1) },
                            onIncrease = { item -> changeQuantity(item.product.id, item.quantity + 1) },
                            onDelete = { item -> changeQuantity(item.product.id, 0) },
                            onBack = { currentStep = 3 },
                            isSubmitting = isSubmitting,
                            onSubmit = {
                                if (orderedByName.trim().isBlank()) {
                                    currentStep = 1
                                    customerError = "Naam op bestelling is verplicht."
                                    return@StaffOrderCartStep
                                }
                                if (selectedTable == null) {
                                    currentStep = 2
                                    tableError = "Selecteer eerst een tafel."
                                    return@StaffOrderCartStep
                                }
                                if (cart.isEmpty()) {
                                    cartError = "Voeg minstens één product toe."
                                    return@StaffOrderCartStep
                                }

                                scope.launch {
                                    isSubmitting = true
                                    submitError = null

                                    val result = ordersApi.createOrder(
                                        CreateOrderRequest(
                                            customerId = selectedCustomer?.id,
                                            orderedByName = orderedByName.trim(),
                                            tableId = selectedTable!!.id,
                                            note = note.trim().ifBlank { null },
                                            orderSource = "STAFF",
                                            lines = cart.map {
                                                OrderLineRequest(productId = it.product.id, quantity = it.quantity)
                                            }
                                        )
                                    )

                                    when (result) {
                                        is ApiResult.Success -> {
                                            resetFormAfterSubmit()
                                            loadData()
                                            showSuccessDialog = true
                                        }
                                        is ApiResult.Error -> {
                                            submitError = result.message
                                        }
                                    }

                                    isSubmitting = false
                                }
                            }
                        )
                    }
                }
            }
        }
        if (toasterMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 12.dp, end = 20.dp)
            ) {
                Snackbar(
                    containerColor = toasterMessage!!.color,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    action = {},
                ) {
                    Text(toasterMessage!!.message)
                }
            }
        }
    }
}

@Composable
private fun OrderStepIndicator(currentStep: Int) {
    val steps = listOf("Klant", "Tafel", "Producten", "Winkelmandje")

    Surface(
        color = CrewSurface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, label ->
                val stepNumber = index + 1
                val active = stepNumber == currentStep
                val completed = stepNumber < currentStep

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = when {
                            active -> CrewAccent
                            completed -> CrewPrimary
                            else -> CrewBackground
                        },
                        border = if (active || completed) null else BorderStroke(1.dp, CrewBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = stepNumber.toString(),
                                color = if (active) CrewOnAccent else if (completed) MaterialTheme.colorScheme.onPrimary else CrewTextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(label, color = if (active) CrewTextPrimary else CrewTextMuted)
                }
            }
        }


    }
}

@Composable
internal fun TableTile(
    table: TableResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CrewAccent else CrewSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) CrewAccent else CrewBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.TableRestaurant,
                contentDescription = null,
                tint = if (isSelected) Color.Black else CrewAccent,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tafel ${table.tableNumber}",
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else CrewTextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${table.seatCount} zitplaatsen",
                color = if (isSelected) Color.Black.copy(alpha = 0.75f) else CrewTextSecondary,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            table.note?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    color = if (isSelected) Color.Black.copy(alpha = 0.6f) else CrewTextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
internal fun ProductTile(
    product: ProductResponse,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val stockStateColor = when {
        product.stock <= 0 -> StockOut
        product.stock < 5 -> StockLow
        else -> StockAvailable
    }

    val stockLabel = when {
        product.stock <= 0 -> "SOLD OUT"
        product.stock < 5 -> "BIJNA UITVERKOCHT"
        else -> "${product.stock} in voorraad"
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        border = BorderStroke(1.dp, if (quantity > 0) CrewAccent else CrewBorder),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, color = CrewTextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                product.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = CrewTextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                }
                Text(formatPrice(product.price), color = CrewPrimary, fontWeight = FontWeight.SemiBold)
                Text(stockLabel, color = stockStateColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRemove,
                    enabled = quantity > 0 && product.stock > 0,
                    modifier = Modifier.background(CrewBackground, CircleShape)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(quantity.toString(), fontWeight = FontWeight.Bold, color = CrewTextPrimary)
                    if (product.stock > 0 && quantity >= product.stock) {
                        Text(
                            "Max bereikt",
                            color = StockOut,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(
                    onClick = onAdd,
                    enabled = product.stock > 0 && quantity < product.stock,
                    modifier = Modifier.background(CrewAccent, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CrewPrimary)
                }
            }
        }
    }
}

@Composable
internal fun InfoBadge(
    title: String,
    value: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = CrewAccent, modifier = Modifier.size(22.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodySmall, color = CrewTextMuted)
            Text(value, fontWeight = FontWeight.Bold, color = CrewTextPrimary)
        }
    }
}

@Composable
internal fun CartRow(
    item: CartItemUi,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onDelete: () -> Unit
) {
    val subtotal = (item.product.price.toDoubleOrNull() ?: 0.0) * item.quantity

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = CrewSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: product name + unit price
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatPrice(item.product.price),
                    color = CrewTextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }



            // Center: − qty +
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onDecrease,
                    enabled = item.quantity > 0,
                    modifier = Modifier.size(24.dp).background(CrewBackground, CircleShape)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                }
                Spacer(Modifier.width(28.dp))
                Text(item.quantity.toString(), fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(28.dp))
                IconButton(
                    onClick = onIncrease,
                    enabled = item.quantity < item.product.stock,
                    modifier = Modifier.size(24.dp).background(CrewAccent, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CrewPrimary)
                }
            }

            // Right: trash + subtotal
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, Modifier.padding(end = 10.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed)
                }
                Text(formatEuro(subtotal), fontWeight = FontWeight.Bold, color = CrewPrimary)
            }
        }
    }
}

internal fun formatPrice(rawPrice: String): String {
    val value = rawPrice.toDoubleOrNull() ?: return rawPrice
    return formatEuro(value)
}

internal fun formatEuro(value: Double): String {
    val rounded = (value * 100).roundToInt() / 100.0
    val euros = rounded.toInt()
    val cents = ((rounded - euros) * 100).roundToInt()
    return "€$euros,${cents.toString().padStart(2, '0')}"
}


