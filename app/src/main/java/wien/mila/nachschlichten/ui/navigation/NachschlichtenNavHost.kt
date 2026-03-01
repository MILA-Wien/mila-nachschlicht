package wien.mila.nachschlichten.ui.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import wien.mila.nachschlichten.R
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
import wien.mila.nachschlichten.ui.retrieve.RetrieveItemCheckScreen
import wien.mila.nachschlichten.ui.retrieve.RetrieveItemListScreen
import wien.mila.nachschlichten.ui.retrieve.RetrieveItemListViewModel
import wien.mila.nachschlichten.ui.retrieve.RetrieveScreen
import wien.mila.nachschlichten.ui.retrieve.RetrieveViewModel
import wien.mila.nachschlichten.ui.settings.SettingsScreen
import wien.mila.nachschlichten.ui.settings.ShelfEditScreen
import wien.mila.nachschlichten.ui.settings.StorageZoneEditScreen
import wien.mila.nachschlichten.ui.settings.TechnicalSettingsScreen

@Composable
fun NachschlichtenNavHost(
    navController: NavHostController,
    barcodeInputHandler: BarcodeInputHandler,
    modifier: Modifier = Modifier
) {
    val globalNavVm: GlobalNavigationViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        globalNavVm.navigateToCapture.collect { shelfId ->
            navController.navigate("capture/$shelfId") {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
            }
        }
    }
    LaunchedEffect(Unit) {
        globalNavVm.navigateToRetrieve.collect { zoneId ->
            navController.navigate("retrieve/$zoneId") {
                popUpTo(navController.graph.startDestinationId) { inclusive = false }
            }
        }
    }

    var globalUnknownShelfId by remember { mutableStateOf<String?>(null) }
    var globalUnknownZoneId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { globalNavVm.unknownShelfId.collect { globalUnknownShelfId = it } }
    LaunchedEffect(Unit) { globalNavVm.unknownZoneId.collect { globalUnknownZoneId = it } }

    val currentRoute = navController.currentBackStackEntry?.destination?.route
    if (globalUnknownShelfId != null &&
        currentRoute != AppDestination.CAPTURE.route
    ) {
        AlertDialog(
            onDismissRequest = { globalUnknownShelfId = null },
            title = { Text(stringResource(R.string.scan_unknown_shelf_title)) },
            text = { Text(stringResource(R.string.scan_unknown_shelf, globalUnknownShelfId!!)) },
            confirmButton = {
                TextButton(onClick = { globalUnknownShelfId = null }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
    if (globalUnknownZoneId != null &&
        currentRoute != AppDestination.RETRIEVE.route
    ) {
        AlertDialog(
            onDismissRequest = { globalUnknownZoneId = null },
            title = { Text(stringResource(R.string.scan_unknown_zone_title)) },
            text = { Text(stringResource(R.string.scan_unknown_zone, globalUnknownZoneId!!)) },
            confirmButton = {
                TextButton(onClick = { globalUnknownZoneId = null }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
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
                    navController.navigate("capture/$shelfId")
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "capture/{shelfId}",
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
                    navController.navigate("capture/$shelfId/$ean")
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "capture/{shelfId}/{ean}",
            arguments = listOf(
                navArgument("shelfId") { type = NavType.StringType },
                navArgument("ean") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val shelfId = backStackEntry.arguments?.getString("shelfId") ?: ""
            LaunchedEffect(Unit) {
                barcodeInputHandler.barcodeFlow.collect { barcode ->
                    if (!barcode.startsWith("shelf:") && !barcode.startsWith("zone:")) {
                        navController.navigate("capture/$shelfId/$barcode") {
                            popUpTo("capture/{shelfId}/{ean}") { inclusive = true }
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
                    navController.navigate("retrieve/$zoneId")
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "retrieve/{zoneId}",
            arguments = listOf(
                navArgument("zoneId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val zoneId = backStackEntry.arguments?.getString("zoneId") ?: ""
            val viewModel: RetrieveItemListViewModel = hiltViewModel()
            LaunchedEffect(Unit) {
                barcodeInputHandler.barcodeFlow.collect { barcode ->
                    if (!barcode.startsWith("shelf:") && !barcode.startsWith("zone:"))
                        viewModel.onBarcodeScan(barcode)
                }
            }
            RetrieveItemListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToItem = { pendingItemId ->
                    navController.navigate("retrieve/$zoneId/$pendingItemId")
                },
                viewModel = viewModel
            )
        }

        composable(
            route = "retrieve/{zoneId}/{pendingItemId}",
            arguments = listOf(
                navArgument("zoneId") { type = NavType.StringType },
                navArgument("pendingItemId") { type = NavType.LongType }
            )
        ) {
            RetrieveItemCheckScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.SETTINGS.route) {
            DisposableEffect(Unit) {
                barcodeInputHandler.isEnabled = false
                onDispose { barcodeInputHandler.isEnabled = true }
            }
            SettingsScreen(
                onNavigateToTechnicalSettings = { navController.navigate("settings/technical") }
            )
        }

        composable("settings/technical") {
            DisposableEffect(Unit) {
                barcodeInputHandler.isEnabled = false
                onDispose { barcodeInputHandler.isEnabled = true }
            }
            TechnicalSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
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
