package wien.mila.nachschlichten.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import wien.mila.nachschlichten.ui.capture.ArticleCheckScreen
import wien.mila.nachschlichten.ui.capture.CaptureScreen
import wien.mila.nachschlichten.ui.capture.CaptureShelfListScreen
import wien.mila.nachschlichten.ui.capture.CaptureShelfListViewModel
import wien.mila.nachschlichten.ui.capture.CaptureViewModel
import wien.mila.nachschlichten.ui.common.BarcodeInputHandler
import wien.mila.nachschlichten.ui.retrieve.RetrieveItemListScreen
import wien.mila.nachschlichten.ui.retrieve.RetrieveItemListViewModel
import wien.mila.nachschlichten.ui.retrieve.RetrieveScreen
import wien.mila.nachschlichten.ui.retrieve.RetrieveViewModel
import wien.mila.nachschlichten.ui.settings.SettingsScreen
import wien.mila.nachschlichten.ui.settings.ShelfEditScreen
import wien.mila.nachschlichten.ui.settings.StorageZoneEditScreen

@Composable
fun NachschlichtenNavHost(
    navController: NavHostController,
    barcodeInputHandler: BarcodeInputHandler,
    modifier: Modifier = Modifier
) {
    val globalNavVm: GlobalNavigationViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        globalNavVm.navigateToCapture.collect { shelfId ->
            navController.navigate("capture_items/$shelfId") {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
            }
        }
    }
    LaunchedEffect(Unit) {
        globalNavVm.navigateToRetrieve.collect { zoneId ->
            navController.navigate("retrieve_items/$zoneId") {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppDestination.CAPTURE.route,
        modifier = modifier
    ) {
        composable(AppDestination.CAPTURE.route) {
            val viewModel: CaptureShelfListViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                barcodeInputHandler.barcodeFlow.collect { barcode ->
                    if (!barcode.startsWith("zone:")) viewModel.onBarcodeScan(barcode)
                }
            }
            CaptureShelfListScreen(
                onNavigateToItems = { shelfId ->
                    navController.navigate("capture_items/$shelfId")
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "capture_items/{shelfId}",
            arguments = listOf(navArgument("shelfId") { type = NavType.StringType })
        ) {
            val viewModel: CaptureViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                barcodeInputHandler.barcodeFlow.collect { barcode ->
                    if (!barcode.startsWith("shelf:") && !barcode.startsWith("zone:"))
                        viewModel.onBarcodeScan(barcode)
                }
            }
            CaptureScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToArticleCheck = { ean, shelfId ->
                    navController.navigate("article_check/$ean/$shelfId")
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "article_check/{ean}/{shelfId}",
            arguments = listOf(
                navArgument("ean") { type = NavType.StringType },
                navArgument("shelfId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val shelfId = backStackEntry.arguments?.getString("shelfId") ?: ""
            LaunchedEffect(Unit) {
                barcodeInputHandler.barcodeFlow.collect { barcode ->
                    if (!barcode.startsWith("shelf:") && !barcode.startsWith("zone:")) {
                        navController.navigate("article_check/$barcode/$shelfId") {
                            popUpTo("article_check/{ean}/{shelfId}") { inclusive = true }
                        }
                    }
                }
            }
            ArticleCheckScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.RETRIEVE.route) {
            val viewModel: RetrieveViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                barcodeInputHandler.barcodeFlow.collect { barcode ->
                    if (!barcode.startsWith("shelf:")) viewModel.onBarcodeScan(barcode)
                }
            }
            RetrieveScreen(
                onNavigateToItems = { zoneId ->
                    navController.navigate("retrieve_items/$zoneId")
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "retrieve_items/{zoneId}",
            arguments = listOf(
                navArgument("zoneId") { type = NavType.StringType }
            )
        ) {
            val viewModel: RetrieveItemListViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                barcodeInputHandler.barcodeFlow.collect { barcode ->
                    if (!barcode.startsWith("shelf:") && !barcode.startsWith("zone:"))
                        viewModel.onBarcodeScan(barcode)
                }
            }
            RetrieveItemListScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        composable(AppDestination.SETTINGS.route) {
            DisposableEffect(Unit) {
                barcodeInputHandler.isEnabled = false
                onDispose { barcodeInputHandler.isEnabled = true }
            }
            SettingsScreen(
                onNavigateToShelfEdit = { shelfId ->
                    val route = if (shelfId != null) "settings/shelf_edit?shelfId=$shelfId"
                    else "settings/shelf_edit"
                    navController.navigate(route)
                },
                onNavigateToZoneEdit = { zoneId ->
                    val route = if (zoneId != null) "settings/zone_edit?zoneId=$zoneId"
                    else "settings/zone_edit"
                    navController.navigate(route)
                }
            )
        }

        composable(
            route = "settings/shelf_edit?shelfId={shelfId}",
            arguments = listOf(
                navArgument("shelfId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            DisposableEffect(Unit) {
                barcodeInputHandler.isEnabled = false
                onDispose { barcodeInputHandler.isEnabled = true }
            }
            ShelfEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "settings/zone_edit?zoneId={zoneId}",
            arguments = listOf(
                navArgument("zoneId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            DisposableEffect(Unit) {
                barcodeInputHandler.isEnabled = false
                onDispose { barcodeInputHandler.isEnabled = true }
            }
            StorageZoneEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
