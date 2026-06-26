package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.navigation.BazarrScreen
import com.dnfapps.arrmatey.navigation.LocalBazarrNavigator
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.ui.screens.BazarrDetailsScreen
import com.dnfapps.arrmatey.ui.screens.BazarrScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazarrTab(
    windowSizeClass: WindowSizeClass,
    wideRailIsVisible: Boolean,
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigator<BazarrScreen> = navigationManager.bazarr
) {
    CompositionLocalProvider(LocalBazarrNavigator provides navigation) {
        NavDisplay(
            backStack = navigation.backStack,
            onBack = { navigation.popBackStack() },
            entryProvider  = entryProvider {
                entry<BazarrScreen.Library> {
                    BazarrScreen(wideRailIsVisible = wideRailIsVisible)
                }
                entry<BazarrScreen.Details> { details ->
                    BazarrDetailsScreen(details.id, details.type)
                }
            }
        )
    }
}
