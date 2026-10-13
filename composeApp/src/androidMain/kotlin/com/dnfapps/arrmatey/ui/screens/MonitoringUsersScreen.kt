package com.dnfapps.arrmatey.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.arr.viewmodel.InstancesViewModel
import com.dnfapps.arrmatey.instances.model.InstanceType
import com.dnfapps.arrmatey.ui.components.NoMonitoringInstanceView
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MonitoringUsersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUser: (ref: String) -> Unit = {},
    instancesViewModel: InstancesViewModel =
        koinViewModel(
            key = "monitoring_instances",
            parameters = { parametersOf(listOf(InstanceType.Tracearr, InstanceType.Tautulli)) },
        ),
) {
    val instancesState by instancesViewModel.instancesState.collectAsStateWithLifecycle()

    when (instancesState.selectedInstance?.type) {
        InstanceType.Tracearr -> {
            TracearrUsersScreen(
                onNavigateBack = onNavigateBack,
                onNavigateToUser = onNavigateToUser,
            )
        }
        InstanceType.Tautulli -> {
            TautulliUsersScreen(
                onNavigateBack = onNavigateBack,
            )
        }
        else -> {
            NoMonitoringInstanceView()
        }
    }
}
