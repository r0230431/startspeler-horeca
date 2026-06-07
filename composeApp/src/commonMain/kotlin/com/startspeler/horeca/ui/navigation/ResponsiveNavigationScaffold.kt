package com.startspeler.horeca.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.app.AppMode
import com.startspeler.horeca.auth.CrewRole
import com.startspeler.horeca.ui.navigation.crew.CrewCompactScaffold
import com.startspeler.horeca.ui.navigation.crew.CrewSidebar

@Composable
fun ResponsiveNavigationScaffold(
    appMode: AppMode,
    crewRole: CrewRole? = null,
    screenWidth: Dp,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onLogout: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val isCompact = screenWidth < 840.dp

    when (appMode) {
        AppMode.CUSTOMER -> {
            val items = customerNavigationItems

            if (isCompact) {
                Scaffold(
                    bottomBar = {
                        CustomerBottomNavigation(
                            items = items,
                            selectedIndex = selectedIndex,
                            onItemSelected = onItemSelected
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        content()
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    CrewSidebar(
                        title = "StartSpeler",
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        content()
                    }
                }
            }
        }

        AppMode.CREW -> {
            val role = crewRole ?: CrewRole.STAFF
            val items = visibleStaffNavigationItems(role)

            if (isCompact) {
                CrewCompactScaffold(
                    title = if (role == CrewRole.ADMIN) "Admin" else "Medewerker",
                    items = items,
                    selectedIndex = selectedIndex,
                    onItemSelected = onItemSelected,
                    content = content
                )
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    CrewSidebar(
                        title = if (role == CrewRole.ADMIN) "Admin" else "Medewerker",
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected,
                        onLogout = onLogout
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}