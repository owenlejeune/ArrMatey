package com.dnfapps.arrmatey.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnfapps.arrmatey.shared.MR
import com.dnfapps.arrmatey.ui.components.navigation.NavigationDrawerButton
import com.dnfapps.arrmatey.utils.MokoStrings
import com.dnfapps.arrmatey.utils.koinInjectParams
import com.dnfapps.arrmatey.utils.mokoString
import com.dnfapps.arrmatey.utils.navigationBarBottomInset
import com.dnfapps.arrmatey.webpage.viewmodel.CustomWebpageViewerViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomWebpageViewerScreen(
    webpageId: Long,
    customWebpageViewModel: CustomWebpageViewerViewModel = koinInjectParams(webpageId)
) {
    val webpage by customWebpageViewModel.webpage.collectAsStateWithLifecycle()
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val lifecyclerOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecyclerOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    webView?.onPause()
                }

                Lifecycle.Event.ON_RESUME -> {
                    webView?.onResume()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    webView?.apply {
                        stopLoading()
                        loadUrl("about:blank")
                        clearHistory()
                        clearCache(true)
                        destroy()
                    }
                    webView = null
                }

                else -> {}
            }
        }

        lifecyclerOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecyclerOwner.lifecycle.removeObserver(observer)
            webView?.apply {
                stopLoading()
                loadUrl("about:blank")
                clearHistory()
                clearCache(true)
                removeAllViews()
                destroyDrawingCache()
                destroy()
            }
            webView = null
        }
    }

    BackHandler(enabled = canGoBack) {
        webView?.goBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(webpage?.name ?: "") },
                navigationIcon = { NavigationDrawerButton() },
                actions = {
                    IconButton(
                        onClick = { webView?.goBack() },
                        enabled = canGoBack
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                    IconButton(
                        onClick = { webView?.goForward() },
                        enabled = canGoForward
                    ) {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    }
                    Box {
                        IconButton(onClick = {
                            menuExpanded = true
                        }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenuPopup(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuGroup(
                                shapes = MenuDefaults.groupShape(0, 1)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(mokoString(MR.strings.refresh)) },
                                    selected = false,
                                    shapes = MenuDefaults.itemShape(0, 2),
                                    leadingIcon = { Icon(Icons.Default.Refresh, null) },
                                    onClick = {
                                        menuExpanded = false
                                        webView?.reload()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(mokoString(MR.strings.share)) },
                                    selected = false,
                                    shapes = MenuDefaults.itemShape(1, 2),
                                    leadingIcon = { Icon(Icons.Default.Share, null) },
                                    onClick = {
                                        menuExpanded = false
                                        // todo -share
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            webpage?.let { webpage ->
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewClient = object : WebViewClient() {
                                override fun doUpdateVisitedHistory(
                                    view: WebView?,
                                    url: String?,
                                    isReload: Boolean
                                ) {
                                    super.doUpdateVisitedHistory(view, url, isReload)
                                    canGoBack = view?.canGoBack() == true
                                    canGoForward = view?.canGoForward() == true
                                }
                            }

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(true)
                                builtInZoomControls = true
                                displayZoomControls = false
                            }

                            val headersMap = webpage.headers.associate { it.key to it.value }
                            loadUrl(webpage.url, headersMap)
                        }.also { webView = it }
                    },
                    update = { view ->
                        canGoBack = view.canGoBack()
                        canGoForward = view.canGoForward()
                    },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { view ->
                        view.stopLoading()
                        view.loadUrl("about:blank")
                        view.clearHistory()
                        view.clearCache(true)
                        view.removeAllViews()
                        view.destroyDrawingCache()
                        view.destroy()
                    }
                )
            }
        }
    }
}