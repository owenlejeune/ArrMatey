package com.dnfapps.arrmatey.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.dnfapps.arrmatey.navigation.Navigation
import com.dnfapps.arrmatey.navigation.SettingsScreen
import com.dnfapps.arrmatey.ui.components.navigation.BackButton
import com.dnfapps.arrmatey.webpage.model.CustomWebpage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomWebpageViewerScreen(
    webpage: CustomWebpage,
    navigation: Navigation<SettingsScreen>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(webpage.name) },
                navigationIcon = { BackButton(navigation) }
            )
        }
    ) { paddingValues ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                    }

                    // Add custom headers
                    val headersMap = webpage.headers.associate { it.key to it.value }
                    loadUrl(webpage.url, headersMap)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}