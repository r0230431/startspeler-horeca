package com.startspeler.horeca.ui.navigation.crew

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.navigation.NavigationItem
import com.startspeler.horeca.ui.theme.crew.CrewPrimaryDark
import com.startspeler.horeca.ui.theme.crew.CrewSidebarItemActive
import com.startspeler.horeca.ui.theme.crew.CrewSidebarItemDefault

@Composable
fun CrewNavigationItemsLayout(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, item ->
            val selected = selectedIndex == index

            NavigationRailItem(
                selected = selected,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = CrewSidebarItemActive,
                    selectedTextColor = CrewSidebarItemActive,
                    indicatorColor = CrewPrimaryDark,
                    unselectedIconColor = CrewSidebarItemDefault,
                    unselectedTextColor = CrewSidebarItemDefault
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            )
        }
    }
}