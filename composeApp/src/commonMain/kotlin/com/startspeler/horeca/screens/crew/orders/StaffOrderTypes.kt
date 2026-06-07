package com.startspeler.horeca.screens.crew.orders

import com.startspeler.horeca.data.models.products.ProductResponse

internal data class CartItemUi(
    val product: ProductResponse,
    val quantity: Int
)
