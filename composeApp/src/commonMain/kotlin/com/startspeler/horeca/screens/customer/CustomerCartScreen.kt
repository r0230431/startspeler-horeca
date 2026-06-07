package com.startspeler.horeca.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startspeler.horeca.data.models.orders.CustomerCartItem
import com.startspeler.horeca.customer.CustomerFlowState
import com.startspeler.horeca.ui.theme.customer.CustomerBackground
import com.startspeler.horeca.ui.theme.customer.CustomerBorder
import com.startspeler.horeca.ui.theme.customer.CustomerPrimary
import com.startspeler.horeca.ui.theme.customer.CustomerSurface
import com.startspeler.horeca.ui.theme.customer.CustomerTextPrimary
import com.startspeler.horeca.ui.theme.customer.CustomerTextSecondary

@Composable
fun CustomerCartScreen(
    state: CustomerFlowState,
    contentPadding: PaddingValues = PaddingValues(),
    onNoteChange: (String) -> Unit,
    onIncrement: (productId: Int, currentQuantity: Int) -> Unit,
    onDecrement: (productId: Int, currentQuantity: Int) -> Unit,
    onClearCart: () -> Unit,
    onPlaceOrder: () -> Unit,
) {
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                if (state.cartItems.isNotEmpty()) {
                    TextButton(onClick = onClearCart) {
                        Text("Leegmaken", color = Color(0xFFEF5350), fontSize = 14.sp)
                    }
                }
            }

            Text(
                text = "Check out",
                fontSize = 42.sp,
                fontWeight = FontWeight.Normal,
                color = CustomerTextPrimary,
                lineHeight = 44.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.cartItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = CustomerTextSecondary,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Je winkelmandje is leeg",
                            fontSize = 18.sp,
                            color = CustomerTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(state.cartItems, key = { it.product.id }) { item ->
                        CartItemCard(
                            item = item,
                            onIncrement = { onIncrement(item.product.id, item.quantity) },
                            onDecrement = { onDecrement(item.product.id, item.quantity) }
                        )
                    }

                    item {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Extra toevoegingen",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CustomerTextPrimary,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.note,
                            onValueChange = { if (it.length <= 300) onNoteChange(it) },
                            label = { Text("Opmerking bij je bestelling (max 300 tekens)", color = CustomerTextSecondary) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Comment,
                                    contentDescription = null,
                                    tint = CustomerPrimary.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CustomerPrimary,
                                unfocusedBorderColor = CustomerBorder,
                                focusedContainerColor = CustomerSurface,
                                unfocusedContainerColor = CustomerSurface,
                                focusedTextColor = CustomerTextPrimary,
                                unfocusedTextColor = CustomerTextPrimary,
                                cursorColor = CustomerPrimary
                            )
                        )
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }

                Surface(
                    color = CustomerBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Totaal (${state.totalItems} items)", color = CustomerTextSecondary, fontSize = 16.sp)
                            Text(
                                formatCartPrice(state.totalPrice),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = CustomerPrimary
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = onPlaceOrder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !state.isPlacingOrder && state.cartItems.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomerPrimary,
                                contentColor = Color.Black,
                                disabledContainerColor = CustomerPrimary.copy(alpha = 0.4f),
                                disabledContentColor = Color.Black.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            if (state.isPlacingOrder) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    "Bestelling plaatsen",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
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
private fun CartItemCard(
    item: CustomerCartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Surface(
        color = CustomerSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(CustomerBackground, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.quantity.toString(),
                    color = CustomerPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Spacer(Modifier.size(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CustomerTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${formatCartPrice(item.unitPrice)} per stuk",
                    fontSize = 14.sp,
                    color = CustomerTextSecondary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = formatCartPrice(item.subtotal),
                    fontSize = 15.sp,
                    color = CustomerPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(CustomerBackground, RoundedCornerShape(20.dp))
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                    Icon(
                        if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                        contentDescription = null,
                        tint = if (item.quantity == 1) Color(0xFFEF5350) else CustomerTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = item.quantity.toString(),
                    color = CustomerTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = CustomerTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun formatCartPrice(price: Double): String {
    val cents = (price * 100).toInt()
    val whole = cents / 100
    val decimals = (cents % 100).toString().padStart(2, '0')
    return "€ ${whole}.${decimals}"
}
