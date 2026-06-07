package com.startspeler.horeca.screens.customer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.startspeler.horeca.ui.theme.AccentGold
import com.startspeler.horeca.ui.theme.customer.CustomerBackground
import com.startspeler.horeca.ui.theme.customer.CustomerPrimary
import com.startspeler.horeca.ui.theme.customer.CustomerSurface
import com.startspeler.horeca.ui.theme.customer.CustomerTextPrimary
import com.startspeler.horeca.ui.theme.customer.CustomerTextSecondary

enum class CustomerBottomDestination {
    MENU,
    SEARCH,
    CART,
}

@Composable
fun CustomerBottomBar(
    selectedDestination: CustomerBottomDestination,
    cartCount: Int,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp),
        color = CustomerSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomerBottomBarItem(
                icon = Icons.Default.LocalBar,
                label = "Menu",
                selected = selectedDestination == CustomerBottomDestination.MENU,
                onClick = onMenuClick,
            )

            CustomerBottomBarItem(
                icon = Icons.Default.Search,
                label = "Zoek",
                selected = selectedDestination == CustomerBottomDestination.SEARCH,
                onClick = onSearchClick,
            )

            CustomerBottomBarItem(
                icon = Icons.Default.ShoppingBag,
                label = "Mandje",
                selected = selectedDestination == CustomerBottomDestination.CART,
                badgeCount = cartCount,
                onClick = onCartClick,
            )
        }
    }
}

@Composable
private fun CustomerBottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0,
) {
    val contentColor = if (selected) CustomerPrimary else CustomerTextSecondary

    Box(contentAlignment = Alignment.TopEnd) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (selected) CustomerBackground.copy(alpha = 0.45f) else Color.Transparent)
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                color = if (selected) CustomerTextPrimary else CustomerTextSecondary,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
        }

        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-2).dp)
                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    .clip(CircleShape)
                    .background(AccentGold)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
