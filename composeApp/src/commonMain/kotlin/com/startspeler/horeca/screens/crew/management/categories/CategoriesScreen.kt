package com.startspeler.horeca.screens.crew.management.categories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.categories.CreateCategoryRequest
import com.startspeler.horeca.data.models.categories.UpdateCategoryRequest
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.ui.components.ScreenHeader
import com.startspeler.horeca.ui.theme.crew.*
import kotlinx.coroutines.launch

private enum class CategoryActivityFilter {
    ALL,
    ACTIVE,
    INACTIVE,
}

@Composable
fun CategoriesScreen(
    onBackToOverview: (() -> Unit)? = null,
) {
    val viewModel = remember { CategoryViewModel() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedActivityFilter by remember { mutableStateOf(CategoryActivityFilter.ALL) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryResponse?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryResponse?>(null) }
    var feedbackDialogMessage by remember { mutableStateOf<String?>(null) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    val filteredCategories = remember(
        viewModel.categories,
        searchQuery,
        selectedActivityFilter,
    ) {
        val query = searchQuery.trim()
        viewModel.categories.filter { category ->
            val matchesSearch = query.isBlank() || category.name.contains(query, ignoreCase = true)
            val matchesActivity = when (selectedActivityFilter) {
                CategoryActivityFilter.ALL -> true
                CategoryActivityFilter.ACTIVE -> category.isActive
                CategoryActivityFilter.INACTIVE -> !category.isActive
            }
            matchesSearch && matchesActivity
        }
    }

    val hasActiveFilters =
        searchQuery.isNotBlank() ||
                selectedActivityFilter != CategoryActivityFilter.ALL

    fun resetFilters() {
        searchQuery = ""
        selectedActivityFilter = CategoryActivityFilter.ALL
    }

    fun closeFormDialog() {
        showCreateDialog = false
        editingCategory = null
        isSubmitting = false
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    title = "Categorieën",
                    hasActiveFilters = hasActiveFilters,
                    onResetFilters = { resetFilters() },
                    onRefresh = { scope.launch { viewModel.loadCategories() } }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Zoek op naam") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Zoekterm wissen")
                                }
                            }
                        },
                        colors = outlinedColors(),
                        shape = RoundedCornerShape(16.dp),
                    )

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrewAccent,
                            contentColor = CrewOnAccent,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Categorie toevoegen")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CategoryActivityFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedActivityFilter == filter,
                            onClick = { selectedActivityFilter = filter },
                            label = {
                                Text(
                                    when (filter) {
                                        CategoryActivityFilter.ALL -> "Alle"
                                        CategoryActivityFilter.ACTIVE -> "Actief"
                                        CategoryActivityFilter.INACTIVE -> "Inactief"
                                    }
                                )
                            }
                        )
                    }
                }
            }


            if (viewModel.isLoading && viewModel.categories.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = CrewSurface,
                        shadowElevation = 1.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text(
                                text = "Categorieën worden vernieuwd...",
                                color = CrewTextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            if (viewModel.errorMessage != null && viewModel.categories.isNotEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Text(
                            text = viewModel.errorMessage.orEmpty(),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item {
                when {
                    viewModel.isLoading && viewModel.categories.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    viewModel.errorMessage != null && viewModel.categories.isEmpty() -> {
                        ManagementInfoCard(
                            title = "Categorieën konden niet geladen worden.",
                            description = viewModel.errorMessage.orEmpty(),
                            buttonLabel = "Opnieuw proberen",
                            onRetry = {
                                scope.launch {
                                    viewModel.loadCategories()
                                }
                            },
                        )
                    }

                    filteredCategories.isEmpty() -> {
                        EmptyCategoriesCard(
                            hasActiveSearch = searchQuery.isNotBlank() || selectedActivityFilter != CategoryActivityFilter.ALL,
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            filteredCategories.forEach { category ->
                                CategoryCard(
                                    category = category,
                                    onEdit = { editingCategory = category },
                                    onDelete = { categoryToDelete = category },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CategoryFormDialog(
            category = null,
            existingCategories = viewModel.categories,
            isSubmitting = isSubmitting,
            onDismiss = { if (!isSubmitting) closeFormDialog() },
            onSubmit = { name, description, displayOrder, isActive ->
                isSubmitting = true
                scope.launch {
                    when (
                        val result = viewModel.createCategory(
                            CreateCategoryRequest(
                                name = name,
                                description = description,
                                displayOrder = displayOrder,
                                isActive = true,
                            )
                        )
                    ) {
                        is ApiResult.Success -> {
                            closeFormDialog()
                            feedbackDialogMessage = "Categorie succesvol opgeslagen."
                        }

                        is ApiResult.Error -> {
                            isSubmitting = false
                            errorDialogMessage = result.message
                        }
                    }
                }
            },
        )
    }

    editingCategory?.let { category ->
        CategoryFormDialog(
            category = category,
            existingCategories = viewModel.categories,
            isSubmitting = isSubmitting,
            onDismiss = { if (!isSubmitting) closeFormDialog() },
            onSubmit = { name, description, displayOrder, isActive ->
                isSubmitting = true
                scope.launch {
                    when (
                        val result = viewModel.updateCategory(
                            id = category.id,
                            request = UpdateCategoryRequest(
                                name = name,
                                description = description,
                                displayOrder = displayOrder,
                                isActive = isActive,
                            )
                        )
                    ) {
                        is ApiResult.Success -> {
                            closeFormDialog()
                            feedbackDialogMessage = "Categorie succesvol gewijzigd."
                        }

                        is ApiResult.Error -> {
                            isSubmitting = false
                            errorDialogMessage = result.message
                        }
                    }
                }
            },
        )
    }

    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) categoryToDelete = null
            },
            title = { Text("Categorie verwijderen") },
            text = { Text("Ben je zeker dat je deze categorie wil verwijderen?") },
            confirmButton = {
                Button(
                    enabled = !isSubmitting,
                    onClick = {
                        isSubmitting = true
                        scope.launch {
                            when (val result = viewModel.deleteCategory(category.id)) {
                                is ApiResult.Success -> {
                                    categoryToDelete = null
                                    isSubmitting = false
                                    feedbackDialogMessage = "Categorie succesvol verwijderd."
                                }

                                is ApiResult.Error -> {
                                    categoryToDelete = null
                                    isSubmitting = false
                                    errorDialogMessage = result.message
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White,
                    ),
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(18.dp),
                            color = Color.White,
                        )
                    }
                    Text("Ja")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSubmitting,
                    onClick = { categoryToDelete = null },
                ) {
                    Text("Annuleren", color = CrewTextSecondary)
                }
            },
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
            },
        )
    }

    errorDialogMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorDialogMessage = null },
            title = { Text("Fout") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorDialogMessage = null }) {
                    Text("OK")
                }
            },
        )
    }
}

@Composable
private fun CategoryCard(
    category: CategoryResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        border = BorderStroke(1.dp, CrewBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CrewTextPrimary,
                    )
                    Text(
                        text = category.description?.takeIf { it.isNotBlank() }
                            ?: "Geen omschrijving toegevoegd.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CrewTextSecondary,
                        fontStyle = if (category.description.isNullOrBlank()) FontStyle.Italic else FontStyle.Normal,
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Categorie wijzigen",
                        tint = CrewPrimary,
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Categorie verwijderen",
                        tint = Color(0xFFDC2626),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryMetaBadge(
                    label = "Display order ${category.displayOrder}",
                    background = CrewAccent.copy(alpha = 0.18f),
                    content = CrewOnAccent,
                )
                CategoryMetaBadge(
                    label = if (category.isActive) "Actief" else "Inactief",
                    background = if (category.isActive) Color(0xFF22C55E).copy(alpha = 0.14f) else CrewBorder.copy(alpha = 0.5f),
                    content = if (category.isActive) Color(0xFF15803D) else CrewTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun CategoryMetaBadge(
    label: String,
    background: Color,
    content: Color,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background,
        contentColor = content,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun EmptyCategoriesCard(
    hasActiveSearch: Boolean,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CrewSurface,
        shadowElevation = 2.dp,
    ) {
        Text(
            text = if (hasActiveSearch) {
                "Geen categorieën gevonden voor de huidige zoekterm of filter."
            } else {
                "Er zijn nog geen categorieën aangemaakt."
            },
            modifier = Modifier.padding(20.dp),
            color = CrewTextSecondary,
        )
    }
}

@Composable
private fun ManagementInfoCard(
    title: String,
    description: String,
    buttonLabel: String,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CrewSurface,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                color = CrewTextSecondary,
            )
            Button(onClick = onRetry) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
private fun CategoryFormDialog(
    category: CategoryResponse?,
    existingCategories: List<CategoryResponse>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (name: String, description: String?, displayOrder: Int, isActive: Boolean) -> Unit,
) {
    var name by remember(category?.id) { mutableStateOf(category?.name.orEmpty()) }
    var description by remember(category?.id) { mutableStateOf(category?.description.orEmpty()) }
    var displayOrder by remember(category?.id) { mutableStateOf(category?.displayOrder?.toString().orEmpty()) }
    var isActive by remember(category?.id) { mutableStateOf(category?.isActive ?: true) }

    var nameTouched by remember(category?.id) { mutableStateOf(false) }
    var displayOrderTouched by remember(category?.id) { mutableStateOf(false) }

    val trimmedName = name.trim()
    val trimmedDescription = description.trim()
    val displayOrderValue = displayOrder.toIntOrNull()

    val duplicateNameError = existingCategories
        .firstOrNull { it.id != category?.id && it.name.equals(trimmedName, ignoreCase = true) }
        ?.let { "Er bestaat al een categorie met deze naam." }

    val duplicateDisplayOrderError = existingCategories
        .firstOrNull { it.id != category?.id && it.displayOrder == displayOrderValue }
        ?.let { "Er bestaat al een categorie met deze display order." }

    val nameError = when {
        trimmedName.isBlank() -> "Naam is verplicht."
        trimmedName.length > 100 -> "Naam mag maximaal 100 tekens bevatten."
        else -> null
    }

    val displayOrderError = when {
        displayOrder.isBlank() -> "Display order is verplicht."
        displayOrderValue == null -> "Display order moet een geheel getal zijn."
        displayOrderValue < 0 -> "Display order mag niet negatief zijn."
        else -> null
    }

    val descriptionError = when {
        trimmedDescription.length > 255 -> "Omschrijving mag maximaal 255 tekens bevatten."
        else -> null
    }

    val nameUiError = if (nameTouched) nameError ?: duplicateNameError else null
    val displayOrderUiError = if (displayOrderTouched) displayOrderError ?: duplicateDisplayOrderError else null

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        title = {
            Text(if (category == null) "Categorie toevoegen" else "Categorie wijzigen")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    label = { Text("Naam") },
                    singleLine = true,
                    isError = nameUiError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedColors(),
                )

                if (nameUiError != null) {
                    Text(
                        text = nameUiError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                OutlinedTextField(
                    value = displayOrder,
                    onValueChange = {
                        displayOrder = if (it == "-") it else it.filter { char -> char.isDigit() || char == '-' }
                        displayOrderTouched = true
                    },
                    label = { Text("Display order") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = displayOrderUiError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedColors(),
                )

                if (displayOrderUiError != null) {
                    Text(
                        text = displayOrderUiError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Omschrijving (optioneel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = descriptionError != null,
                    colors = outlinedColors(),
                )

                if (descriptionError != null) {
                    Text(
                        text = descriptionError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                if (category == null) {
                    Text(
                        text = "Nieuwe categorieën worden automatisch actief opgeslagen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrewTextSecondary,
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "Actief",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "Inactieve categorieën blijven bewaard maar kunnen uit de menukaart verdwijnen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrewTextSecondary,
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && nameError == null && displayOrderError == null && descriptionError == null && duplicateNameError == null && duplicateDisplayOrderError == null,
                onClick = {
                    onSubmit(
                        trimmedName,
                        trimmedDescription.ifBlank { null },
                        displayOrderValue ?: 0,
                        if (category == null) true else isActive,
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrewAccent,
                    contentColor = CrewOnAccent,
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(18.dp),
                    )
                }
                Text(if (category == null) "Toevoegen" else "Opslaan")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSubmitting, onClick = onDismiss) {
                Text("Annuleren", color = CrewTextSecondary)
            }
        },
    )
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
