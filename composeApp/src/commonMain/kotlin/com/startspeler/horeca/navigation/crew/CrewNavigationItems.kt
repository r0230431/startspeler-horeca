package com.startspeler.horeca.navigation.crew

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Settings
import com.startspeler.horeca.auth.CrewRole
import com.startspeler.horeca.navigation.NavigationItem

val crewNavigationItems = listOf(
    NavigationItem(
        key = "home",
        label = "Home",
        icon = Icons.Outlined.Home,
        allowedRoles = setOf(CrewRole.ADMIN, CrewRole.STAFF)
    ),
    NavigationItem(
        key = "orders",
        label = "Bestellen",
        icon = Icons.Outlined.ShoppingCart,
        allowedRoles = setOf(CrewRole.ADMIN, CrewRole.STAFF)
    ),
    NavigationItem(
        key = "payments",
        label = "Betalen",
        icon = Icons.Outlined.Payments,
        allowedRoles = setOf(CrewRole.ADMIN, CrewRole.STAFF)
    ),
    NavigationItem(
        key = "cash_register",
        label = "Kassa",
        icon = Icons.Outlined.PointOfSale,
        allowedRoles = setOf(CrewRole.ADMIN, CrewRole.STAFF)
    ),
    NavigationItem(
        key = "overviews",
        label = "Overzichten",
        icon = Icons.Outlined.Assessment,
        allowedRoles = setOf(CrewRole.ADMIN, CrewRole.STAFF)
    ),
    NavigationItem(
        key = "customers",
        label = "Klanten",
        icon = Icons.Outlined.People,
        allowedRoles = setOf(CrewRole.ADMIN, CrewRole.STAFF)
    ),
    NavigationItem(
        key = "inventory",
        label = "Voorraadbeheer",
        icon = Icons.Outlined.Inventory2,
        allowedRoles = setOf(CrewRole.ADMIN, CrewRole.STAFF)
    ),
    NavigationItem(
        key = "manage",
        label = "Beheer",
        icon = Icons.Outlined.Settings,
        allowedRoles = setOf(CrewRole.ADMIN)
    )
)