package com.dnfapps.arrmatey.ui.tabs

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.NavigationManager
import com.dnfapps.arrmatey.navigation.SeerrScreen
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import com.dnfapps.arrmatey.ui.screens.RequestsScreen
import com.dnfapps.arrmatey.ui.screens.SeerrDetailsScreen
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SeerrTab(
    viewModel: RequestsViewModel = koinInject(),
    navigationManager: NavigationManager = koinInject(),
    navigation: Navigation<SeerrScreen> = navigationManager.requests()
) {
    NavDisplay(
        backStack = navigation.backStack,
        onBack = { navigation.popBackStack() },
        entryProvider = entryProvider {
            entry<SeerrScreen.Home> {
                RequestsScreen(viewModel = viewModel)
            }
            entry<SeerrScreen.Details> { details ->
                SeerrDetailsScreen(details.tmdbId, details.requestType)
            }
        }
    )
}