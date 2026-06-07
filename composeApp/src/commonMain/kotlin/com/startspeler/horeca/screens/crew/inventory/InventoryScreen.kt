package com.startspeler.horeca.screens.crew.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.auth.CrewRole
import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.ui.components.ScreenHeader
import com.startspeler.horeca.ui.theme.crew.*
import kotlinx.coroutines.launch

private enum class InventoryStatusFilter(val label: String) {
    ALL("Alle"),
    AVAILABLE("Voldoende voorraad"),
    LOW("Te bestellen"),
    SOLD_OUT("Uitverkocht")
}

private val InventoryAccentYellow = Color(0xFFF4C542)
private val InventoryAccentYellowSoft = Color(0xFFFFF6D8)
private val InventoryAccentYellowBorder = Color(0xFFE6C15A)
private val InventoryAccentOnYellow = Color(0xFF4A2E00)

@Composable
fun InventoryScreen(
    crewRole: CrewRole,
) {
    val viewModel = remember { InventoryViewModel() }
    val scope = rememberCoroutineScope()
    val isAdmin = crewRole == CrewRole.ADMIN

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryId by rememberSaveable { mutableStateOf<Int?>(null) }
    var statusFilter by rememberSaveable { mutableStateOf(InventoryStatusFilter.ALL) }
    var feedbackDialogMessage by remember { mutableStateOf<String?>(null) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    val categoryNameById = remember(viewModel.categories) {
        viewModel.categories.associate { it.id to it.name }
    }

    val filteredProducts = remember(viewModel.products, searchQuery, selectedCategoryId, statusFilter) {
        viewModel.products.filter { product ->
            val matchesSearch = searchQuery.isBlank() ||
                    product.name.contains(searchQuery.trim(), ignoreCase = true)
            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val matchesStatus = when (statusFilter) {
                InventoryStatusFilter.ALL -> true
                InventoryStatusFilter.AVAILABLE -> product.stock > product.minimumStock
                InventoryStatusFilter.LOW -> product.stock in 1..product.minimumStock
                InventoryStatusFilter.SOLD_OUT -> product.stock <= 0
            }
            matchesSearch && matchesCategory && matchesStatus
        }
    }

    val hasActiveFilters =
        searchQuery.isNotBlank() ||
                selectedCategoryId != null ||
                statusFilter != InventoryStatusFilter.ALL

    fun resetFilters() {
        searchQuery = ""
        selectedCategoryId = null
        statusFilter = InventoryStatusFilter.ALL
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewBackground)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = 20.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader(
            title = "Voorraadbeheer",
            hasActiveFilters = hasActiveFilters,
            onResetFilters = { resetFilters() },
            onRefresh = { scope.launch { viewModel.loadData() } }
        )

        InventoryFiltersSection(
            categories = viewModel.categories,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedCategoryId = selectedCategoryId,
            onCategorySelected = { selectedCategoryId = it },
            statusFilter = statusFilter,
            onStatusSelected = { statusFilter = it }
        )

        Text(
            text = if (isAdmin) {
                "Admins kunnen huidige voorraad, minimumvoorraad en leveringen aanpassen. Medewerkers kunnen leveringen toevoegen en de huidige voorraad corrigeren."
            } else {
                "Medewerkers kunnen leveringen toevoegen en de huidige voorraad aanpassen. Minimumvoorraad is enkel instelbaar door admins."
            },
            style = MaterialTheme.typography.bodySmall,
            color = CrewTextSecondary,
        )

        when {
            viewModel.isLoading && viewModel.products.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CrewPrimary)
                }
            }

            viewModel.errorMessage != null && viewModel.products.isEmpty() -> {
                InventoryMessageCard(
                    title = "Voorraad kon niet geladen worden",
                    description = viewModel.errorMessage.orEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            filteredProducts.isEmpty() -> {
                InventoryMessageCard(
                    title = "Geen actieve producten gevonden",
                    description = "Pas je zoekterm of filters aan om producten te tonen.",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        InventoryProductCard(
                            product = product,
                            categoryName = categoryNameById[product.categoryId] ?: "Onbekende categorie",
                            isAdmin = isAdmin,
                            onSaveInventory = { stock, minimumStock ->
                                scope.launch {
                                    when (val result = viewModel.updateInventory(product.id, stock, minimumStock)) {
                                        is ApiResult.Success -> {
                                            feedbackDialogMessage = "Voorraad van ${product.name} werd opgeslagen."
                                        }
                                        is ApiResult.Error -> {
                                            errorDialogMessage = result.message
                                        }
                                    }
                                }
                            },
                            onSaveStock = { stock ->
                                scope.launch {
                                    when (val result = viewModel.updateStock(product.id, stock)) {
                                        is ApiResult.Success -> {
                                            feedbackDialogMessage = "Huidige voorraad van ${product.name} werd bijgewerkt."
                                        }
                                        is ApiResult.Error -> {
                                            errorDialogMessage = result.message
                                        }
                                    }
                                }
                            },
                            onAddDelivery = { quantity ->
                                scope.launch {
                                    when (val result = viewModel.addDelivery(product.id, quantity)) {
                                        is ApiResult.Success -> {
                                            feedbackDialogMessage = "Levering voor ${product.name} werd toegevoegd."
                                        }
                                        is ApiResult.Error -> {
                                            errorDialogMessage = result.message
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    feedbackDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { feedbackDialogMessage = null },
            title = { Text("Gelukt") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { feedbackDialogMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    errorDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorDialogMessage = null },
            title = { Text("Er ging iets mis") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorDialogMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun InventoryFiltersSection(
    categories: List<CategoryResponse>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    statusFilter: InventoryStatusFilter,
    onStatusSelected: (InventoryStatusFilter) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val wideLayout = maxWidth >= 940.dp

        if (wideLayout) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f)
                )
                CategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = onCategorySelected,
                    modifier = Modifier.width(220.dp)
                )
                StatusDropdown(
                    selectedFilter = statusFilter,
                    onStatusSelected = onStatusSelected,
                    modifier = Modifier.width(220.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SearchField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryDropdown(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = onCategorySelected,
                        modifier = Modifier.weight(1f)
                    )
                    StatusDropdown(
                        selectedFilter = statusFilter,
                        onStatusSelected = onStatusSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = 52.dp),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = CrewTextSecondary,
            )
        },
        placeholder = { Text("Zoek product op naam") },
        colors = inventoryFieldColors()
    )
}

@Composable
private fun CategoryDropdown(
    categories: List<CategoryResponse>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedLabel = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Alle categorieën"
    FilterDropdown(
        label = selectedLabel,
        modifier = modifier,
    ) { closeMenu ->
        DropdownMenuItem(
            text = { Text("Alle categorieën") },
            onClick = {
                onCategorySelected(null)
                closeMenu()
            }
        )
        categories.forEach { category ->
            DropdownMenuItem(
                text = { Text(category.name) },
                onClick = {
                    onCategorySelected(category.id)
                    closeMenu()
                }
            )
        }
    }
}

@Composable
private fun StatusDropdown(
    selectedFilter: InventoryStatusFilter,
    onStatusSelected: (InventoryStatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterDropdown(
        label = selectedFilter.label,
        modifier = modifier,
    ) { closeMenu ->
        InventoryStatusFilter.entries.forEach { filter ->
            DropdownMenuItem(
                text = { Text(filter.label) },
                onClick = {
                    onStatusSelected(filter)
                    closeMenu()
                }
            )
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    modifier: Modifier = Modifier,
    menuContent: @Composable ((closeMenu: () -> Unit) -> Unit),
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CrewPrimary.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, CrewPrimary.copy(alpha = 0.18f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CrewTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = "Open filter",
                        tint = CrewPrimary
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            menuContent { expanded = false }
        }
    }
}

@Composable
private fun InventoryProductCard(
    product: ProductResponse,
    categoryName: String,
    isAdmin: Boolean,
    onSaveInventory: (stock: Int, minimumStock: Int) -> Unit,
    onSaveStock: (stock: Int) -> Unit,
    onAddDelivery: (quantity: Int) -> Unit,
) {
    var stockInput by rememberSaveable(product.id, product.stock) { mutableStateOf(product.stock.toString()) }
    var minimumStockInput by rememberSaveable(product.id, product.minimumStock) { mutableStateOf(product.minimumStock.toString()) }
    var deliveryInput by rememberSaveable(product.id) { mutableStateOf("") }

    var stockError by rememberSaveable(product.id, product.stock) { mutableStateOf<String?>(null) }
    var minimumStockError by rememberSaveable(product.id, product.minimumStock) { mutableStateOf<String?>(null) }
    var deliveryError by rememberSaveable(product.id) { mutableStateOf<String?>(null) }
    var validationDialogMessage by remember { mutableStateOf<String?>(null) }

    val status = stockStatus(product.stock, product.minimumStock)
    val statusColor = when (status) {
        InventoryStatus.AVAILABLE -> StockAvailable
        InventoryStatus.LOW -> InventoryAccentYellowBorder
        InventoryStatus.SOLD_OUT -> StockOut
    }
    val statusLabel = when (status) {
        InventoryStatus.AVAILABLE -> "Voldoende voorraad"
        InventoryStatus.LOW -> "Te bestellen"
        InventoryStatus.SOLD_OUT -> "Uitverkocht"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        border = BorderStroke(1.dp, CrewBorder.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CrewTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrewTextSecondary,
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                InventoryNumberField(
                    label = "Huidige",
                    value = stockInput,
                    onValueChange = {
                        stockInput = it.filter(Char::isDigit)
                        stockError = validateNonNegativeInteger(stockInput, "Huidige voorraad")
                    },
                    error = stockError,
                    modifier = Modifier.weight(1f)
                )

                InventoryNumberField(
                    label = "Minimum",
                    value = minimumStockInput,
                    onValueChange = {
                        if (isAdmin) {
                            minimumStockInput = it.filter(Char::isDigit)
                            minimumStockError = validateNonNegativeInteger(minimumStockInput, "Minimumvoorraad")
                        }
                    },
                    error = minimumStockError,
                    modifier = Modifier.weight(1f),
                    enabled = isAdmin,
                    hint = if (!isAdmin) "Enkel admin" else null,
                )

                CompactIconActionButton(
                    icon = Icons.Outlined.Save,
                    contentDescription = if (isAdmin) "Voorraad en minimumvoorraad opslaan" else "Voorraad opslaan",
                    onClick = {
                        stockError = validateNonNegativeInteger(stockInput, "Huidige voorraad")
                        minimumStockError = if (isAdmin) {
                            validateNonNegativeInteger(minimumStockInput, "Minimumvoorraad")
                        } else {
                            null
                        }

                        val hasErrors = listOf(stockError, minimumStockError).any { it != null }
                        if (hasErrors) {
                            validationDialogMessage = "Controleer de voorraadvelden van ${product.name}. Enkel gehele, niet-negatieve waarden zijn toegestaan."
                        } else {
                            val stock = stockInput.toInt()
                            if (isAdmin) {
                                onSaveInventory(stock, minimumStockInput.toInt())
                            } else {
                                onSaveStock(stock)
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 22.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                InventoryNumberField(
                    label = "Levering",
                    value = deliveryInput,
                    onValueChange = {
                        deliveryInput = it.filter(Char::isDigit)
                        deliveryError = validateDelivery(deliveryInput)
                    },
                    error = deliveryError,
                    modifier = Modifier.weight(1f),
                    placeholder = "Aantal"
                )

                CompactIconActionButton(
                    icon = Icons.Outlined.Add,
                    contentDescription = "Levering toevoegen",
                    onClick = {
                        deliveryError = validateDelivery(deliveryInput)
                        if (deliveryError != null) {
                            validationDialogMessage = "Controleer het leveringsaantal van ${product.name}. Geef een geheel, niet-negatief getal groter dan 0 in."
                        } else {
                            onAddDelivery(deliveryInput.toInt())
                            deliveryInput = ""
                            deliveryError = null
                        }
                    },
                    modifier = Modifier.padding(top = 22.dp)
                )
            }
        }
    }

    validationDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { validationDialogMessage = null },
            title = { Text("Validatie") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { validationDialogMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun InventoryNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    error: String? = null,
    hint: String? = null,
    placeholder: String = "0",
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = CrewTextSecondary,
            modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            singleLine = true,
            enabled = enabled,
            isError = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(placeholder) },
            colors = inventoryFieldColors()
        )
        when {
            error != null -> Text(
                text = error,
                color = ErrorRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 2.dp, top = 4.dp)
            )
            hint != null -> Text(
                text = hint,
                color = CrewTextSecondary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 2.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun CompactIconActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = InventoryAccentYellow,
        border = BorderStroke(1.dp, InventoryAccentYellowBorder)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = InventoryAccentOnYellow
            )
        }
    }
}

@Composable
private fun InventoryMessageCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = CrewSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, CrewBorder.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = CrewTextPrimary,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = CrewTextSecondary,
            )
        }
    }
}

private enum class InventoryStatus { AVAILABLE, LOW, SOLD_OUT }

private fun stockStatus(stock: Int, minimumStock: Int): InventoryStatus {
    return when {
        stock <= 0 -> InventoryStatus.SOLD_OUT
        stock <= minimumStock -> InventoryStatus.LOW
        else -> InventoryStatus.AVAILABLE
    }
}

private fun validateNonNegativeInteger(value: String, fieldName: String): String? {
    if (value.isBlank()) return "$fieldName is verplicht."
    val parsed = value.toIntOrNull() ?: return "$fieldName moet een geheel getal zijn."
    if (parsed < 0) return "$fieldName mag niet negatief zijn."
    return null
}

private fun validateDelivery(value: String): String? {
    if (value.isBlank()) return "Levering is verplicht."
    val parsed = value.toIntOrNull() ?: return "Levering moet een geheel getal zijn."
    if (parsed <= 0) return "Levering moet groter zijn dan 0."
    return null
}

@Composable
private fun inventoryFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = InventoryAccentYellowBorder,
    focusedLabelColor = CrewPrimary,
    unfocusedBorderColor = CrewBorder,
    unfocusedLabelColor = CrewTextSecondary,
    focusedTextColor = CrewTextPrimary,
    unfocusedTextColor = CrewTextPrimary,
    disabledBorderColor = CrewBorder.copy(alpha = 0.6f),
    disabledTextColor = CrewTextPrimary,
    disabledLabelColor = CrewTextSecondary,
    cursorColor = CrewPrimary,
)
