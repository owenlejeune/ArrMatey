package com.dnfapps.arrmatey.entensions

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.seerr.viewmodel.RequestsViewModel
import org.koin.compose.koinInject
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun BadgeContent(
    tabItem: TabItem,
    activityQueueIssuesCount: Int,
    requestsViewModel: RequestsViewModel = koinInject()
) {
    when (tabItem) {
        TabItem.Standard.ACTIVITY -> {
            if (activityQueueIssuesCount > 0) {
                Badge { Text(activityQueueIssuesCount.toString()) }
            }
        }

        TabItem.Standard.REQUESTS -> {
            val pagedData by requestsViewModel.requestsState.collectAsStateWithLifecycle()
            if (pagedData.totalItemCount > 0) {
                val badgeText = if (pagedData.totalItemCount > 9) "9+" else pagedData.totalItemCount.toString()
                Badge { Text(badgeText) }
            }
        }
        else -> {}
    }
}

@Composable
fun TabItemIconView(
    tabItem: TabItem,
    useServiceNavIcons: Boolean,
    activityQueueIssuesCount: Int
) {
    BadgedBox(
        badge = { BadgeContent(tabItem, activityQueueIssuesCount) },
        modifier = Modifier.size(24.dp)
    ) {
        val serviceIcon = tabItem.associatedType?.tabIcon
        if (useServiceNavIcons && serviceIcon != null) {
            Icon(
                painter = painterResource(serviceIcon),
                contentDescription = null
            )
        } else {
            Icon(
                imageVector = tabItem.androidIcon,
                contentDescription = mokoString(tabItem.resource)
            )
        }
    }
}