package com.startspeler.horeca.screens.crew.tables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.tables.TableResponse
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewBorder
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary

@Composable
fun TableFormDialog(
    table: TableResponse? = null,
    isSubmitting: Boolean,
    backendError: String?,
    onDismiss: () -> Unit,
    onSubmit: (tableNumber: Int, seatCount: Int, note: String?) -> Unit,
) {
    var tableNumber by remember(table) { mutableStateOf(table?.tableNumber?.toString().orEmpty()) }
    var seatCount by remember(table) { mutableStateOf(table?.seatCount?.toString().orEmpty()) }
    var note by remember(table) { mutableStateOf(table?.note.orEmpty()) }

    var tableNumberTouched by remember { mutableStateOf(false) }
    var seatCountTouched by remember { mutableStateOf(false) }

    val duplicateTableNumberError =
        if (backendError.isDuplicateTableNumberError()) {
            "Er bestaat al een tafel met dit tafelnummer."
        } else {
            null
        }

    val tableNumberError = when {
        tableNumber.isBlank() -> "Tafelnummer is verplicht."
        tableNumber.toIntOrNull() == null -> "Voer een geldig nummer in."
        (tableNumber.toIntOrNull() ?: 0) <= 0 -> "Tafelnummer moet groter zijn dan 0."
        else -> null
    }

    val seatCountError = when {
        seatCount.isBlank() -> "Aantal zitplaatsen is verplicht."
        seatCount.toIntOrNull() == null -> "Voer een geldig aantal in."
        (seatCount.toIntOrNull() ?: 0) <= 0 -> "Aantal zitplaatsen moet groter zijn dan 0."
        else -> null
    }

    val tableNumberUiError =
        if (tableNumberTouched) tableNumberError ?: duplicateTableNumberError
        else duplicateTableNumberError

    val generalBackendError =
        if (duplicateTableNumberError == null) backendError.toUserFriendlyTableError()
        else null

    AlertDialog(
        onDismissRequest = {
            if (!isSubmitting) onDismiss()
        },
        title = {
            Text(if (table == null) "Tafel toevoegen" else "Tafel wijzigen")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = tableNumber,
                    onValueChange = {
                        tableNumber = it.filter(Char::isDigit)
                        tableNumberTouched = true
                    },
                    label = { Text("Tafelnummer") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = tableNumberUiError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedColors()
                )

                if (tableNumberUiError != null) {
                    Text(
                        text = tableNumberUiError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = seatCount,
                    onValueChange = {
                        seatCount = it.filter(Char::isDigit)
                        seatCountTouched = true
                    },
                    label = { Text("Aantal zitplaatsen") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = seatCountTouched && seatCountError != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedColors()
                )

                if (seatCountTouched && seatCountError != null) {
                    Text(
                        text = seatCountError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Opmerking (optioneel)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = outlinedColors()
                )

                if (!generalBackendError.isNullOrBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(18.dp)
                            )

                            Text(
                                text = generalBackendError,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting && tableNumberError == null && seatCountError == null,
                onClick = {
                    onSubmit(
                        tableNumber.toInt(),
                        seatCount.toInt(),
                        note.ifBlank { null }
                    )
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
                Text(if (table == null) "Toevoegen" else "Opslaan")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSubmitting, onClick = onDismiss) {
                Text("Annuleren", color = CrewTextSecondary)
            }
        }
    )
}

private fun String?.isDuplicateTableNumberError(): Boolean {
    val value = this?.lowercase().orEmpty()
    return value.contains("duplicate") ||
            value.contains("already exists") ||
            value.contains("exists") ||
            value.contains("unique") ||
            value.contains("tafel bestaat al") ||
            value.contains("table already exists")
}

private fun String?.toUserFriendlyTableError(): String? {
    val value = this?.trim().orEmpty()
    if (value.isBlank()) return null

    val lower = value.lowercase()

    return when {
        lower.contains("duplicate") || lower.contains("bestaat al") || lower.contains("already exist") || lower.contains("unique") ->
            "Deze tafel bestaat al."

        lower.contains("timeout") ->
            "De server reageert niet op tijd. Probeer opnieuw."

        lower.contains("network") || lower.contains("connect") ->
            "Verbinding met de server mislukt. Controleer je netwerk en probeer opnieuw."

        else ->
            "Er ging iets mis bij het opslaan van de tafel."
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(    focusedContainerColor = CrewSurface,
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