package com.startspeler.horeca.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.startspeler.horeca.util.resolveApiUrl
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import com.startspeler.horeca.customer.CustomerFlowState
import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.ui.theme.customer.CustomerBackground
import com.startspeler.horeca.ui.theme.customer.CustomerBorder
import com.startspeler.horeca.ui.theme.customer.CustomerPrimary
import com.startspeler.horeca.ui.theme.customer.CustomerSurface
import com.startspeler.horeca.ui.theme.customer.CustomerTextPrimary
import com.startspeler.horeca.ui.theme.customer.CustomerTextSecondary

@Composable
fun CustomerMenuScreen(
    state: CustomerFlowState,
    contentPadding: PaddingValues = PaddingValues(),
    onCategorySelected: (Int?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onProductSelected: (ProductResponse) -> Unit,
    onAddToCart: (ProductResponse, Int) -> Unit,
    onOpenCart: () -> Unit,
    onRetryLoad: () -> Unit,
    onCloseProduct: () -> Unit,
) {
    val session = state.session ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CustomerBackground)
            .padding(bottom = contentPadding.calculateBottomPadding())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Tafel",
                            fontSize = 12.sp,
                            color = CustomerTextSecondary,
                        )
                        Text(
                            text = session.tableLabel,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CustomerTextPrimary,
                        )
                    }
                }

                Text(
                    text = session.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = CustomerTextPrimary,
                )
            }

            if (state.isSearchActive) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    placeholder = { Text("Zoek tussen dranken en snacks...", color = CustomerTextSecondary) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = CustomerTextPrimary, fontSize = 16.sp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = onClearSearch) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = CustomerTextPrimary)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CustomerPrimary,
                        unfocusedBorderColor = CustomerBorder,
                        focusedContainerColor = CustomerSurface,
                        unfocusedContainerColor = CustomerSurface,
                        focusedTextColor = CustomerTextPrimary,
                        unfocusedTextColor = CustomerTextPrimary,
                        cursorColor = CustomerPrimary,
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            } else {
                Text(
                    text = "Choose your drink",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Normal,
                    color = CustomerTextPrimary,
                    lineHeight = 44.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.padding(start = 24.dp, bottom = 24.dp)
                )
            }

            Row(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .width(92.dp)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item {
                        CategoryChip(
                            text = "Alles",
                            selected = state.selectedCategoryId == null,
                            icon = Icons.Default.LocalBar,
                            onClick = { onCategorySelected(null) },
                        )
                    }
                    items(state.categories, key = { it.id }) { category ->
                        CategoryChip(
                            text = category.name,
                            selected = state.selectedCategoryId == category.id,
                            icon = categoryIcon(category),
                            onClick = { onCategorySelected(category.id) },
                        )
                    }
                }

                when {
                    state.isCatalogLoading && state.products.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CustomerPrimary)
                        }
                    }
                    state.catalogError != null && state.products.isEmpty() -> {
                        ErrorState(
                            message = state.catalogError,
                            onRetry = onRetryLoad,
                        )
                    }
                    state.filteredProducts.isEmpty() -> {
                        EmptyState()
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 24.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(state.filteredProducts, key = { it.id }) { product ->
                                ProductCard(
                                    product = product,
                                    category = state.categories.firstOrNull { it.id == product.categoryId },
                                    onQuickAdd = { onAddToCart(product, 1) },
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val containerColor = if (selected) CustomerPrimary else CustomerSurface
    val contentColor = if (selected) CustomerBackground else CustomerTextSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(
                color = containerColor,
                shape = RoundedCornerShape(topEnd = 26.dp, bottomEnd = 26.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor,
                maxLines = 2,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductResponse,
    category: CategoryResponse?,
    onQuickAdd: () -> Unit,
) {
    val soldOut = product.stock <= 0
    val lowStock = product.stock in 1..4
    val resolvedImageUrl = resolveApiUrl(product.imageUrl)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CustomerSurface),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    categoryColor(category).copy(alpha = 0.70f),
                                    CustomerSurface,
                                )
                            )
                        )
                )

                if (resolvedImageUrl != null) {
                    KamelImage(
                        resource = asyncPainterResource(data = resolvedImageUrl),
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = CustomerPrimary,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        onFailure = {
                            Icon(
                                imageVector = categoryIcon(category),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.24f),
                                modifier = Modifier
                                    .size(92.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = categoryIcon(category),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.24f),
                        modifier = Modifier
                            .size(92.dp)
                            .align(Alignment.Center)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.10f))
                )

                if (soldOut) {
                    StockBadge(
                        label = "SOLD OUT",
                        color = Color(0xFFE35D6A),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                    )
                } else if (lowStock) {
                    StockBadge(
                        label = "NOG ${product.stock} BESCHIKBAAR",
                        color = Color(0xFFFFB74D),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(14.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = product.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = CustomerTextPrimary,
                    fontFamily = FontFamily.Serif,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                product.description?.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        color = CustomerTextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = formatPrice(product.price),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CustomerPrimary,
                        )
                    }

                    IconButton(
                        onClick = onQuickAdd,
                        enabled = !soldOut,
                        modifier = Modifier
                            .background(
                                color = if (soldOut) CustomerBorder else CustomerPrimary,
                                shape = CircleShape,
                            )
                            .size(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = if (soldOut) CustomerTextSecondary else Color.Black,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StockBadge(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun ErrorState(
    message: String?,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = message ?: "Er ging iets mis bij het laden van de menukaart.",
                color = CustomerTextPrimary,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRetry) {
                Text("Opnieuw proberen")
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Geen producten gevonden.",
            color = CustomerTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

private fun categoryIcon(category: CategoryResponse?): ImageVector {
    return when (category?.name?.lowercase()) {
        null -> Icons.Default.LocalBar
        "bier", "bieren" -> Icons.Default.LocalBar
        "frisdrank", "frisdranken", "energiedrank", "energiedranken" -> Icons.Default.LocalDrink
        "koffie", "warme dranken", "koffie en thee" -> Icons.Default.Coffee
        "water" -> Icons.Default.WaterDrop
        "snacks" -> Icons.Default.Fastfood
        else -> Icons.Default.LocalBar
    }
}

private fun categoryColor(category: CategoryResponse?): Color {
    return when (category?.name?.lowercase()) {
        "bier", "bieren" -> Color(0xFF855E42)
        "frisdrank", "frisdranken", "energiedrank", "energiedranken" -> Color(0xFF295A8A)
        "koffie", "warme dranken", "koffie en thee" -> Color(0xFF6A4B3C)
        "water" -> Color(0xFF3B7EA1)
        "snacks" -> Color(0xFF7A5F2D)
        else -> Color(0xFF3E2F55)
    }
}

private fun formatPrice(price: String): String {
    val value = price.toDoubleOrNull() ?: return "€ $price"
    val cents = (value * 100).toInt()
    val whole = cents / 100
    val decimals = (cents % 100).toString().padStart(2, '0')
    return "€ ${whole}.${decimals}"
}
