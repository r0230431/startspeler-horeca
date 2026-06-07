package com.startspeler.horeca.screens.crew.management.discounts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.discounts.CreateDiscountRequest
import com.startspeler.horeca.data.models.discounts.DiscountResponse
import com.startspeler.horeca.data.models.discounts.DiscountType
import com.startspeler.horeca.data.models.discounts.UpdateDiscountRequest
import com.startspeler.horeca.data.models.discounts.displayLabel
import com.startspeler.horeca.data.models.discounts.valueFieldLabel
import com.startspeler.horeca.screens.crew.discounts.DiscountViewModel
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
import com.startspeler.horeca.ui.theme.crew.ErrorRed
import com.startspeler.horeca.ui.theme.crew.SuccessGreen
import kotlinx.coroutines.launch

private enum class DiscountActivityFilter { ALL, ACTIVE, INACTIVE }

@Composable
fun DiscountsScreen(
    onBackToOverview: (() -> Unit)? = null,
) {
    val viewModel = remember { DiscountViewModel() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<DiscountType?>(null) }
    var selectedActivityFilter by remember { mutableStateOf(DiscountActivityFilter.ALL) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingDiscount by remember { mutableStateOf<DiscountResponse?>(null) }
    var discountToDelete by remember { mutableStateOf<DiscountResponse?>(null) }
    var formBackendError by remember { mutableStateOf<String?>(null) }
    var feedbackDialogMessage by remember { mutableStateOf<String?>(null) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDiscounts()
    }

    val filteredDiscounts = remember(
        viewModel.discounts,
        searchQuery,
        selectedTypeFilter,
        selectedActivityFilter,
    ) {
        val query = searchQuery.trim()
        viewModel.discounts.filter { discount ->
            val matchesSearch = query.isBlank() || discount.name.contains(query, ignoreCase = true)
            val matchesType = selectedTypeFilter == null || discount.discountType == selectedTypeFilter
            val matchesActivity = when (selectedActivityFilter) {
                DiscountActivityFilter.ALL -> true
                DiscountActivityFilter.ACTIVE -> discount.isActive
                DiscountActivityFilter.INACTIVE -> !discount.isActive
            }
            matchesSearch && matchesType && matchesActivity
        }
    }

    val hasActiveFilters =
        searchQuery.isNotBlank() ||
                selectedTypeFilter != null ||
                selectedActivityFilter != DiscountActivityFilter.ALL

    fun resetFilters() {
        searchQuery = ""
        selectedTypeFilter = null
        selectedActivityFilter = DiscountActivityFilter.ALL
    }

    fun closeFormDialog() {
        showCreateDialog = false
        editingDiscount = null
        formBackendError = null
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
                    title = "Kortingen",
                    hasActiveFilters = hasActiveFilters,
                    onResetFilters = { resetFilters() },
                    onRefresh = { scope.launch { viewModel.loadDiscounts() } }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
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
                                    Icon(Icons.Default.Clear, contentDescription = "Zoekopdracht wissen")
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrewAccent,
                            unfocusedBorderColor = CrewBorder,
                            focusedLabelColor = CrewAccent,
                        ),
                        shape = RoundedCornerShape(16.dp),
                    )

                    Button(
                        onClick = {
                            formBackendError = null
                            showCreateDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrewAccent,
                            contentColor = CrewOnAccent,
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Korting toevoegen")
                    }
                }
            }

            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val wideLayout = maxWidth >= 700.dp

                    if (wideLayout) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SimpleFilterDropdown(
                                items = DiscountType.entries,
                                selectedItem = selectedTypeFilter,
                                onItemSelected = { selectedTypeFilter = it },
                                itemLabel = {
                                    when (it) {
                                        DiscountType.FIXED_AMOUNT -> "Vast"
                                        DiscountType.PERCENTAGE -> "Percentage"
                                    }
                                },
                                allLabel = "Alle types",
                                modifier = Modifier.weight(1f)
                            )

                            SimpleFilterDropdown(
                                items = DiscountActivityFilter.entries,
                                selectedItem = selectedActivityFilter,
                                onItemSelected = { selectedActivityFilter = it ?: DiscountActivityFilter.ALL },
                                itemLabel = {
                                    when (it) {
                                        DiscountActivityFilter.ALL -> "Alle"
                                        DiscountActivityFilter.ACTIVE -> "Actief"
                                        DiscountActivityFilter.INACTIVE -> "Inactief"
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
                                items = DiscountType.entries,
                                selectedItem = selectedTypeFilter,
                                onItemSelected = { selectedTypeFilter = it },
                                itemLabel = {
                                    when (it) {
                                        DiscountType.FIXED_AMOUNT -> "Vast"
                                        DiscountType.PERCENTAGE -> "Percentage"
                                    }
                                },
                                allLabel = "Alle types",
                                modifier = Modifier.fillMaxWidth()
                            )

                            SimpleFilterDropdown(
                                items = DiscountActivityFilter.entries,
                                selectedItem = selectedActivityFilter,
                                onItemSelected = { selectedActivityFilter = it ?: DiscountActivityFilter.ALL },
                                itemLabel = {
                                    when (it) {
                                        DiscountActivityFilter.ALL -> "Alle"
                                        DiscountActivityFilter.ACTIVE -> "Actief"
                                        DiscountActivityFilter.INACTIVE -> "Inactief"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item {
                when {
                    viewModel.isLoading && viewModel.discounts.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    viewModel.errorMessage != null && viewModel.discounts.isEmpty() -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = CrewSurface,
                            tonalElevation = 0.dp,
                            shadowElevation = 2.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Kortingen konden niet geladen worden.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = viewModel.errorMessage.orEmpty(),
                                    color = CrewTextSecondary,
                                )
                                Button(onClick = { scope.launch { viewModel.loadDiscounts() } }) {
                                    Text("Opnieuw proberen")
                                }
                            }
                        }
                    }

                    filteredDiscounts.isEmpty() -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = CrewSurface,
                            shadowElevation = 2.dp,
                        ) {
                            Text(
                                text = if (viewModel.discounts.isEmpty()) {
                                    "Er zijn nog geen kortingen beschikbaar."
                                } else {
                                    "Geen kortingen gevonden voor de opgegeven filters."
                                },
                                modifier = Modifier.padding(20.dp),
                                color = CrewTextSecondary,
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            filteredDiscounts.forEach { discount ->
                                DiscountListCard(
                                    discount = discount,
                                    isSubmitting = isSubmitting,
                                    onEditClick = {
                                        formBackendError = null
                                        editingDiscount = discount
                                    },
                                    onDeleteClick = { discountToDelete = discount }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        DiscountFormDialog(
            isSubmitting = isSubmitting,
            backendError = formBackendError,
            onDismiss = { closeFormDialog() },
            onSubmit = { formData ->
                scope.launch {
                    isSubmitting = true
                    formBackendError = null
                    when (val result = viewModel.createDiscount(
                        CreateDiscountRequest(
                            name = formData.name,
                            description = formData.description,
                            discountType = formData.discountType,
                            discountValue = formData.discountValue,
                            isActive = true,
                        )
                    )) {
                        is ApiResult.Success -> {
                            closeFormDialog()
                            feedbackDialogMessage = "Korting succesvol opgeslagen."
                        }
                        is ApiResult.Error -> {
                            formBackendError = result.message
                            if (!result.message.isFieldValidationError()) {
                                errorDialogMessage = result.message
                            }
                        }
                    }
                    isSubmitting = false
                }
            }
        )
    }

    editingDiscount?.let { discount ->
        DiscountFormDialog(
            discount = discount,
            isSubmitting = isSubmitting,
            backendError = formBackendError,
            onDismiss = { closeFormDialog() },
            onSubmit = { formData ->
                scope.launch {
                    isSubmitting = true
                    formBackendError = null
                    when (val result = viewModel.updateDiscount(
                        discount.id,
                        UpdateDiscountRequest(
                            name = formData.name,
                            description = formData.description,
                            discountType = formData.discountType,
                            discountValue = formData.discountValue,
                            isActive = formData.isActive,
                        )
                    )) {
                        is ApiResult.Success -> {
                            closeFormDialog()
                            feedbackDialogMessage = "Korting succesvol gewijzigd."
                        }
                        is ApiResult.Error -> {
                            formBackendError = result.message
                            if (!result.message.isFieldValidationError()) {
                                errorDialogMessage = result.message
                            }
                        }
                    }
                    isSubmitting = false
                }
            }
        )
    }

    discountToDelete?.let { discount ->
        AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) discountToDelete = null
            },
            title = { Text("Korting verwijderen") },
            text = { Text("Ben je zeker dat je deze korting wil verwijderen?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            when (val result = viewModel.deleteDiscount(discount.id)) {
                                is ApiResult.Success -> discountToDelete = null
                                is ApiResult.Error -> {
                                    discountToDelete = null
                                    errorDialogMessage = result.message
                                }
                            }
                            isSubmitting = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = MaterialTheme.colorScheme.onError,
                    )
                ) {
                    Text("Ja")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { discountToDelete = null },
                    enabled = !isSubmitting,
                ) {
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
            title = { Text("Actie niet gelukt") },
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
private fun DiscountListCard(
    discount: DiscountResponse,
    isSubmitting: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = CrewAccent.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = null,
                        tint = CrewPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = discount.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = CrewTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = discount.description?.takeIf { it.isNotBlank() }
                            ?: "Geen omschrijving toegevoegd.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CrewTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                IconButton(onClick = onEditClick, enabled = !isSubmitting) {
                    Icon(Icons.Default.Edit, contentDescription = "Korting wijzigen", tint = CrewPrimary)
                }
                IconButton(onClick = onDeleteClick, enabled = !isSubmitting) {
                    Icon(Icons.Default.Delete, contentDescription = "Korting verwijderen", tint = ErrorRed)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DiscountInfoBadge(
                    label = discount.discountType.displayLabel,
                    containerColor = CrewAccent.copy(alpha = 0.18f),
                    contentColor = CrewPrimary,
                )
                DiscountInfoBadge(
                    label = discount.formattedValue,
                    containerColor = CrewPrimary.copy(alpha = 0.12f),
                    contentColor = CrewPrimary,
                )
                DiscountInfoBadge(
                    label = if (discount.isActive) "Actief" else "Inactief",
                    containerColor = if (discount.isActive) SuccessGreen.copy(alpha = 0.18f) else ErrorRed.copy(alpha = 0.16f),
                    contentColor = if (discount.isActive) SuccessGreen else ErrorRed,
                )
            }
        }
    }
}

@Composable
private fun DiscountInfoBadge(
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private val DiscountResponse.formattedValue: String
    get() = when (discountType) {
        DiscountType.PERCENTAGE -> "${discountValue.trim()}%"
        DiscountType.FIXED_AMOUNT -> "€ ${discountValue.trim()}"
    }

private data class DiscountFormData(
    val name: String,
    val description: String?,
    val discountType: DiscountType,
    val discountValue: String,
    val isActive: Boolean,
)

@Composable
private fun DiscountFormDialog(
    discount: DiscountResponse? = null,
    isSubmitting: Boolean,
    backendError: String?,
    onDismiss: () -> Unit,
    onSubmit: (DiscountFormData) -> Unit,
) {
    var name by remember(discount) { mutableStateOf(discount?.name.orEmpty()) }
    var description by remember(discount) { mutableStateOf(discount?.description.orEmpty()) }
    var selectedType by remember(discount) { mutableStateOf(discount?.discountType) }
    var value by remember(discount) { mutableStateOf(discount?.discountValue.orEmpty()) }
    var isActive by remember(discount) { mutableStateOf(discount?.isActive ?: true) }

    var nameTouched by remember { mutableStateOf(false) }
    var typeTouched by remember { mutableStateOf(false) }
    var valueTouched by remember { mutableStateOf(false) }
    var descriptionTouched by remember { mutableStateOf(false) }

    val backendNameError = backendError.toDiscountNameError()
    val backendValueError = backendError.toDiscountValueError()
    val backendDescriptionError = backendError.toDiscountDescriptionError()

    val nameError = when {
        name.isBlank() -> "Naam van de korting is verplicht."
        name.trim().length > 100 -> "Naam mag maximaal 100 tekens bevatten."
        else -> null
    }

    val typeError = if (selectedType == null) "Type korting is verplicht." else null

    val valueError = validateDiscountValue(value = value, discountType = selectedType)

    val descriptionError = when {
        description.trim().length > 255 -> "Omschrijving mag maximaal 255 tekens bevatten."
        else -> null
    }

    val showNameError = if (nameTouched) nameError ?: backendNameError else backendNameError
    val showTypeError = if (typeTouched) typeError else null
    val showValueError = if (valueTouched) valueError ?: backendValueError else backendValueError
    val showDescriptionError = if (descriptionTouched) descriptionError ?: backendDescriptionError else backendDescriptionError

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        title = {
            Text(if (discount == null) "Korting toevoegen" else "Korting wijzigen")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    label = { Text("Naam") },
                    singleLine = true,
                    isError = showNameError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedColors()
                )
                showNameError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Text(
                    text = "Type korting",
                    style = MaterialTheme.typography.labelLarge,
                    color = CrewTextSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == DiscountType.FIXED_AMOUNT,
                        onClick = {
                            selectedType = DiscountType.FIXED_AMOUNT
                            typeTouched = true
                        },
                        label = { Text("Vast (€)") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CrewSurface,
                            labelColor = CrewPrimary,
                            selectedContainerColor = CrewAccent,
                            selectedLabelColor = CrewOnAccent
                        )
                    )
                    FilterChip(
                        selected = selectedType == DiscountType.PERCENTAGE,
                        onClick = {
                            selectedType = DiscountType.PERCENTAGE
                            typeTouched = true
                        },
                        label = { Text("Percentage (%)") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CrewSurface,
                            labelColor = CrewPrimary,
                            selectedContainerColor = CrewAccent,
                            selectedLabelColor = CrewOnAccent
                        )
                    )
                }
                showTypeError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        valueTouched = true
                    },
                    label = { Text(selectedType?.valueFieldLabel ?: "Waarde") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = showValueError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedColors()
                )
                showValueError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionTouched = true
                    },
                    label = { Text("Omschrijving (optioneel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    isError = showDescriptionError != null,
                    colors = outlinedColors()
                )
                showDescriptionError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                if (discount != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Actief",
                                style = MaterialTheme.typography.titleSmall,
                                color = CrewTextPrimary,
                            )
                            Text(
                                text = "Inactieve kortingen blijven bestaan maar kunnen apart gefilterd worden.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CrewTextSecondary,
                            )
                        }
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            enabled = !isSubmitting,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    nameTouched = true
                    typeTouched = true
                    valueTouched = true
                    descriptionTouched = true

                    if (nameError == null && typeError == null && valueError == null && descriptionError == null) {
                        val descriptionValue = description.trim().ifBlank { null }
                        val normalizedValue = value.trim().replace(',', '.')
                        val type = selectedType!!
                        onSubmit(
                            DiscountFormData(
                                name = name.trim(),
                                description = descriptionValue,
                                discountType = type,
                                discountValue = normalizedValue,
                                isActive = if (discount == null) true else isActive,
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrewAccent,
                    contentColor = CrewOnAccent
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(18.dp)
                    )
                }
                Text(if (discount == null) "Opslaan" else "Wijzigen")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSubmitting, onClick = onDismiss) {
                Text("Annuleren", color = CrewTextSecondary)
            }
        }
    )
}

private fun validateDiscountValue(
    value: String,
    discountType: DiscountType?,
): String? {
    val normalized = value.trim().replace(',', '.')
    if (normalized.isBlank()) return "Waarde van de korting is verplicht."

    val parsed = normalized.toDoubleOrNull() ?: return "Voer een geldig getal in."

    if (parsed <= 0.0) {
        return "Waarde van de korting moet groter zijn dan 0."
    }

    if (discountType == DiscountType.PERCENTAGE && parsed > 100.0) {
        return "Percentage mag maximaal 100 zijn."
    }

    return null
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

private fun String?.toDiscountNameError(): String? {
    val value = this?.trim().orEmpty()
    return when {
        value.contains("bestaat al", ignoreCase = true) -> "Er bestaat al een korting met deze naam."
        value.contains("naam van de korting is verplicht", ignoreCase = true) -> "Naam van de korting is verplicht."
        value.contains("maximaal 100", ignoreCase = true) -> "Naam mag maximaal 100 tekens bevatten."
        else -> null
    }
}

private fun String?.toDiscountValueError(): String? {
    val value = this?.trim().orEmpty()
    return when {
        value.contains("percentagekorting", ignoreCase = true) -> "Percentage moet groter zijn dan 0 en mag maximaal 100 zijn."
        value.contains("vast kortingsbedrag", ignoreCase = true) -> "Vast kortingsbedrag moet groter zijn dan 0."
        value.contains("waarde van de korting", ignoreCase = true) -> "Waarde van de korting is verplicht."
        else -> null
    }
}

private fun String?.toDiscountDescriptionError(): String? {
    val value = this?.trim().orEmpty()
    return if (value.contains("omschrijving", ignoreCase = true) && value.contains("255")) {
        "Omschrijving mag maximaal 255 tekens bevatten."
    } else {
        null
    }
}

private fun String.isFieldValidationError(): Boolean {
    return toDiscountNameError() != null || toDiscountValueError() != null || toDiscountDescriptionError() != null
}
