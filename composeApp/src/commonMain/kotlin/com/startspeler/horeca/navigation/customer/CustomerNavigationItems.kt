package com.startspeler.horeca.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Menu
import com.startspeler.horeca.navigation.NavigationItem

val customerNavigationItems = listOf(
    NavigationItem(
        key = "menu",
        label = "Menu",
        icon = Icons.Outlined.Menu
    ),
    NavigationItem(
        key = "cart",
        label = "Cart",
        icon = Icons.Outlined.ShoppingCart
    )
)

