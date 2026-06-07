package com.startspeler.horeca.ui.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.ui.theme.crew.CrewBorder
import com.startspeler.horeca.ui.theme.crew.CrewOnPrimary
import com.startspeler.horeca.ui.theme.crew.CrewPrimary
import com.startspeler.horeca.ui.theme.crew.CrewSurface

@Composable
fun ScreenHeaderActions(
    hasActiveFilters: Boolean,
    onResetFilters: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onResetFilters() },
            enabled = hasActiveFilters,
            colors = ButtonDefaults.buttonColors(
                containerColor = CrewPrimary,
                contentColor = CrewOnPrimary
            )
        ) {
            Text("Filters wissen", color = CrewOnPrimary)
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = CrewSurface,
            border = BorderStroke(1.dp, CrewBorder)
        ) {
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(42.dp),

            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Vernieuwen",
                    tint = CrewPrimary
                )
            }
        }
    }
}