package com.startspeler.horeca.ui.navigation.crew

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import horeca.composeapp.generated.resources.logoweb
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import com.startspeler.horeca.navigation.NavigationItem
import com.startspeler.horeca.ui.theme.crew.CrewPrimaryDark
import com.startspeler.horeca.ui.theme.crew.CrewSidebarBackground
import com.startspeler.horeca.ui.theme.crew.CrewSidebarBorder
import com.startspeler.horeca.ui.theme.crew.CrewSidebarItemDefault
import com.startspeler.horeca.ui.theme.crew.CrewSidebarItemMuted
import horeca.composeapp.generated.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrewCompactScaffold(
    title: String,
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(186.dp)
                    .background(CrewSidebarBackground)
                    .border(1.dp, CrewSidebarBorder),
                drawerContainerColor = CrewSidebarBackground
            ) {
                CrewDrawerContent(
                    title = title,
                    items = items,
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        onItemSelected(index)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = CrewSidebarBackground,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CrewSidebarBackground,
                        titleContentColor = CrewSidebarItemMuted,
                        navigationIconContentColor = CrewSidebarItemDefault
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.logoweb),
                                contentDescription = "StartSpeler logo",
                                modifier = Modifier.height(34.dp)
                            )

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                color = CrewSidebarItemMuted
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = "Open menu"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CrewPrimaryDark)
                    .padding(innerPadding)
            ) {
                content()
            }
        }
    }
}