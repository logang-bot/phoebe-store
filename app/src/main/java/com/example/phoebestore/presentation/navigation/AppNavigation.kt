package com.example.phoebestore.presentation.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.phoebestore.presentation.screens.CreateProductScreen
import com.example.phoebestore.presentation.screens.CreateStoreScreen
import com.example.phoebestore.presentation.screens.HomeScreen
import com.example.phoebestore.presentation.screens.ProductListScreen
import com.example.phoebestore.presentation.screens.RecordSaleScreen
import com.example.phoebestore.presentation.screens.SaleDetailScreen
import com.example.phoebestore.presentation.screens.SalesListScreen
import com.example.phoebestore.presentation.screens.StoreDetailScreen
import com.example.phoebestore.presentation.screens.StoreListScreen
import com.example.phoebestore.ui.screen.home.HomeScreen
import com.example.phoebestore.ui.screen.product.CreateProductScreen
import com.example.phoebestore.ui.screen.product.ProductListScreen
import com.example.phoebestore.ui.screen.sale.RecordSaleScreen
import com.example.phoebestore.ui.screen.sale.SaleDetailScreen
import com.example.phoebestore.ui.screen.sale.SalesListScreen
import com.example.phoebestore.ui.screen.store.CreateStoreScreen
import com.example.phoebestore.ui.screen.store.StoreDetailScreen
import com.example.phoebestore.ui.screen.store.StoreListScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = HomeScreen,
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } }
    ) {
        composable<HomeScreen> {
            HomeScreen(
                onNavigateToStoreList = {
                    navController.navigate(StoreListScreen)
                },
                onNavigateToStoreDetail = { storeId ->
                    navController.navigate(StoreDetailScreen(storeId))
                },
                onNavigateToCreateSale = { storeId ->
                    navController.navigate(RecordSaleScreen(storeId))
                }
            )
        }

        composable<StoreListScreen> {
            StoreListScreen(
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
                onNavigateToCreateSale = {
                    navController.navigate(RecordSaleScreen(route.storeId))
                }
            )
        }

        composable<SalesListScreen> { backStackEntry ->
            val route = backStackEntry.toRoute<SalesListScreen>()
            SalesListScreen(
                storeId = route.storeId,
                onNavigateToSaleDetail = { saleId ->
                    navController.navigate(SaleDetailScreen(saleId))
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
                onStoreSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable<CreateProductScreen> { backStackEntry ->
            val route = backStackEntry.toRoute<CreateProductScreen>()
            CreateProductScreen(
                storeId = route.storeId,
                productId = route.productId,
                onProductSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable<ProductListScreen> { backStackEntry ->
            val route = backStackEntry.toRoute<ProductListScreen>()
            ProductListScreen(
                storeId = route.storeId,
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
                onSaleRecorded = {
                    navController.popBackStack()
                }
            )
        }
    }
}
