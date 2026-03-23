package com.dnfapps.arrmatey.entensions

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnfapps.arrmatey.compose.TabItem
import com.dnfapps.arrmatey.utils.mokoString
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun BadgeContent(
    tabItem: TabItem,
    activityQueueIssuesCount: Int
) {
    when (tabItem) {
        TabItem.Standard.ACTIVITY -> {
            if (activityQueueIssuesCount > 0) {
                Badge { Text(activityQueueIssuesCount.toString()) }
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