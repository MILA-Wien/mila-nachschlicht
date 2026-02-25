package wien.mila.nachschlichten

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import wien.mila.nachschlichten.ui.common.BarcodeInputHandler
import wien.mila.nachschlichten.ui.navigation.AppDestination
import wien.mila.nachschlichten.ui.navigation.NachschlichtenNavHost

@Composable
fun NachschlichtenApp(barcodeInputHandler: BarcodeInputHandler) {
    val navController = rememberNavController()
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.CAPTURE) }

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
                    onClick = {
                        if (destination != currentDestination) {
                            currentDestination = destination
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
