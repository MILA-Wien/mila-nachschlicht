package wien.mila.nachschlichten

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import wien.mila.nachschlichten.ui.common.BarcodeInputHandler
import wien.mila.nachschlichten.ui.navigation.AppDestination
import wien.mila.nachschlichten.ui.navigation.GlobalNavigationViewModel
import wien.mila.nachschlichten.ui.navigation.NachschlichtenNavHost

@Composable
fun NachschlichtenApp(barcodeInputHandler: BarcodeInputHandler) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route
    val currentDestination: AppDestination = when {
        route?.startsWith("retrieve") == true -> AppDestination.RETRIEVE
        route?.startsWith("settings") == true -> AppDestination.SETTINGS
        route?.startsWith("help") == true -> AppDestination.HELP
        else -> AppDestination.CAPTURE
    }

    val globalNavVm: GlobalNavigationViewModel = hiltViewModel()
    val pendingCount by globalNavVm.pendingCount.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
    NavigationSuiteScaffold(
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surface
        ),
        navigationSuiteItems = {
            AppDestination.entries.forEach { destination ->
                item(
                    icon = { Icon(destination.icon, contentDescription = stringResource(destination.labelRes)) },
                    label = { Text(stringResource(destination.labelRes)) },
                    selected = destination == currentDestination,
                    badge = if (destination == AppDestination.RETRIEVE && pendingCount > 0) {
                        {
                            Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                                Text("%d".format(pendingCount))
                            }
                        }
                    } else null,
                    onClick = {
                        if (destination != currentDestination) {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) {
        NachschlichtenNavHost(navController = navController, barcodeInputHandler = barcodeInputHandler)
    }
    } // Surface
}
