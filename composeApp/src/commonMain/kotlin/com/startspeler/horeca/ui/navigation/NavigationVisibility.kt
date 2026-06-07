package com.startspeler.horeca.ui.navigation

import com.startspeler.horeca.auth.CrewRole
import com.startspeler.horeca.navigation.NavigationItem
import com.startspeler.horeca.navigation.crew.crewNavigationItems

fun visibleStaffNavigationItems(role: CrewRole): List<NavigationItem> {
    return crewNavigationItems.filter { role in it.allowedRoles }
}

fun canAccessScreen(role: CrewRole, screenKey: String): Boolean {
    return when (screenKey) {
        "home", "orders", "customers", "payments", "cash_register", "inventory", "overviews" -> true
        "manage" -> role == CrewRole.ADMIN
        else -> false
    }
}