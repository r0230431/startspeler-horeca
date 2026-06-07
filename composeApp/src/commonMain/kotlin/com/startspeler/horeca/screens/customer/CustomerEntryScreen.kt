package com.startspeler.horeca.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startspeler.horeca.ui.theme.customer.CustomerBackground
import com.startspeler.horeca.ui.theme.customer.CustomerBorder
import com.startspeler.horeca.ui.theme.customer.CustomerError
import com.startspeler.horeca.ui.theme.customer.CustomerPrimary
import com.startspeler.horeca.ui.theme.customer.CustomerSurface
import com.startspeler.horeca.ui.theme.customer.CustomerTextPrimary
import com.startspeler.horeca.ui.theme.customer.CustomerTextSecondary
import horeca.composeapp.generated.resources.Res
import horeca.composeapp.generated.resources.logoweb
import org.jetbrains.compose.resources.painterResource

@Composable
fun CustomerEntryScreen(
    isSubmitting: Boolean,
    backendError: String?,
    prefilledTableNumber: String? = null,
    prefilledCustomerName: String? = null,
    onStartOrder: (tableNumber: String, customerName: String) -> Unit,
    onCrewLoginClick: () -> Unit,
) {
    val navigationScrollState = rememberScrollState()

    val hasPrefilledTable = !prefilledTableNumber.isNullOrBlank()
    val hasPrefilledName = !prefilledCustomerName.isNullOrBlank()

    var tableNumber by rememberSaveable(prefilledTableNumber) {
        mutableStateOf(prefilledTableNumber.orEmpty())
    }
    var customerName by rememberSaveable(prefilledCustomerName) { mutableStateOf(prefilledCustomerName.orEmpty()) }
    var showValidation by rememberSaveable { mutableStateOf(false) }

    val tableError = when {
        !showValidation -> null
        tableNumber.isBlank() -> "Tafelnummer is verplicht"
        tableNumber.toIntOrNull() == null -> "Tafelnummer moet numeriek zijn"
        else -> null
    }

    val nameError = when {
        hasPrefilledName -> null
        !showValidation -> null
        customerName.isBlank() -> "Naam is verplicht"
        else -> null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CustomerBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp)
                .verticalScroll(navigationScrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.logoweb),
                contentDescription = "StartSpeler Logo",
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(0.4f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welkom bij StartSpeler",
                color = CustomerTextPrimary,
                fontSize = 42.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    hasPrefilledName && hasPrefilledTable -> "Welkom terug, ${prefilledCustomerName}!"
                    hasPrefilledName -> "Welkom terug, ${prefilledCustomerName}! Geef je tafelnummer in."
                    hasPrefilledTable -> "Geef je naam in om te beginnen met bestellen."
                    else -> "Geef je tafelnummer en naam in om te beginnen met bestellen."
                },
                color = CustomerTextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (!hasPrefilledTable) {
                CustomerOutlinedField(
                    value = tableNumber,
                    onValueChange = {
                        tableNumber = it.filter(Char::isDigit)
                    },
                    placeholder = "Tafelnummer",
                    leadingIcon = Icons.Default.Place,
                    error = tableError,
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (hasPrefilledName) {
                // Naam opgeslagen - toon als niet-aanpasbaar label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CustomerSurface, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CustomerTextSecondary,
                    )
                    Text(
                        text = prefilledCustomerName.orEmpty(),
                        color = CustomerTextPrimary,
                        fontSize = 16.sp,
                    )
                }
            } else {
                CustomerOutlinedField(
                    value = customerName,
                    onValueChange = { if (it.length <= 50) customerName = it },
                    placeholder = "Jouw naam",
                    leadingIcon = Icons.Default.Person,
                    error = nameError,
                )
            }

            AnimatedVisibility(
                visible = !backendError.isNullOrBlank(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = backendError.orEmpty(),
                    color = CustomerError,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    showValidation = true
                    if (tableError == null && nameError == null) {
                        onStartOrder(tableNumber.trim(), customerName.trim())
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CustomerPrimary,
                    contentColor = Color.Black
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Bestellen", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CustomerOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    error: String?,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, color = CustomerTextSecondary)
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (error != null) CustomerError else CustomerPrimary,
            unfocusedBorderColor = if (error != null) CustomerError else CustomerBorder,
            focusedTextColor = CustomerTextPrimary,
            unfocusedTextColor = CustomerTextPrimary,
            cursorColor = CustomerPrimary,
            focusedContainerColor = CustomerSurface,
            unfocusedContainerColor = CustomerSurface,
        ),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = CustomerTextSecondary
            )
        },
        isError = error != null,
        supportingText = error?.let { { Text(it, color = CustomerError, fontSize = 13.sp) } }
    )
}
