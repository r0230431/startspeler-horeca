package com.startspeler.horeca.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.startspeler.horeca.auth.AuthApi
import com.startspeler.horeca.auth.CrewRole
import com.startspeler.horeca.auth.TokenStore
import com.startspeler.horeca.auth.UserSession
import com.startspeler.horeca.isAdminUrl
import com.startspeler.horeca.loadCustomerName
import com.startspeler.horeca.saveCustomerName
import com.startspeler.horeca.customer.CustomerFlowController
import com.startspeler.horeca.customer.CustomerSession
import com.startspeler.horeca.screens.crew.CrewLoginScreen
import com.startspeler.horeca.screens.crew.DashboardScreen
import com.startspeler.horeca.screens.crew.customers.CustomersScreen
import com.startspeler.horeca.screens.crew.inventory.InventoryScreen
import com.startspeler.horeca.screens.crew.kassa.KassaScreen
import com.startspeler.horeca.screens.crew.management.ManagementScreen
import com.startspeler.horeca.screens.crew.orders.StaffOrderScreen
import com.startspeler.horeca.screens.crew.overviews.OverviewNavigationRequest
import com.startspeler.horeca.screens.crew.overviews.OverviewTab
import com.startspeler.horeca.screens.crew.overviews.OverviewsScreen
import com.startspeler.horeca.screens.crew.payments.PaymentsScreen
import com.startspeler.horeca.screens.customer.CustomerEntryScreen
import com.startspeler.horeca.screens.customer.CustomerFlowScreen
import com.startspeler.horeca.ui.navigation.ResponsiveNavigationScaffold
import com.startspeler.horeca.ui.navigation.visibleStaffNavigationItems
import com.startspeler.horeca.ui.theme.crew.StartSpelerCrewTheme
import com.startspeler.horeca.ui.theme.customer.StartSpelerCustomerTheme
import com.startspeler.horeca.screens.customer.SplashScreen
import kotlinx.coroutines.launch

private sealed interface RootState {
    data object CustomerEntry : RootState
    data object CrewLogin : RootState
    data class CustomerQrSplash(val tableNumber: String) : RootState
    data class CustomerActive(val session: CustomerSession) : RootState
    data class CrewActive(val session: UserSession) : RootState
}

@Composable
@Preview
fun App(initialQrTableNumber: String? = null) {
    val authApi = remember { AuthApi() }
    val customerFlowController = remember { CustomerFlowController() }
    val scope = rememberCoroutineScope()
    val isAdmin = remember { isAdminUrl() }

    //var rootState by remember { mutableStateOf<RootState>(RootState.CustomerEntry) }
    var rootState by remember(initialQrTableNumber) {
        mutableStateOf<RootState>(
            when {
                isAdmin -> RootState.CrewLogin
                !initialQrTableNumber.isNullOrBlank() -> RootState.CustomerQrSplash(initialQrTableNumber)
                else -> RootState.CustomerEntry
            }
        )
    }

    var isCrewLoading by remember { mutableStateOf(false) }
    var crewLoginError by remember { mutableStateOf<String?>(null) }
    var isCustomerLoading by remember { mutableStateOf(false) }
    var customerEntryError by remember { mutableStateOf<String?>(null) }
    var prefilledTableNumber by remember(initialQrTableNumber) {
        mutableStateOf(initialQrTableNumber)
    }

    when (val state = rootState) {
        RootState.CustomerEntry -> {
            StartSpelerCustomerTheme {
                CustomerEntryScreen(
                    isSubmitting = isCustomerLoading,
                    backendError = customerEntryError,
                    prefilledTableNumber = prefilledTableNumber,
                    prefilledCustomerName = loadCustomerName(),
                    onStartOrder = { tableNumber, customerName ->
                        customerEntryError = null
                        isCustomerLoading = true
                        scope.launch {
                            try {
                                val result = customerFlowController.startSession(tableNumber, customerName)
                                result
                                    .onSuccess { session ->
                                        saveCustomerName(customerName)
                                        rootState = RootState.CustomerActive(session)
                                    }
                                    .onFailure { throwable ->
                                        customerEntryError = throwable.message ?: "De klantflow kon niet gestart worden."
                                    }
                            } finally {
                                isCustomerLoading = false
                            }
                        }
                    },
                    onCrewLoginClick = {
                        crewLoginError = null
                        rootState = RootState.CrewLogin
                    }
                )
            }
        }

        RootState.CrewLogin -> {
            StartSpelerCrewTheme {
                CrewLoginScreen(
                    isLoading = isCrewLoading,
                    errorMessage = crewLoginError,
                    onLoginClicked = { username, password ->
                        crewLoginError = null
                        isCrewLoading = true

                        scope.launch {
                            try {
                                val response = authApi.login(username, password)

                                TokenStore.token = response.token

                                val role = when (response.role.uppercase()) {
                                    "ADMIN" -> CrewRole.ADMIN
                                    else -> CrewRole.STAFF
                                }

                                rootState = RootState.CrewActive(
                                    UserSession(
                                        appMode = AppMode.CREW,
                                        crewRole = role,
                                        crewMemberId = response.crewMemberId,
                                        username = response.username,
                                        token = response.token
                                    )
                                )
                            } catch (_: Exception) {
                                crewLoginError = "Ongeldige gebruikersnaam of wachtwoord."
                            } finally {
                                isCrewLoading = false
                            }
                        }
                    }
                )
            }
        }

        is RootState.CustomerQrSplash -> {
            StartSpelerCustomerTheme {
                SplashScreen(
                    onSplashFinished = {
                        val savedName = loadCustomerName()
                        if (savedName != null) {
                            // Naam gekend → automatisch sessie starten voor de gescande tafel
                            customerEntryError = null
                            isCustomerLoading = true
                            rootState = RootState.CustomerEntry
                            scope.launch {
                                try {
                                    val result = customerFlowController.startSession(state.tableNumber, savedName)
                                    result
                                        .onSuccess { session ->
                                            rootState = RootState.CustomerActive(session)
                                        }
                                        .onFailure { throwable ->
                                            customerEntryError = throwable.message ?: "De klantflow kon niet gestart worden."
                                        }
                                } finally {
                                    isCustomerLoading = false
                                }
                            }
                        } else {
                            rootState = RootState.CustomerEntry
                        }
                    }
                )
            }
        }

        is RootState.CustomerActive -> {
            StartSpelerCustomerTheme {
                CustomerFlowScreen(
                    session = state.session,
                    onExitCustomerFlow = {
                        prefilledTableNumber = null
                        rootState = RootState.CustomerEntry
                    }
                )
            }
        }

        is RootState.CrewActive -> {
            StartSpelerCrewTheme {
                CrewAppShell(
                    session = state.session,
                    onLogout = {
                        TokenStore.token = null
                        prefilledTableNumber = null
                        rootState = if (isAdmin) RootState.CrewLogin else RootState.CustomerEntry
                    }
                )
            }
        }
    }
}

@Composable
private fun CrewAppShell(
    session: UserSession,
    onLogout: () -> Unit,
) {
    BoxWithConstraints {
        var selectedItemKey by remember { mutableStateOf("home") }
        var overviewNavigationRequest by remember { mutableStateOf(OverviewNavigationRequest()) }
        var overviewRequestCounter by remember { mutableStateOf(0L) }
        val visibleItems = visibleStaffNavigationItems(session.crewRole)
        val selectedIndex = visibleItems.indexOfFirst { it.key == selectedItemKey }
            .takeIf { it >= 0 } ?: 0

        var managementResetToken by remember { mutableStateOf(0) }

        ResponsiveNavigationScaffold(
            appMode = session.appMode,
            crewRole = session.crewRole,
            screenWidth = maxWidth,
            selectedIndex = selectedIndex,
            onItemSelected = { index ->
                val clickedKey = visibleItems[index].key

                if (clickedKey == "manage" && selectedItemKey == "manage") {
                    managementResetToken += 1
                }

                selectedItemKey = clickedKey
            },
            onLogout = onLogout,
        ) {
            when (selectedItemKey) {
                "home" -> DashboardScreen(onNavigate = { selectedItemKey = it })
                "orders" -> StaffOrderScreen(onOpenDashboard = { selectedItemKey = "home" })
                "customers" -> CustomersScreen(crewRole = session.crewRole)
                "payments" -> PaymentsScreen()
                "cash_register" -> KassaScreen(
                    onOpenPaymentMethodDetails = { preset ->
                        overviewRequestCounter += 1
                        overviewNavigationRequest = OverviewNavigationRequest(
                            selectedTab = OverviewTab.PAYMENTS,
                            paymentFiltersPreset = preset,
                            requestId = overviewRequestCounter
                        )
                        selectedItemKey = "overviews"
                    }
                )
                "overviews" -> OverviewsScreen(navigationRequest = overviewNavigationRequest)
                "inventory" -> InventoryScreen(crewRole = session.crewRole)
                "manage" -> ManagementScreen(
                    resetToOverviewToken = managementResetToken
                )
            }
        }
    }
}