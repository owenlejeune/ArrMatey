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
import androidx.compose.material.icons.filled.ArrowBack
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
    hasBottomBar: Boolean = false,
    customWebpageViewModel: CustomWebpageViewerViewModel = koinInjectParams(webpageId),
    mokoStrings: MokoStrings = koinInject()
) {
    val webpage by customWebpageViewModel.webpage.collectAsStateWithLifecycle()
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isToolbarVisible by remember { mutableStateOf(true) }
    var lastScrollY by remember { mutableIntStateOf(0) }

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
                navigationIcon = { NavigationDrawerButton() }
            )
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            webpage?.let { webpage ->
                AnimatedVisibility(
                    visible = isToolbarVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(y = -ScreenOffset)
                        .zIndex(1f)
                        .padding(
                            end = 12.dp,
                            bottom = if (!hasBottomBar) {
                                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                            } else {
                                0.dp
                            }
                        )
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        content = {
                            AppBarRow {
                                clickableItem(
                                    onClick = { webView?.goBack() },
                                    icon = {
                                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                                    },
                                    label = mokoStrings.getString(MR.strings.back),
                                    enabled = canGoBack
                                )
                                clickableItem(
                                    onClick = { webView?.goForward() },
                                    icon = {
                                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                                    },
                                    label = mokoStrings.getString(MR.strings.forward),
                                    enabled = canGoForward
                                )
                                clickableItem(
                                    onClick = { webView?.reload() },
                                    icon = {
                                        Icon(Icons.Default.Refresh, null)
                                    },
                                    label = mokoStrings.getString(MR.strings.refresh)
                                )
                                clickableItem(
                                    onClick = { },
                                    icon = {
                                        Icon(Icons.Default.Share, null)
                                    },
                                    label = mokoStrings.getString(MR.strings.share)
                                )
                            }
                        }
                    )
                }

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

                            setOnScrollChangeListener { _, _, scrollY, _, _ ->
                                val scrollDelta = scrollY - lastScrollY

                                when {
                                    scrollDelta > 10 -> {
                                        isToolbarVisible = false
                                    }
                                    scrollDelta < -10 -> {
                                        isToolbarVisible = true
                                    }
                                }

                                lastScrollY = scrollY
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