package com.example.phoebestore.presentation.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.phoebestore.presentation.screens.CreateProductScreen
import com.example.phoebestore.presentation.screens.CreateStoreScreen
import com.example.phoebestore.presentation.screens.HomeScreen
import com.example.phoebestore.presentation.screens.ProductListScreen
import com.example.phoebestore.presentation.screens.RecordSaleScreen
import com.example.phoebestore.presentation.screens.SaleDetailScreen
import com.example.phoebestore.presentation.screens.SalesListScreen
import com.example.phoebestore.presentation.screens.InventoryHistoryScreen
import com.example.phoebestore.presentation.screens.CreditSalesListScreen
import com.example.phoebestore.presentation.screens.SalesReportScreen
import com.example.phoebestore.presentation.screens.StoreDetailScreen
import com.example.phoebestore.presentation.screens.StoreListScreen
import com.example.phoebestore.ui.common.AppBottomNavBar
import com.example.phoebestore.ui.screen.home.HomeScreen
import com.example.phoebestore.ui.screen.product.InventoryHistoryScreen
import com.example.phoebestore.ui.screen.product.CreateProductScreen
import com.example.phoebestore.ui.screen.product.ProductListScreen
import com.example.phoebestore.ui.screen.sale.RecordSaleScreen
import com.example.phoebestore.ui.screen.sale.SaleDetailScreen
import com.example.phoebestore.ui.screen.sale.SalesListScreen
import com.example.phoebestore.ui.screen.sale.CreditSalesListScreen
import com.example.phoebestore.ui.screen.sale.SalesReportScreen
import com.example.phoebestore.ui.screen.store.CreateStoreScreen
import com.example.phoebestore.ui.screen.store.StoreDetailScreen
import com.example.phoebestore.ui.screen.store.StoreListScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    syncViewModel: SyncViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isSyncing by syncViewModel.isSyncing.collectAsState()

    LaunchedEffect(Unit) {
        syncViewModel.syncError.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val currentEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentEntry?.destination
    val isOnHome = currentDestination?.hasRoute(HomeScreen::class) == true
    val isOnStores = currentDestination?.hasRoute(StoreListScreen::class) == true
    val showBottomBar = isOnHome || isOnStores

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavBar(
                    isHomeSelected = isOnHome,
                    onHomeClick = {
                        navController.navigate(HomeScreen) {
                            popUpTo(HomeScreen) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onStoresClick = {
                        navController.navigate(StoreListScreen) {
                            popUpTo(HomeScreen) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box {
            NavHost(
                navController = navController,
                startDestination = HomeScreen,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) {
            composable<HomeScreen> {
                HomeScreen(
                    onNavigateToCreateSale = { storeId ->
                        navController.navigate(RecordSaleScreen(storeId))
                    }
                )
            }

            composable<StoreListScreen> {
                StoreListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToStoreDetail = { storeId ->
                        navController.navigate(StoreDetailScreen(storeId))
                    },
                    onNavigateToCreateStore = {
                        navController.navigate(CreateStoreScreen())
                    }
                )
            }

            composable<StoreDetailScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<StoreDetailScreen>()
                StoreDetailScreen(
                    storeId = route.storeId,
                    onNavigateToEditStore = { storeId ->
                        navController.navigate(CreateStoreScreen(storeId))
                    },
                    onNavigateToProductList = { storeId ->
                        navController.navigate(ProductListScreen(storeId))
                    },
                    onNavigateToSalesList = { storeId ->
                        navController.navigate(SalesListScreen(storeId))
                    },
                    onNavigateToInventoryHistory = { storeId ->
                        navController.navigate(InventoryHistoryScreen(storeId))
                    },
                    onNavigateToCreditSales = { storeId ->
                        navController.navigate(CreditSalesListScreen(storeId))
                    },
                    onNavigateToCreateSale = {
                        navController.navigate(RecordSaleScreen(route.storeId))
                    },
                    onDeleteStore = { navController.popBackStack() }
                )
            }

            composable<SalesListScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<SalesListScreen>()
                SalesListScreen(
                    storeId = route.storeId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSaleDetail = { saleId ->
                        navController.navigate(SaleDetailScreen(saleId))
                    },
                    onNavigateToReport = { fromDate, toDate, productId ->
                        navController.navigate(SalesReportScreen(route.storeId, fromDate, toDate, productId))
                    }
                )
            }

            composable<SaleDetailScreen> {
                SaleDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable<CreateStoreScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<CreateStoreScreen>()
                CreateStoreScreen(
                    storeId = route.storeId,
                    onStoreSaved = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<CreateProductScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<CreateProductScreen>()
                CreateProductScreen(
                    storeId = route.storeId,
                    productId = route.productId,
                    onProductSaved = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<ProductListScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<ProductListScreen>()
                ProductListScreen(
                    storeId = route.storeId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCreateProduct = { storeId ->
                        navController.navigate(CreateProductScreen(storeId))
                    },
                    onNavigateToEditProduct = { storeId, productId ->
                        navController.navigate(CreateProductScreen(storeId, productId))
                    }
                )
            }

            composable<RecordSaleScreen> { backStackEntry ->
                val route = backStackEntry.toRoute<RecordSaleScreen>()
                RecordSaleScreen(
                    storeId = route.storeId,
                    onSaleRecorded = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<CreditSalesListScreen> {
                CreditSalesListScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable<SalesReportScreen> {
                SalesReportScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable<InventoryHistoryScreen> {
                InventoryHistoryScreen(onNavigateBack = { navController.popBackStack() })
            }
            }
            if (isSyncing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
