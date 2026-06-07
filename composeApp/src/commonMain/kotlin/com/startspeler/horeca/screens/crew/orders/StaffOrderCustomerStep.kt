package com.startspeler.horeca.screens.crew.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.customers.CustomerResponse
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewBorder
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextMuted
import com.startspeler.horeca.ui.theme.crew.CrewTextPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary
import com.startspeler.horeca.ui.theme.crew.ErrorRed
import com.startspeler.horeca.ui.theme.crew.SuccessGreen

@Composable
internal fun StaffOrderCustomerStep(
    customerQuery: String,
    onCustomerQueryChange: (String) -> Unit,
//    orderedByName: String,
//    onOrderedByNameChange: (String) -> Unit,
    customerError: String?,
    selectedCustomer: CustomerResponse?,
    filteredCustomers: List<CustomerResponse>,
    onSelectCustomer: (CustomerResponse) -> Unit,
    onShowCreateCustomer: () -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customerQuery,
                onValueChange = onCustomerQueryChange,
                modifier = Modifier.weight(1f),
                label = { Text("Zoek klant") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )
            Button(
                onClick = onShowCreateCustomer,
                colors = ButtonDefaults.buttonColors(containerColor = CrewAccent, contentColor = CrewOnAccent)
            ) {
                Icon(Icons.Default.PersonAdd, null)
                Spacer(Modifier.width(8.dp))
                Text("Nieuwe klant toevoegen")
            }
        }

//        OutlinedTextField(
//            value = orderedByName,
//            onValueChange = onOrderedByNameChange,
//            modifier = Modifier.fillMaxWidth(),
//            label = { Text("Naam op bestelling") },
//            singleLine = true,
//            isError = customerError != null,
//            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
//        )

        customerError?.let { Text(it, color = ErrorRed) }

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.elevatedCardColors(containerColor = CrewSurface)
        ) {
            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Geen klanten gevonden.", color = CrewTextMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        val isSelected = selectedCustomer?.id == customer.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectCustomer(customer) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CrewAccent else CrewSurface //.copy(alpha = 0.28f)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) CrewAccent else CrewBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(customer.username, fontWeight = FontWeight.SemiBold, color = CrewTextPrimary)
                                    Text("Klant #${customer.id}", color = CrewTextMuted)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CrewPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedCustomer?.let { customer ->
            Surface(
                color = SuccessGreen.copy(alpha = 0.18f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SuccessGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen)
                    Column {
                        Text("Geselecteerde klant", fontWeight = FontWeight.SemiBold, color = CrewTextPrimary)
                        Text(customer.username, color = CrewTextSecondary)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrewPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Volgende")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }
}
