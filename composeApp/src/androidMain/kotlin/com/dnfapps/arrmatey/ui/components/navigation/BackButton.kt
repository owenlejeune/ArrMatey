package com.dnfapps.arrmatey.ui.components.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.dnfapps.arrmatey.navigation.Navigator
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.utils.mokoString

@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = mokoString(MR.strings.back),
        )
    }
}

@Composable
fun BackButton(navigation: Navigator<*>) {
    BackButton(onClick = { navigation.popBackStack() })
}
