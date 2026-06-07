package com.startspeler.horeca.screens.customer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.startspeler.horeca.customer.CustomerFlowController
import com.startspeler.horeca.customer.CustomerSession
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.screens.customer.components.CustomerBottomBar
import com.startspeler.horeca.screens.customer.components.CustomerBottomDestination
import com.startspeler.horeca.screens.customer.components.LuxuryDialog
import com.startspeler.horeca.ui.theme.customer.CustomerBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private enum class CustomerDestination {
    MENU,
    CART,
}

@Composable
fun CustomerFlowScreen(
    session: CustomerSession,
    onExitCustomerFlow: () -> Unit,
) {
    val controller = remember { CustomerFlowController() }
    val scope = rememberCoroutineScope()
    val state by controller.state.collectAsState()
    val destination = remember { androidx.compose.runtime.mutableStateOf(CustomerDestination.MENU) }

    androidx.compose.runtime.LaunchedEffect(session.customer.id, session.table.id) {
        controller.initializeSession(session)
        controller.loadCatalog()
    }

    // Elke 30 seconden stil de producten vernieuwen zodat voorraad up-to-date is
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (isActive) {
            delay(30_000L)
            controller.refreshCatalogSilently()
        }
    }

    val selectedBottomDestination = when {
        destination.value == CustomerDestination.CART -> CustomerBottomDestination.CART
        state.isSearchActive -> CustomerBottomDestination.SEARCH
        else -> CustomerBottomDestination.MENU
    }

    Scaffold(
        containerColor = CustomerBackground,
        bottomBar = {
            CustomerBottomBar(
                selectedDestination = selectedBottomDestination,
                cartCount = state.totalItems,
                onMenuClick = {
                    destination.value = CustomerDestination.MENU
                    controller.closeProduct()
                    controller.deactivateSearch()
                },
                onSearchClick = {
                    destination.value = CustomerDestination.MENU
                    controller.closeProduct()
                    controller.activateSearch()
                },
                onCartClick = {
                    controller.closeProduct()
                    destination.value = CustomerDestination.CART
                },
            )
        }
    ) { innerPadding ->
        when (destination.value) {
            CustomerDestination.MENU -> {
                CustomerMenuScreen(
                    state = state,
                    contentPadding = innerPadding,
                    onCategorySelected = controller::selectCategory,
                    onSearchQueryChange = controller::updateSearchQuery,
                    onClearSearch = controller::clearSearchAndFilters,
                    onProductSelected = controller::openProduct,
                    onAddToCart = controller::addToCart,
                    onOpenCart = {
                        controller.closeProduct()
                        destination.value = CustomerDestination.CART
                    },
                    onRetryLoad = { scope.launch { controller.loadCatalog() } },
                    //onBackToEntry = onExitCustomerFlow,
                    onCloseProduct = controller::closeProduct,
                )
            }

            CustomerDestination.CART -> {
                CustomerCartScreen(
                    state = state,
                    contentPadding = innerPadding,
                    onNoteChange = controller::updateNote,
                    onIncrement = { productId, quantity -> controller.updateCartQuantity(productId, quantity + 1) },
                    onDecrement = { productId, quantity ->
                        if (quantity <= 1) controller.removeFromCart(productId)
                        else controller.updateCartQuantity(productId, quantity - 1)
                    },
                    onClearCart = controller::clearCart,
                    onPlaceOrder = {
                        scope.launch {
                            when (controller.placeOrder()) {
                                is ApiResult.Success -> {
                                    controller.clearSearchAndFilters()
                                    controller.closeProduct()
                                    destination.value = CustomerDestination.MENU
                                }
                                is ApiResult.Error -> Unit
                            }
                        }
                    }
                )
            }
        }
    }

    state.orderSuccess?.let { order ->
        LuxuryDialog(
            title = "Bestelling geplaatst",
            message = "Je bestelling #${order.id} werd doorgestuurd naar de bar. Je kan gerust nog extra bestellingen plaatsen.",
            icon = Icons.Default.CheckCircle,
            confirmText = "Verder",
            onDismiss = controller::clearOrderFeedback,
        )
    }

    state.orderError?.let { message ->
        LuxuryDialog(
            title = "Bestelling mislukt",
            message = message,
            icon = Icons.Default.Error,
            iconColor = androidx.compose.ui.graphics.Color(0xFFE35D6A),
            confirmText = "Sluiten",
            onDismiss = controller::clearOrderFeedback,
        )
    }
}
