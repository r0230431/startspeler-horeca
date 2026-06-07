package com.startspeler.horeca.screens.crew.management

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startspeler.horeca.screens.crew.management.categories.CategoriesScreen
import com.startspeler.horeca.screens.crew.management.crew.CrewMembersScreen
import com.startspeler.horeca.screens.crew.management.discounts.DiscountsScreen
import com.startspeler.horeca.screens.crew.management.products.ProductsScreen
import com.startspeler.horeca.screens.crew.tables.TablesScreen
import com.startspeler.horeca.ui.theme.crew.*

enum class ManagementSection {
    OVERVIEW,
    CATEGORIES,
    PRODUCTS,
    TABLES,
    DISCOUNTS,
    CREW,
}

private data class ManagementFeature(
    val title: String,
    val description: String,
    val section: ManagementSection,
    val icon: ImageVector,
    val iconTint: Color,
)

@Composable
fun ManagementScreen(
    resetToOverviewToken: Int = 0,
) {
    var activeSection by androidx.compose.runtime.remember { mutableStateOf(ManagementSection.OVERVIEW) }

    LaunchedEffect(resetToOverviewToken) {
        activeSection = ManagementSection.OVERVIEW
    }

    when (activeSection) {
        ManagementSection.OVERVIEW -> ManagementOverviewSection(
            onOpenSection = { activeSection = it }
        )

        ManagementSection.TABLES -> TablesScreen(
            onBackToOverview = { activeSection = ManagementSection.OVERVIEW }
        )

        ManagementSection.CATEGORIES -> CategoriesScreen(
            onBackToOverview = { activeSection = ManagementSection.OVERVIEW },
        )

            ManagementSection.PRODUCTS -> ProductsScreen(
            onBackToOverview = { activeSection = ManagementSection.OVERVIEW }
        )

        ManagementSection.DISCOUNTS -> DiscountsScreen(
            onBackToOverview = { activeSection = ManagementSection.OVERVIEW }
        )

        ManagementSection.CREW -> CrewMembersScreen(
            onBackToOverview = { activeSection = ManagementSection.OVERVIEW }
        )
    }
}

@Composable
private fun ManagementOverviewSection(
    onOpenSection: (ManagementSection) -> Unit,
) {
    val features = listOf(
        ManagementFeature(
            title = "Categorieën",
            description = "Beheer de categorieën van de menukaart.",
            section = ManagementSection.CATEGORIES,
            icon = Icons.Outlined.Category,
            iconTint = Color(0xFF7C3AED),
        ),
        ManagementFeature(
            title = "Producten",
            description = "Voeg producten toe en beheer prijs, voorraad en status.",
            section = ManagementSection.PRODUCTS,
            icon = Icons.Outlined.Inventory2,
            iconTint = Color(0xFF2563EB),
        ),
        ManagementFeature(
            title = "Tafels",
            description = "Overzicht van tafels met CRUD en QR-codes.",
            section = ManagementSection.TABLES,
            icon = Icons.Outlined.TableBar,
            iconTint = Color(0xFF0F766E),
        ),
        ManagementFeature(
            title = "Kortingen",
            description = "Beheer vaste en procentuele kortingen.",
            section = ManagementSection.DISCOUNTS,
            icon = Icons.Outlined.LocalOffer,
            iconTint = Color(0xFFEA580C),
        ),
        ManagementFeature(
            title = "Medewerkers",
            description = "Beheer crewleden en rollen voor admins en medewerkers.",
            section = ManagementSection.CREW,
            icon = Icons.Outlined.ManageAccounts,
            iconTint = Color(0xFFBE185D),
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewBackground)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = 24.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "Beheer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CrewTextPrimary,
        )

        Text(
            text = "Alle beheersacties lopen via dit overzicht. Klantenbeheer blijft apart beschikbaar in voor zowel admins als medewerkers.",
            style = MaterialTheme.typography.bodyMedium,
            color = CrewTextSecondary,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            features.forEach { feature ->
                ManagementFeatureCard(
                    feature = feature,
                    onClick = { onOpenSection(feature.section) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ManagementFeatureCard(
    feature: ManagementFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CrewSurface),
        border = BorderStroke(1.dp, CrewBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = feature.iconTint.copy(alpha = 0.14f),
                contentColor = feature.iconTint,
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CrewTextPrimary,
                )

                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CrewTextSecondary,
                )
            }

        }
    }
}

@Composable
private fun StatusBadge(
    label: String,
    active: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (active) CrewAccent.copy(alpha = 0.22f) else CrewBorder.copy(alpha = 0.45f),
        contentColor = if (active) CrewOnAccent else CrewTextSecondary,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ManagementPlaceholderSection(
    title: String,
    description: String,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CrewBackground)
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 16.dp,
                bottom = 24.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = CrewAccent,
            )
            Text(
                text = "Terug naar beheer",
                modifier = Modifier.padding(start = 8.dp),
                color = CrewAccent,
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CrewTextPrimary,
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CrewSurface),
            border = BorderStroke(1.dp, CrewBorder),
        ) {
            Text(
                text = description,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = CrewTextSecondary,
            )
        }
    }
}
