package com.dnfapps.arrmatey.ui.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.navigation.navigationManager

@Composable
fun NavigationDrawerButton() {
    val navManager = navigationManager
    IconButton(onClick = {
        navManager.openDrawer()
    }) {
        Icon(Icons.Default.Menu, null)
    }
}
