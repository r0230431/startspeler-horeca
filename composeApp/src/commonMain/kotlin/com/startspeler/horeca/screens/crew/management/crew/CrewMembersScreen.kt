package com.startspeler.horeca.screens.crew.management.crew

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.auth.CrewRole
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.crew.CreateCrewMemberRequest
import com.startspeler.horeca.data.models.crew.CrewMemberResponse
import com.startspeler.horeca.data.models.crew.UpdateCrewMemberRequest
import com.startspeler.horeca.ui.components.ScreenHeader
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewBackground
import com.startspeler.horeca.ui.theme.crew.CrewBorder
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary
import kotlinx.coroutines.launch
import kotlin.random.Random

private enum class CrewRoleFilter {
    ALL,
    ADMIN,
    STAFF,
}

@Composable
fun CrewMembersScreen(
    onBackToOverview: (() -> Unit)? = null,
) {
    val viewModel = remember { CrewMembersViewModel() }
    val scope = rememberCoroutineScope()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedRoleFilter by rememberSaveable { mutableStateOf(CrewRoleFilter.ALL) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingCrewMember by remember { mutableStateOf<CrewMemberResponse?>(null) }
    var crewMemberToDelete by remember { mutableStateOf<CrewMemberResponse?>(null) }
    var feedbackDialogMessage by remember { mutableStateOf<String?>(null) }
    var errorDialogMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCrewMembers()
    }

    val filteredCrewMembers = remember(viewModel.crewMembers, searchQuery, selectedRoleFilter) {
        val query = searchQuery.trim()
        viewModel.crewMembers.filter { crewMember ->
            val matchesSearch = query.isBlank() || crewMember.username.contains(query, ignoreCase = true)
            val matchesRole = when (selectedRoleFilter) {
                CrewRoleFilter.ALL -> true
                CrewRoleFilter.ADMIN -> crewMember.role == CrewRole.ADMIN
                CrewRoleFilter.STAFF -> crewMember.role == CrewRole.STAFF
            }
            matchesSearch && matchesRole
        }
    }

    val adminCount = remember(viewModel.crewMembers) {
        viewModel.crewMembers.count { it.role == CrewRole.ADMIN }
    }

    val hasActiveFilters =
        searchQuery.isNotBlank() ||
                selectedRoleFilter != CrewRoleFilter.ALL

    fun resetFilters() {
        searchQuery = ""
        selectedRoleFilter = CrewRoleFilter.ALL
    }

    fun closeFormDialog() {
        showCreateDialog = false
        editingCrewMember = null
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
                    title = "Medewerkers",
                    hasActiveFilters = hasActiveFilters,
                    onResetFilters = { resetFilters() },
                    onRefresh = { scope.launch { viewModel.loadCrewMembers() } }
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
                        Text("Medewerker toevoegen")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CrewRoleFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedRoleFilter == filter,
                            onClick = { selectedRoleFilter = filter },
                            label = {
                                Text(
                                    when (filter) {
                                        CrewRoleFilter.ALL -> "Alle rollen"
                                        CrewRoleFilter.ADMIN -> "Admin"
                                        CrewRoleFilter.STAFF -> "Medewerker"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            if (viewModel.isLoading && viewModel.crewMembers.isNotEmpty()) {
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
                                text = "Medewerkers worden vernieuwd...",
                                color = CrewTextSecondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            if (viewModel.errorMessage != null && viewModel.crewMembers.isNotEmpty()) {
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
                    viewModel.isLoading && viewModel.crewMembers.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    viewModel.errorMessage != null && viewModel.crewMembers.isEmpty() -> {
                        ManagementInfoCard(
                            title = "Medewerkers konden niet geladen worden.",
                            description = viewModel.errorMessage.orEmpty(),
                            buttonLabel = "Opnieuw proberen",
                            onRetry = {
                                scope.launch {
                                    viewModel.loadCrewMembers()
                                }
                            },
                        )
                    }

                    filteredCrewMembers.isEmpty() -> {
                        EmptyCrewMembersCard(
                            hasActiveSearch = searchQuery.isNotBlank() || selectedRoleFilter != CrewRoleFilter.ALL,
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            filteredCrewMembers.forEach { crewMember ->
                                val isLastAdmin = crewMember.role == CrewRole.ADMIN && adminCount <= 1

                                CrewMemberCard(
                                    crewMember = crewMember,
                                    isLastAdmin = isLastAdmin,
                                    onEdit = { editingCrewMember = crewMember },
                                    onDelete = {
                                        if (!isLastAdmin) {
                                            crewMemberToDelete = crewMember
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CrewMemberFormDialog(
            crewMember = null,
            existingCrewMembers = viewModel.crewMembers,
            isLastAdmin = false,
            isSubmitting = isSubmitting,
            onDismiss = { if (!isSubmitting) closeFormDialog() },
            onSubmit = { username, role, password ->
                isSubmitting = true
                scope.launch {
                    when (
                        val result = viewModel.createCrewMember(
                            CreateCrewMemberRequest(
                                username = username,
                                password = password.orEmpty(),
                                role = role,
                            )
                        )
                    ) {
                        is ApiResult.Success -> {
                            closeFormDialog()
                            feedbackDialogMessage = "Medewerker succesvol opgeslagen."
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

    editingCrewMember?.let { crewMember ->
        val isLastAdmin = crewMember.role == CrewRole.ADMIN && adminCount <= 1

        CrewMemberFormDialog(
            crewMember = crewMember,
            existingCrewMembers = viewModel.crewMembers,
            isLastAdmin = false,
            isSubmitting = isSubmitting,
            onDismiss = { if (!isSubmitting) closeFormDialog() },
            onSubmit = { username, role, password ->
                isSubmitting = true
                scope.launch {
                    when (
                        val result = viewModel.updateCrewMember(
                            id = crewMember.id,
                            request = UpdateCrewMemberRequest(
                                username = username,
                                role = role,
                                password = password,
                            )
                        )
                    ) {
                        is ApiResult.Success -> {
                            closeFormDialog()
                            feedbackDialogMessage = "Medewerker succesvol gewijzigd."
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

    crewMemberToDelete?.let { crewMember ->
        AlertDialog(
            onDismissRequest = {
                if (!isSubmitting) crewMemberToDelete = null
            },
            title = { Text("Medewerker verwijderen") },
            text = { Text("Ben je zeker dat je deze medewerker wil verwijderen?") },
            confirmButton = {
                Button(
                    enabled = !isSubmitting,
                    onClick = {
                        isSubmitting = true
                        scope.launch {
                            when (val result = viewModel.deleteCrewMember(crewMember.id)) {
                                is ApiResult.Success -> {
                                    crewMemberToDelete = null
                                    isSubmitting = false
                                    feedbackDialogMessage = "Medewerker succesvol verwijderd."
                                }

                                is ApiResult.Error -> {
                                    crewMemberToDelete = null
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
                    onClick = { crewMemberToDelete = null },
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
private fun CrewMemberCard(
    crewMember: CrewMemberResponse,
    isLastAdmin: Boolean,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ){
                    Text(
                        text = crewMember.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CrewTextPrimary,
                    )
                    RoleBadge(role = crewMember.role)
                }

                if (isLastAdmin) {
                    Text(
                        text = "Laatste admin kan niet verwijderd worden",
                        style = MaterialTheme.typography.bodySmall,
                        color = CrewTextSecondary,
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Medewerker wijzigen",
                    tint = CrewPrimary,
                )
            }

            IconButton(
                onClick = onDelete,
                enabled = !isLastAdmin,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Medewerker verwijderen",
                    tint = if (isLastAdmin) {
                        CrewTextSecondary.copy(alpha = 0.45f)
                    } else {
                        Color(0xFFDC2626)
                    },
                )
            }
        }
    }
}

@Composable
private fun RoleBadge(role: CrewRole) {
    val isAdmin = role == CrewRole.ADMIN
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (isAdmin) CrewAccent.copy(alpha = 0.22f) else CrewBorder.copy(alpha = 0.35f),
        contentColor = if (isAdmin) CrewOnAccent else CrewTextSecondary,
    ) {
        Text(
            text = role.toDisplayName(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun EmptyCrewMembersCard(hasActiveSearch: Boolean) {
    val title = if (hasActiveSearch) {
        "Geen medewerkers gevonden"
    } else {
        "Nog geen medewerkers beschikbaar"
    }

    val description = if (hasActiveSearch) {
        "Pas je zoekterm of rolfilter aan om medewerkers terug te vinden."
    } else {
        "Voeg een eerste medewerker toe om toegang tot het crew-gedeelte te beheren."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CrewSurface,
        border = BorderStroke(1.dp, CrewBorder),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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

@Composable
private fun ManagementInfoCard(
    title: String,
    description: String,
    buttonLabel: String,
    onRetry: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = CrewSurface,
        border = BorderStroke(1.dp, CrewBorder),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CrewTextPrimary,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = CrewTextSecondary,
            )
            TextButton(onClick = onRetry) {
                Text(buttonLabel, color = CrewPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CrewMemberFormDialog(
    crewMember: CrewMemberResponse?,
    existingCrewMembers: List<CrewMemberResponse>,
    isLastAdmin: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (username: String, role: CrewRole, password: String?) -> Unit,
) {
    val isEdit = crewMember != null

    var username by remember(crewMember) { mutableStateOf(crewMember?.username.orEmpty()) }
    var role by remember(crewMember) { mutableStateOf(crewMember?.role ?: CrewRole.STAFF) }
    var password by remember(crewMember) { mutableStateOf("") }
    var passwordVisible by remember(crewMember) { mutableStateOf(false) }
    var passwordEditingEnabled by remember(crewMember) { mutableStateOf(!isEdit) }
    var showEnablePasswordDialog by remember(crewMember) { mutableStateOf(false) }

    var usernameError by remember(crewMember) { mutableStateOf<String?>(null) }
    var passwordError by remember(crewMember) { mutableStateOf<String?>(null) }
    var validationDialogMessage by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        usernameError = null
        passwordError = null

        val trimmedUsername = username.trim()
        if (trimmedUsername.isBlank()) {
            usernameError = "De naam van de medewerker is verplicht."
        } else {
            val duplicate = existingCrewMembers.any {
                it.id != crewMember?.id && it.username.equals(trimmedUsername, ignoreCase = true)
            }
            if (duplicate) {
                usernameError = "Er bestaat al een medewerker met deze naam."
            }
        }

        if (!isEdit || passwordEditingEnabled) {
            if (password.isBlank()) {
                passwordError = if (isEdit) {
                    "Geef een nieuw wachtwoord in."
                } else {
                    "Wachtwoord is verplicht."
                }
            } else if (password.length < 8) {
                passwordError = "Wachtwoord moet minstens 8 tekens bevatten."
            } else if (!password.any(Char::isLetter) || !password.any(Char::isDigit)) {
                passwordError = "Wachtwoord moet minstens één letter en één cijfer bevatten."
            }
        }

        if (isEdit && isLastAdmin && role != CrewRole.ADMIN) {
            validationDialogMessage = "De laatste administrator kan niet gewijzigd worden naar medewerker."
            return false
        }

        val hasError = usernameError != null || passwordError != null
        if (hasError) {
            validationDialogMessage = "Controleer de ingevulde velden en pas de foutmeldingen aan."
        }
        return !hasError
    }

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        title = {
            Text(if (isEdit) "Medewerker wijzigen" else "Medewerker toevoegen")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        if (usernameError != null) usernameError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    label = { Text("Naam") },
                    singleLine = true,
                    isError = usernameError != null,
                    supportingText = {
                        usernameError?.let { Text(it) }
                    },
                    colors = outlinedColors(),
                )

                RoleDropdownField(
                    selectedRole = role,
                    onRoleSelected = { role = it },
                    enabled = !isSubmitting,
                    helperText = if (isLastAdmin) {
                        "De laatste administrator moet admin blijven."
                    } else {
                        null
                    },
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (passwordError != null) passwordError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting && passwordEditingEnabled,
                    label = {
                        Text(if (isEdit) "Nieuw wachtwoord" else "Wachtwoord")
                    },
                    singleLine = true,
                    placeholder = {
                        Text(
                            if (isEdit && !passwordEditingEnabled) "Verborgen"
                            else "Minstens 8 tekens"
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = passwordError != null,
                    supportingText = {
                        when {
                            passwordError != null -> Text(passwordError!!)
                            isEdit && !passwordEditingEnabled -> Text("Klik op het potloodje om een nieuw wachtwoord in te stellen.")
                            else -> Text("Gebruik minstens één letter en één cijfer.")
                        }
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                enabled = !isSubmitting && passwordEditingEnabled,
                                onClick = { passwordVisible = !passwordVisible },
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Wachtwoord verbergen" else "Wachtwoord tonen",
                                )
                            }

                            if (isEdit) {
                                IconButton(
                                    enabled = !isSubmitting,
                                    onClick = {
                                        if (passwordEditingEnabled) {
                                            passwordEditingEnabled = false
                                            passwordVisible = false
                                            password = ""
                                            passwordError = null
                                        } else {
                                            showEnablePasswordDialog = true
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = if (passwordEditingEnabled) Icons.Default.Clear else Icons.Default.Edit,
                                        contentDescription = if (passwordEditingEnabled) "Wachtwoord niet wijzigen" else "Wachtwoord wijzigen",
                                    )
                                }
                            }
                        }
                    },
                    colors = outlinedColors(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        enabled = !isSubmitting && passwordEditingEnabled,
                        onClick = {
                            val generated = generatePassword()
                            password = generated
                            passwordVisible = true
                            passwordError = null
                        }
                    ) {
                        Text("Genereer wachtwoord", color = CrewPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    if (!validate()) return@Button
                    onSubmit(
                        username.trim(),
                        role,
                        when {
                            !isEdit -> password
                            passwordEditingEnabled -> password
                            else -> null
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrewAccent,
                    contentColor = CrewOnAccent,
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(18.dp),
                        strokeWidth = 2.dp,
                        color = CrewOnAccent,
                    )
                }
                Text(if (isEdit) "Opslaan" else "Toevoegen")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSubmitting, onClick = onDismiss) {
                Text("Annuleren", color = CrewTextSecondary)
            }
        },
    )

    if (showEnablePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showEnablePasswordDialog = false },
            title = { Text("Wachtwoord wijzigen") },
            text = { Text("Ben je zeker dat je het wachtwoord wil wijzigen?") },
            confirmButton = {
                Button(
                    onClick = {
                        showEnablePasswordDialog = false
                        passwordEditingEnabled = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrewAccent,
                        contentColor = CrewOnAccent,
                    )
                ) {
                    Text("Ja")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnablePasswordDialog = false }) {
                    Text("Annuleren", color = CrewTextSecondary)
                }
            }
        )
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
private fun RoleDropdownField(
    selectedRole: CrewRole,
    onRoleSelected: (CrewRole) -> Unit,
    enabled: Boolean,
    helperText: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Rol",
            style = MaterialTheme.typography.labelLarge,
            color = CrewTextPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        Box {
            OutlinedTextField(
                value = selectedRole.toDisplayName(),
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                readOnly = true,
                singleLine = true,
                colors = outlinedColors(),
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Transparent)
                    .padding(end = 52.dp)
            )

            TextButton(
                enabled = enabled,
                onClick = { expanded = true },
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Text("Kies", color = CrewPrimary)
            }

            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                CrewRole.entries.forEach { role ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(role.toDisplayName()) },
                        onClick = {
                            expanded = false
                            onRoleSelected(role)
                        }
                    )
                }
            }
        }

        helperText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = CrewTextSecondary,
            )
        }
    }
}

private fun CrewRole.toDisplayName(): String = when (this) {
    CrewRole.ADMIN -> "Admin"
    CrewRole.STAFF -> "Medewerker"
}

private fun generatePassword(length: Int = 8): String {
    val uppercase = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    val lowercase = "abcdefghijkmnopqrstuvwxyz"
    val digits = "23456789"
    val special = "!@#%&*-_"
    val all = uppercase + lowercase + digits + special

    val base = mutableListOf(
        uppercase.random(),
        lowercase.random(),
        digits.random(),
        special.random(),
    )

    repeat((length - base.size).coerceAtLeast(0)) {
        base += all.random()
    }

    return base.shuffled(Random.Default).joinToString("")
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