package com.startspeler.horeca.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.startspeler.horeca.auth.CrewRole

data class NavigationItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val allowedRoles: Set<CrewRole> = emptySet()
)