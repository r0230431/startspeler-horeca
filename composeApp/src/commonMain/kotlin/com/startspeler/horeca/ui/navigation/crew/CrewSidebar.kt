package com.startspeler.horeca.ui.navigation.crew

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.navigation.NavigationItem
import com.startspeler.horeca.ui.theme.crew.*
import horeca.composeapp.generated.resources.Res
import horeca.composeapp.generated.resources.logoweb
import org.jetbrains.compose.resources.painterResource
import com.startspeler.horeca.ui.components.LogoutButton

@Composable
fun CrewSidebar(
    title: String,
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    onLogout: () -> Unit = { }
) {
    val navigationScrollState = rememberScrollState()

    Surface(
        modifier = Modifier
            .width(186.dp)
            .fillMaxHeight()
            .border(
                width = 1.dp,
                color = CrewSidebarBorder,
                //shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
            ),
        color = CrewSidebarBackground,
        //shape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .background(CrewSidebarBackground)
                .padding(horizontal = 14.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.logoweb),
                contentDescription = "Logo",
                modifier = Modifier.width(186.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = CrewSidebarItemMuted,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            CrewNavigationItemsLayout(
                items = items,
                selectedIndex = selectedIndex,
                onItemSelected = onItemSelected,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(navigationScrollState)
            )

            LogoutButton(onLogout)
        }
    }
}