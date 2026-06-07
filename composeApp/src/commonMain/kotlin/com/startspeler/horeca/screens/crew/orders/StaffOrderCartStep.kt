package com.startspeler.horeca.screens.crew.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.tables.TableResponse
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewSurface
import com.startspeler.horeca.ui.theme.crew.CrewTextMuted
import com.startspeler.horeca.ui.theme.crew.ErrorRed

@Composable
internal fun StaffOrderCartStep(
    cartError: String?,
    orderedByName: String,
    selectedTable: TableResponse?,
    cart: List<CartItemUi>,
    note: String,
    onNoteChange: (String) -> Unit,
    total: Double,
    onDecrease: (CartItemUi) -> Unit,
    onIncrease: (CartItemUi) -> Unit,
    onDelete: (CartItemUi) -> Unit,
    onBack: () -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        cartError?.let { Text(it, color = ErrorRed) }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = CrewSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoBadge(title = "Klant", value = orderedByName, icon = Icons.Default.Person)
                InfoBadge(
                    title = "Tafel",
                    value = selectedTable?.tableNumber?.let { "Tafel $it" } ?: "-",
                    icon = Icons.Default.TableRestaurant
                )
            }
        }

        if (cart.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Winkelmandje is leeg.", color = CrewTextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cart, key = { it.product.id }) { item ->
                    CartRow(
                        item = item,
                        onDecrease = { onDecrease(item) },
                        onIncrease = { onIncrease(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Opmerking") },
            minLines = 1
        )

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = CrewAccent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Totaal",
                    fontWeight = FontWeight.Bold,
                    color = CrewOnAccent,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    formatEuro(total),
                    fontWeight = FontWeight.ExtraBold,
                    color = CrewOnAccent,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                Spacer(Modifier.width(8.dp))
                Text("Terug")
            }
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = CrewAccent, contentColor = CrewOnAccent)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.width(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Bezig...")
                } else {
                    Text("Bestelling bevestigen")
                }
            }
        }
    }
}
