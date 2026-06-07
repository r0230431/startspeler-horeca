package com.startspeler.horeca.screens.crew.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.tables.TableResponse
import com.startspeler.horeca.screens.crew.tables.components.TableTile
import com.startspeler.horeca.screens.crew.tables.qr.rememberTableQrActions
import com.startspeler.horeca.ui.components.ScreenHeader
import com.startspeler.horeca.ui.theme.crew.*
import kotlinx.coroutines.launch

@Composable
fun TablesScreen(
    onBackToOverview: (() -> Unit)? = null,
) {
    val viewModel = remember { TableViewModel() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTable by remember { mutableStateOf<TableResponse?>(null) }
    var editingTable by remember { mutableStateOf<TableResponse?>(null) }
    var tableToDelete by remember { mutableStateOf<TableResponse?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }
    var minSeatCountQuery by remember { mutableStateOf("") }
    var qrActionMessage by remember { mutableStateOf<String?>(null) }
    val qrActions = rememberTableQrActions()

    LaunchedEffect(Unit) {
        viewModel.loadTables()
    }

    val filteredTables = remember(viewModel.tables, searchQuery, minSeatCountQuery) {
        val minSeats = minSeatCountQuery.toIntOrNull()

        viewModel.tables.filter { table ->
            val matchesNumber =
                searchQuery.isBlank() || table.tableNumber.toString().contains(searchQuery.trim())

            val matchesSeats =
                minSeats == null || table.seatCount >= minSeats

            matchesNumber && matchesSeats
        }
    }

    val hasActiveFilters =
        searchQuery.isNotBlank() ||
                minSeatCountQuery.isNotBlank()

    fun resetFilters() {
        searchQuery = ""
        minSeatCountQuery = ""
    }

    fun closeFormDialogs() {
        showCreateDialog = false
        showEditDialog = false
        editingTable = null
        formError = null
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
                    title = "Tafels",
                    hasActiveFilters = hasActiveFilters,
                    onResetFilters = { resetFilters() },
                    onRefresh = { scope.launch { viewModel.loadTables() } }
                )
            }

            item {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    val isCompact = maxWidth < 600.dp
                    if (isCompact) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it.filter(Char::isDigit) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Zoek op tafelnummer") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrewAccent,
                                    unfocusedBorderColor = CrewBorder,
                                    focusedLabelColor = CrewAccent,
                                ),
                                shape = RoundedCornerShape(16.dp),
                            )
                            OutlinedTextField(
                                value = minSeatCountQuery,
                                onValueChange = { minSeatCountQuery = it.filter(Char::isDigit) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Minimum zitplaatsen") },
                                leadingIcon = { Icon(Icons.Default.EventSeat, contentDescription = null) },
                                trailingIcon = {
                                    if (minSeatCountQuery.isNotBlank()) {
                                        IconButton(onClick = { minSeatCountQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = null)
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
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrewAccent,
                                    contentColor = CrewOnAccent,
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Tafel toevoegen")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it.filter(Char::isDigit) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Zoek op tafelnummer") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrewAccent,
                                    unfocusedBorderColor = CrewBorder,
                                    focusedLabelColor = CrewAccent,
                                )
                            )
                            OutlinedTextField(
                                value = minSeatCountQuery,
                                onValueChange = { minSeatCountQuery = it.filter(Char::isDigit) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("Minimum zitplaatsen") },
                                leadingIcon = { Icon(Icons.Default.EventSeat, contentDescription = null) },
                                trailingIcon = {
                                    if (minSeatCountQuery.isNotBlank()) {
                                        IconButton(onClick = { minSeatCountQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = null)
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrewAccent,
                                    unfocusedBorderColor = CrewBorder,
                                    focusedLabelColor = CrewAccent,
                                )
                            )
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrewAccent,
                                    contentColor = CrewOnAccent,
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Tafel toevoegen")
                            }
                        }
                    }
                }
            }

            item {
                when {
                    viewModel.isLoading && viewModel.tables.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    viewModel.errorMessage != null && viewModel.tables.isEmpty() -> {
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
                                    text = "Tafels konden niet geladen worden.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = viewModel.errorMessage.orEmpty(),
                                    color = CrewTextSecondary,
                                )
                                Button(onClick = {
                                    scope.launch { viewModel.loadTables() }
                                }) {
                                    Text("Opnieuw proberen")
                                }
                            }
                        }
                    }

                    filteredTables.isEmpty() -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = CrewSurface,
                            shadowElevation = 2.dp,
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) {
                                    "Er zijn nog geen tafels aangemaakt."
                                } else {
                                    "Geen tafels gevonden voor tafelnummer $searchQuery."
                                },
                                modifier = Modifier.padding(20.dp),
                                color = CrewTextSecondary,
                            )
                        }
                    }

                    else -> {
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val columnCount = (maxWidth / 192.dp).toInt().coerceAtLeast(1)
                            val rows = filteredTables.chunked(columnCount)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { table ->
                                            TableTile(
                                                table = table,
                                                onClick = { selectedTable = table },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        repeat(columnCount - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        TableFormDialog(
            table = null,
            isSubmitting = isSubmitting,
            backendError = formError,
            onDismiss = { if (!isSubmitting) closeFormDialogs() },
            onSubmit = { tableNumber, seatCount, note ->
                isSubmitting = true
                formError = null
                scope.launch {
                    when (val result = viewModel.createTable(tableNumber, seatCount, note)) {
                        is ApiResult.Success -> closeFormDialogs()
                        is ApiResult.Error -> formError = result.message
                    }
                    isSubmitting = false
                }
            }
        )
    }

    selectedTable?.let { table ->
        TableDetailsDialog(
            table = table,
            canShareQr = qrActions.canShare,
            canPrintQr = qrActions.canPrint,
            shareQrLabel = qrActions.shareLabel,
            printQrLabel = qrActions.printLabel,
            onShareQr = {
                qrActionMessage = qrActions.shareQr(
                    generateTableQrContent(table.tableNumber),
                    "tafel_${table.tableNumber}_qr"
                )
            },
            onPrintQr = {
                qrActionMessage = qrActions.printQr(
                    generateTableQrContent(table.tableNumber),
                    "tafel_${table.tableNumber}_qr"
                )
            },
            onDismiss = { selectedTable = null },
            onEdit = {
                editingTable = table
                selectedTable = null
                showEditDialog = true
            },
            onDelete = {
                selectedTable = null
                tableToDelete = table
            }
        )
    }

    if (showEditDialog) {
        editingTable?.let { table ->
            TableFormDialog(
                table = table,
                isSubmitting = isSubmitting,
                backendError = formError,
                onDismiss = { if (!isSubmitting) closeFormDialogs() },
                onSubmit = { tableNumber, seatCount, note ->
                    isSubmitting = true
                    formError = null
                    scope.launch {
                        when (val result = viewModel.updateTable(table.id, tableNumber, seatCount, note)) {
                            is ApiResult.Success -> closeFormDialogs()
                            is ApiResult.Error -> formError = result.message
                        }
                        isSubmitting = false
                    }
                }
            )
        }
    }

    tableToDelete?.let { table ->
        AlertDialog(
            onDismissRequest = { tableToDelete = null },
            title = { Text("Tafel verwijderen") },
            text = {
                Text("Ben je zeker dat je tafel ${table.tableNumber} wil verwijderen?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            when (val result = viewModel.deleteTable(table.id)) {
                                is ApiResult.Success -> tableToDelete = null
                                is ApiResult.Error -> {
                                    tableToDelete = null
                                    formError = result.message
                                }
                            }
                            isSubmitting = false
                        }
                    }
                ) {
                    Text("Verwijderen")
                }
            },
            dismissButton = {
                TextButton(onClick = { tableToDelete = null }) {
                    Text("Annuleren")
                }
            }
        )
    }

    if (!formError.isNullOrBlank() && !showCreateDialog && !showEditDialog) {
        AlertDialog(
            onDismissRequest = { formError = null },
            title = { Text("Actie niet gelukt") },
            text = { Text(formError.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { formError = null }) {
                    Text("OK")
                }
            }
        )
    }

    if (!qrActionMessage.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = { qrActionMessage = null },
            title = { Text("QR-actie") },
            text = { Text(qrActionMessage.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { qrActionMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}
