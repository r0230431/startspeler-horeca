package com.startspeler.horeca.screens.crew.customers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.auth.CrewRole
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.customers.CustomerResponse
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewBackground
import com.startspeler.horeca.ui.theme.crew.CrewBorder
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextMuted
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary
import com.startspeler.horeca.ui.theme.crew.ErrorRed
import kotlinx.coroutines.launch

@Composable
fun CustomersScreen(
    crewRole: CrewRole,
) {
    val viewModel = remember { CustomerViewModel() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var customerToDelete by remember { mutableStateOf<CustomerResponse?>(null) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var actionError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCustomers()
    }

    val filteredCustomers = remember(viewModel.customers, searchQuery) {
        val query = searchQuery.trim()
        viewModel.customers.filter { customer ->
            query.isBlank() || customer.username.contains(query, ignoreCase = true)
        }
    }

    val isAdmin = crewRole == CrewRole.ADMIN

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewBackground)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = 24.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Klanten",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CrewTextPrimary,
        )

        Text(
            text = "Raadpleeg alle klanten alfabetisch en verwijder klanten rechtstreeks vanuit de lijst.",
            style = MaterialTheme.typography.bodyMedium,
            color = CrewTextSecondary,
        )

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
                label = { Text("Zoek klant op naam") },
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

            if (isAdmin) {
                Button(
                    onClick = { showDeleteAllDialog = true },
                    enabled = !isSubmitting && viewModel.customers.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Alles verwijderen")
                }
            }
        }

        when {
            viewModel.isLoading && viewModel.customers.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            viewModel.errorMessage != null && viewModel.customers.isEmpty() -> {
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
                            text = "Klanten konden niet geladen worden.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = viewModel.errorMessage.orEmpty(),
                            color = CrewTextSecondary,
                        )
                        Button(onClick = { scope.launch { viewModel.loadCustomers() } }) {
                            Text("Opnieuw proberen")
                        }
                    }
                }
            }

            filteredCustomers.isEmpty() -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = CrewSurface,
                    shadowElevation = 2.dp,
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) {
                            "Er zijn nog geen klanten beschikbaar."
                        } else {
                            "Geen klanten gevonden voor \"${searchQuery.trim()}\"."
                        },
                        modifier = Modifier.padding(20.dp),
                        color = CrewTextSecondary,
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerListCard(
                            customer = customer,
                            isSubmitting = isSubmitting,
                            onDeleteClick = { customerToDelete = customer }
                        )
                    }
                }
            }
        }
    }

    customerToDelete?.let { customer ->
        AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) customerToDelete = null
            },
            title = { Text("Klant verwijderen") },
            text = { Text("Ben je zeker dat je klant '${customer.username}' wil verwijderen?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            when (val result = viewModel.deleteCustomer(customer.id)) {
                                is ApiResult.Success -> customerToDelete = null
                                is ApiResult.Error -> {
                                    customerToDelete = null
                                    actionError = result.message
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
                    onClick = { customerToDelete = null },
                    enabled = !isSubmitting,
                ) {
                    Text("Annuleren")
                }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) showDeleteAllDialog = false
            },
            title = { Text("Alle klanten verwijderen") },
            text = { Text("Ben je zeker dat je alle klanten wil verwijderen?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isSubmitting = true
                            when (val result = viewModel.deleteAllCustomers()) {
                                is ApiResult.Success -> showDeleteAllDialog = false
                                is ApiResult.Error -> {
                                    showDeleteAllDialog = false
                                    actionError = result.message
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
                    onClick = { showDeleteAllDialog = false },
                    enabled = !isSubmitting,
                ) {
                    Text("Annuleren")
                }
            }
        )
    }

    actionError?.let { message ->
        AlertDialog(
            onDismissRequest = { actionError = null },
            title = { Text("Actie niet gelukt") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { actionError = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun CustomerListCard(
    customer: CustomerResponse,
    isSubmitting: Boolean,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        border = BorderStroke(1.dp, CrewBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CrewAccent.copy(alpha = 0.18f),
                contentColor = CrewPrimary,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = customer.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = CrewTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Klant #${customer.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CrewTextMuted,
                )
            }

            IconButton(
                onClick = onDeleteClick,
                enabled = !isSubmitting,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Klant verwijderen",
                    tint = ErrorRed,
                )
            }
        }
    }
}
