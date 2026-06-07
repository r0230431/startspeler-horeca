package com.startspeler.horeca.screens.crew.management.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.products.CreateProductRequest
import com.startspeler.horeca.data.models.products.ProductImageUploadResponse
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.data.models.products.UpdateProductRequest
import com.startspeler.horeca.ui.components.ScreenHeader
import com.startspeler.horeca.ui.shared.SimpleFilterDropdown
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewBackground
import com.startspeler.horeca.ui.theme.crew.CrewBorder
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary
import com.startspeler.horeca.util.PickedImageFile
import com.startspeler.horeca.util.rememberImagePicker
import com.startspeler.horeca.util.resolveApiUrl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import kotlin.math.round

private enum class ProductStatusFilter { ALL, ACTIVE, INACTIVE }
private enum class ProductStockFilter { ALL, AVAILABLE, SOLD_OUT }

@Composable
fun ProductsScreen(
    onBackToOverview: (() -> Unit)? = null,
) {
    val viewModel = remember { ProductViewModel() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var statusFilter by remember { mutableStateOf(ProductStatusFilter.ALL) }
    var stockFilter by remember { mutableStateOf(ProductStockFilter.ALL) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductResponse?>(null) }
    var selectedProduct by remember { mutableStateOf<ProductResponse?>(null) }
    var productToDelete by remember { mutableStateOf<ProductResponse?>(null) }
    var feedbackDialogMessage by remember { mutableStateOf<String?>(null) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    val filteredProducts = remember(
        viewModel.products,
        searchQuery,
        selectedCategoryId,
        statusFilter,
        stockFilter,
    ) {
        viewModel.products.filter { product ->
            val matchesSearch = searchQuery.isBlank() ||
                product.name.contains(searchQuery.trim(), ignoreCase = true)

            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId

            val matchesStatus = when (statusFilter) {
                ProductStatusFilter.ALL -> true
                ProductStatusFilter.ACTIVE -> product.isActive
                ProductStatusFilter.INACTIVE -> !product.isActive
            }

            val matchesStock = when (stockFilter) {
                ProductStockFilter.ALL -> true
                ProductStockFilter.AVAILABLE -> product.stock > 0
                ProductStockFilter.SOLD_OUT -> product.stock <= 0
            }

            matchesSearch && matchesCategory && matchesStatus && matchesStock
        }
    }

    val hasActiveFilters =
        searchQuery.isNotBlank() ||
                selectedCategoryId != null ||
                statusFilter != ProductStatusFilter.ALL ||
                stockFilter != ProductStockFilter.ALL

    fun resetFilters() {
        searchQuery = ""
        selectedCategoryId = null
        statusFilter = ProductStatusFilter.ALL
        stockFilter = ProductStockFilter.ALL
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewBackground)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = 24.dp,
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (onBackToOverview != null) {
                item {
                    TextButton(onClick = onBackToOverview) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = CrewPrimary,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Terug naar beheer",
                            color = CrewPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            item {
                ScreenHeader(
                    title = "Producten",
                    hasActiveFilters = hasActiveFilters,
                    onResetFilters = { resetFilters() },
                    onRefresh = { scope.launch { viewModel.loadData() } }
                )
            }

            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    if (maxWidth < 700.dp) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProductSearchField(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it }
                            )
                            ActionRow(
                                onRefresh = { scope.launch { viewModel.loadData() } },
                                onCreate = { showCreateDialog = true }
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductSearchField(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.weight(1f)
                            )
                            ActionRow(
                                onRefresh = { scope.launch { viewModel.loadData() } },
                                onCreate = { showCreateDialog = true }
                            )
                        }
                    }
                }
            }

            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val wideLayout = maxWidth >= 900.dp

                    if (wideLayout) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SimpleFilterDropdown(
                                items = viewModel.categories,
                                selectedItem = viewModel.categories.firstOrNull { it.id == selectedCategoryId },
                                onItemSelected = { selectedCategoryId = it?.id },
                                itemLabel = { it.name },
                                allLabel = "Alle categorieën",
                                modifier = Modifier.weight(1f)
                            )

                            SimpleFilterDropdown(
                                items = ProductStatusFilter.entries,
                                selectedItem = statusFilter,
                                onItemSelected = { statusFilter = it ?: ProductStatusFilter.ALL },
                                itemLabel = {
                                    when (it) {
                                        ProductStatusFilter.ALL -> "Alle statussen"
                                        ProductStatusFilter.ACTIVE -> "Actief"
                                        ProductStatusFilter.INACTIVE -> "Inactief"
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )

                            SimpleFilterDropdown(
                                items = ProductStockFilter.entries,
                                selectedItem = stockFilter,
                                onItemSelected = { stockFilter = it ?: ProductStockFilter.ALL },
                                itemLabel = {
                                    when (it) {
                                        ProductStockFilter.ALL -> "Alle voorraad"
                                        ProductStockFilter.AVAILABLE -> "Beschikbaar"
                                        ProductStockFilter.SOLD_OUT -> "Uitverkocht"
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SimpleFilterDropdown(
                                items = viewModel.categories,
                                selectedItem = viewModel.categories.firstOrNull { it.id == selectedCategoryId },
                                onItemSelected = { selectedCategoryId = it?.id },
                                itemLabel = { it.name },
                                allLabel = "Alle categorieën",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                SimpleFilterDropdown(
                                    items = ProductStatusFilter.entries,
                                    selectedItem = statusFilter,
                                    onItemSelected = { statusFilter = it ?: ProductStatusFilter.ALL },
                                    itemLabel = {
                                        when (it) {
                                            ProductStatusFilter.ALL -> "Alle statussen"
                                            ProductStatusFilter.ACTIVE -> "Actief"
                                            ProductStatusFilter.INACTIVE -> "Inactief"
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                SimpleFilterDropdown(
                                    items = ProductStockFilter.entries,
                                    selectedItem = stockFilter,
                                    onItemSelected = { stockFilter = it ?: ProductStockFilter.ALL },
                                    itemLabel = {
                                        when (it) {
                                            ProductStockFilter.ALL -> "Alle voorraad"
                                            ProductStockFilter.AVAILABLE -> "Beschikbaar"
                                            ProductStockFilter.SOLD_OUT -> "Uitverkocht"
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                when {
                    viewModel.isLoading && viewModel.products.isEmpty() -> {
                        LoadingCard("Producten worden geladen…")
                    }

                    viewModel.errorMessage != null && viewModel.products.isEmpty() -> {
                        ErrorCard(
                            title = "Producten konden niet geladen worden.",
                            message = viewModel.errorMessage.orEmpty(),
                            onRetry = { scope.launch { viewModel.loadData() } }
                        )
                    }

                    filteredProducts.isEmpty() -> {
                        EmptyProductsCard(
                            hasFilters = searchQuery.isNotBlank() ||
                                selectedCategoryId != null ||
                                statusFilter != ProductStatusFilter.ALL ||
                                stockFilter != ProductStockFilter.ALL
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            filteredProducts.forEach { product ->
                                ProductListCard(
                                    product = product,
                                    category = viewModel.categories.firstOrNull { it.id == product.categoryId },
                                    onClick = { selectedProduct = product },
                                    onEdit = { editingProduct = product },
                                    onDelete = { productToDelete = product },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        ProductFormDialog(
            title = "Product aanmaken",
            categories = viewModel.categories,
            product = null,
            onDismiss = { showCreateDialog = false },
            onSubmit = { request ->
                scope.launch {
                    when (val result = viewModel.createProduct(request)) {
                        is ApiResult.Success -> {
                            showCreateDialog = false
                            feedbackDialogMessage = "Product succesvol opgeslagen."
                        }
                        is ApiResult.Error -> {
                            errorDialogMessage = result.message
                        }
                    }
                }
            },
            onUploadImage = { file, onResult ->
                scope.launch {
                    onResult(viewModel.uploadImage(file.name, file.mimeType, file.bytes))
                }
            }
        )
    }

    editingProduct?.let { product ->
        ProductFormDialog(
            title = "Product wijzigen",
            categories = viewModel.categories,
            product = product,
            onDismiss = { editingProduct = null },
            onSubmit = { request ->
                scope.launch {
                    when (val result = viewModel.updateProduct(product.id, request.toUpdateRequest())) {
                        is ApiResult.Success -> {
                            editingProduct = null
                            feedbackDialogMessage = "Product succesvol gewijzigd."
                        }
                        is ApiResult.Error -> {
                            errorDialogMessage = result.message
                        }
                    }
                }
            },
            onUploadImage = { file, onResult ->
                scope.launch {
                    onResult(viewModel.uploadImage(file.name, file.mimeType, file.bytes))
                }
            }
        )
    }

    selectedProduct?.let { product ->
        ProductDetailsDialog(
            product = product,
            category = viewModel.categories.firstOrNull { it.id == product.categoryId },
            onDismiss = { selectedProduct = null },
            onEdit = {
                selectedProduct = null
                editingProduct = product
            },
            onDelete = {
                selectedProduct = null
                productToDelete = product
            }
        )
    }

    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Product verwijderen") },
            text = {
                Text("Wil je '${product.name}' definitief verwijderen? Als dit product al in bestellingen voorkomt, zal verwijderen niet lukken.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            when (val result = viewModel.deleteProduct(product.id)) {
                                is ApiResult.Success -> {
                                    productToDelete = null
                                    feedbackDialogMessage = "Product succesvol verwijderd."
                                }
                                is ApiResult.Error -> {
                                    productToDelete = null
                                    errorDialogMessage = result.message
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Verwijderen")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Annuleren")
                }
            }
        )
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
private fun ProductSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Zoek op productnaam") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        colors = productFormFieldColors(),
    )
}

@Composable
private fun ActionRow(
    onRefresh: () -> Unit,
    onCreate: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onCreate,
            colors = ButtonDefaults.buttonColors(
                containerColor = CrewAccent,
                contentColor = CrewOnAccent,
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Product toevoegen")
        }
    }
}

@Composable
private fun CategoryTabs(
    categories: List<CategoryResponse>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
) {
    val selectedIndex = categories.indexOfFirst { it.id == selectedCategoryId }
        .let { if (it >= 0) it + 1 else 0 }

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        divider = {},
        containerColor = Color.Transparent,
        contentColor = CrewTextPrimary,
    ) {
        Tab(
            selected = selectedCategoryId == null,
            onClick = { onCategorySelected(null) },
            text = { Text("Alles") },
            selectedContentColor = CrewAccent,
            unselectedContentColor = CrewTextSecondary,
        )

        categories.forEach { category ->
            Tab(
                selected = selectedCategoryId == category.id,
                onClick = { onCategorySelected(category.id) },
                text = { Text(category.name) },
                selectedContentColor = CrewAccent,
                unselectedContentColor = CrewTextSecondary,
            )
        }
    }
}

@Composable
private fun ProductListCard(
    product: ProductResponse,
    category: CategoryResponse?,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CrewSurface,
        border = BorderStroke(1.dp, CrewBorder),
        shadowElevation = 2.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImagePreview(
                imageUrl = product.imageUrl,
                category = category,
                productName = product.name,
                modifier = Modifier.size(88.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CrewTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    StatusBadge(
                        label = if (product.isActive) "Actief" else "Inactief",
                        background = if (product.isActive) Color(0xFF22C55E).copy(alpha = 0.15f) else CrewBorder.copy(alpha = 0.45f),
                        content = if (product.isActive) Color(0xFF15803D) else CrewTextSecondary,
                    )

                    stockLabel(product)?.let { label ->
                        StatusBadge(
                            label = label,
                            background = if (product.stock <= 0) Color(0xFFE35D6A).copy(alpha = 0.18f) else Color(0xFFFFB74D).copy(alpha = 0.20f),
                            content = CrewTextPrimary,
                        )
                    }
                }

                Text(
                    text = category?.name ?: "Geen categorie",
                    style = MaterialTheme.typography.bodySmall,
                    color = CrewTextSecondary,
                )

                product.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CrewTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatPrice(product.price),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CrewAccent,
                        )
                        Text(
                            text = "Voorraad ${product.stock} • Minimum ${product.minimumStock}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrewTextSecondary,
                        )
                    }

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Wijzigen", tint = CrewPrimary)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Verwijderen", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductDetailsDialog(
    product: ProductResponse,
    category: CategoryResponse?,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(product.name) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ProductImagePreview(
                    imageUrl = product.imageUrl,
                    category = category,
                    productName = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                ProductDetailsRow("Categorie", category?.name ?: "Geen categorie")
                ProductDetailsRow("Prijs", formatPrice(product.price))
                ProductDetailsRow("Actief", if (product.isActive) "Ja" else "Nee")
                ProductDetailsRow("Huidige voorraad", product.stock.toString())
                ProductDetailsRow("Minimumvoorraad", product.minimumStock.toString())
                ProductDetailsRow("Omschrijving", product.description?.ifBlank { "—" } ?: "—")
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Verwijderen")
                }
                Button(onClick = onEdit) {
                    Text("Wijzigen")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Sluiten")
            }
        }
    )
}

@Composable
private fun ProductDetailsRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = CrewTextSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = CrewTextPrimary,
        )
    }
}

@Composable
private fun ProductFormDialog(
    title: String,
    categories: List<CategoryResponse>,
    product: ProductResponse?,
    onDismiss: () -> Unit,
    onSubmit: (CreateProductRequest) -> Unit,
    onUploadImage: (
        PickedImageFile,
        (ApiResult<ProductImageUploadResponse>) -> Unit
    ) -> Unit,
) {
    var name by remember(product?.id) { mutableStateOf(product?.name.orEmpty()) }
    var description by remember(product?.id) { mutableStateOf(product?.description.orEmpty()) }
    var price by remember(product?.id) { mutableStateOf(product?.price.orEmpty()) }
    var stock by remember(product?.id) { mutableStateOf(product?.stock?.toString().orEmpty()) }
    var minimumStock by remember(product?.id) { mutableStateOf(product?.minimumStock?.toString().orEmpty()) }
    var selectedCategoryId by remember(product?.id) { mutableStateOf(product?.categoryId) }
    var imageUrl by remember(product?.id) { mutableStateOf(product?.imageUrl) }
    var isActive by remember(product?.id) { mutableStateOf(product?.isActive ?: true) }

    var nameError by remember(product?.id) { mutableStateOf<String?>(null) }
    var priceError by remember(product?.id) { mutableStateOf<String?>(null) }
    var stockError by remember(product?.id) { mutableStateOf<String?>(null) }
    var minimumStockError by remember(product?.id) { mutableStateOf<String?>(null) }
    var categoryError by remember(product?.id) { mutableStateOf<String?>(null) }
    var formDialogMessage by remember(product?.id) { mutableStateOf<String?>(null) }
    var uploadStateMessage by remember(product?.id) { mutableStateOf<String?>(null) }
    var isUploadingImage by remember(product?.id) { mutableStateOf(false) }

    fun validateName(value: String): String? = when {
        value.isBlank() -> "Productnaam is verplicht."
        value.trim().length < 2 -> "Productnaam moet minstens 2 tekens bevatten."
        value.trim().length > 150 -> "Productnaam is te lang."
        else -> null
    }

    fun validatePrice(value: String): String? = when {
        value.isBlank() -> "Prijs is verplicht."
        !Regex("""^\d+(?:[.,]\d{1,2})?$""").matches(value.trim()) -> "Gebruik een geldig bedrag, bv. 2,50."
        else -> null
    }

    fun validateInteger(value: String, label: String): String? = when {
        value.isBlank() -> "$label is verplicht."
        value.toIntOrNull() == null -> "$label moet een geheel getal zijn."
        value.toInt() < 0 -> "$label mag niet negatief zijn."
        else -> null
    }

    val imagePicker = rememberImagePicker(
        onImagePicked = { pickedFile ->
            if (pickedFile.bytes.size > 2 * 1024 * 1024) {
                uploadStateMessage = "De afbeelding is te groot. Maximum 2 MB toegestaan."
                return@rememberImagePicker
            }

            if (pickedFile.mimeType !in setOf("image/jpeg", "image/jpg", "image/png", "image/webp")) {
                uploadStateMessage = "Ongeldig bestandstype. Gebruik JPG, JPEG, PNG of WEBP."
                return@rememberImagePicker
            }

            isUploadingImage = true
            onUploadImage(pickedFile) { result ->
                isUploadingImage = false
                when (result) {
                    is ApiResult.Success -> {
                        imageUrl = result.data.imageUrl
                        uploadStateMessage = "Afbeelding succesvol opgeladen."
                    }
                    is ApiResult.Error -> {
                        uploadStateMessage = result.message
                    }
                }
            }
        },
        onError = { message -> uploadStateMessage = message }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProductImagePreview(
                    imageUrl = imageUrl,
                    category = categories.firstOrNull { it.id == selectedCategoryId },
                    productName = name.ifBlank { "Nieuw product" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { imagePicker.launch() },
                        enabled = !isUploadingImage,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (imageUrl.isNullOrBlank()) "Afbeelding kiezen" else "Afbeelding wijzigen")
                    }

                    if (imageUrl != null) {
                        TextButton(onClick = { imageUrl = null }) {
                            Text("Afbeelding verwijderen")
                        }
                    }
                }

                if (isUploadingImage) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("Afbeelding wordt opgeladen…", color = CrewTextSecondary)
                    }
                }

                uploadStateMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message.contains("succesvol", ignoreCase = true)) CrewPrimary else MaterialTheme.colorScheme.error,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = validateName(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Productnaam *") },
                        isError = nameError != null,
                        singleLine = true,
                        colors = outlinedColors(),
                    )
                    FieldError(nameError)
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Omschrijving (optioneel)") },
                    minLines = 2,
                    maxLines = 4,
                    colors = outlinedColors(),
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CategoryDropdownField(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = {
                            selectedCategoryId = it
                            categoryError = null
                        }
                    )
                    FieldError(categoryError)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            price = it
                            priceError = validatePrice(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Prijs *") },
                        isError = priceError != null,
                        singleLine = true,
                        colors = outlinedColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    FieldError(priceError)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = {
                                stock = it.filter { char -> char.isDigit() }
                                stockError = validateInteger(stock, "Huidige voorraad")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Huidige voorraad *") },
                            isError = stockError != null,
                            singleLine = true,
                            colors = outlinedColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        FieldError(stockError)
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = minimumStock,
                            onValueChange = {
                                minimumStock = it.filter { char -> char.isDigit() }
                                minimumStockError = validateInteger(minimumStock, "Minimumvoorraad")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Minimumvoorraad *") },
                            isError = minimumStockError != null,
                            singleLine = true,
                            colors = outlinedColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        FieldError(minimumStockError)
                    }
                }

                if (product != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Actief op de menukaart",
                                style = MaterialTheme.typography.bodyLarge,
                                color = CrewTextPrimary,
                            )
                            Text(
                                text = "Inactieve producten blijven in de database maar verdwijnen uit de menukaart.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrewTextSecondary,
                            )
                        }

                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                } else {
                    Text(
                        text = "Nieuwe producten worden automatisch op actief gezet",
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    nameError = validateName(name)
                    priceError = validatePrice(price)
                    stockError = validateInteger(stock, "Huidige voorraad")
                    minimumStockError = validateInteger(minimumStock, "Minimumvoorraad")
                    categoryError = if (selectedCategoryId == null) "Categorie is verplicht." else null

                    val hasErrors = listOf(nameError, priceError, stockError, minimumStockError, categoryError).any { it != null }
                    if (hasErrors) {
                        formDialogMessage = "Controleer de gemarkeerde velden voordat je opslaat."
                        return@Button
                    }

                    onSubmit(
                        CreateProductRequest(
                            categoryId = selectedCategoryId!!,
                            name = name.trim(),
                            description = description.trim().ifBlank { null },
                            stock = stock.toInt(),
                            minimumStock = minimumStock.toInt(),
                            price = price.trim().replace(',', '.'),
                            imageUrl = imageUrl,
                            isActive = if (product == null) true else isActive,
                        )
                    )
                },
                enabled = !isUploadingImage
            ) {
                Text(if (product == null) "Toevoegen" else "Opslaan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploadingImage) {
                Text("Annuleren")
            }
        }
    )

    formDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { formDialogMessage = null },
            title = { Text("Validatieprobleem") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { formDialogMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun CreateProductRequest.toUpdateRequest(): UpdateProductRequest {
    return UpdateProductRequest(
        categoryId = categoryId,
        name = name,
        description = description,
        stock = stock,
        minimumStock = minimumStock,
        price = price,
        imageUrl = imageUrl,
        isActive = isActive,
    )
}

@Composable
private fun productFormFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CrewAccent,
    unfocusedBorderColor = CrewBorder,
    focusedLabelColor = CrewAccent,
)

@Composable
private fun FieldError(message: String?) {
    if (message != null) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun CategoryDropdownField(
    categories: List<CategoryResponse>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Kies categorie"

    Box {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Categorie *") },
            enabled = false,
            colors = productFormFieldColors()
        )

        Surface(
            modifier = Modifier.matchParentSize(),
            color = Color.Transparent,
            onClick = { expanded = true }
        ) {}

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.name) },
                    onClick = {
                        onCategorySelected(category.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LoadingCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CrewSurface,
        border = BorderStroke(1.dp, CrewBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            Text(message, color = CrewTextSecondary)
        }
    }
}

@Composable
private fun ErrorCard(
    title: String,
    message: String,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CrewSurface,
        border = BorderStroke(1.dp, CrewBorder),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = message,
                color = CrewTextSecondary,
            )
            Button(onClick = onRetry) {
                Text("Opnieuw proberen")
            }
        }
    }
}

@Composable
private fun EmptyProductsCard(
    hasFilters: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CrewSurface,
        border = BorderStroke(1.dp, CrewBorder),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (hasFilters) "Geen producten gevonden." else "Er zijn nog geen producten beschikbaar.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CrewTextPrimary,
            )
            Text(
                text = if (hasFilters) {
                    "Pas je filters of zoekterm aan om opnieuw resultaten te zien."
                } else {
                    "Voeg een eerste product toe om de menukaart op te bouwen."
                },
                color = CrewTextSecondary,
            )
        }
    }
}

@Composable
fun ProductImagePreview(
    imageUrl: String?,
    category: CategoryResponse?,
    productName: String,
    modifier: Modifier = Modifier,
) {
    val resolvedUrl = resolveApiUrl(imageUrl)

    Surface(
        modifier = modifier.defaultMinSize(minHeight = 88.dp),
        shape = RoundedCornerShape(20.dp),
        color = categoryColor(category).copy(alpha = 0.18f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                categoryColor(category).copy(alpha = 0.70f),
                                CrewSurface
                            )
                        )
                    )
            )

            if (resolvedUrl != null) {
                KamelImage(
                    resource = asyncPainterResource(data = resolvedUrl),
                    contentDescription = productName,
                    modifier = Modifier.fillMaxSize(),
                    onLoading = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    },
                    onFailure = {
                        ProductImageFallback(category = category, modifier = Modifier.fillMaxSize())
                    }
                )
            } else {
                ProductImageFallback(category = category, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun ProductImageFallback(
    category: CategoryResponse?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.30f),
            modifier = Modifier.size(54.dp)
        )
    }
}

@Composable
private fun StatusBadge(
    label: String,
    background: Color,
    content: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background,
        contentColor = content
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun stockLabel(product: ProductResponse): String? {
    return when {
        product.stock <= 0 -> "Uitverkocht"
        product.minimumStock > 0 && product.stock <= product.minimumStock -> "Bijna uitverkocht"
        else -> null
    }
}

private fun categoryIcon(category: CategoryResponse?): ImageVector {
    return when (category?.name?.lowercase()) {
        "bier", "bieren" -> Icons.Outlined.LocalBar
        "frisdrank", "frisdranken", "energiedrank", "energiedranken" -> Icons.Outlined.LocalDrink
        "koffie", "warme dranken", "koffie en thee" -> Icons.Outlined.Coffee
        "water" -> Icons.Outlined.WaterDrop
        "snacks" -> Icons.Outlined.Fastfood
        else -> Icons.Default.Inventory2
    }
}

private fun categoryColor(category: CategoryResponse?): Color {
    return when (category?.name?.lowercase()) {
        "bier", "bieren" -> Color(0xFF855E42)
        "frisdrank", "frisdranken", "energiedrank", "energiedranken" -> Color(0xFF295A8A)
        "koffie", "warme dranken", "koffie en thee" -> Color(0xFF6A4B3C)
        "water" -> Color(0xFF3B7EA1)
        "snacks" -> Color(0xFF7A5F2D)
        else -> Color(0xFF3E2F55)
    }
}

private fun formatPrice(price: String): String {
    val normalized = price.replace(',', '.')
    val value = normalized.toDoubleOrNull() ?: return "€ $price"
    val rounded = round(value * 100).toInt()
    val euros = rounded / 100
    val cents = (rounded % 100).toString().padStart(2, '0')
    return "€ $euros,$cents"
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CrewSurface,
    unfocusedContainerColor = CrewSurface,
    disabledContainerColor = CrewSurface,
    focusedBorderColor = CrewPrimary,
    unfocusedBorderColor = CrewBorder,
    disabledBorderColor = CrewBorder.copy(alpha = 0.6f),
    focusedTextColor = CrewTextPrimary,
    unfocusedTextColor = CrewTextPrimary,
    disabledTextColor = CrewTextSecondary,
    focusedLabelColor = CrewPrimary,
    unfocusedLabelColor = CrewTextSecondary,
    disabledLabelColor = CrewTextSecondary,
)