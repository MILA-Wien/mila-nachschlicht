package wien.mila.nachschlichten.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import wien.mila.nachschlichten.ui.capture.ArticleCheckScreen
import wien.mila.nachschlichten.ui.capture.CaptureScreen
import wien.mila.nachschlichten.ui.retrieve.RetrieveItemListScreen
import wien.mila.nachschlichten.ui.retrieve.RetrieveScreen
import wien.mila.nachschlichten.ui.settings.SettingsScreen
import wien.mila.nachschlichten.ui.settings.ShelfEditScreen
import wien.mila.nachschlichten.ui.settings.StorageZoneEditScreen

@Composable
fun NachschlichtenNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.CAPTURE.route,
        modifier = modifier
    ) {
        composable(AppDestination.CAPTURE.route) {
            CaptureScreen(
                onNavigateToArticleCheck = { ean, shelfId ->
                    navController.navigate("article_check/$ean/$shelfId")
                }
            )
        }

        composable(
            route = "article_check/{ean}/{shelfId}",
            arguments = listOf(
                navArgument("ean") { type = NavType.StringType },
                navArgument("shelfId") { type = NavType.StringType }
            )
        ) {
            ArticleCheckScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.RETRIEVE.route) {
            RetrieveScreen(
                onNavigateToItems = { zoneId ->
                    navController.navigate("retrieve_items/$zoneId")
                }
            )
        }

        composable(
            route = "retrieve_items/{zoneId}",
            arguments = listOf(
                navArgument("zoneId") { type = NavType.StringType }
            )
        ) {
            RetrieveItemListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.SETTINGS.route) {
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
            StorageZoneEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
