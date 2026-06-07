package com.startspeler.horeca.data.models.orders

import com.startspeler.horeca.data.models.products.ProductResponse

data class CustomerCartItem(
    val product: ProductResponse,
    val quantity: Int,
) {
    val unitPrice: Double = product.price.toDoubleOrNull() ?: 0.0
    val subtotal: Double = unitPrice * quantity
}