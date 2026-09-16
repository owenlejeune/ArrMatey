package com.dnfapps.arrmatey.ui.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.entensions.TabItemIconView
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun HomeTabNavIcon(
    tabItem: TabItem,
    useServiceNavIcons: Boolean,
    activityQueueIssuesCount: Int,
) {
    when (tabItem) {
        is TabItem.Standard -> {
            TabItemIconView(
                tabItem = tabItem,
                useServiceNavIcons = useServiceNavIcons,
                activityQueueIssuesCount = activityQueueIssuesCount,
            )
        }

        is TabItem.CustomWebpage -> {
            Icon(
                Icons.Default.Language,
                contentDescription = tabItem.name,
            )
        }

        else -> {}
    }
}

@Composable
fun HomeTabNavLabel(tabItem: TabItem) {
    when (tabItem) {
        is TabItem.Standard -> Text(text = mokoString(tabItem.resource))
        is TabItem.CustomWebpage -> Text(text = tabItem.name)
        else -> {}
    }
}
