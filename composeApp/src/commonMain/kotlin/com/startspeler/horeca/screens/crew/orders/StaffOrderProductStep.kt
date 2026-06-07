package com.startspeler.horeca.screens.crew.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.ui.theme.crew.CrewAccent
import com.startspeler.horeca.ui.theme.crew.CrewOnAccent
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewTextMuted
import com.startspeler.horeca.ui.theme.crew.CrewTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StaffOrderProductStep(
    productQuery: String,
    onProductQueryChange: (String) -> Unit,
    onClearProductQuery: () -> Unit,
    selectedCategoryId: Int?,
    categories: List<CategoryResponse>,
    onSelectCategory: (Int?) -> Unit,
    filteredProducts: List<ProductResponse>,
    currentQuantity: (Int) -> Int,
    onAddToCart: (ProductResponse) -> Unit,
    onRemoveFromCart: (ProductResponse) -> Unit,
    cartQuantity: Int,
    onBack: () -> Unit,
    onNext: () -> Unit,
    showProductError: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = productQuery,
            onValueChange = onProductQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Zoek product") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (productQuery.isNotBlank()) {
                    IconButton(onClick = onClearProductQuery) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            singleLine = true
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onSelectCategory(null) },
                label = { Text("Alle") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CrewPrimary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = CrewAccent.copy(alpha = 0.5f),
                    labelColor = CrewOnAccent
                )
            )
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategoryId == category.id,
                    onClick = { onSelectCategory(category.id) },
                    label = { Text(category.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CrewPrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = CrewAccent.copy(alpha = 0.5f),
                        labelColor = CrewOnAccent
                    )
                )
            }
        }

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Geen producten gevonden.", color = CrewTextMuted)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(165.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductTile(
                        product = product,
                        quantity = currentQuantity(product.id),
                        onAdd = { onAddToCart(product) },
                        onRemove = { onRemoveFromCart(product) }
                    )
                }
            }
        }

        if (showProductError) {
            Text(
                "Gelieve minstens 1 product toe te voegen om te kunnen verdergaan.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                Spacer(Modifier.width(8.dp))
                Text("Terug")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = CrewAccent)
                Spacer(Modifier.width(6.dp))
                Text(
                    "$cartQuantity product(en)",
                    color = CrewTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrewPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Winkelmandje")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
            }
        }
    }
}
